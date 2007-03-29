package tests.customext;

import org.apache.ws.commons.schema.extensions.ExtensionSerializer;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.w3c.dom.Node;

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
     
    }
}
