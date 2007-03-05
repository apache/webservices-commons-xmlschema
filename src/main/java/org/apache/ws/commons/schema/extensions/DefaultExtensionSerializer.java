package org.apache.ws.commons.schema.extensions;

import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Node;
import org.w3c.dom.Document;

import java.util.Map;
import java.util.Iterator;

/**

 */
public class DefaultExtensionSerializer implements ExtensionSerializer{

    /**
     * serialize the given element
     *
     * @param schemaObject - Parent schema element
     * @param classOfType  - the class of the object to be serialized
     * @param node - The DOM Node that is the parent of the serialzation
     */
    public void serialize(XmlSchemaObject schemaObject, Class classOfType, Node node) {
        // serialization is somewhat tricky in most cases hence this default serializer will
        // do the exact reverse of the deserializer - look for any plain 'as is' items
        // and attach them to the parent node.
        // we just attach the raw node either to the meta map of
        // elements or the attributes
        Map metaInfoMap = schemaObject.getMetaInfoMap();
        Document parentDoc = node.getOwnerDocument();
        if (metaInfoMap.containsKey(Constants.MetaDataConstants.EXTERNAL_ATTRIBUTES)){
            Map attribMap  = (Map)metaInfoMap.get(Constants.MetaDataConstants.EXTERNAL_ATTRIBUTES);
            for(Iterator it = attribMap.keySet().iterator();it.hasNext();){
                Object key = it.next();
                Object value = attribMap.get(key);
                // this comparison may not be the most ideal but lets keep it for now
                if (value.getClass().equals(classOfType)){
//                   Attr newAtt =
                }

                node.appendChild(
                        parentDoc.importNode((Node)it.next(),true));
            }
        }

        if (metaInfoMap.containsKey(Constants.MetaDataConstants.EXTERNAL_ELEMENTS)){
            Map elementMap  = (Map)metaInfoMap.get(Constants.MetaDataConstants.EXTERNAL_ELEMENTS);
            for(Iterator it = elementMap.values().iterator();it.hasNext();){
                node.appendChild(
                        parentDoc.importNode((Node)it.next(),true));
            }
        }

    }
}
