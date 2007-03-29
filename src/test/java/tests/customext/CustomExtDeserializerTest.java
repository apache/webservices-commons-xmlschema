package tests.customext;

import junit.framework.TestCase;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.constants.Constants;
import tests.Resources;

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

    }
}
