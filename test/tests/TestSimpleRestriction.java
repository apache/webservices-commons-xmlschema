package tests;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaSequence;

import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.FileInputStream;
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
import junit.framework.TestCase;

public class TestSimpleRestriction extends TestCase {
    public void testSimpleRestriction() throws Exception {
        QName TYPE_QNAME = new QName("http://soapinterop.org/types",
                "layoutComponentType");
        QName ELEMENT_QNAME = new QName("http://soapinterop.org/types",
                "foo");

        InputStream is = new FileInputStream("test-resources/SimpleContentRestriction.xsd");
        XmlSchemaCollection schema = new XmlSchemaCollection();
        schema.read(new StreamSource(is), null);

        XmlSchemaType simpleType = schema.getTypeByQName(TYPE_QNAME);
        assertNotNull(simpleType);

        XmlSchemaElement elem = schema.getElementByQName(ELEMENT_QNAME);
        assertNotNull(elem);

        XmlSchemaType type = elem.getSchemaType();
        assertNotNull(type);

    }
}
