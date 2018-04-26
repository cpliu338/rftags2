package wsd.m2.web;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.bson.*;
import org.bson.conversions.Bson;
import wsd.m2.MongoBean;

/**
 *
 * @author cpliu
 */
@Named
@ViewScoped
public class SampleBean implements Serializable {
    
    @javax.inject.Inject
    MongoBean mongoBean;
    
    private int counter, counter3;
    private MongoCollection<Document> coll;
    private String result;
    private List<Object[]> model;
    
    @PostConstruct
    public void init() {
        coll = mongoBean.getDatabase().getCollection("points", Document.class);
        model = new ArrayList<>();
        for (int i=0; i<8; i++) {
            Integer[] ir = new Integer[2];
            ir[0] = i; ir[1] = i*2;
            model.add(ir);
        }
    }
    
    public void writeRandom() {
        Document last = coll.find(Filters.eq("point_id", counter)).sort(Sorts.descending("time")).first();
        Date start = last.getDate("time");
        Double value = last.getDouble("value");
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Starting from {0} with value {1}", 
                new Object[] {start, value});
        long time = System.currentTimeMillis();
        int count = 0;
        for (int i=0; i<100; i++) {
            start.setTime(new Double(start.getTime() + (1000 + Math.random() * 100000.0)).longValue());
            value += (Math.random()-0.5) * 6;
            if (value < 0.001) {
                value = 0.0;
                continue;
            }
            if (value > 5.999) {
                value = 6.0;
                continue;
            }
            Document d = new Document("time", start).append("point_id", this.counter).append("value", value);
            count++;
            coll.insertOne(d);
        }
        time = System.currentTimeMillis() - time;
        FacesMessage msg = new FacesMessage();
        msg.setDetail("Inserted doc:" + count);
        msg.setSummary("Time required in ms:" + time);
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    /**
     * db.points.aggregate([ 
     * {$match: {point_id:1}}, 
     * {$project: {_id:0, value:1, "timestamp": {$subtract: ["$time", new Date ('1970-01-01')]}}}, 
     * {$project: {value: 1, "time2" : {$divide: ["$timestamp", 60000]} }}, 
     * {$project: {value: 1, "timeslot": {$floor: "$time2"}}} 
     * {$group: {_id: "$timeslot", average: {$avg: "$value"}} }
     * ])
     */
    public void build() {
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Updated counter {0}", counter);
        List<Bson> aggregates = new ArrayList<>();
        BsonInt32 one = new BsonInt32(1);
        BsonArray ar1 = new BsonArray();
        ar1.add(new BsonString("$time"));
        ar1.add(new BsonDateTime(0L));
        BsonDocument result1 =
        new BsonDocument("timestamp",
            new BsonDocument("$subtract", ar1)
        ).append("_id", new BsonInt32(0)).append("value", one);
        BsonArray ar2 = new BsonArray();
        ar2.add(new BsonString("$timestamp"));
        ar2.add(new BsonInt32(60000));
        BsonDocument result2 =
        new BsonDocument("time2",
            new BsonDocument("$divide", ar2)
        ).append("value", one);
        BsonDocument result3 =
        new BsonDocument("timeslot",
            new BsonDocument("$floor", new BsonString("$time2"))
        ).append("value", one);
        Bson result4 = Aggregates.group("$timeslot", new BsonField("average", new BsonDocument("$avg", new BsonString("$value"))));
        result = result4.toString();
        aggregates = Arrays.asList(
            Aggregates.project(result1),
            Aggregates.project(result2),
            Aggregates.project(result3),
            result4,
            Aggregates.limit(20)
        );
        model.clear();
        for (Document d : coll.aggregate(aggregates)) {
            model.add(new Object[] {
                d.getDouble("_id"),
                d.getDouble("average")
            });
        }
    }
    /**
     * db.points.aggregate([
    { "$group": {
        "_id": {
            "$subtract": [
                { "$subtract": [ "$time", new Date("1970-01-01") ] },
                { "$mod": [ 
                    { "$subtract": [ "$time", new Date("1970-01-01") ] },
                    1000 * 60
                ]}
            ]
        },
        "avg": { "$avg": "$value" }
    }},
    { "$sort": {"_id": 1}}
    ])
     */
    public void aggregate() {
        int interval = 60000;
        // { "$subtract": [ "$time", new Date("1970-01-01") ] }
        BsonDocument subtract = new BsonDocument("$subtract", 
            new BsonArray(Arrays.asList(
                new BsonString("$time"),
                new BsonDateTime(0L)
            ))
        );
        BsonDocument mod = new BsonDocument("$mod",
            new BsonArray(Arrays.asList(
                subtract, new BsonInt32(interval)
            ))
        );
        //BsonDocument _id = new BsonDocument("_id", 
        BsonDocument _id =  new BsonDocument("$subtract", 
            new BsonArray(Arrays.asList(
                subtract,
                mod
            ))
        );
        // "avg": { "$avg": "$value" }
        Bson group = Aggregates.group(_id, 
                new BsonField("average", new BsonDocument("$avg", new BsonString("$value"))),
                new BsonField("max", new BsonDocument("$max", new BsonString("$value")))
        );
        List<Bson> aggregates = Arrays.asList(
            group,
            Aggregates.sort(new Document("_id", 1)),
            Aggregates.limit(20)
        );
        model.clear();
        for (Document d : coll.aggregate(aggregates)) {
            model.add(new Object[] {
                new Date(d.getLong("_id")),
                d.getDouble("average"),
                d.getDouble("max")
            });
        }
    }

    /**
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * @param counter the counter to set
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

    /**
     * @return the model
     */
    public List<Object[]> getModel() {
        return model;
    }

    /**
     * @return the counter3
     */
    public int getCounter3() {
        return counter3;
    }

    /**
     * @param counter3 the counter3 to set
     */
    public void setCounter3(int counter3) {
        this.counter3 = counter3;
    }

    /**
     * @return the result
     */
    public String getResult() {
        return result;
    }

    /**
     * @param result the result to set
     */
    public void setResult(String result) {
        this.result = result;
    }

}
