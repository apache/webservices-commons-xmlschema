package tests;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.FileInputStream;
import java.io.InputStream;

public class TestSimpleRestriction extends TestCase {
    public void testSimpleRestriction() throws Exception {
        QName TYPE_QNAME = new QName("http://soapinterop.org/types",
                "layoutComponentType");
        QName ELEMENT_QNAME = new QName("http://soapinterop.org/types",
                "foo");

        InputStream is = new FileInputStream(Resources.asURI("SimpleContentRestriction.xsd"));
        XmlSchemaCollection schema = new XmlSchemaCollection();
        XmlSchema s = schema.read(new StreamSource(is), null);

        XmlSchemaType simpleType = schema.getTypeByQName(TYPE_QNAME);
        assertNotNull(simpleType);

        XmlSchemaElement elem = schema.getElementByQName(ELEMENT_QNAME);
        assertNotNull(elem);

        XmlSchemaType type = elem.getSchemaType();
        assertNotNull(type);
    }

    public void testSimpleTypeRestrictionWithoutNamespace() throws Exception {
    	InputStream is = new FileInputStream(Resources.asURI("includedWithoutNamespace.xsd"));
    	XmlSchemaCollection schema = new XmlSchemaCollection();
    	XmlSchema s = schema.read(new StreamSource(is), null);
    	XmlSchemaType principalId = schema.getTypeByQName(new QName("", "XdwsPrincipalId"));
    	assertNotNull(principalId);
    	XmlSchemaType groupId = schema.getTypeByQName(new QName("", "XdwsGroupId"));
    	assertNotNull(groupId);
    	assertEquals(groupId.getBaseSchemaType(), principalId);
    }
}
