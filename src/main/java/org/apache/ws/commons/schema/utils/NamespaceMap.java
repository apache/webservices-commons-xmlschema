package org.apache.ws.commons.schema.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class NamespaceMap extends HashMap implements NamespacePrefixList {
    
    public NamespaceMap() {
    }
    
    public NamespaceMap(Map map) {
        super(map);
    }

    public void add(String prefix, String namespaceURI) {
        put(prefix, namespaceURI);
    }

    public String[] getDeclaredPrefixes() {
        Set keys = keySet();
        return (String[]) keys.toArray(new String[keys.size()]);
    }

    public String getNamespaceURI(String prefix) {
        return get(prefix).toString();
    }

    public String getPrefix(String namespaceURI) {
        Iterator iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getValue().toString().equals(namespaceURI)) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    public Iterator getPrefixes(String namespaceURI) {
        ArrayList list = new ArrayList();
        Iterator iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getValue().toString().equals(namespaceURI)) {
                list.add(entry.getKey());
            }
        }
        return list.iterator();
    }
}
