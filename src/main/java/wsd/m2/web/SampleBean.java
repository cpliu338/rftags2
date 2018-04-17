package wsd.m2.web;

import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.event.SelectEvent;
import rf.model.RfAnalogTag;
import wsd.m2.MongoBean;

/**
 *
 * @author cpliu
 */
@Named
@ViewScoped
public class SampleBean implements Serializable {
    
    @javax.inject.Inject
    MongoBean mongoBean;
    
    private int counter;
    
    @PostConstruct
    public void init() {
        counter = 0;
    }
    
    public void testFlow() {
        throw new javax.faces.application.ViewExpiredException();
    }
    
    public void incCounter() {
        counter++;
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Updated counter {0}", counter);
    }

    /**
     * @return the counter
     */
    public int getCounter() {
        return counter;
    }

    /**
     * @param counter the counter to set
     */
    public void setCounter(int counter) {
        this.counter = counter;
    }

}
