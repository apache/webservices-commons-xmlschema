package tests.customext.elt;

import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.extensions.ExtensionDeserializer;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * @author : Ajith Ranabahu
 *         Date: Mar 29, 2007
 *         Time: 9:12:19 PM
 */
public class CustomElementDeserializer implements ExtensionDeserializer {
    /**
     * deserialize the given element
     *
     * @param schemaObject - Parent schema element
     * @param name         - the QName of the element/attribute to be deserialized.
     *                     in the case where a deserializer is used to handle multiple elements/attributes
     *                     this may be useful to determine the correct deserialization
     * @param domNode      - the raw DOM Node read from the source. This will be the
     *                     extension element itself if for an element or the extension attribute object if
     *                     it is an attribute
     */
    public void deserialize(XmlSchemaObject schemaObject, QName name, Node domNode) {
         if (CustomElement.CUSTOM_ELT_QNAME.equals(name)){
             Element elt = (Element)domNode;

             CustomElement customElement = new CustomElement();
             customElement.setPrefix(elt.getAttribute("prefix"));
             customElement.setSuffix(elt.getAttribute("suffix"));

             //put this in the schema object meta info map
             schemaObject.addMetaInfo(CustomElement.CUSTOM_ELT_QNAME,customElement);
         }
    }
}
