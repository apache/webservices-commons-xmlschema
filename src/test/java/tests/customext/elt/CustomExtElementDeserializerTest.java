package tests.customext.elt;

import junit.framework.TestCase;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Document;
import tests.Resources;

import javax.xml.parsers.DocumentBuilderFactory;
import java.util.Iterator;
import java.util.Map;

/**
 * Test class to run through the full cycle of build-check
 */
public class CustomExtElementDeserializerTest extends TestCase {


    public void testDeserialization() throws Exception {
            //set the system property for the custom extension registry
            System.setProperty(Constants.SystemConstants.EXTENSION_REGISTRY_KEY,
                    CustomExtensionRegistry.class.getName());

           //create a DOM document
           DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
           documentBuilderFactory.setNamespaceAware(true);
           Document doc = documentBuilderFactory.newDocumentBuilder().
                   parse(Resources.asURI("/external/externalElementAnnotations.xsd"));

           XmlSchemaCollection schemaCol = new XmlSchemaCollection();
           XmlSchema schema = schemaCol.read(doc,null);
           assertNotNull(schema);

          // get the elements and check whether their annotations are properly
          // populated
           Iterator values = schema.getElements().getValues();
           while (values.hasNext()) {
               XmlSchemaElement elt =  (XmlSchemaElement) values.next();
               assertNotNull(elt);
               Map metaInfoMap = elt.getMetaInfoMap();
               assertNotNull(metaInfoMap);

               CustomElement customElt = (CustomElement)metaInfoMap.get(CustomElement.CUSTOM_ELT_QNAME);
               assertNotNull(customElt);

           }

            //remove our system property
            System.getProperties().remove(Constants.SystemConstants.EXTENSION_REGISTRY_KEY);

    }
}
