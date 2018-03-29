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
public class SampleBean extends ConfigWizard implements Serializable {
    
    @javax.inject.Inject
    MongoBean mongoBean;
    
    private String tagName;
    
    @PostConstruct
    public void init() {
        super.tag = new RfAnalogTag();
    }
    
    public void updateTag() {
        RfAnalogTag tag1 = (RfAnalogTag)tag;
        tag1.setDesc(tagName.substring(1));
        tag1.setName(tagName);
        tag1.setEu_type(1);
        tag1.setMin_eu(2.0);
        tag1.setOffset(0);
        tag1.setUnit("M");
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "Updated tag {0}", tag.getName());
    }

    @Override
    public MongoBean getMongoBean() {
        return mongoBean;
    }
    
    public void assemble() {
        super.chosenTag1();
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "tag name {0} after assemble", tag.getName());
    }
    
    public RfAnalogTag getTag() { return (RfAnalogTag)tag;}
    public void setTag(RfAnalogTag t) { tag = t;}

    /**
     * @return the tagName
     */
    public String getTagName() {
        return tagName;
    }

    /**
     * @param tagName the tagName to set
     */
    public void setTagName(String tagName) {
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "set tagName {0}", tagName);
        this.tagName = tagName;
    }
}
