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
 * XmlSchema.java
 *
 * Created on September 27, 2001, 2:27 AM
 */

package org.apache.axis.xsd.xml.schema;

import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Hashtable;


/**
 * Contains the definition of a schema. All XML Schema definition language (XSD)
 * elements are children of the schema element. Represents the World Wide Web
 * Consortium (W3C) schema element
 *
 * @author mukund
 */

// Oct 15th - momo - initial impl
// Oct 17th - vidyanand - add SimpleType + element
// Oct 18th - momo - add ComplexType
// Oct 19th - vidyanand - handle external
// Dec 6th - Vidyanand - changed RuntimeExceptions thrown to XmlSchemaExceptions
// Jan 15th - Vidyanand - made changes to SchemaBuilder.handleElement to look for an element ref.
// Feb 20th - Joni - Change the getXmlSchemaFromLocation schema 
//            variable to name s.
// Feb 21th - Joni - Port to XMLDomUtil and Tranformation.  

public class XmlSchema extends XmlSchemaAnnotated {
    static final String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    XmlSchemaForm attributeFormDefault, elementFormDefault;

    XmlSchemaObjectTable attributeGroups,
    attributes, elements, groups,
    notations, schemaTypes;
    XmlSchemaDerivationMethod blockDefault, finalDefault;
    XmlSchemaObjectCollection includes, items;
    boolean isCompiled;
    String targetNamespace = "DEFAULT", version;
    Hashtable namespaces;
    String schema_ns_prefix = "";

    /**
     * Creates new XmlSchema
     */
    public XmlSchema() {
        attributeFormDefault = new XmlSchemaForm("Qualified");
        elementFormDefault = new XmlSchemaForm("Qualified");
        blockDefault = new XmlSchemaDerivationMethod("None");
        finalDefault = new XmlSchemaDerivationMethod("None");
        items = new XmlSchemaObjectCollection();
        includes = new XmlSchemaObjectCollection();
        namespaces = new Hashtable();
        elements = new XmlSchemaObjectTable();
        attributeGroups = new XmlSchemaObjectTable();
        attributes = new XmlSchemaObjectTable();
        groups = new XmlSchemaObjectTable();
        notations = new XmlSchemaObjectTable();
        schemaTypes = new XmlSchemaObjectTable();
    }

    public XmlSchema(String namespace) {
        this();
        targetNamespace = namespace;
    }

    protected String getNamespace(String prefix) {
        return "" + namespaces.get(prefix);
    }

    public XmlSchemaForm getAttributeFormDefault() {
        return attributeFormDefault;
    }

    public void setAttributeFormDefault(XmlSchemaForm value) {
        attributeFormDefault = value;
    }

    public XmlSchemaObjectTable getAttributeGroups() {
        return attributeGroups;
    }

    public XmlSchemaObjectTable getAttributes() {
        return attributes;
    }

    public XmlSchemaDerivationMethod getBlockDefault() {
        return blockDefault;
    }

    public void setBlockDefault(XmlSchemaDerivationMethod blockDefault) {
        this.blockDefault = blockDefault;
    }

    public XmlSchemaForm getElementFormDefault() {
        return elementFormDefault;
    }

    public void setElementFormDefault(XmlSchemaForm elementFormDefault) {
        this.elementFormDefault = elementFormDefault;
    }

    public XmlSchemaObjectTable getElements() {
        return elements;
    }

    public XmlSchemaElement getElementByName(QName name) {
        return (XmlSchemaElement)elements.getItem(name);
    }

    public XmlSchemaType getTypeByName(QName name) {
        return (XmlSchemaType)schemaTypes.getItem(name);
    }

    public XmlSchemaDerivationMethod getFinalDefault() {
        return finalDefault;
    }

    public void setFinalDefault(XmlSchemaDerivationMethod finalDefault) {
        this.finalDefault = finalDefault;
    }

    public XmlSchemaObjectTable getGroups() {
        return groups;
    }

    public XmlSchemaObjectCollection getIncludes() {
        return includes;
    }

    public boolean isCompiled() {
        return isCompiled;
    }

    public XmlSchemaObjectCollection getItems() {
        return items;
    }

    public XmlSchemaObjectTable getNotations() {
        return notations;
    }

    public XmlSchemaObjectTable getSchemaTypes() {
        return schemaTypes;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        if (!targetNamespace.equals(""))
            this.targetNamespace = targetNamespace;
    }

    public String getVersion() {
        return version;
    }

    public void compile(ValidationEventHandler eh) {

    }

    public void write(OutputStream out) {
        write(new OutputStreamWriter(out));
    }

    public void write(Writer writer) {
        serialize_internal(this, writer);
    }

    private static void serialize_internal(XmlSchema schema, Writer out) {
        try {
            Document[] serializedSchemas = XmlSchemaSerializer.serializeSchema(schema, true);
            TransformerFactory trFac = TransformerFactory.newInstance();
            Source source = new DOMSource(serializedSchemas[0]);
            Result result = new StreamResult(out);
            javax.xml.transform.Transformer tr = trFac.newTransformer();
            tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tr.setOutputProperty(OutputKeys.INDENT, "yes");
            tr.transform(source, result);
            out.flush();
        } catch (TransformerConfigurationException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (TransformerException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (XmlSchemaSerializer.XmlSchemaSerializerException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (IOException e) {
            throw new XmlSchemaException(e.getMessage());
        }
    }

    public Hashtable getPrefixToNamespaceMap() {
        return namespaces;
    }

    public void setPrefixToNamespaceMap(Hashtable map) {
        this.namespaces = map;
    }

    public void addType(XmlSchemaType type) {
        QName qname = type.getQName();
        if (schemaTypes.contains(qname)) {
            throw new RuntimeException("Schema for namespace '" +
                                       targetNamespace + "' already contains type '" +
                                       qname.getLocalPart());
        }
        schemaTypes.add(qname, type);
    }
}
