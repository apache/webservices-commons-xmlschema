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

package org.apache.ws.commons.schema.constants;

public abstract class Enum {

    public static String NULL = "NULL";

    protected Enum(String value) {
        setValue(value);
    }

    protected Enum() {
        this(NULL);
    }

    protected abstract String[] getValues();

    protected String value = NULL;

    public void setValue(String value) {
        if (value.equals(Enum.NULL))
            this.value = Enum.NULL;
        else {
            String values[] = getValues();
            for (int i = 0; i < values.length; i++) {
                if (values[i].equals(value)) {
                    this.value = value;
                    break;
                }
                if (i == values.length - 1)
                    throw new EnumValueException("Bad Enumeration value '" + value + "'");
            }
        }
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return value;
    }

    public boolean equals(Object what) {
        return what.getClass().equals(this.getClass()) &&
                ((Enum) what).getValue().equals(this.getValue());
    }

    public static class EnumValueException extends RuntimeException {
        public EnumValueException(String mesg) {
            super(mesg);
        }
    }

    protected static final int index(String value, String values[]) {
        for (int i = 0; i < values.length; i++) {
            if (value.equals(values[i]))
                return i;
        }
        return -1;
    }
}

