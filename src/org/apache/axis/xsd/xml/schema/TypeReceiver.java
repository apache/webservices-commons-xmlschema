package org.apache.axis.xsd.xml.schema;

/**
 * A TypeReceiver is something that can have its type set.  This gets used
 * to resolve forward references.
 */
public interface TypeReceiver {
    void setType(XmlSchemaType type);
}
