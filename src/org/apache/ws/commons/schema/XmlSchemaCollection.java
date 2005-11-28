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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.ws.commons.schema.constants.Constants;

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
    /**
     * base URI is used as the base for loading the
     * imports
     */
    String baseUri = null;
    /**
     * In-scope namespaces for XML processing
     */
    Map inScopeNamespaces = new HashMap();

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
     * This section should comply to the XMLSchema specification
     * @see http://www.w3.org/TR/2004/PER-xmlschema-2-20040318/datatypes.html#built-in-datatypes
     * todo - Some types may be missing.Need a thorough comparison with the mentioned document
     * to fix it.
     */
    public void init() {
        //Primitive types
        addSimpleType(xsd, Constants.XSD_STRING.getLocalPart());
        addSimpleType(xsd, Constants.XSD_BOOLEAN.getLocalPart());
        addSimpleType(xsd, Constants.XSD_FLOAT.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DOUBLE.getLocalPart());
        addSimpleType(xsd, Constants.XSD_QNAME.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DECIMAL.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DURATION.getLocalPart());
        addSimpleType(xsd, Constants.XSD_DATE.getLocalPart());
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

    public String getNamespaceForPrefix(String prefix) {
        return (String)inScopeNamespaces.get(prefix);
    }

    public void mapNamespace(String prefix, String namespaceURI) {
        inScopeNamespaces.put(prefix, namespaceURI);
    }
}
