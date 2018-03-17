package rf.model;

import org.bson.*;

/**
 * Base class for all Realflex tags
 * @author cpliu
 */
public abstract class RfTag {
    public static final String TAG = "tag";
    public static final String OFFSET = "offset";
    public static final String INTEGER = "int";
    public static final String DECIMAL = "dec";
    public static final String STRING = "str";
    
    protected Document doc;
    protected Exception exception;
    protected abstract String [] getAttributes();
    protected abstract String getType(int offset);
    protected abstract String getAttribute(int offset);
    
    public Exception getException() { return exception;}
    
    protected Document parseCsvLine(String line) throws CsvFormatException {
        Document d = new Document();
        String[] tokens = line.split(",");
        if (tokens.length != getAttributes().length) throw new CsvFormatException("Invalid line:"+line);
        try {
        for (int i=0; i<tokens.length; i++) {
            switch (getType(i)) {
                case INTEGER:
                    d.append(getAttribute(i), Integer.parseInt(tokens[i]));
                    break;
                case DECIMAL:
                    d.append(getAttribute(i), Double.parseDouble(tokens[i]));
                    break;
                default:
                    d.append(getAttribute(i), tokens[i]);
            }
        }
        }
        catch (NumberFormatException ex) {
            throw new CsvFormatException("Invalid number format:"+line);
        }
        return d;
    }

    /**
     * @return the doc
     */
    public Document getDoc() {
        return doc;
    }
}
