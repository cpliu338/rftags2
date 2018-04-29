package wsd.m2.web;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.BsonField;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.logging.*;
import java.util.stream.Collector;
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
            Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Counter is {0}", counter);
        Document last = coll.find(Filters.eq("point_id", counter)).sort(Sorts.descending("time")).first();
        Date start = last==null ? new Date(System.currentTimeMillis() - 180*86400000L) : last.getDate("time");
            Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Last null is {0}", last==null);
        Integer value = last==null ? 0 : last.getInteger("value");
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Starting from {0} with value {1}", 
                new Object[] {start, value});
        long time = System.currentTimeMillis();
        int count = 0;
        for (int i=0; i<100; i++) {
            start.setTime(new Double(start.getTime() + (3600000 + Math.random() * 100000000.0)).longValue());
            if (Math.random() > 0.15) {
                value = 1 - value;
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
    
    public void writeAnalog() {
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
        String json = "{\"$subtract\": [" +
                "{\"$subtract\": [ \"$time\", new Date(0) ] }," +
                "{ \"$mod\": [ " +
                    "{ \"$subtract\": [ \"$time\", new Date(0) ] }," +
                    "60000" +
                "]}" + 
            "]}";
        BsonDocument _id = BsonDocument.parse(json);
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
    
    public void collect() {
        final Document accum = new Document()
        .append("lastTime", null)
        .append("lastValue", 0)
        .append("ms_as_0", 0L)
        .append("ms_as_1", 0L).append("ms_as_2", 0L).append("ms_as_3", 0L);
        model.clear();
        coll.find(Filters.eq("point_id", counter)).sort(Sorts.ascending("time")).forEach(new java.util.function.Consumer<Document>() {

            @Override
            public void accept(Document doc) {
                if (accum.getDate("lastTime") == null) {
                    accum.put("lastTime", doc.getDate("time"));
                    accum.put("lastValue", doc.getInteger("value"));
                    return;
                }
                Date lastTime = accum.getDate("lastTime");
                Integer lastValue = accum.getInteger("lastValue");
                if (doc.getInteger("value") == lastValue) return;
                long elapsedMs = (doc.getDate("time").getTime() - lastTime.getTime());
                String field = String.format("ms_as_%d", lastValue);
                accum.put(field, accum.getLong(field)+elapsedMs);
                accum.put("lastValue", doc.getInteger("value")); 
                accum.put("lastTime", doc.getDate("time"));
                    Object[] ar = new Object[3];
                    ar[0] = accum.getDate("lastTime");
                    ar[1] = accum.getLong("ms_as_0");
                    ar[2] = accum.getLong("ms_as_1");
                
                model.add(ar);
            }
        });
    }

    private class MyListCollector<Document>
        implements Collector<org.bson.Document, List<org.bson.Document>, List<Object[]>> {

        Date lastTime;
        Integer lastValue;
        long ms_as_0, ms_as_1, ms_as_2, ms_as_3;
        
        @Override
        public Supplier<List<org.bson.Document>> supplier() {
            return ArrayList::new;
        }

        @Override
        public BiConsumer<List<org.bson.Document>, org.bson.Document> accumulator() {
            return (List<org.bson.Document> accum, org.bson.Document doc) ->
            {
                if (lastTime == null) {
                    lastTime = doc.getDate("time");
                    lastValue = doc.getInteger("value");
                    return;
                }
                if (doc.getInteger("value") == lastValue) return;
                long elapsedMs = (doc.getDate("time").getTime() - lastTime.getTime());
                switch (lastValue) {
                    case 0:
                        ms_as_0 += elapsedMs; break;
                    case 1:
                        ms_as_1 += elapsedMs; break;
                    case 2:
                        ms_as_2 += elapsedMs; break;
                    case 3:
                        ms_as_3 += elapsedMs; break;
                }
                lastValue = doc.getInteger("value"); 
                lastTime = doc.getDate("time");
                org.bson.Document d = new org.bson.Document("time", lastTime);
                accum.add(d.append("ms_as_0", ms_as_0)
                    .append("ms_as_1", ms_as_1)
                    .append("ms_as_2", ms_as_2)
                    .append("ms_as_3", ms_as_3)
                );
            };
        }

        @Override
        public BinaryOperator<List<org.bson.Document>> combiner() {
            return (left,right) -> {left.addAll(right); return left;};
        }

        @Override
        public Function<List<org.bson.Document>, List<Object[]>> finisher() {
            return accum -> {
                List<Object[]> result = new ArrayList<>();
                for (org.bson.Document d : accum) {
                    Object[] ar = new Object[3];
                    ar[0] = d.getDate("time");
                    ar[1] = d.getLong("ms_as_0");
                    ar[2] = d.getLong("ms_as_1");
                    result.add(ar);
                }
                return result;
            };
        }

        @Override
        public Set<Characteristics> characteristics() {
            return Collections.EMPTY_SET;
        }
        
    }

}
