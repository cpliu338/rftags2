package wsd.m2.rftags2;

import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import org.bson.Document;
import wsd.authen.LdapClient;
import wsd.authen.LdapLoginModule;
import wsd.m2.MongoBean;

/**
 *
 * 21-Feb-2018 11:38:48.889 INFO [localhost-startStop-2] org.apache.openejb.config.
AutoConfig.processResourceRef Auto-linking resource-ref 'java:comp/env/mongoClie
nt' in bean rftags2.Comp42001292 to Resource(id=mongoBean)
21-Feb-2018 11:38:48.892 INFO [localhost-startStop-2] org.apache.openejb.config.
AutoConfig.processResourceRef Auto-linking resource-ref 'openejb/Resource/rftags
2/mongoBean' in bean rftags2.Comp42001292 to Resource(id=rftags2/mongoBean)
21-Feb-2018 11:38:48.893 INFO [localhost-startStop-2] org.apache.openejb.config.
AutoConfig.processResourceRef Auto-linking resource-ref 'openejb/Resource/mongoC
lient' in bean rftags2.Comp42001292 to Resource(id=rftags2/mongoBean)
21-Feb-2018 11:38:56.938 WARNING [http-nio-8080-exec-6] org.apache.openejb.Injec
tionProcessor.construct Injection: No such property 'wsd.m2.rftags2.AdminBean/mo
ngoClient' in class wsd.m2.rftags2.AdminBean

 * @author cp_liu
 */
@ManagedBean
@ViewScoped
public class AdminBean implements java.io.Serializable {
    
    private static final long serialVersionUID = 34523001L;
    private String user, pwd;
    @Resource(name="churchDB")
    private DataSource dataSource;
    @Inject
    private MongoBean mongoBean;
    
    
    @PostConstruct
    public void init() {
        user = "null";
        if (dataSource != null) {
            try {
                ResultSet rs = dataSource.getConnection().prepareStatement("SELECT role FROM groups WHERE user='cpliu'").executeQuery();
                while (rs.next()) {
                    user = user.concat(rs.getString("role"));
                }
                if (mongoBean == null) {/*
                    String env = "java:comp/env";
                    Context initCtx = new InitialContext();
                    Context envCtx = (Context) initCtx.lookup(env);
                    Object o = envCtx.lookup("mongoClient");
                    if (o == null) {
                        env = "openejb/Resource/rftags2";
                        envCtx = (Context) initCtx.lookup(env);
                        o = envCtx.lookup("mongoClient");
                    }
                    if (o==null)
                        pwd = "Cannot inject mongo client";
                    else {
                        pwd = env;
                        mongoBean = (MongoClient)o;
                    }*/pwd = "MongoClient not injected";
                }
                else
                    pwd = "MongoClient injected";
            } catch (SQLException ex) {
                Logger.getLogger(AdminBean.class.getName()).log(Level.SEVERE, null, ex);
                user = ex.getClass().getName();
            }
        }
    }
    
    public void test() {
        FacesMessage msg = new FacesMessage();
        MongoDatabase db = mongoBean.getDatabase();
        try  {
            MongoCollection<Document> coll = db.getCollection("reconcile");
            Long cnt = coll.count();
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary("No of uncheq:"+cnt);
            msg.setDetail("OK");
        } catch (RuntimeException ex) {
            Logger.getLogger(AdminBean.class.getName()).log(Level.SEVERE, null, ex);
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setSummary(ex.getClass().getName());
            msg.setDetail(ex.getMessage());
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void login() {
        LdapClient client = new LdapClient("ldap://ldap.forumsys.com:389");
        FacesMessage msg = new FacesMessage();
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
        if (client.bind("uid="+user+",dc=example,dc=com", getPwd())) {
            msg.setSummary("OK");
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            try {
                FacesContext.getCurrentInstance().getExternalContext().redirect("admin/config.jsf");
            } catch (IOException ex) {
                Logger.getLogger(AdminBean.class.getName()).log(Level.SEVERE, null, ex);
                msg.setSummary(ex.getMessage());
            }
        }
        else {
            msg.setSummary("Failed");
        }
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    /**
     * @return the user
     */
    public String getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    public void setUser(String user) {
        this.user = user;
    }

    /**
     * @return the pwd
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * @param pwd the pwd to set
     */
    public void setPwd(String pwd) {
        this.pwd = pwd;
    }
}
