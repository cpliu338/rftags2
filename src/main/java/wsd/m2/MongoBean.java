package wsd.m2;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.util.logging.Level;
import java.util.logging.*;
import javax.annotation.*;
import javax.enterprise.context.ApplicationScoped;
import static org.bson.codecs.configuration.CodecRegistries.fromProviders;
import static org.bson.codecs.configuration.CodecRegistries.fromRegistries;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.codecs.pojo.PojoCodecProvider;

/**
 *
 * @author cpliu
 */
@javax.inject.Named
@ApplicationScoped
public class MongoBean implements java.io.Serializable {
    
    MongoClient mongoClient;
    MongoDatabase db;
    CodecRegistry pojoCodecRegistry;

    public CodecRegistry getPojoCodecRegistry() {
        return pojoCodecRegistry;
    }
    
    
    @PostConstruct
    public void init() {
        if (mongoClient == null) {
            mongoClient = new MongoClient("localhost:27017");
        }
        if (db == null && mongoClient != null) {
            db = mongoClient.getDatabase("test");
        }
        pojoCodecRegistry =fromRegistries(MongoClient.getDefaultCodecRegistry(),
                fromProviders(PojoCodecProvider.builder().automatic(true).build()));
    }
    
    public MongoDatabase getDatabase() {
        return db;
    }
    
    @PreDestroy
    public void cleanup() {
        Logger.getLogger(MongoBean.class.getName()).log(Level.INFO, "Cleaning up");
        if (mongoClient != null) mongoClient.close();
        Logger.getLogger(MongoBean.class.getName()).log(Level.INFO, "Closed mongo client");
    }
    
}
