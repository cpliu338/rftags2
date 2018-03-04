package wsd.m2;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.sql.DataSource;
import static wsd.authen.LdapLoginModule.SQL_TEMPLATE;
import static wsd.authen.LdapLoginModule.URL_TEMPLATE;
import static wsd.authen.LdapLoginModule.USER_TEMPLATE;

/**
 * Can be looked up by java:global/rftags2/AuthenticateEjb
 * @see http://tomee.apache.org/examples/lookup-of-ejbs/README.html
 * @author cpliu
 */
@Stateless
public class AuthenticateEjb {
    @Resource(name="mysql2")
    private DataSource ds;
    
    @Resource(name="ldap_server")
    private String ldap_server;
    
    @Inject
    private MongoBean mongoClient;
    
    private String status;
    
    public String getStatus() {return status;}
    
    public boolean authenticate(String name, String password) {
        try {
            return this.bind(name, password);
        } catch (NamingException ex) {
            Logger.getLogger(AuthenticateEjb.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    private boolean bind(String user, String pwd) throws NamingException {
	// Set up environment for creating initial context
	Hashtable env = new Hashtable(11);
	env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	env.put(Context.SECURITY_PRINCIPAL, String.format(USER_TEMPLATE, user));
	env.put(Context.SECURITY_CREDENTIALS, pwd);
        String url = String.format(URL_TEMPLATE, ldap_server);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Bean binding to {0}", url);
	env.put(Context.PROVIDER_URL, url);
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "prin: {0}, cred: {1}",
            new Object[] {
                String.format(USER_TEMPLATE, user), pwd
            });
        DirContext ctx = new InitialLdapContext(env,null);
        ctx.close();
        return true;
    }
    
    public void checkGroups(java.util.List<String> userGroups) {
        try (Connection conn = ds.getConnection(); 
                PreparedStatement stmt = conn.prepareStatement(SQL_TEMPLATE)) {
            stmt.setString(1, "cpliu");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String role = rs.getString("role");
                userGroups.add(role);
            }
        } catch (SQLException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, ex);
        }
    }
    
    @PostConstruct
    public void init() {
        status = (
            (ds==null) ? "No datasource " : ds.getClass().getName()
        ).concat(
            (mongoClient==null) ? "No mongo Client " : mongoClient.getClass().getName()
        ).concat(
            (ldap_server == null) ? "No ldap server read " : ldap_server
        );        
    }
}
