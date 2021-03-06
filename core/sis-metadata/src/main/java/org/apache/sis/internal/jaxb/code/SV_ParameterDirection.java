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
package org.apache.sis.internal.jaxb.code;

import javax.xml.bind.annotation.XmlElement;
import org.opengis.parameter.ParameterDirection;
import org.apache.sis.internal.jaxb.gmd.EnumAdapter;
import org.apache.sis.xml.Namespaces;


/**
 * JAXB adapter for {@link ParameterDirection}, in order to integrate the value in an element
 * complying with ISO-19139 standard. See package documentation for more information
 * about the handling of {@code CodeList} in ISO-19139.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.5
 * @version 0.5
 * @module
 */
public final class SV_ParameterDirection extends EnumAdapter<SV_ParameterDirection, ParameterDirection> {
    /**
     * The enumeration value.
     */
    @XmlElement(name = "SV_ParameterDirection", namespace = Namespaces.SRV)
    private String value;

    /**
     * Empty constructor for JAXB only.
     */
    public SV_ParameterDirection() {
    }

    /**
     * Returns the wrapped value.
     *
     * @param wrapper The wrapper.
     * @return The wrapped value.
     */
    @Override
    public final ParameterDirection unmarshal(final SV_ParameterDirection wrapper) {
        return ParameterDirection.valueOf(name(wrapper.value));
    }

    /**
     * Wraps the given value.
     *
     * @param  e The value to wrap.
     * @return The wrapped value.
     */
    @Override
    public final SV_ParameterDirection marshal(final ParameterDirection e) {
        if (e == null) {
            return null;
        }
        final SV_ParameterDirection wrapper = new SV_ParameterDirection();
        wrapper.value = value(e);
        return wrapper;
    }
}
