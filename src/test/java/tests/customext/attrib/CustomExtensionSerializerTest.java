package tests.customext.attrib;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;

import tests.Resources;

import java.util.Iterator;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * @author : Ajith Ranabahu
 *         Date: Mar 29, 2007
 *         Time: 10:05:54 PM
 */
public class CustomExtensionSerializerTest extends TestCase {

    public void testDeserialization() throws Exception {
        //set the system property for the custom extension registry
        System.setProperty(Constants.SystemConstants.EXTENSION_REGISTRY_KEY,
                CustomExtensionRegistry.class.getName());

        //create a DOM document
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        Document doc1 = documentBuilderFactory.newDocumentBuilder().
                parse(Resources.asURI("/external/externalAnnotations.xsd"));

        XmlSchemaCollection schemaCol = new XmlSchemaCollection();
        XmlSchema schema = schemaCol.read(doc1,null);
        assertNotNull(schema);

        //now serialize it to a byte stream
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        schema.write(baos);


        Document doc2 = documentBuilderFactory.newDocumentBuilder().
                parse(new ByteArrayInputStream(baos.toByteArray()));

        schema = schemaCol.read(doc2,null);
        assertNotNull(schema);

        // get the elements and check whether their annotations are properly
        // populated
        Iterator values = schema.getElements().getValues();
        while (values.hasNext()) {
            XmlSchemaElement elt =  (XmlSchemaElement) values.next();
            assertNotNull(elt);
            Map metaInfoMap = elt.getMetaInfoMap();
            assertNotNull(metaInfoMap);

            CustomAttribute customAttrib = (CustomAttribute)metaInfoMap.get(CustomAttribute.CUSTOM_ATTRIBUTE_QNAME);
            assertNotNull(customAttrib);

        }


        //remove our system property
        System.getProperties().remove(Constants.SystemConstants.EXTENSION_REGISTRY_KEY);

    }

}
