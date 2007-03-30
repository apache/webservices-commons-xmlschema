package tests.customext.attrib;

import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.extensions.ExtensionDeserializer;
import org.w3c.dom.Attr;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;

/**
 * Custom attribute deserializer for our test custom attribute
 */
public class CustomAttributeDeserializer implements ExtensionDeserializer {

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
         if (CustomAttribute.CUSTOM_ATTRIBUTE_QNAME.equals(name)){
             Attr attrib = (Attr)domNode;
             String value = attrib.getValue();
             //break the attrib into
             CustomAttribute customAttrib = new CustomAttribute();
             String[] strings = value.split(":");
             customAttrib.setPrefix(strings[0]);
             customAttrib.setSuffix(strings[1]);

             //put this in the schema object meta info map
             schemaObject.addMetaInfo(CustomAttribute.CUSTOM_ATTRIBUTE_QNAME,customAttrib);
         }
    }
}
