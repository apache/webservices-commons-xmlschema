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
 * XmlSchemaCollection.java
 *
 * Created on September 27, 2001, 3:06 AM
 */

package org.apache.axis.xsd.xml.schema;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Contains a cache of XML Schema definition language (XSD).
 *
 */
public final class XmlSchemaCollection {
    /**
     * Namespaces we know about.  Each one has an equivalent XmlSchema.
     */
    Map namespaces = new HashMap();

    XmlSchema xsd = new XmlSchema(XmlSchema.SCHEMA_NS);

    public void init() {
        XmlSchemaSimpleType type;
        type = new XmlSchemaSimpleType(xsd);
        type.setName("string");
        xsd.addType(type);
        type = new XmlSchemaSimpleType(xsd);
        type.setName("int");
        xsd.addType(type);
        type = new XmlSchemaSimpleType(xsd);
        type.setName("boolean");
        xsd.addType(type);
        type = new XmlSchemaSimpleType(xsd);
        type.setName("float");
        xsd.addType(type);
        type = new XmlSchemaSimpleType(xsd);
        type.setName("double");
        xsd.addType(type);
        type = new XmlSchemaSimpleType(xsd);
        type.setName("long");
        xsd.addType(type);
        type = new XmlSchemaSimpleType(xsd);
        type.setName("short");
        xsd.addType(type);
        type = new XmlSchemaSimpleType(xsd);
        type.setName("qname");
        xsd.addType(type);

        namespaces.put(XmlSchema.SCHEMA_NS, xsd);
    }

    public XmlSchema read(Reader r, ValidationEventHandler veh) {
        return read(new InputSource(r), veh);
    }

    public XmlSchema read(InputSource inputSource, ValidationEventHandler veh) {
        try {
            DocumentBuilderFactory docFac = DocumentBuilderFactory.newInstance();
            docFac.setNamespaceAware(true);
            DocumentBuilder builder = docFac.newDocumentBuilder();
            Document doc = builder.parse(inputSource);
            return read(doc, veh);
        } catch (ParserConfigurationException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (IOException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (SAXException e) {
            throw new XmlSchemaException(e.getMessage());
        }
    }

    public XmlSchema read(Source source, ValidationEventHandler veh) {
        try {
            TransformerFactory trFac = TransformerFactory.newInstance();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamResult result = new StreamResult(out);
            javax.xml.transform.Transformer tr = trFac.newTransformer();
            tr.setOutputProperty(OutputKeys.METHOD, "xml");
            tr.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            tr.transform(source, result);
            ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
            return read(new InputSource(in), veh);
        } catch (TransformerException e) {
            throw new XmlSchemaException(e.getMessage());
        }
    }

    public XmlSchema read(Document doc, ValidationEventHandler veh) {
        SchemaBuilder builder = new SchemaBuilder(this);
        return builder.build(doc, veh);
    }

    public XmlSchema read(Element elem) {
        SchemaBuilder builder = new SchemaBuilder(this);
        return builder.handleXmlSchemaElement(elem);
    }

    /**
     * Creates new XmlSchemaCollection
     */
    public XmlSchemaCollection() {
        init();
    }

    public XmlSchemaElement getElementByQName(QName qname) {
        XmlSchema schema = (XmlSchema)namespaces.get(qname.getNamespaceURI());
        if (schema == null) {
            return null;
        }
        return schema.getElementByName(qname);
    }

    public XmlSchemaType getTypeByQName(QName schemaTypeName) {
        XmlSchema schema = (XmlSchema)namespaces.get(schemaTypeName.getNamespaceURI());
        if (schema == null) {
            return null;
        }
        return schema.getTypeByName(schemaTypeName);
    }

    Map unresolvedTypes = new HashMap();

    void addUnresolvedType(QName type, TypeReceiver receiver) {
        ArrayList receivers = (ArrayList)unresolvedTypes.get(type);
        if (receivers == null) {
            receivers = new ArrayList();
            unresolvedTypes.put(type, receivers);
        }
        receivers.add(receiver);
    }

    void resolveType(QName typeName, XmlSchemaType type) {
        ArrayList receivers = (ArrayList)unresolvedTypes.get(typeName);
        if (receivers == null)
            return;
        for (Iterator i = receivers.iterator(); i.hasNext();) {
            TypeReceiver receiver = (TypeReceiver) i.next();
            receiver.setType(type);
        }
        unresolvedTypes.remove(typeName);
    }
}
