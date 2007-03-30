package tests.customext.elt;

import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.extensions.ExtensionSerializer;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.Attr;

import java.util.Map;

import tests.customext.attrib.CustomAttribute;

/**
 * Custom element serializer
 */
public class CustomElementSerializer implements ExtensionSerializer {
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
       CustomElement customElt = (CustomElement)metaInfoMap.get(CustomElement.CUSTOM_ELT_QNAME);

        Element elt = (Element)domNode;
        Element extElt = elt.getOwnerDocument().createElementNS(CustomElement.CUSTOM_ELT_QNAME.getNamespaceURI(),
                                                             CustomElement.CUSTOM_ELT_QNAME.getLocalPart());
        extElt.setAttribute("prefix",customElt.getPrefix());
        extElt.setAttribute("suffix",customElt.getSuffix());

        elt.appendChild(extElt);

    }
}
