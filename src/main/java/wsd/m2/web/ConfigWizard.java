package wsd.m2.web;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.ValidatorException;
import org.primefaces.event.FlowEvent;

/**
 *
 * @author cpliu
 */
@javax.faces.bean.ManagedBean
@javax.faces.bean.ViewScoped
public class ConfigWizard implements java.io.Serializable {
    private String contents;
    
    public String onFlowProcess(FlowEvent event) {
        if(contents.startsWith("B")) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, event.getOldStep(), "Stay here");
            FacesContext.getCurrentInstance().addMessage("", msg);
            return event.getOldStep();
        }
        else {
            return event.getNewStep();
        }
    }
    
    public void validateContent(FacesContext context, UIComponent component, Object value) throws ValidatorException {
        String submittedValue = (String)value;
        if (submittedValue.startsWith("A")) {
            throw new ValidatorException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Should not strt with A", "Should not strt with A"));
        }
    }

    /**
     * @return the contents
     */
    public String getContents() {
        return contents;
    }

    /**
     * @param contents the contents to set
     */
    public void setContents(String contents) {
        this.contents = contents;
    }

}
