package wsd.m2.rftags2;

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
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import wsd.authen.LdapClient;
import wsd.authen.LdapLoginModule;

/**
 *
 * @author cp_liu
 */
@ManagedBean
@ViewScoped
public class AdminBean implements java.io.Serializable {
    
    private static final long serialVersionUID = 34523001L;
    private String user, pwd;
    @Resource(name="churchDB")
    private DataSource dataSource;
    
    @PostConstruct
    public void init() {
        user = "null";
        if (dataSource != null) {
            try {
                ResultSet rs = dataSource.getConnection().prepareStatement("SELECT role FROM groups WHERE user='cpliu'").executeQuery();
                while (rs.next()) {
                    user = user.concat(rs.getString("role"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(AdminBean.class.getName()).log(Level.SEVERE, null, ex);
                user = ex.getClass().getName();
            }
        }
    }
    
    public void test() {
        FacesMessage msg = new FacesMessage();
        try {
            String s = LdapLoginModule.test(user, pwd);
            msg.setSeverity(FacesMessage.SEVERITY_INFO);
            msg.setSummary("OK");
            msg.setDetail(s);
        } catch (NamingException ex) {
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
