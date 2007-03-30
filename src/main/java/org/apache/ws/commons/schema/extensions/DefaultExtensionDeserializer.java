package org.apache.ws.commons.schema.extensions;

import org.apache.ws.commons.schema.XmlSchemaObject;
import org.apache.ws.commons.schema.constants.Constants;
import org.w3c.dom.Node;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Default deserializer. The action taken when there is nothing specific
 * to be done would be to attach the raw element object as it is to the
 * meta information map for an element or the raw attribute object
 *
 */
public class DefaultExtensionDeserializer implements ExtensionDeserializer {

    /**
     * deserialize the given element
     *
     * @param schemaObject - Parent schema element
     * @param name         - the QName of the element/attribute to be deserialized.
     *                     in the case where a deserializer is used to handle multiple elements/attributes
     *                     this may be useful to determine the correct deserialization
     * @param node - the raw DOM Node read from the source. This will be the
     * extension element itself if for an element or the extension attribute object if
     * it is an attribute
     */
    public void deserialize(XmlSchemaObject schemaObject, QName name, Node node) {

        // we just attach the raw node either to the meta map of
        // elements or the attributes
        Map metaInfoMap =  new HashMap();



        if (node.getNodeType()==Node.ATTRIBUTE_NODE){

            Map attribMap; 
            if (metaInfoMap.containsKey(Constants.MetaDataConstants.EXTERNAL_ATTRIBUTES)){
                attribMap  = (Map)metaInfoMap.get(Constants.MetaDataConstants.EXTERNAL_ATTRIBUTES);
            }else{
                attribMap = new HashMap();
                metaInfoMap.put(Constants.MetaDataConstants.EXTERNAL_ATTRIBUTES,attribMap);
            }
            attribMap.put(name,node);

        }else if (node.getNodeType()==Node.ELEMENT_NODE){
            Map elementMap;
            if (metaInfoMap.containsKey(Constants.MetaDataConstants.EXTERNAL_ELEMENTS)){
                elementMap  = (Map)metaInfoMap.get(Constants.MetaDataConstants.EXTERNAL_ELEMENTS);
            }else{
                elementMap = new HashMap();
                metaInfoMap.put(Constants.MetaDataConstants.EXTERNAL_ELEMENTS,elementMap);
            }
            elementMap.put(name,node);
        }

        //subsequent processing takes place only if this map is not empty
        if (!metaInfoMap.isEmpty()){
            Map metaInfoMapFromSchemaElement = schemaObject.getMetaInfoMap();
            if (metaInfoMapFromSchemaElement==null){
                schemaObject.setMetaInfoMap(metaInfoMap);
            }else{
                metaInfoMapFromSchemaElement.putAll(metaInfoMap);
            }

        }


    }
}
