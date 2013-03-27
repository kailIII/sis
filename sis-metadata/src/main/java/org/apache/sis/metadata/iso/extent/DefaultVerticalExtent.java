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
package org.apache.sis.metadata.iso.extent;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.opengis.geometry.Envelope;
import org.opengis.referencing.crs.VerticalCRS;
import org.opengis.referencing.operation.TransformException;
import org.opengis.metadata.extent.VerticalExtent;
import org.apache.sis.metadata.iso.ISOMetadata;
import org.apache.sis.internal.metadata.ReferencingServices;


/**
 * Vertical domain of dataset.
 *
 * <p>In addition to the standard properties, SIS provides the following methods:</p>
 * <ul>
 *   <li>{@link #setBounds(Envelope)} for setting the extent from the given envelope.</li>
 * </ul>
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @author  Touraïvane (IRD)
 * @author  Cédric Briançon (Geomatys)
 * @since   0.3 (derived from geotk-2.1)
 * @version 0.3
 * @module
 */
@XmlType(name = "EX_VerticalExtent_Type", propOrder = {
    "minimumValue",
    "maximumValue",
    "verticalCRS"
})
@XmlRootElement(name = "EX_VerticalExtent")
public class DefaultVerticalExtent extends ISOMetadata implements VerticalExtent {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -3214554246909844079L;

    /**
     * The lowest vertical extent contained in the dataset.
     */
    private Double minimumValue;

    /**
     * The highest vertical extent contained in the dataset.
     */
    private Double maximumValue;

    /**
     * Provides information about the vertical coordinate reference system to
     * which the maximum and minimum elevation values are measured. The CRS
     * identification includes unit of measure.
     */
    private VerticalCRS verticalCRS;

    /**
     * Constructs an initially empty vertical extent.
     */
    public DefaultVerticalExtent() {
    }

    /**
     * Creates a vertical extent initialized to the specified values.
     *
     * @param minimumValue The lowest vertical extent contained in the dataset.
     * @param maximumValue The highest vertical extent contained in the dataset.
     * @param verticalCRS  The information about the vertical coordinate reference system, or {@code null}.
     */
    public DefaultVerticalExtent(final double minimumValue,
                                 final double maximumValue,
                                 final VerticalCRS verticalCRS)
    {
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.verticalCRS  = verticalCRS;
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
    public static DefaultVerticalExtent castOrCopy(final VerticalExtent object) {
        if (object == null || object instanceof DefaultVerticalExtent) {
            return (DefaultVerticalExtent) object;
        }
        final DefaultVerticalExtent copy = new DefaultVerticalExtent();
        copy.shallowCopy(object);
        return copy;
    }

    /**
     * Returns the lowest vertical extent contained in the dataset.
     */
    @Override
    @XmlElement(name = "minimumValue", required = true)
    public synchronized Double getMinimumValue() {
        return minimumValue;
    }

    /**
     * Sets the lowest vertical extent contained in the dataset.
     *
     * @param newValue The new minimum value.
     */
    public synchronized void setMinimumValue(final Double newValue) {
        checkWritePermission();
        minimumValue = newValue;
    }

    /**
     * Returns the highest vertical extent contained in the dataset.
     */
    @Override
    @XmlElement(name = "maximumValue", required = true)
    public synchronized Double getMaximumValue() {
        return maximumValue;
    }

    /**
     * Sets the highest vertical extent contained in the dataset.
     *
     * @param newValue The new maximum value.
     */
    public synchronized void setMaximumValue(final Double newValue) {
        checkWritePermission();
        maximumValue = newValue;
    }

    /**
     * Provides information about the vertical coordinate reference system to
     * which the maximum and minimum elevation values are measured. The CRS
     * identification includes unit of measure.
     */
    @Override
    @XmlElement(name = "verticalCRS", required = true)
    public synchronized VerticalCRS getVerticalCRS() {
        return verticalCRS;
    }

    /**
     * Sets the information about the vertical coordinate reference system to
     * which the maximum and minimum elevation values are measured.
     *
     * @param newValue The new vertical CRS.
     */
    public synchronized void setVerticalCRS(final VerticalCRS newValue) {
        checkWritePermission();
        verticalCRS = newValue;
    }

    /**
     * Sets this vertical extent to values inferred from the specified envelope. The envelope can
     * be multi-dimensional, in which case the {@linkplain Envelope#getCoordinateReferenceSystem()
     * envelope CRS} must have a vertical component.
     *
     * <p><b>Note:</b> This method is available only if the referencing module is on the classpath.</p>
     *
     * @param  envelope The envelope to use for setting this vertical extent.
     * @throws UnsupportedOperationException if the referencing module is not on the classpath.
     * @throws TransformException if the envelope can't be transformed to a vertical extent.
     *
     * @see DefaultExtent#addElements(Envelope)
     * @see DefaultGeographicBoundingBox#setBounds(Envelope)
     * @see DefaultTemporalExtent#setBounds(Envelope)
     */
    public synchronized void setBounds(final Envelope envelope) throws TransformException {
        checkWritePermission();
        ReferencingServices.getInstance().setBounds(envelope, this);
    }
}