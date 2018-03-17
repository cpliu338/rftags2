package rf.model;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author cpliu
 */
public class RfAnalogTag extends RfTag {
    public static final String SUB_TYPE = "sub_type";
    public static final String EU_TYPE = "eu_type";
    public static final String UNIT = "unit";
    public static final String MIN_EU = "min_eu";
    public static final String MAX_EU = "max_eu";
    public static final String LOLIMIT = "lolimit";
    public static final String HILIMIT = "hilimit";
            
    public static final String CSV_PATTERN = "analog_tag,offset,sub_type,eu_type,unit,min_eu,max_eu,lolimit,hilimit";
    public static final String [] attributes = {TAG, OFFSET, SUB_TYPE,EU_TYPE,UNIT,MIN_EU,MAX_EU,LOLIMIT,HILIMIT};
    public static final String [] types = {STRING, INTEGER, INTEGER, INTEGER, STRING, DECIMAL, DECIMAL, DECIMAL, DECIMAL};
    //private CsvFormatException exception;
    
    @Override
    protected String [] getAttributes() { return attributes;}
    
    @Override
    protected String getAttribute(int offset) {
        return attributes[offset];
    }
    
    @Override
    protected String getType(int offset) {
        return types[offset];
    }
    
    protected RfAnalogTag() {}
    
    public static RfAnalogTag createFromCsv(String line) {
        RfAnalogTag t = new RfAnalogTag();
        try {
            t.doc = t.parseCsvLine(line);
        } catch (CsvFormatException ex) {
            t.exception = ex;
        }
        return t;
    }
}
