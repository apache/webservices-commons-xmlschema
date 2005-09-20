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

/**
 * @author mukund
 */

// October 15th - momo - initial implementation

public abstract class XmlSchemaObject {
    int lineNumber;
    int linePosition;
    String sourceURI;

    /**
     * Creates new XmlSchemaObject
     */
    protected XmlSchemaObject() {
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public int getLinePosition() {
        return linePosition;
    }

    public void setLinePosition(int linePosition) {
        this.linePosition = linePosition;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public boolean equals(Object what) {
        // toDO : implement this once everything completed
        return true;
    }

    public String toString(String prefix, int tab) {
        String xml = new String();
        for (int i = 0; i < tab; i++)
            xml += "\t";

        xml += this.getClass().toString() + "\n";
        return xml;
    }
}
