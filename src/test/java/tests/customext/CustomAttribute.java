package tests.customext;

import javax.xml.namespace.QName;

/**
 * Custom Attribute class
 * The is will be with reference to the http://customattrib.org
 * namespace and will have 'customAttrib' as the name and the
 * value will be a prefix and a suffix seperated with a colon
 * see the  externalAnnotations.xsd for an example schema.
 */
public class CustomAttribute {

    public static final QName CUSTOM_ATTRIBUTE_QNAME = new QName("http://customattrib.org","customAttrib");
    private String prefix;
    private String suffix;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }


}
