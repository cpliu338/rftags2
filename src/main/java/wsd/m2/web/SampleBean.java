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
    
    private int counter, counter3;
    
    private List<Integer[]> model;
    
    @PostConstruct
    public void init() {
        counter = 0;
        model = new ArrayList<>();
        for (int i=0; i<8; i++) {
            Integer[] ir = new Integer[2];
            ir[0] = i; ir[1] = i*2;
            model.add(ir);
        }
    }
    
    public void testFlow() {
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Updating counter");
    }
    
    public void incCounter() {
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Updated counter {0}", counter);
        counter3 = counter * 3;
        if (counter3 > 12) {
            model = null;
            model.clear();
        }
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

    /**
     * @return the model
     */
    public List<Integer[]> getModel() {
        return model;
    }

    /**
     * @return the counter3
     */
    public int getCounter3() {
        return counter3;
    }

    /**
     * @param counter3 the counter3 to set
     */
    public void setCounter3(int counter3) {
        this.counter3 = counter3;
    }

}
