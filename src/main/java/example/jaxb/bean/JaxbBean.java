package example.jaxb.bean;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Created by ko-aoki on 2016/02/13.
 */
public class JaxbBean {

    @XmlElement(name = "fooElm",namespace = "http://zzz.jp/t/xsd")
    public String getFooElm() {
        return fooElm;
    }

    public void setFooElm(String fooElm) {
        this.fooElm = fooElm;
    }

    private String fooElm;
}
