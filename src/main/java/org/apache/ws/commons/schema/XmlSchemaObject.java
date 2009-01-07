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

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.ws.commons.schema.utils.XmlSchemaObjectBase;

public abstract class XmlSchemaObject implements XmlSchemaObjectBase {
    int lineNumber;
    int linePosition;
    String sourceURI;

    /**
     * a map for holding meta information Initially set to null to gain some improvement in memory. will be
     * initialized only if a user attempts
     */
    private Map<Object, Object> metaInfoMap;

    /**
     * Creates new XmlSchemaObject
     */
    protected XmlSchemaObject() {
    }

    /**
     * Add a value to the meta info map will be initialized if not used previously
     * 
     * @param key
     * @param value
     */
    public void addMetaInfo(Object key, Object value) {
        if (metaInfoMap == null) {
            metaInfoMap = new LinkedHashMap<Object, Object>();
        }

        metaInfoMap.put(key, value);
    }

    @Override
    public boolean equals(Object what) {
        if (what == this) {
            return true;
        }

        // note: instanceof returns false if its first operand is null
        if (!(what instanceof XmlSchemaObject)) {
            return false;
        }

        XmlSchemaObject xso = (XmlSchemaObject)what;

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
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public int getLinePosition() {
        return linePosition;
    }

    /**
     * returns the metainfo map. may be null if not utilized
     */
    public Map<Object, Object> getMetaInfoMap() {
        return metaInfoMap;
    }

    public String getSourceURI() {
        return sourceURI;
    }

    public void setLineNumber(int lineNumber) {
        this.lineNumber = lineNumber;
    }

    public void setLinePosition(int linePosition) {
        this.linePosition = linePosition;
    }

    /**
     * Directly set the meta info map into the schema element
     * 
     * @param metaInfoMap
     */
    public void setMetaInfoMap(Map<Object, Object> metaInfoMap) {
        this.metaInfoMap = metaInfoMap;
    }

    public void setSourceURI(String sourceURI) {
        this.sourceURI = sourceURI;
    }

    public String toString(String prefix, int tab) {
        String xml = new String();
        for (int i = 0; i < tab; i++) {
            xml += "\t";
        }

        xml += this.getClass().toString() + "\n";
        return xml;
    }
}
