package wsd.m2;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoDatabase;
import java.util.logging.Level;
import java.util.logging.*;
import javax.annotation.*;
import javax.inject.Singleton;
import javax.naming.*;

/**
 *
 * @author cpliu
 */
@Singleton
public class MongoBean implements java.io.Serializable {
    
    MongoClient mongoClient;
    MongoDatabase db;
    
    @PostConstruct
    public void init() {
        if (mongoClient == null) {
            mongoClient = new MongoClient("localhost:27017");
        }
        if (db == null && mongoClient != null) {
            db = mongoClient.getDatabase("test");
        }
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
