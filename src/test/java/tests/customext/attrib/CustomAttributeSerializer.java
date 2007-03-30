package tests.customext.attrib;

import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.extensions.ExtensionSerializer;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import java.util.Map;

/**
 * serializer for the custom attribute
 */
public class CustomAttributeSerializer  implements ExtensionSerializer {

    /**
     * serialize the given element
     *
     * @param schemaObject - Parent schema object.contains the extension
     *                     to be serialized
     * @param classOfType  - The class of type to be serialized
     * @param domNode      - the parent DOM Node that will ultimately be serialized. The XMLSchema
     *                     serialization mechanism is to create a DOM tree first and serialize it
     */
    public void serialize(XmlSchemaObject schemaObject, Class classOfType, Node domNode) {
        Map metaInfoMap = schemaObject.getMetaInfoMap();
        CustomAttribute att = (CustomAttribute)metaInfoMap.get(CustomAttribute.CUSTOM_ATTRIBUTE_QNAME);

        Element elt = (Element)domNode;
        Attr att1 = elt.getOwnerDocument().createAttributeNS(CustomAttribute.CUSTOM_ATTRIBUTE_QNAME.getNamespaceURI(),
                                                             CustomAttribute.CUSTOM_ATTRIBUTE_QNAME.getLocalPart());
        att1.setValue(att.getPrefix() + ":" + att.getSuffix());
        elt.setAttributeNodeNS(att1);
    }
}
