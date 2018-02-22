package wsd.authen;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.security.auth.Subject;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.spi.LoginModule;
import java.util.*;
import java.util.logging.*;
import javax.annotation.Resource;
import javax.naming.*;
import javax.naming.directory.DirContext;
import javax.naming.ldap.InitialLdapContext;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.sql.DataSource;

public class LdapLoginModule implements LoginModule {

  private CallbackHandler handler;
  private Subject subject;
  private UserPrincipal userPrincipal;
  private RolePrincipal rolePrincipal;
  private String login;
  private List<String> userGroups;
  private java.util.HashMap<String,String> map;
  java.util.Properties props;

    //@Resource(name="churchDB")
    private DataSource dataSource;
    
    public static String test(String env, String lookup) throws NamingException {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup(env);

        Object o = envCtx.lookup(lookup);
        return (o==null) ? "Null" : o.getClass().getName();
    }
    
  @Override
  public void initialize(Subject subject,
      CallbackHandler callbackHandler,
      Map<String, ?> sharedState,
      Map<String, ?> options) {
      try {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup("java:comp/env");

        // Look up our data source
          dataSource = (DataSource)envCtx.lookup("mysql2"); // java:comp/env/churchDB
      } catch (NamingException | RuntimeException ex) {
          Logger.getLogger(LdapLoginModule.class.getName()).log(Level.SEVERE, null, ex);
      }
        userGroups = new ArrayList();
        map = new java.util.HashMap<>();
        handler = callbackHandler;
        this.subject = subject;
        /*
        props = new java.util.Properties();
        try (java.io.InputStream is = LdapLoginModule.class.getResourceAsStream("/ldap.properties")) {
            props.load(is);
            if (!props.containsKey("url")) {
                Logger.getLogger(LdapLoginModule.class.getName()).log(Level.WARNING, "Cannot read property url");
            }
            props.getProperty("principal", "cn=manager,ou=Internal,dc=system,dc=lan");
            props.getProperty("credential", "jMSL5KNZtM+O8RB+");
            props.getProperty("url", "ldaps://192.168.11.224:636");
            props.getProperty("base", "dc=system,dc=lan");
        }
        catch (IOException ex) {
            Logger.getLogger(LdapLoginModule.class.getName()).log(Level.SEVERE, null, ex);
        }
            */
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "LdapLoginModule initialized");
  }
  
    private boolean bind(String user, String pwd) throws NamingException {
	// Set up environment for creating initial context
	Hashtable env = new Hashtable(11);
	env.put(Context.INITIAL_CONTEXT_FACTORY,
	    "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	env.put(Context.SECURITY_PRINCIPAL, user);
	env.put(Context.SECURITY_CREDENTIALS, pwd);
	env.put(Context.PROVIDER_URL, "ldap://ldap.forumsys.com:389");
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "prin: {0}, cred: {1}",
            new Object[] {
                user, pwd
            });
        DirContext ctx = new InitialLdapContext(env,null);
        ctx.close();
        return true;
    }

  @Override
  public boolean login() throws LoginException {

    Callback[] callbacks = new Callback[2];
    callbacks[0] = new NameCallback("login");
    callbacks[1] = new PasswordCallback("password", true);

    try {
        handler.handle(callbacks);
        String name = ((NameCallback) callbacks[0]).getName();
        String password = String.valueOf(((PasswordCallback) callbacks[1])
          .getPassword());
        try {
            bind(name, password);
        } catch (NamingException ex) {
            throw new LoginException("Authentication failed");
        }
        /*
            if (!loggedIn)
                throw new LoginException("Wrong password");
        */
        login = name; // this should be the real name
        map.put("sn", "OK"); // this gives more info
        this.checkGroups();
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Roles found: {0}", userGroups.size());
        return true;
    } catch (IOException | UnsupportedCallbackException e) {
      throw new LoginException(e.getMessage());
    }

  }

  @Override
  public boolean commit() throws LoginException {

    userPrincipal = new UserPrincipal(login);
    userPrincipal.setMap(map);
    subject.getPrincipals().add(userPrincipal);

    if (userGroups != null && userGroups.size() > 0) {
      for (String groupName : userGroups) {
        rolePrincipal = new RolePrincipal(groupName);
        subject.getPrincipals().add(rolePrincipal);
      }
    }

    return true;
  }

  @Override
  public boolean abort() throws LoginException {
    return false;
  }

  @Override
  public boolean logout() throws LoginException {
    subject.getPrincipals().remove(userPrincipal);
    subject.getPrincipals().remove(rolePrincipal);
    return true;
  }
  
    public void checkGroups() {
        if (dataSource != null) {
            try (Connection conn = dataSource.getConnection(); 
                    PreparedStatement stmt = conn.prepareStatement("SELECT role FROM groups WHERE user=?")) {
                stmt.setString(1, "cpliu");
                ResultSet rs = stmt.executeQuery();
                while (rs.next()) {
                    String role = rs.getString("role");
                    userGroups.add(role);
                    Logger.getLogger(this.getClass().getName()).log(Level.INFO, "Add role {0}", role);
                }
            } catch (SQLException ex) {
                map.put("sn", ex.getMessage());
            }
        }
        else {
            map.put("sn", "data source is null");
        }
    }

}