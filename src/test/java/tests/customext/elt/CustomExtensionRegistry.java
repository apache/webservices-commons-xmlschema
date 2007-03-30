package tests.customext.elt;

import org.apache.ws.commons.schema.extensions.ExtensionRegistry;

/**
 * Custom extension registry
 */
public class CustomExtensionRegistry extends ExtensionRegistry {

    public CustomExtensionRegistry() {
           //register our custom type
           registerDeserializer(CustomElement.CUSTOM_ELT_QNAME,new CustomElementDeserializer());
           registerSerializer(CustomElement.class,new CustomElementSerializer());
       }

}
