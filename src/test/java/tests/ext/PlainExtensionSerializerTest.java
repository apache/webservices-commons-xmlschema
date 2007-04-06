package tests.ext;

import junit.framework.TestCase;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import tests.Resources;

import java.util.Iterator;
import java.util.Map;
import java.io.ByteArrayOutputStream;

/**
 * try writing the schemas after they are built
 */
public class PlainExtensionSerializerTest extends TestCase {

     public void testSerialization() throws Exception {

           //create a DOM document
           DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
           documentBuilderFactory.setNamespaceAware(true);
           Document doc = documentBuilderFactory.newDocumentBuilder().
                   parse(Resources.asURI("/external/externalAnnotations.xsd"));

           XmlSchemaCollection schemaCol = new XmlSchemaCollection();
           XmlSchema schema = schemaCol.read(doc,null);
           assertNotNull(schema);

           schema.write(new ByteArrayOutputStream());
     }


    public void testSerialization1() throws Exception {

           //create a DOM document
           DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
           documentBuilderFactory.setNamespaceAware(true);
           Document doc = documentBuilderFactory.newDocumentBuilder().
                   parse(Resources.asURI("/external/externalElementAnnotations.xsd"));

           XmlSchemaCollection schemaCol = new XmlSchemaCollection();
           XmlSchema schema = schemaCol.read(doc,null);
           assertNotNull(schema);

           schema.write(new ByteArrayOutputStream());
     }
}
