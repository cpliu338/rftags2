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
public class MongoBean {
    
    MongoClient mongoClient;
    MongoDatabase db;
    
    @PostConstruct
    public void init() {
        if (mongoClient == null) {
            try {
                String env = "java:comp/env";
                Context initCtx = new InitialContext();
                Context envCtx = (Context) initCtx.lookup(env);
                mongoClient = (MongoClient)envCtx.lookup("mongoClient");
            } catch (NamingException ex) {
                Logger.getLogger(MongoBean.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (db == null && mongoClient != null) {
            db = mongoClient.getDatabase("therismos");
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
