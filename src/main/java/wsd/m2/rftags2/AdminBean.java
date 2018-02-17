package wsd.m2.rftags2;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import wsd.authen.LdapClient;

/**
 *
 * @author cp_liu
 */
@ManagedBean
@ViewScoped
public class AdminBean {
    private String user, pwd;
    
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
