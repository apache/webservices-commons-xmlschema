/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ws.commons.schema;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.resolver.DefaultURIResolver;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Contains a cache of XML Schema definition language (XSD).
 *
 */
public final class XmlSchemaCollection {
    /**
     * Namespaces we know about.  Each one has an equivalent XmlSchema.
     */
    Map namespaces = new HashMap();
    /**
     * base URI is used as the base for loading the
     * imports
     */
    String baseUri = null;
    /**
     * In-scope namespaces for XML processing
     */
    Map inScopeNamespaces = new HashMap();

    /**
     * An org.xml.sax.EntityResolver that is used to
     * resolve the imports/includes
     */
    URIResolver schemaResolver = new DefaultURIResolver();

    XmlSchema xsd = new XmlSchema(XmlSchema.SCHEMA_NS, this);

    /**
     * Set the base URI. This is used when schemas need to be
     * loaded from relative locations
     * @param baseUri
     */
    public void setBaseUri(String baseUri){
        this.baseUri = baseUri;
    }

    /**
     * Register a custom URI resolver
     * @param schemaResolver
     */
    public void setSchemaResolver(URIResolver schemaResolver) {
        this.schemaResolver = schemaResolver;
    }

    /**
     * This section should comply to the XMLSchema specification
     * @see http://www.w3.org/TR/2004/PER-xmlschema-2-20040318/datatypes.html#built-in-datatypes
     *
     * This needs to be inspected by another pair of eyes
     */
    public void init() {
        /*
        Primitive types

        3.2.1 string
        3.2.2 boolean
        3.2.3 decimal
        3.2.4 float
        3.2.5 double
        3.2.6 duration
        3.2.7 dateTime
        3.2.8 time
        3.2.9 date
        3.2.10 gYearMonth
        3.2.11 gYear
        3.2.12 gMonthDay
        3.2.13 gDay
        3.2.14 gMonth
        3.2.15 hexBinary
        3.2.16 base64Binary
        3.2.17 anyURI
        3.2.18 QName
        3.2.19 NOTATION
        */
        addSimpleType(xsd, Constants.XSD_STRING.getLocalPart());
        addSimpleType(xsd, Constants.XSD_BOOLEAN.getLocalPart());
        addSimpleType(xsd, Constants.XSD_FLOAT.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DOUBLE.getLocalPart());
        addSimpleType(xsd, Constants.XSD_QNAME.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DECIMAL.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DURATION.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DATE.getLocalPart());
        addSimpleType(xsd, Constants.XSD_TIME.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DATETIME.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DAY.getLocalPart());
        addSimpleType(xsd, Constants.XSD_MONTH.getLocalPart());
        addSimpleType(xsd, Constants.XSD_MONTHDAY.getLocalPart());
        addSimpleType(xsd, Constants.XSD_YEAR.getLocalPart());
        addSimpleType(xsd, Constants.XSD_YEARMONTH.getLocalPart());
        addSimpleType(xsd, Constants.XSD_NOTATION.getLocalPart());
        addSimpleType(xsd, Constants.XSD_HEXBIN.getLocalPart());
        addSimpleType(xsd, Constants.XSD_BASE64.getLocalPart());
        addSimpleType(xsd, Constants.XSD_ANYURI.getLocalPart());


        /*
         3.3.1 normalizedString
        3.3.2 token
        3.3.3 language
        3.3.4 NMTOKEN
        3.3.5 NMTOKENS
        3.3.6 Name
        3.3.7 NCName
        3.3.8 ID
        3.3.9 IDREF
        3.3.10 IDREFS
        3.3.11 ENTITY
        3.3.12 ENTITIES
        3.3.13 integer
        3.3.14 nonPositiveInteger
        3.3.15 negativeInteger
        3.3.16 long
        3.3.17 int
        3.3.18 short
        3.3.19 byte
        3.3.20 nonNegativeInteger
        3.3.21 unsignedLong
        3.3.22 unsignedInt
        3.3.23 unsignedShort
        3.3.24 unsignedByte
        3.3.25 positiveInteger
        */

         //derived types from decimal
        addSimpleType(xsd, Constants.XSD_LONG.getLocalPart());
        addSimpleType(xsd, Constants.XSD_SHORT.getLocalPart());
        addSimpleType(xsd, Constants.XSD_BYTE.getLocalPart());
        addSimpleType(xsd, Constants.XSD_INTEGER.getLocalPart());
        addSimpleType(xsd, Constants.XSD_INT.getLocalPart());
        addSimpleType(xsd, Constants.XSD_POSITIVEINTEGER.getLocalPart());
        addSimpleType(xsd, Constants.XSD_NEGATIVEINTEGER.getLocalPart());
        addSimpleType(xsd, Constants.XSD_NONPOSITIVEINTEGER.getLocalPart());
        addSimpleType(xsd, Constants.XSD_NONNEGATIVEINTEGER.getLocalPart());
        addSimpleType(xsd, Constants.XSD_UNSIGNEDBYTE.getLocalPart());
        addSimpleType(xsd, Constants.XSD_UNSIGNEDINT.getLocalPart());
        addSimpleType(xsd, Constants.XSD_UNSIGNEDLONG.getLocalPart());
        addSimpleType(xsd, Constants.XSD_UNSIGNEDSHORT.getLocalPart());

        //derived types from string
        addSimpleType(xsd, Constants.XSD_NAME.getLocalPart());
        addSimpleType(xsd, Constants.XSD_NORMALIZEDSTRING.getLocalPart());
        addSimpleType(xsd, Constants.XSD_NCNAME.getLocalPart());
        addSimpleType(xsd, Constants.XSD_NMTOKEN.getLocalPart());
        addSimpleType(xsd, Constants.XSD_NMTOKENS.getLocalPart());
        addSimpleType(xsd, Constants.XSD_ENTITY.getLocalPart());
        addSimpleType(xsd, Constants.XSD_ENTITIES.getLocalPart());
        addSimpleType(xsd, Constants.XSD_ID.getLocalPart());
        addSimpleType(xsd, Constants.XSD_IDREF.getLocalPart());
        addSimpleType(xsd, Constants.XSD_IDREFS.getLocalPart());
        addSimpleType(xsd, Constants.XSD_LANGUAGE.getLocalPart());
        addSimpleType(xsd, Constants.XSD_TOKEN.getLocalPart());

        namespaces.put(XmlSchema.SCHEMA_NS, xsd);
    }

    private void addSimpleType(XmlSchema schema,String typeName){
        XmlSchemaSimpleType type;
        type = new XmlSchemaSimpleType(schema);
        type.setName(typeName);
        schema.addType(type);
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
            return read(doc, inputSource.getSystemId(), veh);
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
        return builder.build(doc, null, veh);
    }

    public XmlSchema read(Element elem) {
        SchemaBuilder builder = new SchemaBuilder(this);
        return builder.handleXmlSchemaElement(elem, null);
    }
    
    public XmlSchema read(Document doc, String uri, ValidationEventHandler veh) {
        SchemaBuilder builder = new SchemaBuilder(this);
        return builder.build(doc, uri, veh);
    }

    public XmlSchema read(Element elem, String uri) {
        SchemaBuilder builder = new SchemaBuilder(this);
        return builder.handleXmlSchemaElement(elem, uri);
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

    public String getNamespaceForPrefix(String prefix) {
        return (String)inScopeNamespaces.get(prefix);
    }

    public void mapNamespace(String prefix, String namespaceURI) {
        inScopeNamespaces.put(prefix, namespaceURI);
    }
}
