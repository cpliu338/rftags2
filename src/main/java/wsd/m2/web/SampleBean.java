package wsd.m2.web;

import java.io.Serializable;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.*;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import org.primefaces.event.SelectEvent;

/**
 *
 * @author cpliu
 */
@Named
@ViewScoped
public class SampleBean implements Serializable {
    
    private final List<String> samples;
    private String p2;
    private String p1;
    
    public SampleBean() {
        samples = new ArrayList<>();
        for (int i=1; i<100; i+=3) {
            samples.add("i"+i);
        }
    }
    
    public void action() {
        p2 = "Product:"+p1;
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "p1 change is {0}", p1);
    }
    
    public void onItemSelect(SelectEvent ev) {
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "event is {0}", 
            (ev==null) ? "null" : ev.getBehavior().toString()
        );
        p2 = "Product:"+p1;
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "p1 select is {0}", p1);
        FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Item Selected", ev.getObject().toString()));
    }

    /**
     * @param qry
     * @return the samples
     */
    public List<String> completeIt(String qry) {
        List<String> result = samples.stream().filter(s -> {
            return s.contains(qry);
        }).collect(Collectors.toList());
        Logger.getLogger(SampleBean.class.getName()).log(Level.INFO, "size is {0} from query {1}", 
                new Object[]{result.size(),qry});
        return result;
    }
    
    public static void main(String[] args) {
        SampleBean bean = new SampleBean();
        for (String i: bean.completeIt("i1")) {
            System.out.println(i);
        }
    }

    /**
     * @return the p2
     */
    public String getP2() {
        return p2;
    }

    /**
     * @return the p1
     */
    public String getP1() {
        return p1;
    }

    /**
     * @param p1 the p1 to set
     */
    public void setP1(String p1) {
        this.p1 = p1;
    }
}
