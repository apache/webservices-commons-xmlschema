/*
 * Copyright 2004,2007 The Apache Software Foundation.
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

import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.utils.NamespaceContextOwner;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
import org.w3c.dom.Document;

import javax.xml.namespace.QName;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;


/**
 * Contains the definition of a schema. All XML Schema definition language (XSD)
 * elements are children of the schema element. Represents the World Wide Web
 * Consortium (W3C) schema element
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

public class XmlSchema extends XmlSchemaAnnotated implements NamespaceContextOwner {
    static final String SCHEMA_NS = "http://www.w3.org/2001/XMLSchema";
    XmlSchemaForm attributeFormDefault, elementFormDefault;

    XmlSchemaObjectTable attributeGroups,
    attributes, elements, groups,
    notations, schemaTypes;
    XmlSchemaDerivationMethod blockDefault, finalDefault;
    XmlSchemaObjectCollection includes, items;
    boolean isCompiled;
    String syntacticalTargetNamespace, logicalTargetNamespace, version;
    String schema_ns_prefix = "";
    XmlSchemaCollection parent;
    private NamespacePrefixList namespaceContext;

    /**
     * Creates new XmlSchema
     */
    public XmlSchema(XmlSchemaCollection parent) {
        this.parent = parent;
        attributeFormDefault = new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        elementFormDefault = new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        blockDefault = new XmlSchemaDerivationMethod(Constants.BlockConstants.NONE);
        finalDefault = new XmlSchemaDerivationMethod(Constants.BlockConstants.NONE);
        items = new XmlSchemaObjectCollection();
        includes = new XmlSchemaObjectCollection();
        elements = new XmlSchemaObjectTable();
        attributeGroups = new XmlSchemaObjectTable();
        attributes = new XmlSchemaObjectTable();
        groups = new XmlSchemaObjectTable();
        notations = new XmlSchemaObjectTable();
        schemaTypes = new XmlSchemaObjectTable();
    }

    public XmlSchema(String namespace, XmlSchemaCollection parent) {
        this(parent);
        syntacticalTargetNamespace = logicalTargetNamespace = namespace;
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
        return syntacticalTargetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        if (!targetNamespace.equals("")) {
            syntacticalTargetNamespace = logicalTargetNamespace = targetNamespace;
        }
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

    public Document[] getAllSchemas() {
        try {
            
            XmlSchemaSerializer xser = new XmlSchemaSerializer();
            xser.setExtReg(this.parent.getExtReg());
            return xser.serializeSchema(this, true);

        } catch (XmlSchemaSerializer.XmlSchemaSerializerException e) {
            throw new XmlSchemaException(e.getMessage());
        }
    }

    private  void serialize_internal(XmlSchema schema, Writer out) {
        try {
            XmlSchemaSerializer xser = new XmlSchemaSerializer();
            xser.setExtReg(this.parent.getExtReg());
            Document[] serializedSchemas = xser.serializeSchema(schema, false);
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

    public void addType(XmlSchemaType type) {
        QName qname = type.getQName();
        if (schemaTypes.contains(qname)) {
            throw new RuntimeException("Schema for namespace '" +
                                       syntacticalTargetNamespace + "' already contains type '" +
                                       qname.getLocalPart());
        }
        schemaTypes.add(qname, type);
    }

    public NamespacePrefixList getNamespaceContext() {
        return namespaceContext;
    }

    /**
     * Sets the schema elements namespace context. This may be used for schema
     * serialization, until a better mechanism was found.
     */
    public void setNamespaceContext(NamespacePrefixList namespaceContext) {
        this.namespaceContext = namespaceContext;
    }
    
    /**
     * Override the equals(Object) method with equivalence checking
     * that is specific to this class.
     */
    public boolean equals(Object what) {
        
        //Note: this method may no longer be required when line number/position are used correctly in XmlSchemaObject.
        //Currently they are simply initialized to zero, but they are used in XmlSchemaObject.equals 
        //which can result in a false positive (e.g. if a WSDL contains 2 inlined schemas).
        
        if (what == this) {
            return true;
        }
        
        //If the inherited behaviour determines that the objects are NOT equal, return false. 
        //Otherwise, do some further equivalence checking.
        
        if(!super.equals(what)) {
            return false;
        }
        
        if (!(what instanceof XmlSchema)) {
            return false;
        }

        XmlSchema xs = (XmlSchema) what;
        
        if (this.id != null) {
            if (!this.id.equals(xs.id)) {
                return false;
            }
        } else {
            if (xs.id != null) {
                return false;
            }
        }
        
        if (this.syntacticalTargetNamespace != null) {
            if (!this.syntacticalTargetNamespace.equals(xs.syntacticalTargetNamespace)) {
                return false;
            }
        } else {
            if (xs.syntacticalTargetNamespace != null) {
                return false;
            }
        }
        
        //TODO decide if further schema content should be checked for equivalence.
        
        return true;
    }
}
