package tests;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.FileInputStream;

import org.apache.axis.xsd.xml.schema.XmlSchemaCollection;
import org.apache.axis.xsd.xml.schema.XmlSchemaElement;
import org.apache.axis.xsd.xml.schema.XmlSchemaType;
import org.apache.axis.xsd.xml.schema.XmlSchemaComplexType;
import org.apache.axis.xsd.xml.schema.XmlSchemaSequence;

import javax.xml.transform.stream.StreamSource;
import javax.xml.namespace.QName;

/**
 */
public class TestForwardRefs extends TestCase {
    public void testForwardRefs() throws Exception {
        QName ELEMENT_QNAME = new QName("http://soapinterop.org/types",
                                        "attrTest");
        InputStream is = new FileInputStream("test-resources/forwardRef.xsd");
        XmlSchemaCollection schema = new XmlSchemaCollection();
        schema.read(new StreamSource(is), null);

        XmlSchemaElement elem = schema.getElementByQName(ELEMENT_QNAME);
        assertNotNull(elem);
        XmlSchemaType type = elem.getSchemaType();
        assertNotNull(type);
        assertTrue(type instanceof XmlSchemaComplexType);
        XmlSchemaComplexType cType = (XmlSchemaComplexType)type;
        XmlSchemaSequence seq = (XmlSchemaSequence)cType.getParticle();
        assertNotNull(seq);
    }
}
