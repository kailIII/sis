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

import java.util.Date;
import java.util.Collection;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.metadata.Identifier;
import org.opengis.metadata.acquisition.Plan;
import org.opengis.metadata.acquisition.Priority;
import org.opengis.metadata.acquisition.RequestedDate;
import org.opengis.metadata.acquisition.Requirement;
import org.opengis.metadata.citation.Citation;
import org.opengis.metadata.citation.ResponsibleParty;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.internal.jaxb.NonMarshalledAuthority;


/**
 * Requirement to be satisfied by the planned data acquisition.
 *
 * @author  Cédric Briançon (Geomatys)
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3 (derived from geotk-3.03)
 * @version 0.3
 * @module
 */
@XmlType(name = "MI_Requirement_Type", propOrder = {
    "citation",
    "identifier",
    "requestors",
    "recipients",
    "priority",
    "requestedDate",
    "expiryDate",
    "satisfiedPlans"
})
@XmlRootElement(name = "MI_Requirement")
public class DefaultRequirement extends ISOMetadata implements Requirement {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = 7305276418007196948L;

    /**
     * Identification of reference or guidance material for the requirement.
     */
    private Citation citation;

    /**
     * Origin of requirement.
     */
    private Collection<ResponsibleParty> requestors;

    /**
     * Person(s), or body(ies), to receive results of requirement.
     */
    private Collection<ResponsibleParty> recipients;

    /**
     * Relative ordered importance, or urgency, of the requirement.
     */
    private Priority priority;

    /**
     * Required or preferred acquisition date and time.
     */
    private RequestedDate requestedDate;

    /**
     * Date and time after which collection is no longer valid,
     * or {@link Long#MIN_VALUE} if none.
     */
    private long expiryDate;

    /**
     * Plan that identifies solution to satisfy the requirement.
     */
    private Collection<Plan> satisfiedPlans;

    /**
     * Constructs an initially empty requirement.
     */
    public DefaultRequirement() {
        expiryDate = Long.MIN_VALUE;
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
    public static DefaultRequirement castOrCopy(final Requirement object) {
        if (object == null || object instanceof DefaultRequirement) {
            return (DefaultRequirement) object;
        }
        final DefaultRequirement copy = new DefaultRequirement();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns the identification of reference or guidance material for the requirement.
     * {@code null} if unspecified.
     */
    @Override
    @XmlElement(name = "citation")
    public synchronized Citation getCitation() {
        return citation;
    }

    /**
     * Sets the identification of reference or guidance material for the requirement.
     *
     * @param newValue The new citation value.
     */
    public synchronized void setCitation(final Citation newValue) {
        checkWritePermission();
        citation = newValue;
    }

    /**
     * Returns the unique name, or code, for the requirement.
     */
    @Override
    @XmlElement(name = "identifier", required = true)
    public Identifier getIdentifier() {
        return NonMarshalledAuthority.getMarshallable(identifiers);
    }

    /**
     * Sets the unique name, or code, for the requirement.
     *
     * @param newValue The new identifier value.
     */
    public synchronized void setIdentifier(final Identifier newValue) {
        checkWritePermission();
        identifiers = nonNullCollection(identifiers, Identifier.class);
        NonMarshalledAuthority.setMarshallable(identifiers, newValue);
    }

    /**
     * Returns the origin of requirement.
     */
    @Override
    @XmlElement(name = "requestor", required = true)
    public synchronized Collection<ResponsibleParty> getRequestors() {
        return requestors = nonNullCollection(requestors, ResponsibleParty.class);
    }

    /**
     * Sets the origin of requirement.
     *
     * @param newValues The new requestors values.
     */
    public synchronized void setRequestors(final Collection<? extends ResponsibleParty> newValues) {
        requestors = writeCollection(newValues, requestors, ResponsibleParty.class);
    }

    /**
     * Returns the person(s), or body(ies), to receive results of requirement.
     */
    @Override
    @XmlElement(name = "recipient", required = true)
    public synchronized Collection<ResponsibleParty> getRecipients() {
        return recipients = nonNullCollection(recipients, ResponsibleParty.class);
    }

    /**
     * Sets the Person(s), or body(ies), to receive results of requirement.
     *
     * @param newValues The new recipients values.
     */
    public synchronized void setRecipients(final Collection<? extends ResponsibleParty> newValues) {
        recipients = writeCollection(newValues, recipients, ResponsibleParty.class);
    }

    /**
     * Returns the relative ordered importance, or urgency, of the requirement.
     */
    @Override
    @XmlElement(name = "priority", required = true)
    public synchronized Priority getPriority() {
        return priority;
    }

    /**
     * Sets the relative ordered importance, or urgency, of the requirement.
     *
     * @param newValue The new priority value.
     */
    public synchronized void setPriority(final Priority newValue) {
        checkWritePermission();
        priority = newValue;
    }

    /**
     * Returns the required or preferred acquisition date and time.
     */
    @Override
    @XmlElement(name = "requestedDate", required = true)
    public synchronized RequestedDate getRequestedDate() {
        return requestedDate;
    }

    /**
     * Sets the required or preferred acquisition date and time.
     *
     * @param newValue The new requested date value.
     */
    public synchronized void setRequestedDate(final RequestedDate newValue) {
        checkWritePermission();
        requestedDate = newValue;
    }

    /**
     * Returns the date and time after which collection is no longer valid.
     */
    @Override
    @XmlElement(name = "expiryDate", required = true)
    public synchronized Date getExpiryDate() {
        final long date = this.expiryDate;
        return (date != Long.MIN_VALUE) ? new Date(date) : null;
    }

    /**
     * Sets the date and time after which collection is no longer valid.
     *
     * @param newValue The new expiry date.
     */
    public synchronized void setExpiryDate(final Date newValue) {
        checkWritePermission();
        expiryDate = (newValue != null) ? newValue.getTime() : Long.MIN_VALUE;
    }

    /**
     * Returns the plan that identifies solution to satisfy the requirement.
     */
    @Override
    @XmlElement(name = "satisfiedPlan")
    public synchronized Collection<Plan> getSatisfiedPlans() {
        return satisfiedPlans = nonNullCollection(satisfiedPlans, Plan.class);
    }

    /**
     * @param newValues The new satisfied plans values.
     */
    public synchronized void setSatisfiedPlans(final Collection<? extends Plan> newValues) {
        satisfiedPlans = writeCollection(newValues, satisfiedPlans, Plan.class);
    }
}