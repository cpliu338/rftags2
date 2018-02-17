package wsd.authen;

import java.util.*;
import java.util.logging.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;

/**
 *
 * @author cpliu
 */
public class LdapClient {

  /*private Subject subject;
  private String login;
  //private List<String> userGroups;
  private java.util.HashMap<String,String> map;
  private DirContext ctx;
    private String principal;
    private String credentials;
    private String base;
    
    NamingEnumeration<SearchResult> results;
  */
  private String url;
  
  public LdapClient(String url) {
      this.url = url;/*"ldap://ldap.forumsys.com:389";
      this.principal = "cn=read-only-admin,dc=example,dc=com";
      this.credentials = "password";
      base = "ou=mathematicians,dc=example,dc=com";
      this.login = "";*/
  }

    public boolean bind(String user, String pwd) {
	// Set up environment for creating initial context
	Hashtable env = new Hashtable(11);
	env.put(Context.INITIAL_CONTEXT_FACTORY,
	    "com.sun.jndi.ldap.LdapCtxFactory");
	env.put(Context.SECURITY_AUTHENTICATION, "simple");
	env.put(Context.SECURITY_PRINCIPAL, user);
	env.put(Context.SECURITY_CREDENTIALS, pwd);
	env.put(Context.PROVIDER_URL, url);
        //env.put("java.naming.ldap.factory.socket", BlindTrustSSLFactory.class.getName());
        Logger.getLogger(this.getClass().getName()).log(Level.INFO, "prin: {0}, cred: {1}",
            new Object[] {
                user, pwd
            });
        try {
            DirContext ctx = new InitialLdapContext(env,null);
            ctx.close();
            return true;
        } catch (NamingException ex) {
            Logger.getLogger(this.getClass().getName()).log(Level.INFO, null, ex);
            return false;
        }
    }

}
