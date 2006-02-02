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
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;
import org.apache.ws.commons.schema.utils.XDOMUtil;
import org.apache.ws.commons.schema.utils.Tokenizer;
import org.apache.ws.commons.schema.constants.Constants;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.namespace.QName;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Map;
import java.util.HashMap;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import java.io.File;

public class SchemaBuilder {
    Document doc;
    XmlSchema schema;
    XmlSchemaCollection collection;

    DocumentBuilderFactory docFac;

    SchemaBuilder(XmlSchemaCollection collection) {
        this.collection = collection;
        schema = new XmlSchema(collection);
    }



    XmlSchema build(Document doc, ValidationEventHandler veh) {
        Element schemaEl = doc.getDocumentElement();
        return handleXmlSchemaElement(schemaEl);
    }

    XmlSchema handleXmlSchemaElement(Element schemaEl) {
        // get all the attributes along with the namespace declns

        setNamespaceAttributes(schema, schemaEl);
        schema.setElementFormDefault(this.getFormDefault(schemaEl,
                "elementFormDefault"));
        schema.setAttributeFormDefault(this.getFormDefault(schemaEl,
                "attributeFormDefault"));
        schema.setBlockDefault(this.getDerivation(schemaEl, "blockDefault"));
        schema.setFinalDefault(this.getDerivation(schemaEl, "finalDefault"));
        schema.setSourceURI(schemaEl.getOwnerDocument().getBaseURI());

        /***********
         * for ( each childElement)
         *		if( simpleTypeElement)
         *			handleSimpleType
         *		else if( complexType)
         *			handleComplexType
         *		else if( element)
         *			handleElement
         *		else if( include)
         *			handleInclude
         *		else if( import)
         *			handleImport
         *		else if (group)
         *			handleGroup
         *		else if (attributeGroup)
         *			handleattributeGroup
         *		else if( attribute)
         *			handleattribute
         *		else if (redefine)
         *			handleRedefine
         *		else if(notation)
         *			handleNotation
         */

        for (Element el = XDOMUtil.getFirstChildElementNS(schemaEl, XmlSchema.SCHEMA_NS); el != null;
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            // String elPrefix = el.getPrefix() == null ? "" : el.getPrefix();
            //if(elPrefix.equals(schema.schema_ns_prefix)) {
            if (el.getLocalName().equals("simpleType")) {
                XmlSchemaType type = handleSimpleType(schema, el, schemaEl);
                schema.addType(type);
                schema.items.add(type);
                collection.resolveType(type.getQName(), type);
            } else if (el.getLocalName().equals("complexType")) {
                XmlSchemaType type = handleComplexType(schema, el, schemaEl);
                schema.addType(type);
                schema.items.add(type);
                collection.resolveType(type.getQName(), type);
            } else if (el.getLocalName().equals("element")) {
                XmlSchemaElement element = handleElement(schema, el, schemaEl, true);
                if (element.qualifiedName != null)
                    schema.elements.collection.put(element.qualifiedName, element);
                else if (element.refName != null)
                    schema.elements.collection.put(element.refName, element);
                schema.items.add(element);
            } else if (el.getLocalName().equals("include")) {
                XmlSchemaInclude include = handleInclude(schema,
                        el, schemaEl);
                schema.includes.add(include);
                schema.items.add(include);
            } else if (el.getLocalName().equals("import")) {
                XmlSchemaImport schemaImport = handleImport(schema,
                        el, schemaEl);
                schema.includes.add(schemaImport);
                schema.items.add(schemaImport);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroup group = handleGroup(schema, el, schemaEl);
                schema.groups.collection.put(group.name, group);
                schema.items.add(group);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroup group = handleAttributeGroup(schema,
                        el, schemaEl);
                schema.attributeGroups.collection.put(group.name, group);
                schema.items.add(group);
            } else if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr = handleAttribute(schema,
                        el, schemaEl);
                schema.attributes.collection.put(attr.qualifiedName, attr);
                schema.items.add(attr);
            } else if (el.getLocalName().equals("redefine")) {
                XmlSchemaRedefine redefine = handleRedefine(schema,
                        el, schemaEl);
                schema.includes.add(redefine);
            } else if (el.getLocalName().equals("notation")) {
                //TODO: implement Notation
            } else if (el.getLocalName().equals("annotation")) {
                // Vidyanand : added this part
                XmlSchemaAnnotation annotation = handleAnnotation(schema, el, schemaEl);
                schema.setAnnotation(annotation);
            }
            //}
        }
        return schema;
    }

    private XmlSchemaAnnotation handleAnnotation(XmlSchema schema,
                                                 Element annotEl,
                                                 Element schemaEl) {
        XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
        XmlSchemaObjectCollection collection = annotation.getItems();
        for (Element el = XDOMUtil.getFirstChildElement(annotEl);
             el != null; el = XDOMUtil.getFirstChildElement(el)) {

            if (el.getLocalName().equals("documentation")) {
                XmlSchemaDocumentation doc = new XmlSchemaDocumentation();
                doc.setMarkup(el.getChildNodes());
                collection.add(doc);
            }
        }
        return annotation;
    }


    private XmlSchemaRedefine handleRedefine(XmlSchema schema,
                                             Element redefineEl, Element schemaEl) {

        XmlSchemaRedefine redefine = new XmlSchemaRedefine();
        redefine.schemaLocation =
                redefineEl.getAttribute("schemaLocation");

        redefine.schema =
                getXmlSchemaFromLocation(redefine.schemaLocation);

        for (Element el = XDOMUtil.getFirstChildElementNS(redefineEl,
                XmlSchema.SCHEMA_NS)
                ; el != null;
                  el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            //                    String elPrefix = el.getPrefix() == null ? "" : el.getPrefix();
            //                    if(elPrefix.equals(schema.schema_ns_prefix)) {
            if (el.getLocalName().equals("simpleType")) {
                XmlSchemaType type =
                        handleSimpleType(schema, el, schemaEl);

                redefine.schemaTypes.collection.put(type.getQName(),
                        type);
                redefine.items.add(type);
            } else if (el.getLocalName().equals("complexType")) {

                XmlSchemaType type = handleComplexType(schema, el,
                        schemaEl);

                redefine.schemaTypes.collection.put(type.getQName(),
                        type);
                redefine.items.add(type);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroup group = handleGroup(schema, el,
                        schemaEl);
                redefine.groups.collection.put(group.name, group);
                redefine.items.add(group);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroup group =
                        handleAttributeGroup(schema, el, schemaEl);

                redefine.attributeGroups.collection.put(group.name, group);
                redefine.items.add(group);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation annotation = handleAnnotation(el);
                redefine.setAnnotation(annotation);
            }
            //                  }
        }
        return redefine;
    }

    void setNamespaceAttributes(XmlSchema schema, Element schemaEl) {
        // If this schema is embedded in a WSDL, we need to get the 
        // namespaces from the all the parent nodes first.
        Node parent = schemaEl.getParentNode();
        if (parent instanceof Element) setNamespaceAttributes(schema, (Element) parent);

        NamedNodeMap map = schemaEl.getAttributes();
        for (int i = 0; i < map.getLength(); i++) {
            if (map.item(i).getNodeName().startsWith("xmlns:")) {
                schema.namespaces.put(map.item(i).getLocalName(),
                        map.item(i).getNodeValue());

                if (map.item(i).getNodeValue().equals(XmlSchema.SCHEMA_NS))
                    schema.schema_ns_prefix = map.item(i).getLocalName();
            } else if (map.item(i).getNodeName().startsWith("xmlns")) {
                schema.namespaces.put("", map.item(i).getNodeValue());
            }
        }
        if (schemaEl.getAttributeNode("targetNamespace") != null) {
            String contain = schemaEl.getAttribute("targetNamespace");

            if (!schema.namespaces.containsValue(contain))
                putNamespace("", contain);

            if (!contain.equals(""))
                schema.targetNamespace = contain;
        } else {
            putNamespace("", schema.targetNamespace);
        }
        // only populate it if it isn't already in there
        if(!collection.namespaces.containsKey(schema.targetNamespace)){
            collection.namespaces.put(schema.targetNamespace, schema);
        }
    }

    private void putNamespace(String prefix, String namespace) {
        if (!schema.namespaces.containsKey(prefix)) {
            schema.namespaces.put(prefix, namespace);
        } else {
            String genPrefix = "gen" +
                    new java.util.Random().nextInt(999);
            schema.namespaces.put(prefix, namespace);
        }
    }

    XmlSchemaSimpleType handleSimpleType(XmlSchema schema,
                                         Element simpleEl, Element schemaEl) {

        XmlSchemaSimpleType simpleType = new XmlSchemaSimpleType(schema);
        if (simpleEl.hasAttribute("name")) {
            simpleType.name = simpleEl.getAttribute("name");
        }

        if (simpleEl.hasAttribute("final")) {
            String finalstr = simpleEl.getAttribute("final");

            if (finalstr.equalsIgnoreCase("all") |
                    finalstr.equalsIgnoreCase("#all"))

                simpleType.setFinal(new XmlSchemaDerivationMethod("All"));
            else
                simpleType.setFinal(new XmlSchemaDerivationMethod(finalstr));
        }

        Element simpleTypeAnnotationEl =
                XDOMUtil.getFirstChildElementNS(simpleEl,
                        XmlSchema.SCHEMA_NS, "annotation");

        if (simpleTypeAnnotationEl != null) {
            XmlSchemaAnnotation simpleTypeAnnotation =
                    handleAnnotation(simpleTypeAnnotationEl);

            simpleType.setAnnotation(simpleTypeAnnotation);
        }

        Element unionEl, listEl, restrictionEl;

        if ((restrictionEl =
                XDOMUtil.getFirstChildElementNS(simpleEl, XmlSchema.SCHEMA_NS, "restriction")) != null) {

            XmlSchemaSimpleTypeRestriction restriction =
                    new XmlSchemaSimpleTypeRestriction();

            Element restAnnotationEl =
                    XDOMUtil.getFirstChildElementNS(restrictionEl,
                            XmlSchema.SCHEMA_NS, "annotation");

            if (restAnnotationEl != null) {
                XmlSchemaAnnotation restAnnotation =
                        handleAnnotation(restAnnotationEl);
                restriction.setAnnotation(restAnnotation);
            }
            /** if (restriction has a base attribute )
             *		set the baseTypeName and look up the base type
             *	else if( restricion has a SimpleType Element as child)
             *		get that element and do a handleSimpleType;
             *	get the children of restriction other than annotation
             * and simpleTypes and construct facets from it;
             *
             *	set the restriction has the content of the simpleType
             *
             **/

            Element inlineSimpleType =
                    XDOMUtil.getFirstChildElementNS(restrictionEl,
                            XmlSchema.SCHEMA_NS, "simpleType");

            if (restrictionEl.hasAttribute("base")) {
                String name = restrictionEl.getAttribute("base");
                String[] temp = Tokenizer.tokenize(name, ":");
                String namespace = "";

                if (temp.length != 1) {
                    namespace = temp[0];
                }

                //let it crash because its mean being refered
                //to unregistered namespace
                namespace = schema.namespaces.get(namespace).toString();
                name = Tokenizer.lastToken(name, ":")[1];
                restriction.baseTypeName = new QName(namespace, name);
                //simpleType.name = name;
            } else if (inlineSimpleType != null) {
                XmlSchemaSimpleType baseType =
                        handleSimpleType(schema, inlineSimpleType, schemaEl);

                restriction.baseType = baseType;
            }
            for (Element el = XDOMUtil.getFirstChildElementNS(restrictionEl, XmlSchema.SCHEMA_NS)
                    ; el != null;
                      el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

                if (!el.getLocalName().equals("annotation")
                        &&
                        !el.getLocalName().equals("simpleType")) {

                    XmlSchemaFacet facet = XmlSchemaFacet.construct(el);
                    Element annotation =
                            XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS,
                                    "annotation");

                    if (annotation != null) {
                        XmlSchemaAnnotation facetAnnotation =
                                handleAnnotation(annotation);
                        facet.setAnnotation(facetAnnotation);
                    }
                    restriction.facets.add(facet);
                }

            }
            simpleType.content = restriction;

        } else if ((listEl = XDOMUtil.getFirstChildElementNS(simpleEl,
                XmlSchema.SCHEMA_NS, "list")) != null) {

            XmlSchemaSimpleTypeList list = new XmlSchemaSimpleTypeList();

            /******
             * if( list has an itemType attribute )
             *		set the baseTypeName and look up the base type
             * else if( list has a SimpleTypeElement as child)
             *		get that element and do a handleSimpleType
             *
             * set the list has the content of the simpleType
             */
            Element inlineListType, listAnnotationEl;
            if (listEl.hasAttribute("itemType")) {
                String name = listEl.getAttribute("itemType");

                String[] namespaceFromEl = Tokenizer.tokenize(name, ":");
                String namespace;

                if (namespaceFromEl.length > 1) {
                    Object result =
                            schema.namespaces.get(namespaceFromEl[0]);
                    if (result == null)
                        throw new XmlSchemaException("No namespace "
                                + "found in given itemType");

                    namespace = result.toString();
                } else
                    namespace = schema.targetNamespace;

                //Object nsFromEl = schema.namespaces.get(namespaceFromEl[0]);
                //namespace = (nsFromEl==null)?  "": nsFromEl.toString();
                name = Tokenizer.lastToken(name, ":")[1];
                list.itemTypeName = new QName(namespace, name);

            } else if ((inlineListType =
                    XDOMUtil.getFirstChildElementNS(listEl, XmlSchema.SCHEMA_NS,
                            "simpleType")) != null) {

                XmlSchemaSimpleType baseType =
                        handleSimpleType(schema, inlineListType, schemaEl);
                list.itemType = baseType;
            }

            if ((listAnnotationEl =
                    XDOMUtil.getFirstChildElementNS(listEl, XmlSchema.SCHEMA_NS,
                            "annotation")) != null) {

                XmlSchemaAnnotation listAnnotation =
                        handleAnnotation(listAnnotationEl);

                list.setAnnotation(listAnnotation);
            }
            simpleType.content = list;

        } else if ((unionEl =
                XDOMUtil.getFirstChildElementNS(simpleEl, XmlSchema.SCHEMA_NS,
                        "union")) != null) {

            XmlSchemaSimpleTypeUnion union =
                    new XmlSchemaSimpleTypeUnion();

            /******
             * if( union has a memberTypes attribute )
             *		add the memberTypeSources string
             *		for (each memberType in the list )
             *			lookup(memberType)
             *	for( all SimpleType child Elements)
             *		add the simpleTypeName (if any) to the memberType Sources
             *		do a handleSimpleType with the simpleTypeElement
             */
            if (unionEl.hasAttribute("memberTypes")) {
                String memberTypes = unionEl.getAttribute("memberTypes");
                union.memberTypesSource = memberTypes;
                Vector v = new Vector();
                StringTokenizer tokenizer = new StringTokenizer(memberTypes, " ");
                while (tokenizer.hasMoreTokens()) {
                    String member = tokenizer.nextToken();
                    int pos = member.indexOf(":");
                    String prefix = "";
                    String localName = "";
                    if (pos == -1) {
                        localName = member;
                    } else {
                        prefix = member.substring(0, pos);
                        localName = member.substring(pos + 1);
                    }
                    v.add(new QName((String) schema.namespaces.get(prefix),
                            localName));
                }
                union.memberTypesQNames = new QName[v.size()];
                v.copyInto(union.memberTypesQNames);
            }

            Element inlineUnionType =
                    XDOMUtil.getFirstChildElementNS(unionEl, XmlSchema.SCHEMA_NS,
                            "simpleType");
            while (inlineUnionType != null) {

                XmlSchemaSimpleType unionSimpleType =
                        handleSimpleType(schema, inlineUnionType,
                                schemaEl);

                union.baseTypes.add(unionSimpleType);

                union.memberTypesSource += " " + unionSimpleType.name;

                inlineUnionType =
                        XDOMUtil.getNextSiblingElementNS(inlineUnionType,
                                XmlSchema.SCHEMA_NS,
                                "simpleType");
            }

            //NodeList annotations = unionEl.getElementsByTagNameNS(
            //XmlSchema.SCHEMA_NS, "annotation");
            Element unionAnnotationEl =
                    XDOMUtil.getFirstChildElementNS(unionEl, XmlSchema.SCHEMA_NS,
                            "annotation");

            if (unionAnnotationEl != null) {
                XmlSchemaAnnotation unionAnnotation =
                        handleAnnotation(unionAnnotationEl);

                union.setAnnotation(unionAnnotation);
            }
            simpleType.content = union;
        }
        return simpleType;
    }

    XmlSchemaComplexType handleComplexType(XmlSchema schema,
                                           Element complexEl, Element schemaEl) {

        /******
         * set the complexTypeName if any
         * for( eachChildNode)
         * if ( simpleContent)
         *		if( restrcition)
         *			handle_simple_content_restriction
         *		else if( extension)
         *			handle_simple_content_extension
         *		break; // it has to be the only child
         * else if( complexContent)
         *		if( restriction)
         *			handle_complex_content_restriction
         *		else if( extension)
         *			handle_complex_content_extension
         *		break; // it has to be the only child
         * else if( group)
         *		if( group has ref)
         *			store the group name
         *		else
         *			handleGroup
         * else if( sequence )
         *		handleSequence
         * else if( all )
         *		handleAll
         * else if(choice)
         *		handleChoice
         * else if(attribute)
         *		handleAttribute
         * else if(attributeGroup)
         *		handleAttributeGroup
         *  else if(anyAttribute)
         *		handleAnyAttribute
         */

        XmlSchemaComplexType ct = new XmlSchemaComplexType(schema);

        if (complexEl.hasAttribute("name")) {
            String name = complexEl.getAttribute("name");

            //String namespace = (schema.targetNamespace==null)?
            //                  "":schema.targetNamespace;

            ct.name = name;
        }
        for (Element el = XDOMUtil.getFirstChildElementNS(complexEl,
                XmlSchema.SCHEMA_NS)
                ; el != null;
                  el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            //String elPrefix = el.getPrefix() == null ? "" :
            //el.getPrefix();
            //if(elPrefix.equals(schema.schema_ns_prefix)) {
            if (el.getLocalName().equals("sequence")) {
                XmlSchemaSequence sequence = handleSequence(schema,
                        el, schemaEl);

                ct.particle = sequence;
            } else if (el.getLocalName().equals("choice")) {
                XmlSchemaChoice choice = handleChoice(schema,
                        el, schemaEl);

                ct.particle = choice;
            } else if (el.getLocalName().equals("all")) {
                XmlSchemaAll all = handleAll(schema, el, schemaEl);
                ct.particle = all;
            } else if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr = handleAttribute(schema,
                        el, schemaEl);
                ct.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroup =
                        handleAttributeGroupRef(schema, el, schemaEl);
                ct.attributes.add(attrGroup);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroupRef group =
                        handleGroupRef(schema, el, schemaEl);
                ct.particle = (group.particle == null) ?
                        (XmlSchemaParticle) group : group.particle;
            } else if (el.getLocalName().equals("simpleContent")) {
                XmlSchemaSimpleContent simpleContent =
                        handleSimpleContent(schema, el, schemaEl);
                ct.contentModel = simpleContent;
            } else if (el.getLocalName().equals("complexContent")) {
                XmlSchemaComplexContent complexContent =
                        handleComplexContent(schema, el, schemaEl);
                ct.contentModel = complexContent;
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                ct.setAnnotation(ann);
            }
            //}
        }
        if (complexEl.hasAttribute("block")) {
            String blockStr = complexEl.getAttribute("block");
            if (blockStr.equalsIgnoreCase("all") |
                    blockStr.equalsIgnoreCase("#all")) {

                ct.setBlock(new XmlSchemaDerivationMethod("All"));
            } else
                ct.setBlock(new XmlSchemaDerivationMethod(blockStr));
            //ct.setBlock(new XmlSchemaDerivationMethod(block));
        }
        if (complexEl.hasAttribute("final")) {
            String finalstr = complexEl.getAttribute("final");
            if (finalstr.equalsIgnoreCase("all") |
                    finalstr.equalsIgnoreCase("#all")) {

                ct.setFinal(new XmlSchemaDerivationMethod("All"));
            } else
                ct.setFinal(new XmlSchemaDerivationMethod(finalstr));
        }
        if (complexEl.hasAttribute("abstract")) {
            String abs = complexEl.getAttribute("abstract");
            if (abs.equalsIgnoreCase("true"))
                ct.setAbstract(true);
            else
                ct.setAbstract(false);
        }
        if (complexEl.hasAttribute("mixed")) {
            String mixed = complexEl.getAttribute("mixed");
            if (mixed.equalsIgnoreCase("true"))
                ct.setMixed(true);
            else
                ct.setMixed(false);
        }
        return ct;
    }

    private XmlSchemaSimpleContent
            handleSimpleContent(XmlSchema schema, Element simpleEl,
                                Element schemaEl) {

        XmlSchemaSimpleContent simpleContent =
                new XmlSchemaSimpleContent();

        for (Element el = XDOMUtil.getFirstChildElementNS(simpleEl,
                XmlSchema.SCHEMA_NS)
                ; el != null;
                  el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("restriction")) {
                XmlSchemaSimpleContentRestriction restriction =
                        handleSimpleContentRestriction(schema, el, schemaEl);

                simpleContent.content = restriction;
            } else if (el.getLocalName().equals("extension")) {
                XmlSchemaSimpleContentExtension ext =
                        handleSimpleContentExtension(schema, el, schemaEl);
                simpleContent.content = ext;
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                simpleContent.setAnnotation(ann);
            }
        }
        return simpleContent;
    }

    private XmlSchemaComplexContent handleComplexContent(XmlSchema schema,
                                                         Element complexEl,
                                                         Element schemaEl) {

        XmlSchemaComplexContent complexContent =
                new XmlSchemaComplexContent();

        for (Element el =
                XDOMUtil.getFirstChildElementNS(complexEl, XmlSchema.SCHEMA_NS);
             el != null;
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("restriction")) {
                XmlSchemaComplexContentRestriction restriction =
                        handleComplexContentRestriction(schema, el,
                                schemaEl);
                complexContent.content = restriction;
            } else if (el.getLocalName().equals("extension")) {
                XmlSchemaComplexContentExtension ext =
                        handleComplexContentExtension(schema, el,
                                schemaEl);
                complexContent.content = ext;
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                complexContent.setAnnotation(ann);
            }
        }
        return complexContent;
    }

    private XmlSchemaSimpleContentRestriction
            handleSimpleContentRestriction(XmlSchema schema,
                                           Element restrictionEl, Element schemaEl) {

        XmlSchemaSimpleContentRestriction restriction =
                new XmlSchemaSimpleContentRestriction();

        if (restrictionEl.hasAttribute("base")) {
            String name = restrictionEl.getAttribute("base");
            Object result = schema.namespaces.get(Tokenizer.tokenize(name, ":")[0]);

            if (result == null)
                throw new XmlSchemaException("No namespace found in "
                        + "given base simple content type");
            name = Tokenizer.lastToken(name, ":")[1];
            restriction.baseTypeName = new QName(result.toString(), name);
        }

        if (restrictionEl.hasAttribute("id"))
            restriction.id = restrictionEl.getAttribute("id");

        // check back simpleContent tag children to add attributes and simpleType if any occur
        for (
                Element el = XDOMUtil.getFirstChildElementNS(restrictionEl,
                        XmlSchema.SCHEMA_NS)
                        ; el != null;
                          el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr =
                        handleAttribute(schema, el, schemaEl);
                restriction.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroup =
                        handleAttributeGroupRef(schema, el, schemaEl);
                restriction.attributes.add(attrGroup);
            } else if (el.getLocalName().equals("simpleType")) {
                XmlSchemaSimpleType type =
                        handleSimpleType(schema, el, schemaEl);
                restriction.baseType = type;
            } else if (el.getLocalName().equals("anyAttribute")) {
                restriction.anyAttribute =
                        handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                restriction.setAnnotation(ann);
            } else {
                XmlSchemaFacet facet = XmlSchemaFacet.construct(el);
                NodeList annotations =
                        el.getElementsByTagNameNS(XmlSchema.SCHEMA_NS, "annotation");

                if (annotations.getLength() > 0) {
                    XmlSchemaAnnotation facetAnnotation =
                            handleAnnotation(el);
                    facet.setAnnotation(facetAnnotation);
                }
                restriction.facets.add(facet);
            }
        }
        return restriction;
    }

    private XmlSchemaSimpleContentExtension
            handleSimpleContentExtension(XmlSchema schema, Element extEl,
                                         Element schemaEl) {

        XmlSchemaSimpleContentExtension ext =
                new XmlSchemaSimpleContentExtension();

        if (extEl.hasAttribute("base")) {
            String name = extEl.getAttribute("base");
            if (name.indexOf(':') != -1) {
                String nsFromEl = Tokenizer.tokenize(name, ":")[0];
                Object result = schema.namespaces.get(nsFromEl);

                if (result == null)
                    throw new XmlSchemaException("No namespace found in "
                            + "given base simple content type");
                name = Tokenizer.lastToken(name, ":")[1];
                ext.baseTypeName = new QName(result.toString(), name);
            } else {
                ext.baseTypeName = new QName(schema.getNamespace(""), name);
            }
        }

        for (
                Element el = XDOMUtil.getFirstChildElementNS(extEl, XmlSchema.SCHEMA_NS)
                        ; el != null;
                          el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr =
                        handleAttribute(schema, el, schemaEl);
                ext.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroup =
                        handleAttributeGroupRef(schema, el, schemaEl);
                ext.attributes.add(attrGroup);
            } else if (el.getLocalName().equals("anyAttribute")) {
                ext.anyAttribute =
                        handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                ext.setAnnotation(ann);
            }
        }
        return ext;
    }

    private XmlSchemaComplexContentRestriction
            handleComplexContentRestriction(XmlSchema schema,
                                            Element restrictionEl, Element schemaEl) {

        XmlSchemaComplexContentRestriction restriction =
                new XmlSchemaComplexContentRestriction();

        if (restrictionEl.hasAttribute("base")) {
            String name = restrictionEl.getAttribute("base");
            String prefix;
            if (name.indexOf(":") < 0)
                prefix = "";
            else
                prefix = name.substring(0, name.indexOf(":"));

            Object result = schema.namespaces.get(prefix);

            if (result == null)
                throw new XmlSchemaException("No namespace found in "
                        + "given base complex content base type");

            name = Tokenizer.lastToken(name, ":")[1];
            restriction.baseTypeName = new QName(result.toString(), name);
        }
        for (Element el = XDOMUtil.getFirstChildElementNS(restrictionEl,
                XmlSchema.SCHEMA_NS)
                ; el != null;
                  el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("sequence")) {
                XmlSchemaSequence sequence =
                        handleSequence(schema, el, schemaEl);
                restriction.particle = sequence;
            } else if (el.getLocalName().equals("choice")) {
                XmlSchemaChoice choice = handleChoice(schema, el, schemaEl);
                restriction.particle = choice;
            } else if (el.getLocalName().equals("all")) {
                XmlSchemaAll all = handleAll(schema, el, schemaEl);
                restriction.particle = all;
            } else if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr =
                        handleAttribute(schema, el, schemaEl);
                restriction.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroup =
                        handleAttributeGroupRef(schema, el, schemaEl);
                restriction.attributes.add(attrGroup);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroupRef group =
                        handleGroupRef(schema, el, schemaEl);
                restriction.particle = group;
            } else if (el.getLocalName().equals("anyAttribute")) {
                restriction.anyAttribute =
                        handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                restriction.setAnnotation(ann);
            }
        }
        return restriction;
    }

    private XmlSchemaComplexContentExtension
            handleComplexContentExtension(XmlSchema schema,
                                          Element extEl, Element schemaEl) {

        XmlSchemaComplexContentExtension ext =
                new XmlSchemaComplexContentExtension();

        if (extEl.hasAttribute("base")) {
            String name = extEl.getAttribute("base");
            String namespaceFromEl = "";
            if (name.indexOf(":") > 0)
                namespaceFromEl = Tokenizer.tokenize(name, ":")[0];
            Object result = schema.namespaces.get(namespaceFromEl);

            if (result == null)
                throw new XmlSchemaException("No namespace found in "
                        + "given base complex content base type");

            //                    String namespace = (result==null)? "" : result.toString();
            name = Tokenizer.lastToken(name, ":")[1];
            ext.baseTypeName = new QName(result.toString(), name);
        }

        for (Element el = XDOMUtil.getFirstChildElementNS(extEl, XmlSchema.SCHEMA_NS)
                ; el != null;
                  el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("sequence")) {
                XmlSchemaSequence sequence =
                        handleSequence(schema, el, schemaEl);
                ext.particle = sequence;
            } else if (el.getLocalName().equals("choice")) {
                XmlSchemaChoice choice =
                        handleChoice(schema, el, schemaEl);
                ext.particle = choice;
            } else if (el.getLocalName().equals("all")) {
                XmlSchemaAll all = handleAll(schema, el, schemaEl);
                ext.particle = all;
            } else if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr =
                        handleAttribute(schema, el, schemaEl);
                ext.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroup =
                        handleAttributeGroupRef(schema, el, schemaEl);
                ext.attributes.add(attrGroup);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroupRef group =
                        handleGroupRef(schema, el, schemaEl);
                ext.particle = group;
            } else if (el.getLocalName().equals("anyAttribute")) {
                ext.anyAttribute =
                        handleAnyAttribute(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                ext.setAnnotation(ann);
            }
        }
        return ext;
    }

    private XmlSchemaAttributeGroupRef
            handleAttributeGroupRef(XmlSchema schema, Element attrGroupEl,
                                    Element schemaEl) {

        XmlSchemaAttributeGroupRef attrGroup =
                new XmlSchemaAttributeGroupRef();

        if (attrGroupEl.hasAttribute("ref")) {
            String ref = attrGroupEl.getAttribute("ref");
            String parts[] = Tokenizer.tokenize(ref, ":");
            String prefix = "";
            if (parts.length > 1)
                prefix = parts[0];
            Object result = schema.namespaces.get(prefix);
            //String namespace = (result==null)? "":result.toString();
            if (result == null)
                throw new XmlSchemaException("No namespace found in "
                        + "given ref name");

            ref = Tokenizer.lastToken(ref, ":")[1];
            attrGroup.refName = new QName(result.toString(), ref);
        }

        if (attrGroupEl.hasAttribute("id"))
            attrGroup.id = attrGroupEl.getAttribute("id");

        Element annotationEl =
                XDOMUtil.getFirstChildElementNS(attrGroupEl,
                        XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation = handleAnnotation(annotationEl);
            attrGroup.setAnnotation(annotation);
        }
        return attrGroup;
    }

    private XmlSchemaSequence handleSequence(XmlSchema schema,
                                             Element sequenceEl,
                                             Element schemaEl) {

        XmlSchemaSequence sequence = new XmlSchemaSequence();
        for (Element el = XDOMUtil.getFirstChildElementNS(sequenceEl, XmlSchema.SCHEMA_NS)
                ; el != null;
                  el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("sequence")) {
                XmlSchemaSequence seq = handleSequence(schema, el,
                        schemaEl);
                sequence.items.add(seq);
            } else if (el.getLocalName().equals("element")) {
                XmlSchemaElement element = handleElement(schema, el,
                        schemaEl, false);
                sequence.items.add(element);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroupRef group = handleGroupRef(schema, el,
                        schemaEl);
                sequence.items.add(group);
            } else if (el.getLocalName().equals("choice")) {
                XmlSchemaChoice choice = handleChoice(schema, el,
                        schemaEl);
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

    private XmlSchemaAny handleAny(XmlSchema schema,
                                   Element anyEl,
                                   Element schemaEl) {

        XmlSchemaAny any = new XmlSchemaAny();

        if (anyEl.hasAttribute("namespace"))
            any.namespace = anyEl.getAttribute("namespace");

        if (anyEl.hasAttribute("processContents")) {
            String processContent = getEnumString(anyEl,
                    "processContents");

            any.processContent =
                    new XmlSchemaContentProcessing(processContent);
        }

        Element annotationEl = XDOMUtil.getFirstChildElementNS(anyEl,
                XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation =
                    handleAnnotation(annotationEl);
            any.setAnnotation(annotation);
        }
        any.minOccurs = getMinOccurs(anyEl);
        any.maxOccurs = getMaxOccurs(anyEl);

        return any;
    }

    private XmlSchemaChoice handleChoice(XmlSchema schema, Element choiceEl,
                                         Element schemaEl) {
        XmlSchemaChoice choice = new XmlSchemaChoice();

        if (choiceEl.hasAttribute("id"))
            choice.id = choiceEl.getAttribute("id");

        for (Element el = XDOMUtil.getFirstChildElementNS(choiceEl,
                XmlSchema.SCHEMA_NS)
                ; el != null;
                  el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("sequence")) {
                XmlSchemaSequence seq =
                        handleSequence(schema, el, schemaEl);
                choice.items.add(seq);
            } else if (el.getLocalName().equals("element")) {
                XmlSchemaElement element =
                        handleElement(schema, el, schemaEl, false);
                choice.items.add(element);
            } else if (el.getLocalName().equals("group")) {
                XmlSchemaGroupRef group =
                        handleGroupRef(schema, el, schemaEl);
                choice.items.add(group);
            } else if (el.getLocalName().equals("choice")) {
                XmlSchemaChoice choiceItem =
                        handleChoice(schema, el, schemaEl);
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

    private XmlSchemaAll handleAll(XmlSchema schema, Element allEl,
                                   Element schemaEl) {

        XmlSchemaAll all = new XmlSchemaAll();

        for (Element el = XDOMUtil.getFirstChildElementNS(allEl, XmlSchema.SCHEMA_NS);
             el != null; el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

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

    private XmlSchemaGroup handleGroup(XmlSchema schema, Element groupEl,
                                       Element schemaEl) {

        XmlSchemaGroup group = new XmlSchemaGroup();
        group.name = groupEl.getAttribute("name");

        for (Element el = XDOMUtil.getFirstChildElementNS(groupEl,
                XmlSchema.SCHEMA_NS);
             el != null;
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("all")) {
                group.particle = handleAll(schema, el, schemaEl);
            } else if (el.getLocalName().equals("sequence")) {
                group.particle = handleSequence(schema, el, schemaEl);
            } else if (el.getLocalName().equals("choice")) {
                group.particle = handleChoice(schema, el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation groupAnnotation =
                        handleAnnotation(el);
                group.setAnnotation(groupAnnotation);
            }
        }
        return group;
    }

    private XmlSchemaAttributeGroup handleAttributeGroup(XmlSchema schema,
                                                         Element groupEl,
                                                         Element schemaEl) {
        XmlSchemaAttributeGroup attrGroup =
                new XmlSchemaAttributeGroup();

        if (groupEl.hasAttribute("name"))
            attrGroup.name = groupEl.getAttribute("name");
        if (groupEl.hasAttribute("id"))
            attrGroup.id = groupEl.getAttribute("id");

        for (Element el = XDOMUtil.getFirstChildElementNS(groupEl,
                XmlSchema.SCHEMA_NS);
             el != null;
             el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

            if (el.getLocalName().equals("attribute")) {
                XmlSchemaAttribute attr =
                        handleAttribute(schema, el, schemaEl);
                attrGroup.attributes.add(attr);
            } else if (el.getLocalName().equals("attributeGroup")) {
                XmlSchemaAttributeGroupRef attrGroupRef =
                        handleAttributeGroupRef(schema, el, schemaEl);
                attrGroup.attributes.add(attrGroupRef);
            } else if (el.getLocalName().equals("anyAttribute")) {
                attrGroup.anyAttribute = handleAnyAttribute(schema,
                        el, schemaEl);
            } else if (el.getLocalName().equals("annotation")) {
                XmlSchemaAnnotation ann = handleAnnotation(el);
                attrGroup.setAnnotation(ann);
            }
        }
        return attrGroup;
    }

    private XmlSchemaAnyAttribute handleAnyAttribute(XmlSchema schema,
                                                     Element anyAttrEl,
                                                     Element schemaEl) {

        XmlSchemaAnyAttribute anyAttr = new XmlSchemaAnyAttribute();

        if (anyAttrEl.hasAttribute("namespace"))
            anyAttr.namespace = anyAttrEl.getAttribute("namespace");

        if (anyAttrEl.hasAttribute("processContents")) {

            String contentProcessing = getEnumString(anyAttrEl,
                    "processContents");

            anyAttr.processContent = new XmlSchemaContentProcessing(contentProcessing);
        }
        if (anyAttrEl.hasAttribute("id"))
            anyAttr.id = anyAttrEl.getAttribute("id");

        Element annotationEl =
                XDOMUtil.getFirstChildElementNS(anyAttrEl,
                        XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation =
                    handleAnnotation(annotationEl);

            anyAttr.setAnnotation(annotation);
        }
        return anyAttr;
    }

    private XmlSchemaGroupRef handleGroupRef(XmlSchema schema,
                                             Element groupEl, Element schemaEl) {

        XmlSchemaGroupRef group = new XmlSchemaGroupRef();

        Element annotationEl = XDOMUtil.getFirstChildElementNS(groupEl,
                XmlSchema.SCHEMA_NS,
                "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation =
                    handleAnnotation(annotationEl);

            group.setAnnotation(annotation);
        }

        if (groupEl.hasAttribute("ref")) {
            String ref = groupEl.getAttribute("ref");
            String parts[] = Tokenizer.tokenize(ref, ":");
            String prefix = "";
            if (parts.length > 1)
                prefix = parts[0];
            Object result = schema.namespaces.get(prefix);
            //String namespace = (result==null)?"":result.toString();
            if (result == null)
                throw new XmlSchemaException("No namespace found in "
                        + "given ref group");
            ref = Tokenizer.lastToken(ref, ":")[1];
            group.refName = new QName(result.toString(), ref);

            return group;
        }
        for (Element el = XDOMUtil.getFirstChildElementNS(groupEl,
                XmlSchema.SCHEMA_NS)
                ; el != null;
                  el = XDOMUtil.getNextSiblingElement(el)) {

            if (el.getLocalName().equals("sequence")) {
                XmlSchemaSequence sequence =
                        handleSequence(schema, el, schemaEl);
                group.particle = sequence;
            } else if (el.getLocalName().equals("all")) {
                XmlSchemaAll all = handleAll(schema, el, schemaEl);
                group.particle = all;
            } else if (el.getLocalName().equals("choice")) {
                XmlSchemaChoice choice = handleChoice(schema, el,
                        schemaEl);
                group.particle = choice;
            }
        }
        return group;
    }

    private XmlSchemaAttribute handleAttribute(XmlSchema schema,
                                               Element attrEl, Element schemaEl) {
        //todo: need to implement different rule of attribute such as
        //restriction between ref and name.  This can be implemented
        //in the compile function
        XmlSchemaAttribute attr = new XmlSchemaAttribute();

        if (attrEl.hasAttribute("name")) {
            String name = attrEl.getAttribute("name");
            //String namespace = (schema.targetNamespace==null)?
            //                  "" :schema.targetNamespace;

            attr.name = name;
            attr.qualifiedName = new QName(schema.targetNamespace, name);
        }

        if (attrEl.hasAttribute("type")) {
            String name = attrEl.getAttribute("type");
            String type[] = Tokenizer.tokenize(name, ":");
            String namespace;

            if (type.length > 1) {
                Object result = schema.namespaces.get(type[0]);
                if (result == null)
                    throw new XmlSchemaException("No namespace found"
                            + " in given attribute type");

                namespace = result.toString();
            } else
                namespace = schema.targetNamespace;
            name = Tokenizer.lastToken(name, ":")[1];
            attr.schemaTypeName = new QName(namespace, name);
        }

        if (attrEl.hasAttribute("default"))
            attr.defaultValue = attrEl.getAttribute("default");

        if (attrEl.hasAttribute("fixed"))
            attr.fixedValue = attrEl.getAttribute("fixed");

        if (attrEl.hasAttribute("form")) {
            String formValue = getEnumString(attrEl, "form");
            attr.form = new XmlSchemaForm(formValue);
        }
        if (attrEl.hasAttribute("id"))
            attr.id = attrEl.getAttribute("id");


        if (attrEl.hasAttribute("use")) {
            String useType = getEnumString(attrEl, "use");
            attr.use = new XmlSchemaUse(useType);
        }
        if (attrEl.hasAttribute("ref")) {
            String name = attrEl.getAttribute("ref");
            String[] namespaceFromEl = Tokenizer.tokenize(name, ":");
            String namespace;

            if (namespaceFromEl.length > 1) {
                Object result =
                        schema.namespaces.get(namespaceFromEl[0]);
                if (result == null && namespaceFromEl[0].equals(Constants.XMLNS_PREFIX)) {
                    result = Constants.XMLNS_URI;
                }
                if (result == null)
                    throw new XmlSchemaException("No namespace found in"
                            + " given ref");
                namespace = result.toString();
            } else
                namespace = schema.targetNamespace;
            name = Tokenizer.lastToken(name, ":")[1];
            attr.refName = new QName(namespace, name);
            attr.name = name;
        }

        Element simpleTypeEl =
                XDOMUtil.getFirstChildElementNS(attrEl,
                        XmlSchema.SCHEMA_NS, "simpleType");

        if (simpleTypeEl != null) {
            attr.schemaType = handleSimpleType(schema, simpleTypeEl,
                    schemaEl);
        }

        Element annotationEl =
                XDOMUtil.getFirstChildElementNS(attrEl,
                        XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation =
                    handleAnnotation(annotationEl);

            attr.setAnnotation(annotation);
        }

        NamedNodeMap attrNodes = attrEl.getAttributes();
        Vector attrs = new Vector();
        for (int i = 0; i < attrNodes.getLength(); i++) {
            Attr att = (Attr) attrNodes.item(i);
            if (!att.getName().equals("name") &&
                    !att.getName().equals("type") &&
                    !att.getName().equals("default") &&
                    !att.getName().equals("fixed") &&
                    !att.getName().equals("form") &&
                    !att.getName().equals("id") &&
                    !att.getName().equals("use") &&
                    !att.getName().equals("ref")) {


                attrs.add(att);
                String value = att.getValue();

                if (value.indexOf(":") > -1) {
                    // there is a possiblily of some namespace mapping
                    String prefix = value.substring(0, value.indexOf(":"));
                    //String value = ( String) value.substring( value.indexOf( ":" ) + 1);
                    String namespace = (String) schema.namespaces.get(prefix);
                    if (namespace != null) {
                        Attr nsAttr = attrEl.getOwnerDocument().createAttribute("xmlns:" + prefix);
                        nsAttr.setValue(namespace);
                        attrs.add(nsAttr);
                    }
                }
            }
        }

        if (attrs.size() > 0)
            attr.setUnhandledAttributes((Attr[]) attrs.toArray(new Attr[0]));
        return attr;
    }

    /********
     * handle_simple_content_restriction
     *
     * if( restriction has base attribute )
     *		set the baseType
     * else if( restriciton has an inline simpleType )
     *		handleSimpleType
     * add facets if any to the restriction
     */

    /*********
     * handle_simple_content_extension
     *
     * extension should have a base name and cannot have any inline defn
     * for( each childNode  )
     *		if( attribute)
     *			handleAttribute
     *		else if( attributeGroup)
     *			handleAttributeGroup
     *		else if( anyAttribute)
     *			handleAnyAttribute
     */

    /**
     * ********
     * handle_complex_content_restriction
     */
    XmlSchemaElement handleElement(XmlSchema schema,
                                   Element el,
                                   Element schemaEl,
                                   boolean isGlobal) {

        XmlSchemaElement element = new XmlSchemaElement();

        if (el.getAttributeNode("name") != null)
            element.name = el.getAttribute("name");

        //                String namespace = (schema.targetNamespace==null)?
        //                                      "" : schema.targetNamespace;

        boolean isQualified = schema.getElementFormDefault().getValue().equals(XmlSchemaForm.QUALIFIED);
        if (el.hasAttribute("form")) {
            String formDef = el.getAttribute("form");
            element.form = new XmlSchemaForm(formDef);
            isQualified = formDef.equals(XmlSchemaForm.QUALIFIED);
        }

        String ns = (isQualified || isGlobal) ? schema.targetNamespace :
                null;

        if(element.name != null) {
            element.qualifiedName = new QName(ns, element.name);
        }

        Element annotationEl =
                XDOMUtil.getFirstChildElementNS(el,
                        XmlSchema.SCHEMA_NS,
                        "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation annotation =
                    handleAnnotation(annotationEl);

            element.setAnnotation(annotation);
        }
        if (el.getAttributeNode("type") != null) {
            String typeName = el.getAttribute("type");
            String[] args = Tokenizer.tokenize(typeName, ":");
            String namespace = "";

            if (args.length > 1) {
                /*
                  A particular problem in the logic of namespace handling is that
                  a user can do a namespace declaration in the following fashion
                  <xs:element name="inChar" type="q1:char" xmlns:q1="http://schemas.microsoft.com/Serialization/"/>
                  Hence the current element attributes need to be inspected to see
                  whether there is a namespace declaration in the current element itself.
                */

                Map elementNameSpaceMap = new HashMap();
                populateElementNamespaces(el, elementNameSpaceMap);
                Object elementNs = elementNameSpaceMap.get(args[0]);
                String result  = elementNs!=null?elementNs.toString():schema.getNamespace(args[0]);

                if (result == null)
                    throw new XmlSchemaException(
                            "Couldn't map prefix '" + args[0] +
                                    "' to a namespace");

                namespace = result;
            } else {
                namespace = schema.targetNamespace;
            }
            typeName = Tokenizer.lastToken(typeName, ":")[1];
            QName typeQName = new QName(namespace, typeName);
            element.schemaTypeName = typeQName;

            XmlSchemaType type = collection.getTypeByQName(typeQName);
            if (type == null) {
                // Could be a forward reference...
                collection.addUnresolvedType(typeQName, element);
            }
            element.schemaType = type;
        } else if (el.getAttributeNode("ref") != null) {
            String refName = el.getAttribute("ref");

            String[] args = Tokenizer.tokenize(refName, ":");
            String namespace;
            if (args.length > 1) {
                Object result = schema.namespaces.get(args[0]);
                if (result == null)
                    throw new XmlSchemaException("No namespace found in"
                            + "given ref");

                namespace = result.toString();
            } else
                namespace = schema.targetNamespace;
            refName = Tokenizer.lastToken(refName, ":")[1];
            element.setRefName(new QName(namespace, refName));
            element.name = refName;
        }

        Element simpleTypeEl, complexTypeEl, keyEl, keyrefEl, uniqueEl;

        if ((simpleTypeEl = XDOMUtil.getFirstChildElementNS(el,
                XmlSchema.SCHEMA_NS, "simpleType")) != null) {

            XmlSchemaSimpleType simpleType =
                    handleSimpleType(schema, simpleTypeEl, schemaEl);
            element.schemaType = simpleType;
            element.schemaTypeName = simpleType.getQName();
        } else if ((complexTypeEl =
                XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS,
                        "complexType")) != null) {

            XmlSchemaComplexType complexType =
                    handleComplexType(schema, complexTypeEl, schemaEl);

            element.schemaType = complexType;
        } else if ((keyEl =
                XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS, "key")) != null) {

            XmlSchemaIdentityConstraint key =
                    handleConstraint(schema, keyEl, schemaEl, "Key");
            element.constraints.add(key);
        } else if ((keyrefEl = XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS, "keyref")) != null) {

            XmlSchemaKeyref keyRef =
                    (XmlSchemaKeyref) handleConstraint(schema, keyrefEl,
                            schemaEl, "Keyref");

            if (el.hasAttribute("refer")) {
                String name = el.getAttribute("refer");
                String qName[] =
                        Tokenizer.tokenize(name, ":");
                String namespace;

                if (qName.length > 1) {
                    Object result = schema.namespaces.get(qName[0]);
                    namespace = result.toString();
                } else
                    namespace = schema.targetNamespace;
                name = Tokenizer.lastToken(name, ":")[1];
                keyRef.refer = new QName(namespace, name);
            }

            element.constraints.add(keyRef);

        } else if ((uniqueEl =
                XDOMUtil.getFirstChildElementNS(el,
                        XmlSchema.SCHEMA_NS, "unique")) != null) {

            XmlSchemaIdentityConstraint unique =
                    handleConstraint(schema, uniqueEl, schemaEl, "Unique");

            element.constraints.add(unique);
        }

        if (el.hasAttribute("abstract"))
            element.isAbstract =
                    new Boolean(el.getAttribute("abstract")).booleanValue();

        if (el.hasAttribute("block"))
            element.block = getDerivation(el, "block");

        if (el.hasAttribute("default"))
            element.defaultValue = el.getAttribute("default");

        if (el.hasAttribute("final"))
            element.finalDerivation = getDerivation(el, "final");

        if (el.hasAttribute("fixed"))
            element.fixedValue = el.getAttribute("fixed");

        if (el.hasAttribute("id"))
            element.id = el.getAttribute("id");

        if (el.hasAttribute("nillable"))
            element.isNillable =
                    new Boolean(el.getAttribute("nillable")).booleanValue();


        element.minOccurs = getMinOccurs(el);
        element.maxOccurs = getMaxOccurs(el);
        return element;
    }

    private void populateElementNamespaces(Element el, Map elementNameSpaceMap) {
        Node node;
        NamedNodeMap attributes = el.getAttributes();
        for (int i = 0; i < attributes.getLength(); i++) {
            node = attributes.item(i);
            if (node.getNodeName().startsWith("xmlns:")){
                elementNameSpaceMap.put(node.getLocalName(),
                        node.getNodeValue());
            }
        }
    }

    private XmlSchemaIdentityConstraint handleConstraint(XmlSchema schema,
                                                         Element constraintEl, Element schemaEl, String type) {

        try {
            XmlSchemaIdentityConstraint constraint =
                    (XmlSchemaIdentityConstraint) Class.forName("org.apache.ws.commons.schema.XmlSchema" + type).newInstance();

            if (constraintEl.hasAttribute("name"))
                constraint.name = constraintEl.getAttribute("name");

            if (constraintEl.hasAttribute("refer")) {
                String name = constraintEl.getAttribute("refer");
                String[] namespaceFromEl =
                        Tokenizer.tokenize(name, ":");
                String namespace;

                if (namespaceFromEl.length > 1) {
                    Object result =
                            schema.namespaces.get(namespaceFromEl[0]);
                    if (result == null)
                        throw new XmlSchemaException("No namespace found in "
                                + "given base simple content type");

                    namespace = result.toString();
                } else
                    namespace = schema.targetNamespace;

                name = Tokenizer.lastToken(name, ":")[1];
                constraint.name = name; // need to confirm as it is not name but refer
                ((XmlSchemaKeyref) constraint).refer =
                        new QName(namespace, name);

            }
            for (Element el = XDOMUtil.getFirstChildElementNS(constraintEl,
                    XmlSchema.SCHEMA_NS);
                 el != null;
                 el = XDOMUtil.getNextSiblingElementNS(el, XmlSchema.SCHEMA_NS)) {

                //    String elPrefix = el.getPrefix() == null ? ""
                //     : el.getPrefix();
                //if(elPrefix.equals(schema.schema_ns_prefix)) {
                if (el.getLocalName().equals("selector")) {
                    XmlSchemaXPath selectorXPath =
                            new XmlSchemaXPath();
                    selectorXPath.xpath = el.getAttribute("xpath");

                    Element annotationEl =
                            XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS,
                                    "annotation");
                    if (annotationEl != null) {
                        XmlSchemaAnnotation annotation =
                                handleAnnotation(annotationEl);

                        selectorXPath.setAnnotation(annotation);
                    }
                    constraint.selector = selectorXPath;
                } else if (el.getLocalName().equals("field")) {
                    XmlSchemaXPath fieldXPath = new XmlSchemaXPath();
                    fieldXPath.xpath = el.getAttribute("xpath");
                    constraint.fields.add(fieldXPath);

                    Element annotationEl =
                            XDOMUtil.getFirstChildElementNS(el, XmlSchema.SCHEMA_NS,
                                    "annotation");

                    if (annotationEl != null) {
                        XmlSchemaAnnotation annotation =
                                handleAnnotation(annotationEl);

                        fieldXPath.setAnnotation(annotation);
                    }
                } else if (el.getLocalName().equals("annotation")) {
                    XmlSchemaAnnotation constraintAnnotation =
                            handleAnnotation(el);
                    constraint.setAnnotation(constraintAnnotation);
                }
            }
            return constraint;
        } catch (ClassNotFoundException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (InstantiationException e) {
            throw new XmlSchemaException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new XmlSchemaException(e.getMessage());
        }
    }

    XmlSchemaImport handleImport(XmlSchema schema, Element importEl,
                                 Element schemaEl) {

        XmlSchemaImport schemaImport = new XmlSchemaImport();

        Element annotationEl =
                XDOMUtil.getFirstChildElementNS(importEl, XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation importAnnotation =
                    handleAnnotation(annotationEl);
            schemaImport.setAnnotation(importAnnotation);
        }

        schemaImport.namespace = importEl.getAttribute("namespace");
        schemaImport.schemaLocation =
                importEl.getAttribute("schemaLocation");

        if ((schemaImport.schemaLocation != null) && (!schemaImport.schemaLocation.equals(""))) {
            if(schema.getSourceURI()!=null) {
                schemaImport.schema =
                        getXmlSchemaFromLocation(schemaImport.schemaLocation, schema.getSourceURI());
            } else {
                schemaImport.schema = getXmlSchemaFromLocation(schemaImport.schemaLocation);
            }
        }
        return schemaImport;
    }

    XmlSchemaInclude handleInclude(XmlSchema schema,
                                   Element includeEl, Element schemaEl) {

        XmlSchemaInclude include = new XmlSchemaInclude();

        Element annotationEl =
                XDOMUtil.getFirstChildElementNS(includeEl,
                        XmlSchema.SCHEMA_NS, "annotation");

        if (annotationEl != null) {
            XmlSchemaAnnotation includeAnnotation =
                    handleAnnotation(annotationEl);
            include.setAnnotation(includeAnnotation);
        }

        include.schemaLocation =
                includeEl.getAttribute("schemaLocation");


        if(schema.getSourceURI()!=null) {
            include.schema =
                    getXmlSchemaFromLocation(include.schemaLocation, schema.getSourceURI());
        } else {
            include.schema =
                    getXmlSchemaFromLocation(include.schemaLocation);
        }
        XmlSchemaObjectCollection coll = include.schema.getItems();

        return include;
    }

    /**
     * Traversing if encounter appinfo or documentation
     * add it to annotation collection
     */
    XmlSchemaAnnotation handleAnnotation(Element annotEl) {
        XmlSchemaObjectCollection content = new XmlSchemaObjectCollection();
        XmlSchemaAppInfo appInfoObj;
        XmlSchemaDocumentation docsObj;

        for (Element appinfo = XDOMUtil.getFirstChildElementNS(annotEl,
                XmlSchema.SCHEMA_NS, "appinfo");
             appinfo != null;
             appinfo = XDOMUtil.getNextSiblingElementNS(appinfo, XmlSchema.SCHEMA_NS, "appinfo")) {

            appInfoObj = handleAppInfo(appinfo);
            content.add(appInfoObj);
        }
        for (Element documentation = XDOMUtil.getFirstChildElementNS(annotEl,
                XmlSchema.SCHEMA_NS, "documentation");
             documentation != null;
             documentation = XDOMUtil.getNextSiblingElementNS(documentation,


                     XmlSchema.SCHEMA_NS, "documentation")) {

            docsObj = handleDocumentation(documentation);
            content.add(docsObj);
        }

        XmlSchemaAnnotation annotation = new XmlSchemaAnnotation();
        annotation.items = content;
        return annotation;
    }

    //create new XmlSchemaAppinfo and add value goten from element
    //to this obj
    XmlSchemaAppInfo handleAppInfo(Element content) {
        XmlSchemaAppInfo appInfo = new XmlSchemaAppInfo();
        NodeList markup = getChild(content);

        if (!content.hasAttribute("source") && markup.getLength() <= 0)
            return null;
        appInfo.setSource(getAttribute(content, "source"));
        appInfo.setMarkup(markup);
        return appInfo;
    }

    //iterate each documentation element, create new XmlSchemaAppinfo and add to collection
    XmlSchemaDocumentation handleDocumentation(Element content) {
        XmlSchemaDocumentation documentation = new XmlSchemaDocumentation();
        NodeList markup = getChild(content);

        if (!content.hasAttribute("source") &&
                !content.hasAttribute("xml:lang") &&
                (markup == null || markup.getLength() <= 0))
            return null;

        documentation.setSource(getAttribute(content, "source"));
        documentation.setLanguage(getAttribute(content, "xml:lang"));
        documentation.setMarkup(getChild(content));

        return documentation;
    }

    private String getAttribute(Element content, String attrName) {
        if (content.hasAttribute(attrName))
            return content.getAttribute(attrName);
        return null;
    }

    private NodeList getChild(Element content) {
        NodeList childs = content.getChildNodes();
        if (childs.getLength() > 0)
            return childs;
        return null;
    }

    long getMinOccurs(Element el) {
        try {
            if (el.getAttributeNode("minOccurs") != null) {
                String value = el.getAttribute("minOccurs");
                if (value.equals("unbounded"))
                    return Long.MAX_VALUE;
                else
                    return new Long(value).longValue();
            }
            return 1;
        } catch (java.lang.NumberFormatException e) {
            return 1;
        }
    }

    long getMaxOccurs(Element el) {
        try {
            if (el.getAttributeNode("maxOccurs") != null) {
                String value = el.getAttribute("maxOccurs");
                if (value.equals("unbounded"))
                    return Long.MAX_VALUE;
                else
                    return new Long(value).longValue();
            }
            return 1;
        } catch (java.lang.NumberFormatException e) {
            return 1;
        }
    }

    XmlSchemaForm getFormDefault(Element el, String attrName) {
        if (el.getAttributeNode(attrName) != null) {
            String value = el.getAttribute(attrName);
            return new XmlSchemaForm(value);
        } else
            return new XmlSchemaForm("unqualified");
    }

    //Check value entered by user and change according to .net spec,
    //according to w3c spec have to be "#all"
    //but in .net the valid enum value is "all".
    XmlSchemaDerivationMethod getDerivation(Element el, String attrName) {
        if (el.hasAttribute(attrName) && !el.getAttribute(attrName).equals("")) {
            //#all | List of (extension | restriction | substitution
            String derivationMethod = el.getAttribute(attrName).trim();
            char c = Character.toUpperCase(derivationMethod.charAt(0));
            if (derivationMethod.equals("#all"))
                return new XmlSchemaDerivationMethod("All");
            else
                return new XmlSchemaDerivationMethod(c + derivationMethod.substring(1));
        }
        return new XmlSchemaDerivationMethod("None");
    }

    //Check value entered by user and change according to .net spec, user
    String getEnumString(Element el, String attrName) {
        if (el.hasAttribute(attrName)) {
            String contentProcessing = el.getAttribute(attrName).trim();
            char c = Character.toUpperCase(contentProcessing.charAt(0));
            return c + contentProcessing.substring(1);
        }
        return "None";
    }

    XmlSchema getXmlSchemaFromLocation(String schemaLocation) {
        return getXmlSchemaFromLocation(schemaLocation, collection.baseUri);
    }

    XmlSchema getXmlSchemaFromLocation(String schemaLocation, String baseURI) {
        if (baseURI!=null){
            if (!isAbsolute(schemaLocation)){
                try {
                    schemaLocation = getURL(new URL(baseURI), schemaLocation).toString();    
                } catch (Exception e) {
                    schemaLocation = baseURI +
                                     (schemaLocation.startsWith("/")?"":"/")+
                                     schemaLocation;
                }
            }
        }
        return collection.read(new InputSource(schemaLocation), null);
    }

    /**
     * Find whether a given uri is relative or not
     *
     * @param uri
     * @return boolean
     */
    private boolean isAbsolute(String uri) {
        return uri.startsWith("http://");
    }

    /**
     * This is essentially a call to "new URL(contextURL, spec)" with extra handling in case spec is
     * a file.
     *
     * @param contextURL
     * @param spec
     * @return
     * @throws java.io.IOException
     */
    private static URL getURL(URL contextURL, String spec) throws IOException {

        // First, fix the slashes as windows filenames may have backslashes
        // in them, but the URL class wont do the right thing when we later
        // process this URL as the contextURL.
        String path = spec.replace('\\', '/');

        // See if we have a good URL.
        URL url = null;

        try {

            // first, try to treat spec as a full URL
            url = new URL(contextURL, path);

            // if we are deail with files in both cases, create a url
            // by using the directory of the context URL.
            if ((contextURL != null) && url.getProtocol().equals("file")
                    && contextURL.getProtocol().equals("file")) {
                url = getFileURL(contextURL, path);
            }
        } catch (MalformedURLException me) {

            // try treating is as a file pathname
            url = getFileURL(contextURL, path);
        }

        // Everything is OK with this URL, although a file url constructed
        // above may not exist.  This will be caught later when the URL is
        // accessed.
        return url;
    }    // getURL

    /**
     * Method getFileURL
     *
     * @param contextURL
     * @param path
     * @return
     * @throws IOException
     */
    private static URL getFileURL(URL contextURL, String path)
            throws IOException {

        if (contextURL != null) {

            // get the parent directory of the contextURL, and append
            // the spec string to the end.
            String contextFileName = contextURL.getFile();
            URL parent = null;
            File parentFile = new File(contextFileName).getParentFile();
            if (parentFile != null) {
                parent = parentFile.toURL();
            }
            if (parent != null) {
                return new URL(parent, path);
            }
        }

        return new URL("file", "", path);
    }    // getFileURL
}
