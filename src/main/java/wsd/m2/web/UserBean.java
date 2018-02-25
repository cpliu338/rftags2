package wsd.m2.web;

import java.io.*;
import java.util.*;
import java.util.logging.*;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import wsd.authen.UserPrincipal;

/**
 *
 * @author cpliu
 */
@ManagedBean
@SessionScoped
public class UserBean implements java.io.Serializable {
    private UserPrincipal user;
    private Map userMap;
    
    public UserBean() {
        userMap = Collections.EMPTY_MAP;
    }

    public File getBasePath() {
        javax.naming.Context initCtx;
        String base1;
        try {
            initCtx = new javax.naming.InitialContext();
            javax.naming.Context envCtx = (javax.naming.Context) initCtx.lookup("java:comp/env");
            base1 = envCtx.lookup("datapath").toString();
            return new File(base1);
        } catch (javax.naming.NamingException ex) {
            Logger.getLogger(UserBean.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public Map getUserMap() {
        return userMap;
    }
    
    public static final String[] groups = {"logged_in","admin","sis3"};
   
    public String logout() {
        user = null;
        userMap = Collections.EMPTY_MAP;
        Logger.getLogger(UserBean.class.getName()).log(Level.INFO, "Logging out");
        ExternalContext ec = FacesContext.getCurrentInstance().getExternalContext();
        HttpSession session = (HttpSession)ec.getSession(true);
        session.invalidate(); 
        this.user = null;
        this.userMap.clear();
        return "/index?faces-redirect=true";
    }
    
    public boolean isAdmin() {
        return isInRole("admin");
    }
    
    public boolean isInRole(String r) {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)fc.getExternalContext().getRequest();
        return req.isUserInRole(r);
    }
    
    public boolean isLoggedIn() {
        FacesContext fc = FacesContext.getCurrentInstance();
        HttpServletRequest req = (HttpServletRequest)fc.getExternalContext().getRequest();
        if (req.getUserPrincipal() == null) {
            user = null;
            userMap = Collections.EMPTY_MAP;
        Logger.getLogger(UserBean.class.getName()).log(Level.FINE, "Not logged in");
            return false;
        }
        // if just logged in via j_security check, set user
        user = (UserPrincipal) req.getUserPrincipal();
        userMap = user.getMap();
        Logger.getLogger(UserBean.class.getName()).log(Level.FINE, "User:{0}", user.getName());
        return user!=null;
    }

    /**
     * @return the name
     */
    public String getName() {
        return user == null ? "Not logged in" : user.getName();
    }

}