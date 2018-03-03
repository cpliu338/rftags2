package wsd.m2;

import com.mongodb.MongoClient;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.sql.DataSource;

/**
 * Can be looked up by java:global/rftags2/AuthenticateEjb
 * @see http://tomee.apache.org/examples/lookup-of-ejbs/README.html
 * @author cpliu
 */
@Stateless
public class AuthenticateEjb {
    @Resource(name="mysql2")
    private DataSource ds;
    @Inject
    private MongoBean mongoClient;
    
    private String status;
    
    public String getStatus() {return status;}
    
    public boolean authenticate(String name, String password) {
        return name.equals(password);
    }
    
    @PostConstruct
    public void init() {
        status = (
            (ds==null) ? "No datasource " : ds.getClass().getName()
        ).concat(
            (mongoClient==null) ? "No mongo Client " : mongoClient.getClass().getName()
        );        
    }
}
