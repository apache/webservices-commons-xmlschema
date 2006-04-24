package org.apache.ws.commons.schema.resolver;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;
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

/**
 * This resolver provides the means of resolving the imports and includes of a
 * given schema document. The system will call this default resolver if there
 * is no other resolver present in the system
 */
public class DefaultURIResolver implements URIResolver {


    /**
     * As for the resolver the publid ID is the target namespace of the
     * schema and the schemaLocation is the value of the schema location
     * @param publicId
     * @param schemaLocation
     * @return
     * @throws SAXException
     * @throws IOException
     */
    public InputSource resolveEntity(String namespace,
                                     String schemaLocation,
                                     String baseUri){

        if (baseUri!=null){
            if (!isAbsolute(schemaLocation)){
                try {
                    schemaLocation =
                            getURL(new URL(baseUri), schemaLocation).toString();
                } catch (Exception e) {
                    schemaLocation = baseUri +
                            (schemaLocation.startsWith("/")?"":"/")+
                            schemaLocation;
                }
            }
        }
        return new InputSource(schemaLocation);



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
     * This is essentially a call to "new URL(contextURL, spec)"
     * with extra handling in case spec is
     * a file.
     *
     * @param contextURL
     * @param spec
     * @return
     * @throws java.io.IOException
     */
    private URL getURL(URL contextURL, String spec) throws IOException {

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
    private URL getFileURL(URL contextURL, String path)
            throws IOException {

        if (contextURL != null) {

            // get the parent directory of the contextURL, and append
            // the spec string to the end.
            String contextFileName = contextURL.getFile();
            URL parent = null;
            //the logic for finding the parent file is this.
            //1.if the contextURI represents a file then take the parent file
            //of it
            //2. If the contextURI represents a directory, then take that as
            //the parent
            File parentFile;
            File contextFile = new File(contextFileName);
            if (contextFile.isDirectory()){
                parentFile = contextFile;
            }else{
                parentFile = contextFile.getParentFile();
            }

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
