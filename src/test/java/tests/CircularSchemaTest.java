package tests;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileInputStream;

public class CircularSchemaTest extends TestCase
{
    public void testCircular() throws Exception {
        XmlSchemaCollection schemas = new XmlSchemaCollection();
        File file = new File(Resources.asURI("circular/a.xsd"));
        InputSource source = new InputSource(new FileInputStream(file));
        source.setSystemId(file.toURL().toString());
        
        schemas.read(source, null);
        
        XmlSchema[] xmlSchemas = schemas.getXmlSchemas();
        assertNotNull(xmlSchemas);
        assertEquals(3, xmlSchemas.length);
    }
}