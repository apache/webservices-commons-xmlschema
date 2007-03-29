package tests.customext;

import org.apache.ws.commons.schema.extensions.ExtensionRegistry;

/**
 * Custom extension registry to test the functionality
 * of the extension mechanism
 */
public class CustomExtensionRegistry extends ExtensionRegistry {

    public CustomExtensionRegistry() {
        //register our custom type
        registerDeserializer(CustomAttribute.CUSTOM_ATTRIBUTE_QNAME,new CustomAttributeDeserializer());
        registerSerializer(CustomAttribute.class,new CustomAttributeSerializer());
    }


}
