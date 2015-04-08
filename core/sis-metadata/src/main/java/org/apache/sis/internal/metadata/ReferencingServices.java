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
package org.apache.sis.internal.metadata;

import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.referencing.IdentifiedObject;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.apache.sis.metadata.iso.extent.DefaultExtent;
import org.apache.sis.metadata.iso.extent.DefaultVerticalExtent;
import org.apache.sis.metadata.iso.extent.DefaultTemporalExtent;
import org.apache.sis.metadata.iso.extent.DefaultGeographicBoundingBox;
import org.apache.sis.metadata.iso.extent.DefaultSpatialTemporalExtent;
import org.apache.sis.internal.system.SystemListener;
import org.apache.sis.internal.system.Modules;
import org.apache.sis.io.wkt.FormattableObject;
import org.apache.sis.util.resources.Errors;


/**
 * Provides access to services defined in the {@code "sis-referencing"} module.
 * This class searches for the {@link org.apache.sis.internal.referencing.ServicesForMetadata}
 * implementation using Java reflection.
 *
 * <p>This class also opportunistically defines some constants related to "referencing by coordinates"
 * but needed by metadata.</p>
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.3
 * @version 0.6
 * @module
 */
public abstract class ReferencingServices extends SystemListener {
    /**
     * The length of one nautical mile, which is {@value} metres.
     */
    public static final double NAUTICAL_MILE = 1852;

    /**
     * The GRS80 {@linkplain org.apache.sis.referencing.datum.DefaultEllipsoid#getAuthalicRadius() authalic radius},
     * which is {@value} metres.
     */
    public static final double AUTHALIC_RADIUS = 6371007;

    /**
     * The services, fetched when first needed.
     */
    private static volatile ReferencingServices instance;

    /**
     * For subclass only. This constructor registers this instance as a {@link SystemListener}
     * in order to force a new {@code ReferencingServices} lookup if the classpath changes.
     */
    protected ReferencingServices() {
        super(Modules.METADATA);
        SystemListener.add(this);
    }

    /**
     * Invoked when the classpath changed. Resets the {@link #instance} to {@code null}
     * in order to force the next call to {@link #getInstance()} to fetch a new one,
     * which may be different.
     */
    @Override
    protected final void classpathChanged() {
        synchronized (ReferencingServices.class) {
            instance = null;
        }
    }

    /**
     * Returns the singleton instance.
     *
     * @return The singleton instance.
     * @throws UnsupportedOperationException If the {@code "sis-referencing"} module has not
     *         been found on the classpath.
     */
    public static ReferencingServices getInstance() throws UnsupportedOperationException {
        ReferencingServices c = instance;
        if (c == null) {
            synchronized (ReferencingServices.class) {
                c = instance;
                if (c == null) try {
                    instance = c = (ReferencingServices) Class.forName("org.apache.sis.internal.referencing.ServicesForMetadata").newInstance();
                } catch (ClassNotFoundException exception) {
                    throw new UnsupportedOperationException(Errors.format(
                            Errors.Keys.MissingRequiredModule_1, "sis-referencing"), exception);
                } catch (ReflectiveOperationException exception) {
                    // Should never happen if we didn't broke our helper class.
                    throw new AssertionError(exception);
                }
            }
        }
        return c;
    }

    /**
     * Returns a fully implemented parameter descriptor.
     *
     * @param  parameter A partially implemented parameter descriptor, or {@code null}.
     * @return A fully implemented parameter descriptor, or {@code null} if the given argument was null.
     *
     * @since 0.5
     */
    public abstract ParameterDescriptor<?> toImplementation(ParameterDescriptor<?> parameter);

    /**
     * Converts the given object in a {@code FormattableObject} instance.
     *
     * @param  object The object to wrap.
     * @return The given object converted to a {@code FormattableObject} instance.
     *
     * @see org.apache.sis.referencing.AbstractIdentifiedObject#castOrCopy(IdentifiedObject)
     *
     * @since 0.4
     */
    public abstract FormattableObject toFormattableObject(IdentifiedObject object);

    /**
     * Converts the given object in a {@code FormattableObject} instance. Callers should verify that the given
     * object is not already an instance of {@code FormattableObject} before to invoke this method. This method
     * returns {@code null} if it can not convert the object.
     *
     * @param  object The object to wrap.
     * @return The given object converted to a {@code FormattableObject} instance, or {@code null}.
     *
     * @since 0.6
     */
    public abstract FormattableObject toFormattableObject(MathTransform object);

    /**
     * Sets a geographic bounding box from the specified envelope.
     * If the envelope contains a CRS which is not geographic, then the bounding box will be transformed
     * to a geographic CRS (without datum shift if possible). Otherwise, the envelope is assumed already
     * in a geographic CRS using (<var>longitude</var>, <var>latitude</var>) axis order.
     *
     * @param  envelope The source envelope.
     * @param  target The target bounding box.
     * @throws TransformException If the given envelope can't be transformed.
     */
    public abstract void setBounds(Envelope envelope, DefaultGeographicBoundingBox target)
            throws TransformException;

    /**
     * Sets a vertical extent with the value inferred from the given envelope.
     * Only the vertical ordinates are extracted; all other ordinates are ignored.
     *
     * @param  envelope The source envelope.
     * @param  target The target vertical extent.
     * @throws TransformException If no vertical component can be extracted from the given envelope.
     */
    public abstract void setBounds(Envelope envelope, DefaultVerticalExtent target)
            throws TransformException;

    /**
     * Sets a temporal extent with the value inferred from the given envelope.
     * Only the temporal ordinates are extracted; all other ordinates are ignored.
     *
     * @param  envelope The source envelope.
     * @param  target The target temporal extent.
     * @throws TransformException If no temporal component can be extracted from the given envelope.
     */
    public abstract void setBounds(Envelope envelope, DefaultTemporalExtent target)
            throws TransformException;

    /**
     * Sets the geographic, vertical and temporal extents with the values inferred from the given envelope.
     * If the given {@code target} has more geographic or vertical extents than needed (0 or 1), then the
     * extraneous extents are removed.
     *
     * <p>Behavior regarding missing dimensions:</p>
     * <ul>
     *   <li>If the given envelope has no horizontal component, then all geographic extents are removed
     *       from the given {@code target}. Non-geographic extents (e.g. descriptions and polygons) are
     *       left unchanged.</li>
     *   <li>If the given envelope has no vertical component, then the vertical extent is set to {@code null}.</li>
     *   <li>If the given envelope has no temporal component, then the temporal extent is set to {@code null}.</li>
     * </ul>
     *
     * @param  envelope The source envelope.
     * @param  target The target spatio-temporal extent.
     * @throws TransformException If no temporal component can be extracted from the given envelope.
     */
    public abstract void setBounds(Envelope envelope, DefaultSpatialTemporalExtent target)
            throws TransformException;

    /**
     * Initializes a horizontal, vertical and temporal extent with the values inferred from
     * the given envelope.
     *
     * @param  envelope The source envelope.
     * @param  target The target extent.
     * @throws TransformException If a coordinate transformation was required and failed.
     */
    public abstract void addElements(Envelope envelope, DefaultExtent target) throws TransformException;
}
