package tests;

import java.io.File;
import java.io.FileInputStream;
import java.util.Set;

import junit.framework.TestCase;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.xml.sax.InputSource;

public class CircularSchemaTest extends TestCase
{
    public void testCircular() throws Exception {
        XmlSchemaCollection schemas = new XmlSchemaCollection();
        File file = new File(Resources.asURI("circular/a.xsd"));
        InputSource source = new InputSource(new FileInputStream(file));
        source.setSystemId(file.toURL().toString());
        
        XmlSchema schema = schemas.read(source, null);
        
        Set xmlSchemas = schemas.getXmlSchemas();
        assertNotNull(xmlSchemas);
        assertEquals(2, xmlSchemas.size());
    }
}