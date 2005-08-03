/*
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *    Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Axis" and "Apache Software Foundation" must
 *    not be used to endorse or promote products derived from this
 *    software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without prior written
 *    permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * XmlSchemaElement.java
 *
 * Created on September 27, 2001, 3:36 AM
 */

package org.apache.axis.xsd.xml.schema;

import javax.xml.namespace.QName;


/**
 * Class for elements. Represents the World Wide Web Consortium (W3C) element element.
 *
 * @author mukund
 */

// October 15th - momo - initial implementation

public class XmlSchemaElement extends XmlSchemaParticle {

    /**
     * Attribute used to block a type derivation.
     */
    XmlSchemaDerivationMethod block;

    /**
     * The value after an element has been compiled to post-schema infoset.
     * This value is either from the type itself or, if not defined on the type, taken from the schema element.
     */
    XmlSchemaDerivationMethod blockResolved;
    XmlSchemaObjectCollection constraints;

    /**
     * Provides the default value of the element if its content
     * is a simple type or the element's content is textOnly.
     */
    String defaultValue;
    String fixedValue;

    /**
     * Returns the correct common runtime library
     * object based upon the SchemaType for the element.
     */
    Object elementType;

    XmlSchemaDerivationMethod finalDerivation;
    XmlSchemaDerivationMethod finalDerivationResolved;

    /**
     * The default value is the value of the elementFormDefault attribute for the schema element containing the attribute.
     * The default is Unqualified.
     */
    XmlSchemaForm form;
    boolean isAbstract;
    boolean isNillable;
    String name;
    QName qualifiedName;
    QName refName;

    /**
     * Returns the type of the element.
     * This can either be a complex type or a simple type.
     */
    XmlSchemaType schemaType;

    /**
     * QName of a built-in data type defined in this schema or another
     * schema indicated by the specified namespace.
     */
    QName schemaTypeName;

    /**
     * QName of an element that can be a substitute for this element.
     */
    QName substitutionGroup;

    /**
     * Creates new XmlSchemaElement
     */
    public XmlSchemaElement() {
        constraints = new XmlSchemaObjectCollection();
        isAbstract = false;
        isNillable = false;
        form = new XmlSchemaForm("None");
        finalDerivation = new XmlSchemaDerivationMethod("None");
        block = new XmlSchemaDerivationMethod("None");
    }

    /**
     * Returns a collection of constraints on the element.
     */
    public XmlSchemaObjectCollection getConstraints() {
        return constraints;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public XmlSchemaDerivationMethod getBlock() {
        return block;
    }

    public void setBlock(XmlSchemaDerivationMethod block) {
        this.block = block;
    }

    public XmlSchemaDerivationMethod getFinal() {
        return finalDerivation;
    }

    public void setFinal(XmlSchemaDerivationMethod finalDerivation) {
        this.finalDerivation = finalDerivation;
    }

    public XmlSchemaDerivationMethod getBlockResolved() {
        return blockResolved;
    }

    public String getFixedValue() {
        return fixedValue;
    }

    public void setFixedValue(String fixedValue) {
        this.fixedValue = fixedValue;
    }

    public Object getElementType() {
        return elementType;
    }

    public XmlSchemaForm getForm() {
        return form;
    }

    public void setForm(XmlSchemaForm form) {
        this.form = form;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
    }

    public boolean isNillable() {
        return isNillable;
    }

    public void setNillable(boolean isNillable) {
        this.isNillable = isNillable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public QName getRefName() {
        return refName;
    }

    public void setRefName(QName refName) {
        this.refName = refName;
    }

    public QName getQName() {
        return qualifiedName;
    }

    public void setQName(QName qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public XmlSchemaType getSchemaType() {
        return schemaType;
    }

    public void setSchemaType(XmlSchemaType schemaType) {
        this.schemaType = schemaType;
    }

    public QName getSchemaTypeName() {
        return schemaTypeName;
    }

    public void setSchemaTypeName(QName schemaTypeName) {
        this.schemaTypeName = schemaTypeName;
    }

    public QName getSubstitutionGroup() {
        return substitutionGroup;
    }

    public void setSubstitutionGroup(QName substitutionGroup) {
        this.substitutionGroup = substitutionGroup;
    }

    public String toString(String prefix, int tab) {
        String xml = new String();

        if (!prefix.equals("") && prefix.indexOf(":") == -1)
            prefix += ":";

        for (int i = 0; i < tab; i++)
            xml += "\t";

        xml += "<" + prefix + "element ";

        if (!name.equals(""))
            xml += "name=\"" + name + "\" ";

        if (schemaTypeName != null)
            xml += "type=\"" + schemaTypeName + "\"";

        if (refName != null)
            xml += "ref=\"" + refName + "\" ";

        if (minOccurs != 1)
            xml += "minOccurs=\"" + minOccurs + "\" ";

        if (maxOccurs != 1)
            xml += "maxOccurs=\"" + maxOccurs + "\" ";

        xml += ">\n";

        if (constraints != null)
            xml += constraints.toString(prefix, (tab + 1));

        if (schemaType != null) {
            xml += schemaType.toString(prefix, (tab + 1));
        }
        for (int i = 0; i < tab; i++)
            xml += "\t";

        xml += "</" + prefix + "element>\n";

        return xml;
    }
}
