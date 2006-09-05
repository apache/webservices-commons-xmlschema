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

    /**
     * <p>Get Namespace URI bound to a prefix in the current scope.</p>
     * <p/>
     * <p>When requesting a Namespace URI by prefix, the following
     * table describes the returned Namespace URI value for all
     * possible prefix values:</p>
     * <p/>
     * <table border="2" rules="all" cellpadding="4">
     * <thead>
     * <tr>
     * <td align="center" colspan="2">
     * <code>getNamespaceURI(prefix)</code>
     * return value for specified prefixes
     * </td>
     * </tr>
     * <tr>
     * <td>prefix parameter</td>
     * <td>Namespace URI return value</td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td><code>DEFAULT_NS_PREFIX</code> ("")</td>
     * <td>default Namespace URI in the current scope or
     * <code>{@link javax.xml.XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI("")}</code>
     * when there is no default Namespace URI in the current scope</td>
     * </tr>
     * <tr>
     * <td>bound prefix</td>
     * <td>Namespace URI bound to prefix in current scope</td>
     * </tr>
     * <tr>
     * <td>unbound prefix</td>
     * <td><code>{@link javax.xml.XMLConstants#NULL_NS_URI XMLConstants.NULL_NS_URI("")}</code> </td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
     * <td><code>XMLConstants.XML_NS_URI</code>
     * ("http://www.w3.org/XML/1998/namespace")</td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
     * ("http://www.w3.org/2000/xmlns/")</td>
     * </tr>
     * <tr>
     * <td><code>null</code></td>
     * <td><code>IllegalArgumentException</code> is thrown</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param prefix prefix to look up
     * @return Namespace URI bound to prefix in the current scope
     */
    public String getNamespaceURI(String prefix) {
        return (String) get(prefix);
    }

    /**
     * <p>Get prefix bound to Namespace URI in the current scope.</p>
     * <p/>
     * <p>To get all prefixes bound to a Namespace URI in the current
     * scope, use {@link #getPrefixes(String namespaceURI)}.</p>
     * <p/>
     * <p>When requesting a prefix by Namespace URI, the following
     * table describes the returned prefix value for all Namespace URI
     * values:</p>
     * <p/>
     * <table border="2" rules="all" cellpadding="4">
     * <thead>
     * <tr>
     * <td align="center" colspan="2">
     * <code>getPrefix(namespaceURI)</code> return value for
     * specified Namespace URIs
     * </td>
     * </tr>
     * <tr>
     * <td>Namespace URI parameter</td>
     * <td>prefix value returned</td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>&lt;default Namespace URI&gt;</td>
     * <td><code>XMLConstants.DEFAULT_NS_PREFIX</code> ("")
     * </td>
     * </tr>
     * <tr>
     * <td>bound Namespace URI</td>
     * <td>prefix bound to Namespace URI in the current scope,
     * if multiple prefixes are bound to the Namespace URI in
     * the current scope, a single arbitrary prefix, whose
     * choice is implementation dependent, is returned</td>
     * </tr>
     * <tr>
     * <td>unbound Namespace URI</td>
     * <td><code>null</code></td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XML_NS_URI</code>
     * ("http://www.w3.org/XML/1998/namespace")</td>
     * <td><code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
     * ("http://www.w3.org/2000/xmlns/")</td>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
     * </tr>
     * <tr>
     * <td><code>null</code></td>
     * <td><code>IllegalArgumentException</code> is thrown</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param namespaceURI URI of Namespace to lookup
     * @return prefix bound to Namespace URI in current context
     */
    public String getPrefix(String namespaceURI) {
        Iterator iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getValue().equals(namespaceURI)) {
                return (String) entry.getKey();
            }
        }
        return null;
    }

    /**
     * <p>Get all prefixes bound to a Namespace URI in the current
     * scope.</p>
     * <p/>
     * <p>An Iterator over String elements is returned in an arbitrary, <strong>implementation dependent</strong>, order.</p>
     * <p/>
     * <p><strong>The <code>Iterator</code> is
     * <em>not</em> modifiable.  e.g. the
     * <code>remove()</code> method will throw
     * <code>UnsupportedOperationException</code>.</strong></p>
     * <p/>
     * <p>When requesting prefixes by Namespace URI, the following
     * table describes the returned prefixes value for all Namespace
     * URI values:</p>
     * <p/>
     * <table border="2" rules="all" cellpadding="4">
     * <thead>
     * <tr>
     * <td align="center" colspan="2"><code>
     * getPrefixes(namespaceURI)</code> return value for
     * specified Namespace URIs</td>
     * </tr>
     * <tr>
     * <td>Namespace URI parameter</td>
     * <td>prefixes value returned</td>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>bound Namespace URI,
     * including the &lt;default Namespace URI&gt;</td>
     * <td><code>Iterator</code> over prefixes bound to Namespace URI in
     * the current scope in an arbitrary, <strong>implementation dependent</strong>,
     * order</td>
     * </tr>
     * <tr>
     * <td>unbound Namespace URI</td>
     * <td>empty <code>Iterator</code></td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XML_NS_URI</code>
     * ("http://www.w3.org/XML/1998/namespace")</td>
     * <td><code>Iterator</code> with one element set to
     * <code>XMLConstants.XML_NS_PREFIX</code> ("xml")</td>
     * </tr>
     * <tr>
     * <td><code>XMLConstants.XMLNS_ATTRIBUTE_NS_URI</code>
     * ("http://www.w3.org/2000/xmlns/")</td>
     * <td><code>Iterator</code> with one element set to
     * <code>XMLConstants.XMLNS_ATTRIBUTE</code> ("xmlns")</td>
     * </tr>
     * <tr>
     * <td><code>null</code></td>
     * <td><code>IllegalArgumentException</code> is thrown</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param namespaceURI URI of Namespace to lookup
     * @return <code>Iterator</code> for all prefixes bound to the
     *         Namespace URI in the current scope
     */
    public Iterator getPrefixes(String namespaceURI) {
        ArrayList list = new ArrayList();
        Iterator iterator = entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            if (entry.getValue().equals(namespaceURI)) {
                list.add(entry.getKey());
            }
        }
        return list.iterator();
    }
}
