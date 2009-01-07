/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.ws.commons.schema;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import org.apache.ws.commons.schema.XmlSchemaCollection.SchemaKey;
import org.apache.ws.commons.schema.constants.Constants;
import org.apache.ws.commons.schema.extensions.ExtensionRegistry;
import org.apache.ws.commons.schema.utils.DOMUtil;
import org.apache.ws.commons.schema.utils.NodeNamespaceContext;
import org.apache.ws.commons.schema.utils.TargetNamespaceValidator;
import org.apache.ws.commons.schema.utils.XDOMUtil;

public class SchemaBuilder {
    /*
     * cache of previously resolved schema documents. This cache might be usefull when an application has
     * multiple webservices that each have WSDL documents that import the same schema, for example. On app
     * startup, we may wish to cache XmlSchema objects so we don't build up the schema graph multiple times.
     * key - use a combination of thread id and all three parameters passed to resolveXmlSchema to give
     * minimal thread safety value - XmlSchema object wrapped in a SoftReference to encourage GC in low memory
     * situations.
     * 
     * CAUTION: XmlSchema objects are not likely to be thread-safe. This cache should only be used,
     * then cleared, by callers aware of its existence. It is VERY important that users of this cache call
     * clearCache() after they are done. Usage of the cache is controlled by calling initCache() which will
     * initialize resolvedSchemas to non-null Clearing of cache is done by calling clearCache() which will
     * clear and nullify resolvedSchemas
     */
    private static Map<String, SoftReference<XmlSchema>> resolvedSchemas;
    private static final String[] RESERVED_ATTRIBUTES_LIST =  {"name", 
                                                               "type", 
                                                               "default", 
                                                               "fixed", 
                                                               "form", 
                                                               "id", 
                                                               "use",
                                                               "ref" }; 
    private static final Set<String> RESERVED_ATTRIBUTES  = new HashSet<String>();
    XmlSchemaCollection collection;
    Document currentDocument;
    DocumentBuilderFactory docFac;
    XmlSchema currentSchema;

    /**
     * The extension registry to be used while building the schema model
     */
    private ExtensionRegistry extReg;
    private final TargetNamespaceValidator currentValidator;
    
    static {
        for (String s : RESERVED_ATTRIBUTES_LIST) {
            RESERVED_ATTRIBUTES.add(s);
        }
    }

    /**
     * Schema builder constructor
     * 
     * @param collection
     */
    SchemaBuilder(XmlSchemaCollection collection, TargetNamespaceValidator validator) {
        this.collection = collection;
        this.currentValidator = validator;

        if (collection.getExtReg() != null) {
            this.extReg = collection.getExtReg();
        }

        currentSchema = new XmlSchema();
    }

    public static synchronized void clearCache() {
        if (resolvedSchemas != null) {
            resolvedSchemas.clear(); // necessary?
            resolvedSchemas = null;
        }
    }

    public static synchronized void initCache() {
        if (resolvedSchemas == null) {
            resolvedSchemas = Collections.synchronizedMap(new HashMap<String, SoftReference<XmlSchema>>());
        }
    }

    public ExtensionRegistry getExtReg() {
        return extReg;
    }

    public void setExtReg(ExtensionRegistry extReg) {
        this.extReg = extReg;
    }

    /**
     * build method taking in a document and a validation handler
     * 
     * @param doc
     * @param uri
     * @param veh
     */
    XmlSchema build(Document doc, String uri, ValidationEventHandler veh) {
        Element schemaEl = doc.getDocumentElement();
        XmlSchema xmlSchema = handleXmlSchemaElement(schemaEl, uri);
        xmlSchema.setInputEncoding(DOMUtil.getInputEncoding(doc));
        return xmlSchema;
    }

    XmlSchemaDerivationMethod getDerivation(Element el, String attrName) {
        if (el.hasAttribute(attrName) && !el.getAttribute(attrName).equals("")) {
            // #all | List of (extension | restriction | substitution)
            String derivationMethod = el.getAttribute(attrName).trim();
            return XmlSchemaDerivationMethod.schemaValueOf(derivationMethod);
        }
        return XmlSchemaDerivationMethod.NONE;
    }

    String getEnumString(Element el, String attrName) {
        if (el.hasAttribute(attrName)) {
            return el.getAttribute(attrName).trim();
        }
        return "none"; // local convention for empty value.
    }

    XmlSchemaForm getFormDefault(Element el, String attrName) {
        if (el.getAttributeNode(attrName) != null) {
            String value = el.getAttribute(attrName);
            return XmlSchemaForm.schemaValueOf(value);
        } else {
            return XmlSchemaForm.UNQUALIFIED;
        }
    }

    long getMaxOccurs(Element el) {
        try {
            if (el.getAttributeNode("maxOccurs") != null) {
                String value = el.getAttribute("maxOccurs");
                if ("unbounded".equals(value)) {
                    return Long.MAX_VALUE;
                } else {
                    return Long.parseLong(value);
                }
            }
            return 1;
        } catch (java.lang.NumberFormatException e) {
            return 1;
        }
    }

    long getMinOccurs(Element el) {
        try {
            if (el.getAttributeNode("minOccurs") != null) {
                String value = el.getAttribute("minOccurs");
                if ("unbounded".equals(value)) {
                    return Long.MAX_VALUE;
                } else {
                    return Long.parseLong(value);
                }
            }
            return 1;
        } catch (java.lang.NumberFormatException e) {
            return 1;
        }
    }

    /**
     * Handles the annotation Traversing if encounter appinfo or documentation add it to annotation collection
     */
    XmlSchemaAnnotation handleAnnotation(Element annotEl) {
        XmlSchemaObjectCollection content = new XmlSchemaObjectCollection();
        XmlSchemaAppInfo appInfoObj;
        XmlSchemaDocumentation docsObj;

        for (Element appinfo = XDOMUtil.getFirstChildElementNS(annotEl, XmlSchema.SCHEMA_NS, "appinfo"); 
            appinfo != null; 
            appinfo = XDOMUtil.getNextSiblingElementNS(appinfo, XmlSchema.SCHEMA_NS, "appinfo")) {

            appInfoObj = handleAppInfo(appinfo);
            if (appInfoObj != null) {
                content.add(appInfoObj);
            }
        }
        for (Element documentation = XDOMUtil.getFirstChildElementNS(annotEl, XmlSchema.SCHEMA_NS,
                                                                     "documentation"); 
            documentation != null; 
            documentation = XDOMUtil.getNextSiblingElementNS(documentation, XmlSchema.SCHEMA_NS, 
                                                             "documentation")) {

            docsObj = handleDocumentation(documentation);
            if (docsObj != null) {
                content.add(docsObj);
            }
        }

        XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
        annotation.items = content;

        // process extra attributes and elements
        processExtensibilityComponents(annotation, annotEl);
        return annotation;
    }

    /**
     * create new XmlSchemaAppinfo and add value goten from element to this obj
     * 
     * @param content
     */
    XmlSchemaAppInfo handleAppInfo(Element content) {
        XmlSchemaAppInfo appInfo = new XmlSchemaAppInfo();
        NodeList markup = new DocumentFragmentNodeList(content);

        if (!content.hasAttribute("source")) {
            return null;
        }
        appInfo.setSource(getAttribute(content, "source"));
        appInfo.setMarkup(markup);
        return appInfo;
    }

    /**
     * Handle complex types
     * 
     * @param schema
     * @param complexEl
     * @param schemaEl
     */
    XmlSchemaComplexType handleComplexType(XmlSchema schema, Element complexEl, Element schemaEl) {

        /******
         * set the complexTypeName if any for( eachChildNode) if ( simpleContent) if( restrcition)
         * handle_simple_content_restriction else if( extension) handle_simple_content_extension break; // it
         * has to be the only child else if( complexContent) if( restriction)
         * handle_complex_content_restriction else if( extension) handle_complex_content_extension break; //
         * it has to be the only child else if( group) if( group has ref) store the group name else
         * handleGroup else if( sequence ) handleSequence else if( all ) handleAll else if(choice)
         * handleChoice else if(attribute) handleAttribute else if(attributeGroup) handleAttributeGroup else
         * if(anyAttribute) handleAnyAttribute
         */

        XmlSchemaComplexType ct = new XmlSchemaComplexType(schema);

        if (complexEl.hasAttribute("name")) {

            // String namespace = (schema.targetNamespace==null)?
            // "":schema.targetNamespace;

            ct.name = complexEl.getAttribute("name");
        }
        for (Element el = XDOMUtil.getFirstChildElementNS(complexEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            // String elPrefix = el.getPrefix() == null ? "" :
            // el.getPrefix();
            // if(elPrefix.equals(schema.schema_ns_prefix)) {
            if (el.getLocalName().equals("sequence")) {
                ct.particle = handleSequence(schema, el, schemaEl);
            } else if (el.getLocalName().equals("choice")) {
                ct.particle = handleChoice(schema, el, schemaEl);
            } else if (el.getLocalName().equals("all")) {
                ct.particle = handleAll(schema, el, schemaEl);
            } else if (el.getLocalName().equals("attribute")) {
                ct.attributes.add(handleAttribute(schema, el, schemaEl));
            } else if (el.getLocalName().equals("attributeGroup")) {
                ct.attributes.add(handleAttributeGroupRef(el));
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroupRef group = handleGroupRef(schema, el, schemaEl);
                ct.particle = group.particle == null ? (XmlSchemaParticle)group : group.particle;
            } else if (el.getLocalName().equals("simpleContent")) {
                ct.contentModel = handleSimpleContent(schema, el, schemaEl);
            } else if (el.getLocalName().equals("complexContent")) {
                ct.contentModel = handleComplexContent(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                ct.setAnnotation(handleAnnotation(el));
            } else if (el.getLocalName().equals("anyAttribute")) {
                ct.setAnyAttribute(handleAnyAttribute(schema, el, schemaEl));
            }
            // }
        }
        if (complexEl.hasAttribute("block")) {
            String blockStr = complexEl.getAttribute("block");
            ct.setBlock(XmlSchemaDerivationMethod.schemaValueOf(blockStr));
            // ct.setBlock(new XmlSchemaDerivationMethod(block));
        }
        if (complexEl.hasAttribute("final")) {
            String finalstr = complexEl.getAttribute("final");
            ct.setFinal(XmlSchemaDerivationMethod.schemaValueOf(finalstr));
        }
        if (complexEl.hasAttribute("abstract")) {
            String abs = complexEl.getAttribute("abstract");
            if (abs.equalsIgnoreCase("true")) {
                ct.setAbstract(true);
            } else {
                ct.setAbstract(false);
            }
        }
        if (complexEl.hasAttribute("mixed")) {
            String mixed = complexEl.getAttribute("mixed");
            if (mixed.equalsIgnoreCase("true")) {
                ct.setMixed(true);
            } else {
                ct.setMixed(false);
            }
        }

        // process extra attributes and elements
        processExtensibilityComponents(ct, complexEl);

        return ct;
    }

    // iterate each documentation element, create new XmlSchemaAppinfo and add to collection
    XmlSchemaDocumentation handleDocumentation(Element content) {
        XmlSchemaDocumentation documentation = new XmlSchemaDocumentation();
        List<Node> markup = getChildren(content);

        if (!content.hasAttribute("source") && !content.hasAttribute("xml:lang") && markup == null) {
            return null;
        }

        documentation.setSource(getAttribute(content, "source"));
        documentation.setLanguage(getAttribute(content, "xml:lang"));
        documentation.setMarkup(new DocumentFragmentNodeList(content));

        return documentation;
    }

    /*
     * handle_complex_content_restriction
     */
    /**
     * handle elements
     * 
     * @param schema
     * @param el
     * @param schemaEl
     * @param isGlobal
     */
    XmlSchemaElement handleElement(XmlSchema schema, Element el, Element schemaEl, boolean isGlobal) {

        XmlSchemaElement element = new XmlSchemaElement(schema, isGlobal);

        if (el.getAttributeNode("name") != null) {
            element.setName(el.getAttribute("name"));
        }

        // String namespace = (schema.targetNamespace==null)?
        // "" : schema.targetNamespace;

        boolean isQualified = schema.getElementFormDefault() == XmlSchemaForm.QUALIFIED;
        isQualified = handleElementForm(el, element, isQualified);

        handleElementName(isGlobal, element, isQualified);
        handleElementAnnotation(el, element);
        handleElementGlobalType(el, element);

        Element simpleTypeEl;
        Element complexTypeEl;
        Element keyEl;
        Element keyrefEl;
        Element uniqueEl;
        simpleTypeEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS, "simpleType");
        if (simpleTypeEl != null) {

            XmlSchemaSimpleType simpleType = handleSimpleType(schema, simpleTypeEl, schemaEl);
            element.setSchemaType(simpleType);
            element.setSchemaTypeName(simpleType.getQName());
        } else {
            complexTypeEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS, "complexType");
            if (complexTypeEl != null) {
                element.setSchemaType(handleComplexType(schema, complexTypeEl, schemaEl));
            }
        }

        keyEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS, "key");
        if (keyEl != null) {
            while (keyEl != null) {
                element.getConstraints().add(handleConstraint(keyEl, XmlSchemaKey.class));
                keyEl = XDOMUtil.getNextSiblingElement(keyEl, "key");
            }
        }
        
        keyrefEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS, "keyref");
        if (keyrefEl != null) {
            while (keyrefEl != null) {
                XmlSchemaKeyref keyRef = (XmlSchemaKeyref)handleConstraint(keyrefEl, XmlSchemaKeyref.class);
                if (keyrefEl.hasAttribute("refer")) {
                    String name = keyrefEl.getAttribute("refer");
                    keyRef.refer = getRefQName(name, el);
                }
                element.getConstraints().add(keyRef);
                keyrefEl = XDOMUtil.getNextSiblingElement(keyrefEl, "keyref");
            }
        }
        
        uniqueEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS, "unique");
        if (uniqueEl != null) {
            while (uniqueEl != null) {
                element.getConstraints().add(handleConstraint(uniqueEl, XmlSchemaUnique.class));
                uniqueEl = XDOMUtil.getNextSiblingElement(uniqueEl, "unique");
            }
        }

        if (el.hasAttribute("abstract")) {
            element.setAbstractElement(Boolean.valueOf(el.getAttribute("abstract")).booleanValue());
        }

        if (el.hasAttribute("block")) {
            element.setBlock(getDerivation(el, "block"));
        }

        if (el.hasAttribute("default")) {
            element.setDefaultValue(el.getAttribute("default"));
        }

        if (el.hasAttribute("final")) {
            element.setFinalDerivation(getDerivation(el, "final"));
        }

        if (el.hasAttribute("fixed")) {
            element.setFixedValue(el.getAttribute("fixed"));
        }

        if (el.hasAttribute("id")) {
            element.setId(el.getAttribute("id"));
        }

        if (el.hasAttribute("nillable")) {
            element.setNillable(Boolean.valueOf(el.getAttribute("nillable")).booleanValue());
        }

        if (el.hasAttribute("substitutionGroup")) {
            String substitutionGroup = el.getAttribute("substitutionGroup");
            element.setSubstitutionGroup(getRefQName(substitutionGroup, el));
        }

        element.setMinOccurs(getMinOccurs(el));
        element.setMaxOccurs(getMaxOccurs(el));

        // process extra attributes and elements
        processExtensibilityComponents(element, el);

        return element;
    }

    private void handleElementGlobalType(Element el, XmlSchemaElement element) {
        if (el.getAttributeNode("type") != null) {
            String typeName = el.getAttribute("type");
            element.setSchemaTypeName(getRefQName(typeName, el));
            QName typeQName = element.getSchemaTypeName();

            XmlSchemaType type = collection.getTypeByQName(typeQName);
            if (type == null) {
                // Could be a forward reference...
                collection.addUnresolvedType(typeQName, element);
            }
            element.setSchemaType(type);
        } else if (el.getAttributeNode("ref") != null) {
            String refName = el.getAttribute("ref");
            QName refQName = getRefQName(refName, el);
            element.setRefName(refQName);
            element.setName(refQName.getLocalPart());
        }
    }

    private void handleElementAnnotation(Element el, XmlSchemaElement element) {
        Element annotationEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);

            element.setAnnotation(annotation);
        }
    }

    private void handleElementName(boolean isGlobal, XmlSchemaElement element, boolean isQualified) {
    }

    private boolean handleElementForm(Element el, XmlSchemaElement element, boolean isQualified) {
        if (el.hasAttribute("form")) {
            String formDef = el.getAttribute("form");
            element.setForm(XmlSchemaForm.schemaValueOf(formDef));
        }
        isQualified = element.getForm() == XmlSchemaForm.QUALIFIED;

        return isQualified;
    }

    /**
     * Handle the import
     * 
     * @param schema
     * @param importEl
     * @param schemaEl
     * @return XmlSchemaObject
     */
    XmlSchemaImport handleImport(XmlSchema schema, Element importEl, Element schemaEl) {

        XmlSchemaImport schemaImport = new XmlSchemaImport();

        Element annotationEl = XDOMUtil.getFirstChildElementNS(importEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation importAnnotation = handleAnnotation(annotationEl);
            schemaImport.setAnnotation(importAnnotation);
        }

        schemaImport.namespace = importEl.getAttribute("namespace");
        final String uri = schemaImport.namespace;
        schemaImport.schemaLocation = importEl.getAttribute("schemaLocation");

        TargetNamespaceValidator validator = new TargetNamespaceValidator() {
            public void validate(XmlSchema pSchema) {
                final boolean valid;
                if (isEmpty(uri)) {
                    valid = isEmpty(pSchema.syntacticalTargetNamespace);
                } else {
                    valid = pSchema.syntacticalTargetNamespace.equals(uri);
                }
                if (!valid) {
                    throw new XmlSchemaException("An imported schema was announced to have the namespace "
                                                 + uri + ", but has the namespace "
                                                 + pSchema.syntacticalTargetNamespace);
                }
            }

            private boolean isEmpty(String pValue) {
                return pValue == null || Constants.NULL_NS_URI.equals(pValue);
            }
        };
        if (schemaImport.schemaLocation != null && !schemaImport.schemaLocation.equals("")) {
            if (schema.getSourceURI() != null) {
                schemaImport.schema = resolveXmlSchema(uri, schemaImport.schemaLocation, schema
                    .getSourceURI(), validator);
            } else {
                schemaImport.schema = resolveXmlSchema(schemaImport.namespace, schemaImport.schemaLocation,
                                                       validator);
            }
        }
        return schemaImport;
    }

    /**
     * Handles the include
     * 
     * @param schema
     * @param includeEl
     * @param schemaEl
     */
    XmlSchemaInclude handleInclude(final XmlSchema schema, Element includeEl, Element schemaEl) {

        XmlSchemaInclude include = new XmlSchemaInclude();

        Element annotationEl = XDOMUtil.getFirstChildElementNS(includeEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation includeAnnotation = handleAnnotation(annotationEl);
            include.setAnnotation(includeAnnotation);
        }

        include.schemaLocation = includeEl.getAttribute("schemaLocation");

        // includes are not supposed to have a target namespace
        // we should be passing in a null in place of the target
        // namespace

        final TargetNamespaceValidator validator = newIncludeValidator(schema);
        if (schema.getSourceURI() != null) {
            include.schema = resolveXmlSchema(schema.logicalTargetNamespace, include.schemaLocation, schema
                .getSourceURI(), validator);
        } else {
            include.schema = resolveXmlSchema(schema.logicalTargetNamespace, include.schemaLocation,
                                              validator);
        }

        // process extra attributes and elements
        processExtensibilityComponents(include, includeEl);
        return include;
    }

    /**
     * Handles simple types
     * 
     * @param schema
     * @param simpleEl
     * @param schemaEl
     */
    XmlSchemaSimpleType handleSimpleType(XmlSchema schema, Element simpleEl, Element schemaEl) {
        XmlSchemaSimpleType simpleType = new XmlSchemaSimpleType(schema);
        if (simpleEl.hasAttribute("name")) {
            simpleType.name = simpleEl.getAttribute("name");
        }

        handleSimpleTypeFinal(simpleEl, simpleType);

        Element simpleTypeAnnotationEl = XDOMUtil.getFirstChildElementNS(simpleEl, XmlSchema.SCHEMA_NS,
                                                                         "annotation");

        if (simpleTypeAnnotationEl != null) {
            XmlSchemaAnnotation simpleTypeAnnotation = handleAnnotation(simpleTypeAnnotationEl);

            simpleType.setAnnotation(simpleTypeAnnotation);
        }

        Element unionEl;
        Element listEl;
        Element restrictionEl;
        restrictionEl = XDOMUtil.getFirstChildElementNS(simpleEl, XmlSchema.SCHEMA_NS, "restriction");
        listEl = XDOMUtil.getFirstChildElementNS(simpleEl, XmlSchema.SCHEMA_NS, "list");
        unionEl = XDOMUtil.getFirstChildElementNS(simpleEl, XmlSchema.SCHEMA_NS, "union");
        if (restrictionEl != null) {

            handleSimpleTypeRestriction(schema, schemaEl, simpleType, restrictionEl);

        } else if (listEl != null) {

            handleSimpleTypeList(schema, schemaEl, simpleType, listEl);

        } else if (unionEl != null) {

            handleSimpleTypeUnion(schema, schemaEl, simpleType, unionEl);
        }

        // process extra attributes and elements
        processExtensibilityComponents(simpleType, simpleEl);

        return simpleType;
    }

    private void handleSimpleTypeUnion(XmlSchema schema, Element schemaEl, XmlSchemaSimpleType simpleType,
                                       Element unionEl) {
        XmlSchemaSimpleTypeUnion union = new XmlSchemaSimpleTypeUnion();

        /******
         * if( union has a memberTypes attribute ) add the memberTypeSources string for (each memberType
         * in the list ) lookup(memberType) for( all SimpleType child Elements) add the simpleTypeName (if
         * any) to the memberType Sources do a handleSimpleType with the simpleTypeElement
         */
        if (unionEl.hasAttribute("memberTypes")) {
            String memberTypes = unionEl.getAttribute("memberTypes");
            union.memberTypesSource = memberTypes;
            Vector<QName> v = new Vector<QName>();
            StringTokenizer tokenizer = new StringTokenizer(memberTypes, " ");
            while (tokenizer.hasMoreTokens()) {
                String member = tokenizer.nextToken();
                v.add(getRefQName(member, unionEl));
            }
            union.memberTypesQNames = new QName[v.size()];
            v.copyInto(union.memberTypesQNames);
        }

        Element inlineUnionType = XDOMUtil.getFirstChildElementNS(unionEl, XmlSchema.SCHEMA_NS,
                                                                  "simpleType");
        while (inlineUnionType != null) {

            XmlSchemaSimpleType unionSimpleType = handleSimpleType(schema, inlineUnionType, schemaEl);

            union.baseTypes.add(unionSimpleType);

            if (unionSimpleType.name != null) {
                union.memberTypesSource += " " + unionSimpleType.name;
            }

            inlineUnionType = XDOMUtil.getNextSiblingElementNS(inlineUnionType, XmlSchema.SCHEMA_NS,
                                                               "simpleType");
        }

        // NodeList annotations = unionEl.getElementsByTagNameNS(
        // XmlSchema.SCHEMA_NS, "annotation");
        Element unionAnnotationEl = XDOMUtil.getFirstChildElementNS(unionEl, XmlSchema.SCHEMA_NS,
                                                                    "annotation");

        if (unionAnnotationEl != null) {
            XmlSchemaAnnotation unionAnnotation = handleAnnotation(unionAnnotationEl);

            union.setAnnotation(unionAnnotation);
        }
        simpleType.content = union;
    }

    private void handleSimpleTypeList(XmlSchema schema, Element schemaEl, XmlSchemaSimpleType simpleType,
                                      Element listEl) {
        XmlSchemaSimpleTypeList list = new XmlSchemaSimpleTypeList();

        /******
         * if( list has an itemType attribute ) set the baseTypeName and look up the base type else if(
         * list has a SimpleTypeElement as child) get that element and do a handleSimpleType set the list
         * has the content of the simpleType
         */
        Element inlineListType;
        Element listAnnotationEl;
        inlineListType = XDOMUtil.getFirstChildElementNS(listEl, XmlSchema.SCHEMA_NS, "simpleType");
        if (listEl.hasAttribute("itemType")) {
            String name = listEl.getAttribute("itemType");
            list.itemTypeName = getRefQName(name, listEl);
        } else if (inlineListType != null) {

            list.itemType = handleSimpleType(schema, inlineListType, schemaEl);
        }

        listAnnotationEl = XDOMUtil.getFirstChildElementNS(listEl, XmlSchema.SCHEMA_NS, "annotation");
        if (listAnnotationEl != null) {

            XmlSchemaAnnotation listAnnotation = handleAnnotation(listAnnotationEl);
            list.setAnnotation(listAnnotation);
        }
        simpleType.content = list;
    }

    private void handleSimpleTypeRestriction(XmlSchema schema, Element schemaEl,
                                             XmlSchemaSimpleType simpleType, Element restrictionEl) {
        XmlSchemaSimpleTypeRestriction restriction = new XmlSchemaSimpleTypeRestriction();

        Element restAnnotationEl = XDOMUtil.getFirstChildElementNS(restrictionEl, XmlSchema.SCHEMA_NS,
                                                                   "annotation");

        if (restAnnotationEl != null) {
            XmlSchemaAnnotation restAnnotation = handleAnnotation(restAnnotationEl);
            restriction.setAnnotation(restAnnotation);
        }
        /**
         * if (restriction has a base attribute ) set the baseTypeName and look up the base type else if(
         * restriction has a SimpleType Element as child) get that element and do a handleSimpleType; get
         * the children of restriction other than annotation and simpleTypes and construct facets from it;
         * set the restriction has the content of the simpleType
         **/

        Element inlineSimpleType = XDOMUtil.getFirstChildElementNS(restrictionEl, XmlSchema.SCHEMA_NS,
                                                                   "simpleType");

        if (restrictionEl.hasAttribute("base")) {
            NamespaceContext ctx = NodeNamespaceContext.getNamespaceContext(restrictionEl);
            restriction.baseTypeName = getRefQName(restrictionEl.getAttribute("base"), ctx);
        } else if (inlineSimpleType != null) {

            restriction.baseType = handleSimpleType(schema, inlineSimpleType, schemaEl);
        }
        for (Element el = XDOMUtil.getFirstChildElementNS(restrictionEl, XmlSchema.SCHEMA_NS); 
             el != null;
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (!el.getLocalName().equals("annotation") && !el.getLocalName().equals("simpleType")) {

                XmlSchemaFacet facet = XmlSchemaFacet.construct(el);
                Element annotation = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS,
                                                                     "annotation");

                if (annotation != null) {
                    XmlSchemaAnnotation facetAnnotation = handleAnnotation(annotation);
                    facet.setAnnotation(facetAnnotation);
                }
                restriction.facets.add(facet);
            }

        }
        simpleType.content = restriction;
    }

    private void handleSimpleTypeFinal(Element simpleEl, XmlSchemaSimpleType simpleType) {
        if (simpleEl.hasAttribute("final")) {
            String finalstr = simpleEl.getAttribute("final");
            simpleType.setFinal(XmlSchemaDerivationMethod.schemaValueOf(finalstr));
        }
    }

    /**
     * handles the schema element
     * 
     * @param schemaEl
     * @param systemId
     */
    XmlSchema handleXmlSchemaElement(Element schemaEl, String systemId) {
        // get all the attributes along with the namespace declns
        currentSchema.setNamespaceContext(NodeNamespaceContext.getNamespaceContext(schemaEl));
        setNamespaceAttributes(currentSchema, schemaEl);

        XmlSchemaCollection.SchemaKey schemaKey = 
            new XmlSchemaCollection.SchemaKey(currentSchema.logicalTargetNamespace,
                                              systemId);
        handleSchemaElementBasics(schemaEl, systemId, schemaKey);

        Element el = XDOMUtil.getFirstChildElementNS(schemaEl, XmlSchema.SCHEMA_NS);
        if (el == null
            && XDOMUtil.getFirstChildElementNS(schemaEl, "http://www.w3.org/1999/XMLSchema") != null) {
            throw new XmlSchemaException(
                                         "Schema defined using \"http://www.w3.org/1999/XMLSchema\" " 
                                         +     "is not supported. "
                                             + "Please update the schema to the \"" + XmlSchema.SCHEMA_NS
                                             + "\" namespace");
        }
        for (; el != null; el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {
            handleSchemaElementChild(schemaEl, el);
        }

        // add the extensibility components
        processExtensibilityComponents(currentSchema, schemaEl);

        return currentSchema;
    }

    private void handleSchemaElementChild(Element schemaEl, Element el) {
        if (el.getLocalName().equals("simpleType")) {
            XmlSchemaType type = handleSimpleType(currentSchema, el, schemaEl);
            currentSchema.addType(type);
            currentSchema.items.add(type);
            collection.resolveType(type.getQName(), type);
        } else if (el.getLocalName().equals("complexType")) {
            XmlSchemaType type = handleComplexType(currentSchema, el, schemaEl);
            currentSchema.addType(type);
            currentSchema.items.add(type);
            collection.resolveType(type.getQName(), type);
        } else if (el.getLocalName().equals("element")) {
            XmlSchemaElement element = handleElement(currentSchema, el, schemaEl, true);
            if (element.isTopLevel()) {
                currentSchema.elements.collection.put(element.getQName(), element);
            } else if (element.getRefName() != null) {
                currentSchema.elements.collection.put(element.getRefName(), element);
            }
            currentSchema.items.add(element);
        } else if (el.getLocalName().equals("include")) {
            XmlSchemaInclude include = handleInclude(currentSchema, el, schemaEl);
            currentSchema.includes.add(include);
            currentSchema.items.add(include);

        } else if (el.getLocalName().equals("import")) {
            XmlSchemaImport schemaImport = handleImport(currentSchema, el, schemaEl);
            currentSchema.includes.add(schemaImport);
            currentSchema.items.add(schemaImport);

        } else if (el.getLocalName().equals("group")) {
            XmlSchemaGroup group = handleGroup(currentSchema, el, schemaEl);
            currentSchema.groups.collection.put(group.name, group);
            currentSchema.items.add(group);
        } else if (el.getLocalName().equals("attributeGroup")) {
            XmlSchemaAttributeGroup group = handleAttributeGroup(currentSchema, el, schemaEl);
            currentSchema.attributeGroups.collection.put(group.name, group);
            currentSchema.items.add(group);
        } else if (el.getLocalName().equals("attribute")) {
            XmlSchemaAttribute attr = handleAttribute(currentSchema, el, schemaEl, true); // pass true to
            // indicate that it is
            // a top level child
            currentSchema.attributes.collection.put(attr.getQName(), attr);
            currentSchema.items.add(attr);
        } else if (el.getLocalName().equals("redefine")) {
            XmlSchemaRedefine redefine = handleRedefine(currentSchema, el, schemaEl);
            currentSchema.includes.add(redefine);
        } else if (el.getLocalName().equals("notation")) {
            XmlSchemaNotation notation = handleNotation(currentSchema, el);
            currentSchema.notations.
                collection.put(new QName(currentSchema.getTargetNamespace(), notation.name),
                                            notation);
            currentSchema.items.add(notation);
        } else if (el.getLocalName().equals("annotation")) {
            XmlSchemaAnnotation annotation = handleAnnotation(el);
            currentSchema.setAnnotation(annotation);
        }
    }

    private void handleSchemaElementBasics(Element schemaEl, String systemId,
                                           XmlSchemaCollection.SchemaKey schemaKey) {
        if (!collection.containsSchema(schemaKey)) {
            collection.addSchema(schemaKey, currentSchema);
            currentSchema.parent = collection; // establish parentage now.
        } else {
            throw new XmlSchemaException("Schema name conflict in collection. Namespace: "
                                         + currentSchema.logicalTargetNamespace);
        }

        currentSchema.setElementFormDefault(this.getFormDefault(schemaEl, "elementFormDefault"));
        currentSchema.setAttributeFormDefault(this.getFormDefault(schemaEl, "attributeFormDefault"));
        currentSchema.setBlockDefault(this.getDerivation(schemaEl, "blockDefault"));
        currentSchema.setFinalDefault(this.getDerivation(schemaEl, "finalDefault"));
        /* set id attribute */
        if (schemaEl.hasAttribute("id")) {
            currentSchema.setId(schemaEl.getAttribute("id"));
        }

        currentSchema.setSourceURI(systemId);
    }

    /**
     * Resolve the schemas
     * 
     * @param targetNamespace
     * @param schemaLocation
     */
    XmlSchema resolveXmlSchema(String targetNamespace, String schemaLocation, String baseUri,
                               TargetNamespaceValidator validator) {

        String schemaKey = null;
        if (resolvedSchemas != null) { // cache is initialized, use it
            // Not being very smart about this at the moment. One could, for example,
            // see that the schemaLocation or baseUri is the same as another, but differs
            // only by a trailing slash. As it is now, we assume a single character difference
            // means it's a schema that has yet to be resolved.
            schemaKey = Thread.currentThread().getId() + targetNamespace + schemaLocation + baseUri;
            SoftReference<XmlSchema> softref = resolvedSchemas.get(schemaKey);
            if (softref != null) {
                XmlSchema resolvedSchema = softref.get();
                if (resolvedSchema != null) {
                    return resolvedSchema;
                }
            }
        }

        // use the entity resolver provided if the schema location is present null
        if (schemaLocation != null && !"".equals(schemaLocation)) {
            InputSource source = collection.getSchemaResolver().resolveEntity(targetNamespace,
                                                                              schemaLocation, baseUri);

            // the entity resolver was unable to resolve this!!
            if (source == null) {
                // try resolving it with the target namespace only with the
                // known namespace map
                return collection.getKnownSchema(targetNamespace);
            }
            final String systemId = source.getSystemId() == null ? schemaLocation : source.getSystemId();
            // Push repaired system id back into source where read sees it.
            // It is perhaps a bad thing to patch the source, but this fixes
            // a problem.
            source.setSystemId(systemId);
            final SchemaKey key = new XmlSchemaCollection.SchemaKey(targetNamespace, systemId);
            XmlSchema schema = collection.getSchema(key);
            if (schema != null) {
                return schema;
            }
            if (collection.check(key)) {
                collection.push(key);
                try {
                    XmlSchema readSchema = collection.read(source, null, validator);
                    if (resolvedSchemas != null) {
                        resolvedSchemas.put(schemaKey, new SoftReference<XmlSchema>(readSchema));
                    }
                    return readSchema;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    collection.pop();
                }
            }
        } else {
            XmlSchema schema = collection.getKnownSchema(targetNamespace);
            if (schema != null) {
                return schema;
            }
        }

        return null;
    }

    /**
     * Resolve the schemas
     * 
     * @param targetNamespace
     * @param schemaLocation
     */
    XmlSchema resolveXmlSchema(String targetNamespace, String schemaLocation,
                               TargetNamespaceValidator validator) {

        return resolveXmlSchema(targetNamespace, schemaLocation, collection.baseUri, validator);

    }

    void setNamespaceAttributes(XmlSchema schema, Element schemaEl) {
        // no targetnamespace found !
        if (schemaEl.getAttributeNode("targetNamespace") != null) {
            String contain = schemaEl.getAttribute("targetNamespace");
            schema.setTargetNamespace(contain);
        } else {
            // do nothing here
        }
        if (currentValidator != null) {
            currentValidator.validate(schema);
        }
    }

    private String getAttribute(Element content, String attrName) {
        if (content.hasAttribute(attrName)) {
            return content.getAttribute(attrName);
        }
        return null;
    }

    private List<Node> getChildren(Element content) {
        List<Node> result = new ArrayList<Node>();
        for (Node n = content.getFirstChild(); n != null; n = n.getNextSibling()) {
            result.add(n);
        }
        if (result.size() == 0) {
            return null;
        } else {
            return result;
        }
    }

    private QName getRefQName(String pName, NamespaceContext pContext) {
        final int offset = pName.indexOf(':');
        String uri;
        final String localName;
        final String prefix;
        if (offset == -1) {
            uri = pContext.getNamespaceURI(Constants.DEFAULT_NS_PREFIX);
            if (Constants.NULL_NS_URI.equals(uri)) {
                return new QName(Constants.NULL_NS_URI, pName);
            }
            localName = pName;
            prefix = Constants.DEFAULT_NS_PREFIX;
        } else {
            prefix = pName.substring(0, offset);
            uri = pContext.getNamespaceURI(prefix);
            if (uri == null || Constants.NULL_NS_URI.equals(uri)
                 && currentSchema.parent != null && currentSchema.parent.getNamespaceContext() != null) {
                uri = currentSchema.parent.getNamespaceContext().getNamespaceURI(prefix);
            }

            if (uri == null || Constants.NULL_NS_URI.equals(uri)) {
                throw new IllegalStateException("The prefix " + prefix + " is not bound.");
            }
            localName = pName.substring(offset + 1);
        }
        return new QName(uri, localName, prefix);
    }

    private QName getRefQName(String pName, Node pNode) {
        return getRefQName(pName, NodeNamespaceContext.getNamespaceContext(pNode));
    }

    private XmlSchemaAll handleAll(XmlSchema schema, Element allEl, Element schemaEl) {

        XmlSchemaAll all = new XmlSchemaAll();

        // handle min and max occurences
        all.setMinOccurs(getMinOccurs(allEl));
        all.setMaxOccurs(getMaxOccurs(allEl));

        for (Element el = XDOMUtil.getFirstChildElementNS(allEl, XmlSchema.SCHEMA_NS); 
            el != null; 
            el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("element")) {
                XmlSchemaElement element = handleElement(schema, el, schemaEl, false);
                all.items.add(element);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation annotation = handleAnnotation(el);
                all.setAnnotation(annotation);
            }
        }
        return all;
    }

    /** @noinspection UnusedParameters */
    private XmlSchemaAny handleAny(XmlSchema schema, Element anyEl, Element schemaEl) {

        XmlSchemaAny any = new XmlSchemaAny();

        if (anyEl.hasAttribute("namespace")) {
            any.namespace = anyEl.getAttribute("namespace");
        }

        if (anyEl.hasAttribute("processContents")) {
            String processContent = getEnumString(anyEl, "processContents");

            any.processContent = XmlSchemaContentProcessing.schemaValueOf(processContent);
        }

        Element annotationEl = XDOMUtil.getFirstChildElementNS(anyEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);
            any.setAnnotation(annotation);
        }
        any.setMinOccurs(getMinOccurs(anyEl));
        any.setMaxOccurs(getMaxOccurs(anyEl));

        return any;
    }

    /** @noinspection UnusedParameters */
    private XmlSchemaAnyAttribute handleAnyAttribute(XmlSchema schema, Element anyAttrEl, Element schemaEl) {

        XmlSchemaAnyAttribute anyAttr = new XmlSchemaAnyAttribute();

        if (anyAttrEl.hasAttribute("namespace")) {
            anyAttr.namespace = anyAttrEl.getAttribute("namespace");
        }

        if (anyAttrEl.hasAttribute("processContents")) {

            String contentProcessing = getEnumString(anyAttrEl, "processContents");

            anyAttr.processContent = XmlSchemaContentProcessing.schemaValueOf(contentProcessing);
        }
        if (anyAttrEl.hasAttribute("id")) {
            anyAttr.setId(anyAttrEl.getAttribute("id"));
        }

        Element annotationEl = XDOMUtil.getFirstChildElementNS(anyAttrEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);

            anyAttr.setAnnotation(annotation);
        }
        return anyAttr;
    }

    /**
     * Process non-toplevel attributes
     * 
     * @param schema
     * @param attrEl
     * @param schemaEl
     * @return
     */
    private XmlSchemaAttribute handleAttribute(XmlSchema schema, Element attrEl, Element schemaEl) {
        return handleAttribute(schema, attrEl, schemaEl, false);
    }

    /**
     * Process attributes
     * 
     * @param schema
     * @param attrEl
     * @param schemaEl
     * @param topLevel
     * @return
     */
    private XmlSchemaAttribute handleAttribute(XmlSchema schema, Element attrEl, Element schemaEl,
                                               boolean topLevel) {
        XmlSchemaAttribute attr = new XmlSchemaAttribute(schema, topLevel);

        if (attrEl.hasAttribute("name")) {
            String name = attrEl.getAttribute("name");
            attr.setName(name);
        }

        if (attrEl.hasAttribute("type")) {
            String name = attrEl.getAttribute("type");
            attr.setSchemaTypeName(getRefQName(name, attrEl));
        }

        if (attrEl.hasAttribute("default")) {
            attr.setDefaultValue(attrEl.getAttribute("default"));
        }

        if (attrEl.hasAttribute("fixed")) {
            attr.setFixedValue(attrEl.getAttribute("fixed"));
        }

        if (attrEl.hasAttribute("form")) {
            String formValue = getEnumString(attrEl, "form");
            attr.setForm(XmlSchemaForm.schemaValueOf(formValue));
        }
        
        if (attrEl.hasAttribute("id")) {
            attr.setId(attrEl.getAttribute("id"));
        }

        if (attrEl.hasAttribute("use")) {
            String useType = getEnumString(attrEl, "use");
            attr.setUse(XmlSchemaUse.schemaValueOf(useType));
        }
        if (attrEl.hasAttribute("ref")) {
            String name = attrEl.getAttribute("ref");
            attr.setRefName(getRefQName(name, attrEl));
            attr.setName(name);
        }

        Element simpleTypeEl = XDOMUtil.getFirstChildElementNS(attrEl, XmlSchema.SCHEMA_NS, "simpleType");

        if (simpleTypeEl != null) {
            attr.setSchemaType(handleSimpleType(schema, simpleTypeEl, schemaEl));
        }

        Element annotationEl = XDOMUtil.getFirstChildElementNS(attrEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);

            attr.setAnnotation(annotation);
        }

        NamedNodeMap attrNodes = attrEl.getAttributes();
        Vector<Attr> attrs = new Vector<Attr>();
        NodeNamespaceContext ctx = null;
        for (int i = 0; i < attrNodes.getLength(); i++) {
            Attr att = (Attr)attrNodes.item(i);
            String attName = att.getName();
            if (!RESERVED_ATTRIBUTES.contains(attName)) {

                attrs.add(att);
                String value = att.getValue();

                if (value.indexOf(":") > -1) {
                    // there is a possibility of some namespace mapping
                    String prefix = value.substring(0, value.indexOf(":"));
                    if (ctx == null) {
                        ctx = NodeNamespaceContext.getNamespaceContext(attrEl);
                    }
                    String namespace = ctx.getNamespaceURI(prefix);
                    if (!Constants.NULL_NS_URI.equals(namespace)) {
                        Attr nsAttr = attrEl.getOwnerDocument().createAttribute("xmlns:" + prefix);
                        nsAttr.setValue(namespace);
                        attrs.add(nsAttr);
                    }
                }
            }
        }

        if (attrs.size() > 0) {
            attr.setUnhandledAttributes(attrs.toArray(new Attr[attrs.size()]));
        }

        // process extra attributes and elements
        processExtensibilityComponents(attr, attrEl);
        return attr;
    }

    /*
     * handle_simple_content_restriction if( restriction has base attribute ) set the baseType else if(
     * restriction has an inline simpleType ) handleSimpleType add facets if any to the restriction
     */

    /*
     * handle_simple_content_extension extension should have a base name and cannot have any inline defn for(
     * each childNode ) if( attribute) handleAttribute else if( attributeGroup) handleAttributeGroup else if(
     * anyAttribute) handleAnyAttribute
     */

    private XmlSchemaAttributeGroup handleAttributeGroup(XmlSchema schema, 
                                                         Element groupEl, 
                                                         Element schemaEl) {
        XmlSchemaAttributeGroup attrGroup = new XmlSchemaAttributeGroup();

        if (groupEl.hasAttribute("name")) {
            attrGroup.name = new QName(schema.getTargetNamespace(), groupEl.getAttribute("name"));
        }
        if (groupEl.hasAttribute("id")) {
            attrGroup.setId(groupEl.getAttribute("id"));
        }

        for (Element el = XDOMUtil.getFirstChildElementNS(groupEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr = handleAttribute(schema, el, schemaEl);
                attrGroup.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroupRef = handleAttributeGroupRef(el);
                attrGroup.attributes.add(attrGroupRef);
            } else if (el.getLocalName().equals("anyAttribute")) {
                attrGroup.anyAttribute = handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                attrGroup.setAnnotation(ann);
            }
        }
        return attrGroup;
    }

    private XmlSchemaAttributeGroupRef handleAttributeGroupRef(Element attrGroupEl) {

        XmlSchemaAttributeGroupRef attrGroup = new XmlSchemaAttributeGroupRef();

        if (attrGroupEl.hasAttribute("ref")) {
            String ref = attrGroupEl.getAttribute("ref");
            attrGroup.refName = getRefQName(ref, attrGroupEl);
        }

        if (attrGroupEl.hasAttribute("id")) {
            attrGroup.setId(attrGroupEl.getAttribute("id"));
        }

        Element annotationEl = XDOMUtil
            .getFirstChildElementNS(attrGroupEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);
            attrGroup.setAnnotation(annotation);
        }
        return attrGroup;
    }

    private XmlSchemaChoice handleChoice(XmlSchema schema, Element choiceEl, Element schemaEl) {
        XmlSchemaChoice choice = new XmlSchemaChoice();

        if (choiceEl.hasAttribute("id")) {
            choice.setId(choiceEl.getAttribute("id"));
        }

        choice.setMinOccurs(getMinOccurs(choiceEl));
        choice.setMaxOccurs(getMaxOccurs(choiceEl));

        for (Element el = XDOMUtil.getFirstChildElementNS(choiceEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("sequence")) {
                XmlSchemaSequence seq = handleSequence(schema, el, schemaEl);
                choice.items.add(seq);
            } else if (el.getLocalName().equals("element")) {
                XmlSchemaElement element = handleElement(schema, el, schemaEl, false);
                choice.items.add(element);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroupRef group = handleGroupRef(schema, el, schemaEl);
                choice.items.add(group);
            } else if (el.getLocalName().equals("choice")) {
                XmlSchemaChoice choiceItem = handleChoice(schema, el, schemaEl);
                choice.items.add(choiceItem);
            } else if (el.getLocalName().equals("any")) {
                XmlSchemaAny any = handleAny(schema, el, schemaEl);
                choice.items.add(any);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation annotation = handleAnnotation(el);
                choice.setAnnotation(annotation);
            }
        }
        return choice;
    }

    private XmlSchemaComplexContent handleComplexContent(XmlSchema schema, 
                                                         Element complexEl, Element schemaEl) {

        XmlSchemaComplexContent complexContent = new XmlSchemaComplexContent();

        for (Element el = XDOMUtil.getFirstChildElementNS(complexEl, XmlSchema.SCHEMA_NS); 
             el != null;
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("restriction")) {
                complexContent.content = handleComplexContentRestriction(schema, el, schemaEl);
            } else if (el.getLocalName().equals("extension")) {
                complexContent.content = handleComplexContentExtension(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                complexContent.setAnnotation(handleAnnotation(el));
            }
        }

        if (complexEl.hasAttribute("mixed")) {
            String mixed = complexEl.getAttribute("mixed");
            if (mixed.equalsIgnoreCase("true")) {
                complexContent.setMixed(true);
            } else {
                complexContent.setMixed(false);
            }
        }

        return complexContent;
    }

    private XmlSchemaComplexContentExtension handleComplexContentExtension(XmlSchema schema, Element extEl,
                                                                           Element schemaEl) {

        XmlSchemaComplexContentExtension ext = new XmlSchemaComplexContentExtension();

        if (extEl.hasAttribute("base")) {
            String name = extEl.getAttribute("base");
            ext.baseTypeName = getRefQName(name, extEl);
        }

        for (Element el = XDOMUtil.getFirstChildElementNS(extEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("sequence")) {
                ext.particle = handleSequence(schema, el, schemaEl);
            } else if (el.getLocalName().equals("choice")) {
                ext.particle = handleChoice(schema, el, schemaEl);
            } else if (el.getLocalName().equals("all")) {
                ext.particle = handleAll(schema, el, schemaEl);
            } else if (el.getLocalName().equals("attribute")) {
                ext.attributes.add(handleAttribute(schema, el, schemaEl));
            } else if (el.getLocalName().equals("attributeGroup")) {
                ext.attributes.add(handleAttributeGroupRef(el));
            } else if (el.getLocalName().equals("group")) {
                ext.particle = handleGroupRef(schema, el, schemaEl);
            } else if (el.getLocalName().equals("anyAttribute")) {
                ext.anyAttribute = handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                ext.setAnnotation(handleAnnotation(el));
            }
        }
        return ext;
    }

    private XmlSchemaComplexContentRestriction handleComplexContentRestriction(XmlSchema schema,
                                                                               Element restrictionEl,
                                                                               Element schemaEl) {

        XmlSchemaComplexContentRestriction restriction = new XmlSchemaComplexContentRestriction();

        if (restrictionEl.hasAttribute("base")) {
            String name = restrictionEl.getAttribute("base");
            restriction.baseTypeName = getRefQName(name, restrictionEl);
        }
        for (Element el = XDOMUtil.getFirstChildElementNS(restrictionEl, XmlSchema.SCHEMA_NS);     
            el != null;
            el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("sequence")) {
                restriction.particle = handleSequence(schema, el, schemaEl);
            } else if (el.getLocalName().equals("choice")) {
                restriction.particle = handleChoice(schema, el, schemaEl);
            } else if (el.getLocalName().equals("all")) {
                restriction.particle = handleAll(schema, el, schemaEl);
            } else if (el.getLocalName().equals("attribute")) {
                restriction.attributes.add(handleAttribute(schema, el, schemaEl));
            } else if (el.getLocalName().equals("attributeGroup")) {
                restriction.attributes.add(handleAttributeGroupRef(el));
            } else if (el.getLocalName().equals("group")) {
                restriction.particle = handleGroupRef(schema, el, schemaEl);
            } else if (el.getLocalName().equals("anyAttribute")) {
                restriction.anyAttribute = handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                restriction.setAnnotation(handleAnnotation(el));
            }
        }
        return restriction;
    }

    private XmlSchemaIdentityConstraint 
    handleConstraint(Element constraintEl, 
                     Class<? extends XmlSchemaIdentityConstraint> typeClass) {

        try {
            XmlSchemaIdentityConstraint constraint = typeClass.newInstance(); 

            if (constraintEl.hasAttribute("name")) {
                constraint.name = constraintEl.getAttribute("name");
            }

            if (constraintEl.hasAttribute("refer")) {
                String name = constraintEl.getAttribute("refer");
                ((XmlSchemaKeyref)constraint).refer = getRefQName(name, constraintEl);
            }
            for (Element el = XDOMUtil.getFirstChildElementNS(constraintEl, XmlSchema.SCHEMA_NS); 
                 el != null; 
                 el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

                // String elPrefix = el.getPrefix() == null ? ""
                // : el.getPrefix();
                // if(elPrefix.equals(schema.schema_ns_prefix)) {
                if (el.getLocalName().equals("selector")) {
                    XmlSchemaXPath selectorXPath = new XmlSchemaXPath();
                    selectorXPath.xpath = el.getAttribute("xpath");

                    Element annotationEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS,
                                                                           "annotation");
                    if (annotationEl != null) {
                        XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);

                        selectorXPath.setAnnotation(annotation);
                    }
                    constraint.selector = selectorXPath;
                } else if (el.getLocalName().equals("field")) {
                    XmlSchemaXPath fieldXPath = new XmlSchemaXPath();
                    fieldXPath.xpath = el.getAttribute("xpath");
                    constraint.fields.add(fieldXPath);

                    Element annotationEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS,
                                                                           "annotation");

                    if (annotationEl != null) {
                        XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);

                        fieldXPath.setAnnotation(annotation);
                    }
                } else if (el.getLocalName().equals("annotation")) {
                    XmlSchemaAnnotation constraintAnnotation = handleAnnotation(el);
                    constraint.setAnnotation(constraintAnnotation);
                }
            }
            return constraint;
        } catch (InstantiationException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new XmlSchemaException(e.getMessage());
        }
    }

    private XmlSchemaGroup handleGroup(XmlSchema schema, Element groupEl, Element schemaEl) {

        XmlSchemaGroup group = new XmlSchemaGroup();
        group.name = new QName(schema.getTargetNamespace(), groupEl.getAttribute("name"));

        for (Element el = XDOMUtil.getFirstChildElementNS(groupEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("all")) {
                group.particle = handleAll(schema, el, schemaEl);
            } else if (el.getLocalName().equals("sequence")) {
                group.particle = handleSequence(schema, el, schemaEl);
            } else if (el.getLocalName().equals("choice")) {
                group.particle = handleChoice(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation groupAnnotation = handleAnnotation(el);
                group.setAnnotation(groupAnnotation);
            }
        }
        return group;
    }

    private XmlSchemaGroupRef handleGroupRef(XmlSchema schema, Element groupEl, Element schemaEl) {

        XmlSchemaGroupRef group = new XmlSchemaGroupRef();

        group.setMaxOccurs(getMaxOccurs(groupEl));
        group.setMinOccurs(getMinOccurs(groupEl));

        Element annotationEl = XDOMUtil.getFirstChildElementNS(groupEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);

            group.setAnnotation(annotation);
        }

        if (groupEl.hasAttribute("ref")) {
            String ref = groupEl.getAttribute("ref");
            group.refName = getRefQName(ref, groupEl);
            return group;
        }
        for (Element el = XDOMUtil.getFirstChildElementNS(groupEl, XmlSchema.SCHEMA_NS); 
             el != null;
             el = XDOMUtil.getNextSiblingElement(el)) {

            if (el.getLocalName().equals("sequence")) {
                group.particle = handleSequence(schema, el, schemaEl);
            } else if (el.getLocalName().equals("all")) {
                group.particle = handleAll(schema, el, schemaEl);
            } else if (el.getLocalName().equals("choice")) {
                group.particle = handleChoice(schema, el, schemaEl);
            }
        }
        return group;
    }

    private XmlSchemaNotation handleNotation(XmlSchema schema, Element notationEl) {

        XmlSchemaNotation notation = new XmlSchemaNotation(schema);

        if (notationEl.hasAttribute("id")) {
            notation.setId(notationEl.getAttribute("id"));
        }

        if (notationEl.hasAttribute("name")) {
            notation.name = notationEl.getAttribute("name");
        }

        if (notationEl.hasAttribute("public")) {
            notation.publicNotation = notationEl.getAttribute("public");
        }

        if (notationEl.hasAttribute("system")) {
            notation.system = notationEl.getAttribute("system");
        }

        Element annotationEl = XDOMUtil.getFirstChildElementNS(notationEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);
            notation.setAnnotation(annotation);
        }

        return notation;
    }

    /**
     * Handle redefine
     * 
     * @param schema
     * @param redefineEl
     * @param schemaEl
     * @return
     */
    private XmlSchemaRedefine handleRedefine(XmlSchema schema, Element redefineEl, Element schemaEl) {

        XmlSchemaRedefine redefine = new XmlSchemaRedefine();
        redefine.schemaLocation = redefineEl.getAttribute("schemaLocation");
        final TargetNamespaceValidator validator = newIncludeValidator(schema);

        if (schema.getSourceURI() != null) {
            redefine.schema = resolveXmlSchema(schema.logicalTargetNamespace, redefine.schemaLocation, schema
                .getSourceURI(), validator);
        } else {
            redefine.schema = resolveXmlSchema(schema.logicalTargetNamespace, redefine.schemaLocation,
                                               validator);
        }

        /*
         * FIXME - This seems not right. Since the redefine should take into account the attributes of the
         * original element we cannot just build the type defined in the redefine section - what we need to do
         * is to get the original type object and modify it. However one may argue (quite reasonably) that the
         * purpose of this object model is to provide just the representation and not the validation (as it
         * has been always the case)
         */

        for (Element el = XDOMUtil.getFirstChildElementNS(redefineEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("simpleType")) {
                XmlSchemaType type = handleSimpleType(schema, el, schemaEl);

                redefine.schemaTypes.collection.put(type.getQName(), type);
                redefine.items.add(type);
            } else if (el.getLocalName().equals("complexType")) {

                XmlSchemaType type = handleComplexType(schema, el, schemaEl);

                redefine.schemaTypes.collection.put(type.getQName(), type);
                redefine.items.add(type);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroup group = handleGroup(schema, el, schemaEl);
                redefine.groups.collection.put(group.name, group);
                redefine.items.add(group);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroup group = handleAttributeGroup(schema, el, schemaEl);

                redefine.attributeGroups.collection.put(group.name, group);
                redefine.items.add(group);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation annotation = handleAnnotation(el);
                redefine.setAnnotation(annotation);
            }
            // }
        }
        return redefine;
    }

    private XmlSchemaSequence handleSequence(XmlSchema schema, Element sequenceEl, Element schemaEl) {

        XmlSchemaSequence sequence = new XmlSchemaSequence();

        // handle min and max occurences
        sequence.setMinOccurs(getMinOccurs(sequenceEl));
        sequence.setMaxOccurs(getMaxOccurs(sequenceEl));

        for (Element el = XDOMUtil.getFirstChildElementNS(sequenceEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("sequence")) {
                XmlSchemaSequence seq = handleSequence(schema, el, schemaEl);
                sequence.items.add(seq);
            } else if (el.getLocalName().equals("element")) {
                XmlSchemaElement element = handleElement(schema, el, schemaEl, false);
                sequence.items.add(element);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroupRef group = handleGroupRef(schema, el, schemaEl);
                sequence.items.add(group);
            } else if (el.getLocalName().equals("choice")) {
                XmlSchemaChoice choice = handleChoice(schema, el, schemaEl);
                sequence.items.add(choice);
            } else if (el.getLocalName().equals("any")) {
                XmlSchemaAny any = handleAny(schema, el, schemaEl);
                sequence.items.add(any);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation annotation = handleAnnotation(el);
                sequence.setAnnotation(annotation);
            }
        }
        return sequence;
    }

    private XmlSchemaSimpleContent handleSimpleContent(XmlSchema schema, Element simpleEl, Element schemaEl) {

        XmlSchemaSimpleContent simpleContent = new XmlSchemaSimpleContent();

        for (Element el = XDOMUtil.getFirstChildElementNS(simpleEl, XmlSchema.SCHEMA_NS); 
             el != null; el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("restriction")) {
                simpleContent.content = handleSimpleContentRestriction(schema, el, schemaEl);
            } else if (el.getLocalName().equals("extension")) {
                simpleContent.content = handleSimpleContentExtension(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                simpleContent.setAnnotation(handleAnnotation(el));
            }
        }
        return simpleContent;
    }

    private XmlSchemaSimpleContentExtension handleSimpleContentExtension(XmlSchema schema, Element extEl,
                                                                         Element schemaEl) {

        XmlSchemaSimpleContentExtension ext = new XmlSchemaSimpleContentExtension();

        if (extEl.hasAttribute("base")) {
            String name = extEl.getAttribute("base");
            ext.baseTypeName = getRefQName(name, extEl);
        }

        for (Element el = XDOMUtil.getFirstChildElementNS(extEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr = handleAttribute(schema, el, schemaEl);
                ext.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroup = handleAttributeGroupRef(el);
                ext.attributes.add(attrGroup);
            } else if (el.getLocalName().equals("anyAttribute")) {
                ext.anyAttribute = handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                ext.setAnnotation(ann);
            }
        }
        return ext;
    }

    private XmlSchemaSimpleContentRestriction handleSimpleContentRestriction(XmlSchema schema,
                                                                             Element restrictionEl,
                                                                             Element schemaEl) {

        XmlSchemaSimpleContentRestriction restriction = new XmlSchemaSimpleContentRestriction();

        if (restrictionEl.hasAttribute("base")) {
            String name = restrictionEl.getAttribute("base");
            restriction.baseTypeName = getRefQName(name, restrictionEl);
        }

        if (restrictionEl.hasAttribute("id")) {
            restriction.setId(restrictionEl.getAttribute("id"));
        }

        // check back simpleContent tag children to add attributes and simpleType if any occur
        for (Element el = XDOMUtil.getFirstChildElementNS(restrictionEl, XmlSchema.SCHEMA_NS); 
             el != null; 
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr = handleAttribute(schema, el, schemaEl);
                restriction.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroup = handleAttributeGroupRef(el);
                restriction.attributes.add(attrGroup);
            } else if (el.getLocalName().equals("simpleType")) {
                restriction.baseType = handleSimpleType(schema, el, schemaEl);
            } else if (el.getLocalName().equals("anyAttribute")) {
                restriction.anyAttribute = handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                restriction.setAnnotation(handleAnnotation(el));
            } else {
                XmlSchemaFacet facet = XmlSchemaFacet.construct(el);
                if (XDOMUtil.anyElementsWithNameNS(el, XmlSchema.SCHEMA_NS, "annotation")) {
                    XmlSchemaAnnotation facetAnnotation = handleAnnotation(el);
                    facet.setAnnotation(facetAnnotation);
                }
                restriction.facets.add(facet);
            }
        }
        return restriction;
    }

    private TargetNamespaceValidator newIncludeValidator(final XmlSchema schema) {
        return new TargetNamespaceValidator() {
            public void validate(XmlSchema pSchema) {
                if (isEmpty(pSchema.syntacticalTargetNamespace)) {
                    pSchema.logicalTargetNamespace = schema.logicalTargetNamespace;
                } else {
                    if (!pSchema.syntacticalTargetNamespace.equals(schema.logicalTargetNamespace)) {
                        String msg = "An included schema was announced to have the default target namespace";
                        if (!isEmpty(schema.logicalTargetNamespace)) {
                            msg += " or the target namespace " + schema.logicalTargetNamespace;
                        }
                        throw new XmlSchemaException(msg + ", but has the target namespace "
                                                     + pSchema.logicalTargetNamespace);
                    }
                }
            }

            private boolean isEmpty(String pValue) {
                return pValue == null || Constants.NULL_NS_URI.equals(pValue);
            }
        };
    }

    /**
     * A generic method to process the extra attributes and the the extra elements present within the schema.
     * What are considered extensions are child elements with non schema namespace and child attributes with
     * any namespace
     * 
     * @param schemaObject
     * @param parentElement
     */
    private void processExtensibilityComponents(XmlSchemaObject schemaObject, Element parentElement) {

        if (extReg != null) {
            // process attributes
            NamedNodeMap attributes = parentElement.getAttributes();
            for (int i = 0; i < attributes.getLength(); i++) {
                Attr attribute = (Attr)attributes.item(i);

                String namespaceURI = attribute.getNamespaceURI();
                String name = attribute.getLocalName();

                if (namespaceURI != null && !"".equals(namespaceURI) && // ignore unqualified attributes
                    !namespaceURI.startsWith(Constants.XMLNS_ATTRIBUTE_NS_URI) && // ignore namespaces
                    !Constants.URI_2001_SCHEMA_XSD.equals(namespaceURI)) {
                // does not belong to the schema namespace by any chance!
                    QName qName = new QName(namespaceURI, name);
                    extReg.deserializeExtension(schemaObject, qName, attribute);
                }
            }

            // process elements
            Node child = parentElement.getFirstChild();
            while (child != null) {
                if (child.getNodeType() == Node.ELEMENT_NODE) {
                    Element extElement = (Element)child;
                    String namespaceURI = extElement.getNamespaceURI();
                    String name = extElement.getLocalName();

                    if (namespaceURI != null && !Constants.URI_2001_SCHEMA_XSD.equals(namespaceURI)) {
                    // does not belong to the schema namespace
                        QName qName = new QName(namespaceURI, name);
                        extReg.deserializeExtension(schemaObject, qName, extElement);
                    }
                }
                child = child.getNextSibling();
            }
        }

    }

}
