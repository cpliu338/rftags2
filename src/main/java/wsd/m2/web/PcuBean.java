package wsd.m2.web;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import javax.inject.Named;
import rf.model.Pcu;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import org.bson.BsonDocument;
import org.bson.Document;
import org.primefaces.event.FileUploadEvent;
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
    private MongoCollection collection;
    
    @javax.inject.Inject
    MongoBean mongoBean;

    @javax.annotation.PostConstruct
    public void init() {
        if (mongoBean == null)
            dest = "No mongo bean";
        else {
            dest = "OK";
        }
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
            collection = mongoBean.getDatabase().getCollection("pcus");
            long d = collection.deleteMany(Filters.eq("header.name", ((Document)pcu.getDoc().get("header")).getString("name"))).getDeletedCount();
            if (d>0) {
                Logger.getLogger(PcuBean.class.getName()).log(Level.INFO, "Found and deleted existing {0} docs", d);
            }
            collection.insertOne(pcu.getDoc());
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
