package rf.model;

/**
 *
 * @author cpliu
 */
public class PcuHeader extends RfTag {
    
    public static final String PCUNAME = "name";
    public static final String ADDRESS = "address";
    public static final String DESC = "desc";
            
    public static final String CSV_PATTERN = "name,address,desc";
    public static final String [] attributes = {PCUNAME, ADDRESS, DESC};
    
    
    protected PcuHeader() {}
    /*
    public static PcuHeader createFromCsv(String line) {
        PcuHeader t = new PcuHeader();
        try {
            t.doc = t.parseCsvLine(line);
        } catch (CsvFormatException ex) {
            t.exception = ex;
        }
        return t;
    }
    */
}
