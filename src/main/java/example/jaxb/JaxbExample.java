package example.jaxb;

import example.jaxb.bean.JaxbBean;

import javax.xml.bind.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;

/**
 * Created by ko-aoki on 2016/02/13.
 */
public class JaxbExample {

    public static void main(String[] args) {

        File file = new File("/Users/ko-aoki/dev/Java/ide/ws/sandboxForWork/src/main/resources/test_20160213.xml");
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = new BufferedReader(new FileReader(file));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // StAX用ファクトリの生成
        XMLInputFactory xif = XMLInputFactory.newInstance();
        XMLStreamReader xsr = null;
        try {
            xsr = xif.createXMLStreamReader(
                    new ByteArrayInputStream(sb.toString().getBytes("utf-8")));
            xsr.nextTag();
            // returnを発見
            while(!xsr.getLocalName().equals("return")) {
                xsr.nextTag();
            }
            JAXBContext jc = JAXBContext.newInstance(JaxbBean.class);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement<JaxbBean> je = unmarshaller.unmarshal(xsr, JaxbBean.class);
            JaxbBean value = je.getValue();
            System.out.println(value.getFooElm());
        } catch (XMLStreamException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
    }
}
