package tests.customext.elt;

import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.extensions.ExtensionSerializer;
import org.w3c.dom.Node;

/**
 * @author : Ajith Ranabahu
 *         Date: Mar 29, 2007
 *         Time: 9:30:07 PM
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
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
