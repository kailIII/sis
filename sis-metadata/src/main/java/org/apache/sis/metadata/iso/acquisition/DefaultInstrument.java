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
package org.apache.sis.metadata.iso.acquisition;

import java.util.Collection;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.acquisition.Instrument;
import org.opengis.metadata.acquisition.Platform;
import org.opengis.metadata.citation.Citation;
import org.opengis.util.InternationalString;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.internal.jaxb.NonMarshalledAuthority;


/**
 * Designations for the measuring instruments.
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.03)
 * @version 0.3
 * @module
 */
@XmlType(name = "MI_Instrument_Type", propOrder = {
    "citations",
    "identifier",
    "type",
    "description",
    "mountedOn"
})
@XmlRootElement(name = "MI_Instrument")
public class DefaultInstrument extends ISOMetadata implements Instrument {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 6356044176200794578L;

    /**
     * Complete citation of the instrument.
     */
    private Collection<Citation> citations;

    /**
     * Name of the type of instrument. Examples: framing, line-scan, push-broom, pan-frame.
     */
    private InternationalString type;

    /**
     * Textual description of the instrument.
     */
    private InternationalString description;

    /**
     * Platform on which the instrument is mounted.
     */
    private Platform mountedOn;

    /**
     * Constructs an initially empty instrument.
     */
    public DefaultInstrument() {
    }

    /**
     * Returns a SIS metadata implementation with the same values than the given arbitrary
     * implementation. If the given object is {@code null}, then this method returns {@code null}.
     * Otherwise if the given object is already a SIS implementation, then the given object is
     * returned unchanged. Otherwise a new SIS implementation is created and initialized to the
     * property values of the given object, using a <cite>shallow</cite> copy operation
     * (i.e. properties are not cloned).
     *
     * @param  object The object to get as a SIS implementation, or {@code null} if none.
     * @return A SIS implementation containing the values of the given object (may be the
     *         given object itself), or {@code null} if the argument was null.
     */
    public static DefaultInstrument castOrCopy(final Instrument object) {
        if (object == null || object instanceof DefaultInstrument) {
            return (DefaultInstrument) object;
        }
        final DefaultInstrument copy = new DefaultInstrument();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns the complete citation of the instrument.
     */
    @Override
    @XmlElement(name = "citation")
    public synchronized Collection<Citation> getCitations() {
        return citations = nonNullCollection(citations, Citation.class);
    }

    /**
     * Sets the complete citation of the instrument.
     *
     * @param newValues The new citation values.
     */
    public synchronized void setCitations(final Collection<? extends Citation> newValues) {
        citations = writeCollection(newValues, citations, Citation.class);
    }

    /**
     * Returns the unique identification of the instrument.
     */
    @Override
    @XmlElement(name = "identifier", required = true)
    public Identifier getIdentifier() {
        return NonMarshalledAuthority.getMarshallable(identifiers);
    }

    /**
     * Sets the unique identification of the instrument.
     *
     * @param newValue The new identifier value.
     */
    public synchronized void setIdentifier(final Identifier newValue) {
        checkWritePermission();
        identifiers = nonNullCollection(identifiers, Identifier.class);
        NonMarshalledAuthority.setMarshallable(identifiers, newValue);
    }

    /**
     * Returns the name of the type of instrument. Examples: framing, line-scan, push-broom, pan-frame.
     */
    @Override
    @XmlElement(name = "type", required = true)
    public synchronized InternationalString getType() {
        return type;
    }

    /**
     * Sets the name of the type of instrument. Examples: framing, line-scan, push-broom, pan-frame.
     *
     * @param newValue The new type value.
     */
    public synchronized void setType(final InternationalString newValue) {
        checkWritePermission();
        type = newValue;
    }

    /**
     * Returns the textual description of the instrument. {@code null} if unspecified.
     */
    @Override
    @XmlElement(name = "description")
    public synchronized InternationalString getDescription() {
        return description;
    }

    /**
     * Sets the textual description of the instrument.
     *
     * @param newValue The new description value.
     */
    public synchronized void setDescription(final InternationalString newValue) {
        checkWritePermission();
        description = newValue;
    }

    /**
     * Returns the platform on which the instrument is mounted. {@code null} if unspecified.
     */
    @Override
    @XmlElement(name = "mountedOn")
    public synchronized Platform getMountedOn() {
        return mountedOn;
    }

    /**
     * Sets the platform on which the instrument is mounted.
     *
     * @param newValue The new platform value.
     */
    public synchronized void setMountedOn(final Platform newValue) {
        checkWritePermission();
        mountedOn = newValue;
    }
}