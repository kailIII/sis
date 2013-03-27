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
import java.util.Date;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.acquisition.Context;
import org.opengis.metadata.acquisition.Event;
import org.opengis.metadata.acquisition.Instrument;
import org.opengis.metadata.acquisition.Objective;
import org.opengis.metadata.acquisition.PlatformPass;
import org.opengis.metadata.acquisition.Sequence;
import org.opengis.metadata.acquisition.Trigger;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.internal.jaxb.NonMarshalledAuthority;


/**
 * Identification of a significant collection point within an operation.
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.03)
 * @version 0.3
 * @module
 */
@XmlType(name = "MI_Event_Type", propOrder = {
    "identifier",
    "trigger",
    "context",
    "sequence",
    "time",
    "expectedObjectives",
    "relatedPass",
    "relatedSensors"
})
@XmlRootElement(name = "MI_Event")
public class DefaultEvent extends ISOMetadata implements Event {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -5625600499628778407L;

    /**
     * Initiator of the event.
     */
    private Trigger trigger;

    /**
     * Meaning of the event.
     */
    private Context context;

    /**
     * Relative time ordering of the event.
     */
    private Sequence sequence;

    /**
     * Time the event occurred, or {@link Long#MIN_VALUE} if none.
     */
    private long time;

    /**
     * Objective or objectives satisfied by an event.
     */
    private Collection<Objective> expectedObjectives;

    /**
     * Pass during which an event occurs.
     */
    private PlatformPass relatedPass;

    /**
     * Instrument or instruments for which the event is meaningful.
     */
    private Collection<Instrument> relatedSensors;

    /**
     * Constructs an initially empty acquisition information.
     */
    public DefaultEvent() {
        time = Long.MIN_VALUE;
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
    public static DefaultEvent castOrCopy(final Event object) {
        if (object == null || object instanceof DefaultEvent) {
            return (DefaultEvent) object;
        }
        final DefaultEvent copy = new DefaultEvent();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns the event name or number.
     */
    @Override
    @XmlElement(name = "identifier", required = true)
    public Identifier getIdentifier() {
        return NonMarshalledAuthority.getMarshallable(identifiers);
    }

    /**
     * Sets the event name or number.
     *
     * @param newValue The event identifier value.
     */
    public synchronized void setIdentifier(final Identifier newValue) {
        checkWritePermission();
        identifiers = nonNullCollection(identifiers, Identifier.class);
        NonMarshalledAuthority.setMarshallable(identifiers, newValue);
    }

    /**
     * Returns the initiator of the event.
     */
    @Override
    @XmlElement(name = "trigger", required = true)
    public synchronized Trigger getTrigger() {
        return trigger;
    }

    /**
     * Sets the initiator of the event.
     *
     * @param newValue The new trigger value.
     */
    public synchronized void setTrigger(final Trigger newValue) {
        checkWritePermission();
        trigger = newValue;
    }

    /**
     * Meaning of the event.
     */
    @Override
    @XmlElement(name = "context", required = true)
    public synchronized Context getContext() {
        return context;
    }

    /**
     * Sets the meaning of the event.
     *
     * @param newValue The new context value.
     */
    public synchronized void setContext(final Context newValue) {
        checkWritePermission();
        context = newValue;
    }

    /**
     * Returns the relative time ordering of the event.
     */
    @Override
    @XmlElement(name = "sequence", required = true)
    public synchronized Sequence getSequence() {
        return sequence;
    }

    /**
     * Sets the relative time ordering of the event.
     *
     * @param newValue The new sequence value.
     */
    public synchronized void setSequence(final Sequence newValue) {
        checkWritePermission();
        sequence = newValue;
    }

    /**
     * Returns the time the event occurred.
     */
    @Override
    @XmlElement(name = "time", required = true)
    public synchronized Date getTime() {
        final long date = this.time;
        return (date != Long.MIN_VALUE) ? new Date(date) : null;
    }

    /**
     * Sets the time the event occurred.
     *
     * @param newValue The new time value.
     */
    public synchronized void setTime(final Date newValue) {
        checkWritePermission();
        time = (newValue != null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the objective or objectives satisfied by an event.
     */
    @Override
    @XmlElement(name = "expectedObjective")
    public synchronized Collection<Objective> getExpectedObjectives() {
        return expectedObjectives = nonNullCollection(expectedObjectives, Objective.class);
    }

    /**
     * Sets the objective or objectives satisfied by an event.
     *
     * @param newValues The new expected objectives values.
     */
    public synchronized void setExpectedObjectives(final Collection<? extends Objective> newValues) {
        expectedObjectives = writeCollection(newValues, expectedObjectives, Objective.class);
    }

    /**
     * Returns the pass during which an event occurs. {@code null} if unspecified.
     */
    @Override
    @XmlElement(name = "relatedPass")
    public synchronized PlatformPass getRelatedPass() {
        return relatedPass;
    }

    /**
     * Sets the pass during which an event occurs.
     *
     * @param newValue The new platform pass value.
     */
    public synchronized void setRelatedPass(final PlatformPass newValue) {
        relatedPass = newValue;
    }

    /**
     * Returns the instrument or instruments for which the event is meaningful.
     */
    @Override
    @XmlElement(name = "relatedSensor")
    public synchronized Collection<? extends Instrument> getRelatedSensors() {
        return relatedSensors = nonNullCollection(relatedSensors, Instrument.class);
    }

    /**
     * Sets the instrument or instruments for which the event is meaningful.
     *
     * @param newValues The new instrument values.
     */
    public synchronized void setRelatedSensors(final Collection<? extends Instrument> newValues) {
        relatedSensors = writeCollection(newValues, relatedSensors, Instrument.class);
    }
}