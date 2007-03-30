package tests.customext.attrib;

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
 * Deserialize the custom extension types
 */
public class CustomExtDeserializerTest extends TestCase {


    public void testSimpleTypeSchemaGeneration() throws Exception {
            //set the system property for the custom extension registry
            System.setProperty(Constants.SystemConstants.EXTENSION_REGISTRY_KEY,
                    CustomExtensionRegistry.class.getName());

           //create a DOM document
           DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
           documentBuilderFactory.setNamespaceAware(true);
           Document doc = documentBuilderFactory.newDocumentBuilder().
                   parse(Resources.asURI("/external/externalAnnotations.xsd"));

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

               CustomAttribute customAttrib = (CustomAttribute)metaInfoMap.get(CustomAttribute.CUSTOM_ATTRIBUTE_QNAME);
               assertNotNull(customAttrib);

           }

                 //remove our system property
            System.getProperties().remove(Constants.SystemConstants.EXTENSION_REGISTRY_KEY);;

    }
}
