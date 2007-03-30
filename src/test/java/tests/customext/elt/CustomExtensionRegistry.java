package tests.customext.elt;

import org.apache.ws.commons.schema.extensions.ExtensionRegistry;

/**
 * @author : Ajith Ranabahu
 *         Date: Mar 29, 2007
 *         Time: 9:21:40 PM
 */
public class CustomExtensionRegistry extends ExtensionRegistry {

    public CustomExtensionRegistry() {
           //register our custom type
           registerDeserializer(CustomElement.CUSTOM_ELT_QNAME,new CustomElementDeserializer());
           registerSerializer(CustomElement.class,new CustomElementSerializer());
       }

}
