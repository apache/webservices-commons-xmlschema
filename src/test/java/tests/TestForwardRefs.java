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

package tests;

import junit.framework.TestCase;

import java.io.InputStream;
import java.io.FileInputStream;

import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaSequence;

import javax.xml.transform.stream.StreamSource;
import javax.xml.namespace.QName;

/**
 */
public class TestForwardRefs extends TestCase {
    
    public void testForwardRefs() throws Exception {
        QName ELEMENT_QNAME = new QName("http://soapinterop.org/types",
                                        "attrTest");
        InputStream is = new FileInputStream("test-resources/forwardRef.xsd");
        XmlSchemaCollection schema = new XmlSchemaCollection();
        schema.read(new StreamSource(is), null);

        XmlSchemaElement elem = schema.getElementByQName(ELEMENT_QNAME);
        assertNotNull(elem);
        XmlSchemaType type = elem.getSchemaType();
        assertNotNull(type);
        assertTrue(type instanceof XmlSchemaComplexType);
        XmlSchemaComplexType cType = (XmlSchemaComplexType)type;
        XmlSchemaSequence seq = (XmlSchemaSequence)cType.getParticle();
        assertNotNull(seq);
    }
}
