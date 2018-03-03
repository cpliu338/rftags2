package wsd.m2.rftags2;

import com.mongodb.MongoClient;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import wsd.m2.AuthenticateEjb;


/**
 *
 * @author cpliu
 */
@ManagedBean
@ViewScoped
public class ConfigBean {
    private String field1;
    private String field2;
    
    //@ Resource(name="datapath")
    private String result1;
    
    @EJB
    private AuthenticateEjb bean;
    
    @PostConstruct
    public void init() {
        result1 = (bean == null) ? "No bean" : bean.getStatus();
    }
    
    private Object lookup() throws NamingException {
        Context initCtx = new InitialContext();
        Context envCtx = (Context) initCtx.lookup(field1);
        return envCtx.lookup(field2);
    }
    
    public void checkEnv() {
        try {
            Object o = lookup();
            if (o == null)
                result1 = "Not found";
            else if (o instanceof String)
                result1 = (String)o;
            else
                result1 = o.getClass().getName();
        } catch (NamingException ex) {
            Logger.getLogger(ConfigBean.class.getName()).log(Level.SEVERE, null, ex);
            FacesMessage msg = new FacesMessage();
            msg.setSeverity(FacesMessage.SEVERITY_WARN);
            msg.setSummary(ex.getClass().getName());
            msg.setDetail(ex.getMessage());
            FacesContext.getCurrentInstance().addMessage(null, msg);
            //throw new RuntimeException(ex);
        }
    }
    
    public String test() {
        Object o;
        try {
            o = lookup();
            if (o != null) {
                AuthenticateEjb auth = (AuthenticateEjb)o;
                result1 = Boolean.toString(auth.authenticate(field1, field1));
            }
            else
                result1 = "Object is null";
        } catch (ClassCastException | NamingException ex) {
            Logger.getLogger(ConfigBean.class.getName()).log(Level.SEVERE, null, ex);
            result1 = ex.getMessage();
        }
        return null;
    }

    /**
     * @return the field1
     */
    public String getField1() {
        return field1;
    }

    /**
     * @param field1 the field1 to set
     */
    public void setField1(String field1) {
        this.field1 = field1;
    }

    /**
     * @return the field2
     */
    public String getField2() {
        return field2;
    }

    /**
     * @param field2 the field2 to set
     */
    public void setField2(String field2) {
        this.field2 = field2;
    }

    /**
     * @return the result1
     */
    public String getResult1() {
        return result1;
    }

    /**
     * @param result1 the result1 to set
     */
    public void setResult1(String result1) {
        this.result1 = result1;
    }
}
