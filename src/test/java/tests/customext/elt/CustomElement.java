package tests.customext.elt;

import javax.xml.namespace.QName;

/**
 *  Custom Element class
 * The is will be with reference to the http://customattrib.org
 * namespace and will have 'customElt' as the name and the
 * value will have two attributes , prefix and a suffix
 * see the  externalAnnotationsElt.xsd for an example schema.
 */
public class CustomElement {

    public static final QName CUSTOM_ELT_QNAME = new QName("http://customattrib.org","customElt");
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
