package rf.model;

import java.util.logging.Level;
import javax.validation.constraints.*;

/**
 *
 * @author cpliu
 */
public class RfAnalogTag extends RfTag {
    @Min(value=0)
    @Max(value=99)
    private int eu_type;
    @Size(min=0, max=6)
    private String unit;
    @Digits(integer=15, fraction=5)
    private double min_eu;

    /**
     * @return the eu_type
     */
    public int getEu_type() {
        return eu_type;
    }

    /**
     * @param eu_type the eu_type to set
     */
    public void setEu_type(int eu_type) {
        this.eu_type = eu_type;
    }

    /**
     * @return the unit
     */
    public String getUnit() {
        return unit;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * @return the min_eu
     */
    public double getMin_eu() {
        return min_eu;
    }

    /**
     * @param min_eu the min_eu to set
     */
    public void setMin_eu(double min_eu) {
        this.min_eu = min_eu;
    }
            
    
}
