package wsd.m2.web;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import javax.inject.Named;
import rf.model.Pcu;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import org.bson.*;
import org.bson.conversions.Bson;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.UploadedFile;
import rf.model.CsvFormatException;
import rf.model.RfAnalogTag;
import wsd.m2.MongoBean;
/**
 *
 * @author cpliu
 */
@Named
@javax.faces.view.ViewScoped
public class PcuBean implements java.io.Serializable {
    private RfAnalogTag tag;

    public RfAnalogTag getTag() {
        return tag;
    }
    private String source;
    private String filterUnit;
    @Resource
    String datapath;
    private MongoCollection<RfAnalogTag> collection;
    
    @javax.inject.Inject
    MongoBean mongoBean;
    
    private LazyDataModel<RfAnalogTag> model;

    public LazyDataModel<RfAnalogTag> getModel() {
        return model;
    }
    
    public void filter() {
        filterUnit = "MLD";
        init();
    }
    
    public PcuBean() {
        filterUnit = "";
    }
    
    @javax.annotation.PostConstruct
    public void init() {
        collection = mongoBean.getDatabase().getCollection("wsd_rf_analogs", RfAnalogTag.class)
                .withCodecRegistry(mongoBean.getPojoCodecRegistry());
        model = new LazyDataModel<RfAnalogTag>() {
            @Override
            public List<RfAnalogTag> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
                Bson filter = filterUnit.length()==0 ? new Document() : Filters.eq("unit", PcuBean.this.filterUnit);
                this.setRowCount((int)collection.count(filter));
                final List<RfAnalogTag> arr = new ArrayList<>();
                for (RfAnalogTag t:collection.find(filter, RfAnalogTag.class).skip(first).limit(pageSize)) {
                    arr.add(t);
                }
                return arr;
            }
        };
        tag = new RfAnalogTag();
    }
    
    public void invoke() {
        collection.insertOne(tag);
        FacesMessage msg = new FacesMessage("Saved "+tag.getName());
        FacesContext.getCurrentInstance().addMessage(null, msg);
    }
    
    public void selectTag() {
        Bson filter = Filters.eq("name", source);
        tag = collection.find(filter).first();
        Logger.getLogger(PcuBean.class.getName()).log(Level.INFO, "selecting:{0},found:{1}",
                        new Object[]{ source, tag!=null
                        });
        if (tag == null) tag = new RfAnalogTag();
        RequestContext.getCurrentInstance().execute("PF('dlg-edit').show()");
    }

    /**
     * @return the source
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }
}
