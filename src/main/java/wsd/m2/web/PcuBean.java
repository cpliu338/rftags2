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
import org.bson.*;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.UploadedFile;
import rf.model.CsvFormatException;
import wsd.m2.MongoBean;
/**
 *
 * @author cpliu
 */
@Named
@javax.faces.view.ViewScoped
public class PcuBean implements java.io.Serializable {
    private String dest;
    private String source;
    @Resource
    String datapath;
    private MongoCollection<Document> collection;
    
    @javax.inject.Inject
    MongoBean mongoBean;
    
    private LazyDataModel<Document> model;

    public LazyDataModel<Document> getModel() {
        return model;
    }

    @javax.annotation.PostConstruct
    public void init() {
        collection = mongoBean.getDatabase().getCollection("pcus", Document.class);
        model = new LazyDataModel<Document>() {
            @Override
            public List<Document> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
                Document doc = collection.find(new Document(), Document.class).first();
                List<Document> arr = doc.get("analogs", List.class);
                this.setRowCount(arr.size());
                List<Document> result = arr.stream().skip(first).limit(pageSize).map(d->{
                    d.append("tooltip", d.toJson());
                    return d;
                }).collect(Collectors.toList());
                Logger.getLogger(PcuBean.class.getName()).log(Level.INFO, "load: {0}, {1}", new Object[]{
                    first, pageSize
                });
                return result;
            }
        };
    }
    
    public void handleFileUpload(FileUploadEvent event) {
        UploadedFile file = event.getFile();
        try {
            handleCsv(
                new BufferedReader(new InputStreamReader(file.getInputstream()))
            );
        } catch (IOException | CsvFormatException ex) {
            Logger.getLogger(PcuBean.class.getName()).log(Level.SEVERE, null, ex);
            dest = ex.getMessage();
        }
    }
    
    private void handleCsv(BufferedReader rdr) throws IOException, CsvFormatException {
            Pcu pcu = Pcu.parseFromCsv(rdr);
            long d = collection.replaceOne(
                Filters.eq("header.name", pcu.getDoc().get("header", Document.class).getString("name")),
                pcu.getDoc(),
                new UpdateOptions().upsert(true)
                ).getMatchedCount();
            if (d>0) {
                Logger.getLogger(PcuBean.class.getName()).log(Level.INFO, "Matched {0} docs", d);
            }
            dest = pcu.getDoc().toJson();
    }
    
    public void invoke() {
        //new Pcu();
        try {
            handleCsv(
                    new BufferedReader(new StringReader(source))
            );
        } catch (IOException | CsvFormatException ex) {
            Logger.getLogger(PcuBean.class.getName()).log(Level.SEVERE, null, ex);
            dest = ex.getMessage();
        }
    }

    /**
     * @return the dest
     */
    public String getDest() {
        return dest;
    }

    /**
     * @param dest the dest to set
     */
    public void setDest(String dest) {
        this.dest = dest;
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
