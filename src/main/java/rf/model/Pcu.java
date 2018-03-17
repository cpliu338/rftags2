package rf.model;

import org.bson.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author cpliu
 */
public class Pcu {
    
    private final Document doc;
    
    public Pcu() {
        doc = new Document();
    }
    
    @Override
    public String toString() {
        return doc.toString();
    }
    
    public static Pcu parseFromCsv(BufferedReader rdr) throws IOException, CsvFormatException {
        Pcu pcu = new Pcu();
        List<Document> analogs = new ArrayList<>();
        int stage = 0;
        String line;
        Exception ex = null;
        while ((line = rdr.readLine()) != null) {
            if (line.trim().length()==0) {
                stage = 0;
                continue;
            }
            String firstToken = line.substring(0, line.indexOf(','));
            switch (stage) {
                case 0:
                    if ("name".equals(firstToken)) {
                        stage = 1;
                    }
                    else if ("analog_tag".equals(firstToken)) {
                        stage = 2;
                    }
                    else 
                        throw new CsvFormatException("Wrong header line"+line);
                    break;
                case 1:
                    PcuHeader header = PcuHeader.createFromCsv(line);
                    ex = header.getException();
                    if (ex != null) throw new CsvFormatException(ex.getMessage());
                    pcu.doc.append("header", header.getDoc());
                    break;
                case 2:
                    RfAnalogTag t = RfAnalogTag.createFromCsv(line);
                    ex = t.getException();
                    if (ex != null) throw new CsvFormatException(ex.getMessage());
                    analogs.add(t.getDoc());
                    break;
                default:
                    throw new CsvFormatException("Invalid stage "+stage);
            }
        }
        pcu.getDoc().append("analogs", analogs);
        return pcu;
    }

    /**
     * @return the doc
     */
    public Document getDoc() {
        return doc;
    }
}
