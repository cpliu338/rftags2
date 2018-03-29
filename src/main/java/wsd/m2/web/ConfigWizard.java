package wsd.m2.web;

import rf.model.RfTag;
import wsd.m2.MongoBean;


/**
 *
 * @author cpliu
 */
public abstract class ConfigWizard {
    
    protected RfTag tag;
    private String tag1;
    
    public abstract MongoBean getMongoBean();
    
    public void chosenTag1() {
        tag.setName(tag1.toUpperCase());
    }

    /**
     * @return the tag1
     */
    public String getTag1() {
        return tag1;
    }

    /**
     * @param tag1 the tag1 to set
     */
    public void setTag1(String tag1) {
        this.tag1 = tag1;
    }
}
