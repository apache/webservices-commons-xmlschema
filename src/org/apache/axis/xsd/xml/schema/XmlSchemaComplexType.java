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
 * XmlSchemaComplexType.java
 *
 * Created on September 27, 2001, 3:40 AM
 */

package org.apache.axis.xsd.xml.schema;

/**
 * Class for complex types. Defines a complex type that determines the
 * set of attributes and content of an element. Represents the World Wide 
 * Web Consortium (W3C) complexType element.
 *
 * @author  mukund
 */

public class XmlSchemaComplexType extends XmlSchemaType {
    XmlSchemaAnyAttribute anyAttribute, attributeWildcard;
    XmlSchemaObjectCollection attributes;
    XmlSchemaObjectTable attributeUses;
    XmlSchemaDerivationMethod block, blockResolved;
    XmlSchemaContentModel contentModel;
    XmlSchemaContentType contentType;
    XmlSchemaParticle particleType, particle;
    boolean isAbstract, isMixed;

    /** Creates new XmlSchemaComplexType */
    public XmlSchemaComplexType() {
        attributes = new XmlSchemaObjectCollection();
        block = new XmlSchemaDerivationMethod("None");
        isAbstract = false;
        isMixed = false;
    }

    public XmlSchemaAnyAttribute getAnyAttribute() {
        return anyAttribute;
    }

    public void setAnyAttribute(XmlSchemaAnyAttribute anyAttribute) {
        this.anyAttribute = anyAttribute;
    }

    public XmlSchemaObjectCollection getAttributes() {
        return attributes;
    }

    public XmlSchemaObjectTable getAttributeUses() {
        return attributeUses;
    }

    public XmlSchemaAnyAttribute getAttributeWildcard() {
        return attributeWildcard;
    }

    public XmlSchemaDerivationMethod getBlock() {
        return block;
    }

    public void setBlock(XmlSchemaDerivationMethod block) {
        this.block = block;
    }

    public XmlSchemaDerivationMethod getBlockResolved() {
        return blockResolved;
    }

    public XmlSchemaContentModel getContentModel() {
        return contentModel;
    }

    public void setContentModel(XmlSchemaContentModel contentModel) {
        this.contentModel = contentModel;
    }

    public XmlSchemaContentType getContentType() {
        return contentType;
    }

    public void setContentType(XmlSchemaContentType contentType) {
        this.contentType = contentType;
    }

    public XmlSchemaParticle getContentTypeParticle() {
        return particleType;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    public void setAbstract(boolean b) {
        isAbstract = b;
    }

    public boolean isMixed() {
        return isMixed;
    }

    public void setMixed(boolean b) {
        isMixed = b;
    }

    public XmlSchemaParticle getParticle() {
        return particle;
    }

    public void setParticle(XmlSchemaParticle particle) {
        this.particle = particle;
    }

    public String toString(String prefix, int tab) {
        String xml = new String();

        for (int i = 0; i < tab; i++)
            xml += "\t";

        if (!prefix.equals("") && prefix.indexOf(":") == -1)
            prefix += ":";

        String typeName = name != null ? name : "";

        xml += "<" + prefix + "complexType name=\"" + typeName + "\">\n";

        if (particle != null)
            xml += particle.toString(prefix, (tab + 1));

        if (contentModel != null)
            xml += contentModel.toString(prefix, (tab + 1));

        for (int i = 0; i < attributes.getCount(); i++) {
            xml += attributes.getItem(i).toString(prefix, (tab + 1));
        }

        for (int i = 0; i < tab; i++)
            xml += "\t";

        xml += "</" + prefix + "complexType>\n";
        return xml;
    }
}
