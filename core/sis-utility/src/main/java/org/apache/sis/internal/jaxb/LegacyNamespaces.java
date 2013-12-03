/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.sis.internal.jaxb;


/**
 * Legacy XML namespaces.
 * This class is hopefully temporary, if we can find a way to share the same Java classes between different versions.
 * If such better way is found, then every classes, methods and fields having a JAXB annotation using this namespace
 * should be deleted.
 *
 * @author  Guilhem Legal (Geomatys)
 * @since   0.4
 * @version 0.4
 * @module
 */
public final class LegacyNamespaces {
    /**
     * The {@value} URL, which was used for all GML versions before 3.2.
     */
    public static final String GML = "http://www.opengis.net/gml";

    /**
     * Do not allow instantiation of this class.
     */
    private LegacyNamespaces() {
    }
}
