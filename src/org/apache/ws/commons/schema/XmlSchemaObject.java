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
        if (what == this) {
            return true;
        }
        
        // note: instanceof returns false if its first operand is null 
        if (!(what instanceof XmlSchemaObject)) {
            return false;
        }
        
        XmlSchemaObject xso = (XmlSchemaObject) what;
        
        if (this.lineNumber != xso.lineNumber) {
            return false;
        }
        
        if (this.linePosition != xso.linePosition) {
            return false;
        }
        
        if (this.sourceURI != null) {
            if (!this.sourceURI.equals(xso.sourceURI)) {
                return false;
            }
        } else {
            if (xso.sourceURI != null) {
                return false;
            }
        }
        
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
