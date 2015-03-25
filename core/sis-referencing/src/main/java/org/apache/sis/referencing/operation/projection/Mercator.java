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
package org.apache.sis.referencing.operation.projection;

import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.OperationMethod;
import org.opengis.referencing.operation.TransformException;
import org.apache.sis.internal.referencing.provider.Mercator1SP;
import org.apache.sis.internal.referencing.provider.Mercator2SP;
import org.apache.sis.internal.referencing.provider.PseudoMercator;
import org.apache.sis.internal.referencing.provider.MillerCylindrical;
import org.apache.sis.referencing.operation.matrix.Matrix2;
import org.apache.sis.referencing.IdentifiedObjects;
import org.apache.sis.parameter.Parameters;

import static java.lang.Math.*;
import static java.lang.Double.*;


/**
 * <cite>Mercator Cylindrical</cite> projection (EPSG codes 9804, 9805, 1026, 1024, <span class="deprecated">9841</span>).
 * See the <a href="http://mathworld.wolfram.com/MercatorProjection.html">Mercator projection on MathWorld</a> for an overview.
 *
 * <div class="section">Description</div>
 * The parallels and the meridians are straight lines and cross at right angles; this projection thus produces
 * rectangular charts. The scale is true along the equator (by default) or along two parallels equidistant of the
 * equator (if a scale factor other than 1 is used).
 *
 * <p>This projection is used to represent areas close to the equator. It is also often used for maritime navigation
 * because all the straight lines on the chart are <cite>loxodrome</cite> lines, i.e. a ship following this line would
 * keep a constant azimuth on its compass.</p>
 *
 * <p>This implementation handles both the 1 and 2 standard parallel cases.
 * For <cite>Mercator (variant A)</cite> (EPSG code 9804), the line of contact is the equator.
 * For <cite>Mercator (variant B)</cite> (EPSG code 9805) lines of contact are symmetrical about the equator.</p>
 *
 * <div class="section">Behavior at poles</div>
 * The projection of 90°N gives {@linkplain Double#POSITIVE_INFINITY positive infinity}.
 * The projection of 90°S gives {@linkplain Double#NEGATIVE_INFINITY negative infinity}.
 * Projection of a latitude outside the [-90-ε … 90+ε]° range produces {@linkplain Double#NaN NaN}.
 *
 * @author  André Gosselin (MPO)
 * @author  Martin Desruisseaux (MPO, IRD, Geomatys)
 * @author  Rueben Schulz (UBC)
 * @author  Simon Reynard (Geomatys)
 * @author  Rémi Maréchal (Geomatys)
 * @since   0.6
 * @version 0.6
 * @module
 *
 * @see TransverseMercator
 * @see ObliqueMercator
 */
public class Mercator extends UnitaryProjection {
    /**
     * For cross-version compatibility.
     */
    private static final long serialVersionUID = 2564172914329253286L;

    /**
     * Creates a Mercator projection from the given parameters.
     * The {@code method} argument can be the description of one of the following:
     *
     * <ul>
     *   <li><cite>Mercator (variant A)</cite>, also known as <cite>Mercator (1SP)</cite>.</li>
     *   <li><cite>Mercator (variant B)</cite>, also known as <cite>Mercator (2SP)</cite>.</li>
     * </ul>
     *
     * @param method Description of the projection parameters.
     * @param values The parameter values of the projection to create.
     */
    protected Mercator(final OperationMethod method, final Parameters values) {
        super(method, values);
    }

    /**
     * Returns the parameter descriptors for this unitary projection. Note that the returned descriptor is about
     * the unitary projection, not the full one. Consequently the default implementation returns the descriptor
     * of <cite>Mercator (variant A)</cite> in all cases except for the pseudo-Mercator projection, because the
     * <cite>Mercator (variant B)</cite> case is implemented as the variant A with a different scale factor.
     *
     * @return The <cite>Mercator (variant A)</cite> or <cite>Popular Visualisation Pseudo Mercator</cite>
     *         parameter descriptor.
     */
    @Override
    public ParameterDescriptorGroup getParameterDescriptors() {
        return Mercator1SP.PARAMETERS;
    }

    // No need to override getParameterValues() because no additional
    // parameter are significant to a unitary Mercator projection.


    /**
     * Converts the specified (<var>λ</var>,<var>φ</var>) coordinate (units in radians)
     * and stores the result in {@code dstPts} (linear distance on a unit sphere). In addition,
     * opportunistically computes the projection derivative if {@code derivate} is {@code true}.
     *
     * @return The matrix of the projection derivative at the given source position,
     *         or {@code null} if the {@code derivate} argument is {@code false}.
     * @throws ProjectionException if the coordinate can not be converted.
     */
    @Override
    public Matrix transform(final double[] srcPts, final int srcOff,
                            final double[] dstPts, final int dstOff,
                            final boolean derivate) throws ProjectionException
    {
        final double λ     = srcPts[srcOff];
        final double φ     = srcPts[srcOff + 1];
        final double sinφ  = sin(φ);
        final double ℯsinφ = excentricity * sinφ;
        /*
         * Projection of zero is zero. However the formulas below have a slight rounding error
         * which produce values close to 1E-10, so we will avoid them when y=0. In addition of
         * avoiding rounding error, this also preserve the sign (positive vs negative zero).
         */
        final double y;
        if (sinφ == 0) {
            y = φ;
        } else {
            // See the javadoc of the Spherical inner class for a note
            // about why we perform explicit checks for the pole cases.
            final double absφ = abs(φ);
            if (absφ < PI/2) {
                y = log(exp_y(φ, ℯsinφ));     // Snyder (7-7)
            } else {
                y = copySign(absφ <= (PI/2 + ANGLE_TOLERANCE) ? POSITIVE_INFINITY : NaN, φ);
            }
        }
        if (dstPts != null) {
            dstPts[dstOff]   = λ;
            dstPts[dstOff+1] = y;
        }
        if (!derivate) {
            return null;
        }
        /*
         * End of map projection. Now compute the derivative, if requested.
         */
        final double cosφ = cos(φ);
        final double t = (1 - sinφ) / cosφ;
        return new Matrix2(1, 0, 0, 0.5*(t + 1/t) - excentricitySquared*cosφ / (1 - ℯsinφ*ℯsinφ));
    }

    /**
     * Converts a list of coordinate point ordinal values.
     *
     * <div class="note"><b>Note:</b>
     * We override the super-class method only as an optimization in the special case where the target coordinates
     * are written at the same locations than the source coordinates. In such case, we can take advantage of the
     * fact that the λ values are not modified by the unitary Mercator projection.</div>
     *
     * @throws TransformException if a point can not be transformed.
     */
    @Override
    public void transform(final double[] srcPts, int srcOff,
                          final double[] dstPts, int dstOff, int numPts)
            throws TransformException
    {
        if (srcPts != dstPts || srcOff != dstOff) {
            super.transform(srcPts, srcOff, dstPts, dstOff, numPts);
        } else {
            dstOff--;
            while (--numPts >= 0) {
                final double φ = dstPts[dstOff += 2]; // Same as srcPts[srcOff + 1].
                final double y;
                if (φ != 0) {
                    // See the javadoc of the Spherical inner class for a note
                    // about why we perform explicit checks for the pole cases.
                    final double absφ = abs(φ);
                    if (absφ < PI/2) {
                        y = log(exp_y(φ, excentricity * sin(φ)));
                    } else {
                        y = copySign(absφ <= (PI/2 + ANGLE_TOLERANCE) ? POSITIVE_INFINITY : NaN, φ);
                    }
                    dstPts[dstOff] = y;
                }
            }
        }
    }

    /**
     * Transforms the specified (<var>x</var>,<var>y</var>) coordinates
     * and stores the result in {@code dstPts} (angles in radians).
     *
     * @throws ProjectionException if the point can not be converted.
     */
    @Override
    protected void inverseTransform(final double[] srcPts, final int srcOff,
                                    final double[] dstPts, final int dstOff)
            throws ProjectionException
    {
        final double y   = srcPts[srcOff+1];    // Must be before writing x.
        dstPts[dstOff  ] = srcPts[srcOff  ];    // Must be before writing y.
        dstPts[dstOff+1] = φ(exp(-y));
    }


    /**
     * Provides the transform equations for the spherical case of the Mercator projection.
     *
     * <div class="note"><b>Implementation note:</b>
     * this class contains explicit checks for latitude values at poles. If floating point arithmetic had infinite
     * precision, those checks would not be necessary since the formulas lead naturally to infinite values at poles,
     * which is the correct answer. In practice the infinite value emerges by itself at only one pole, and the other
     * one produces a high value (approximatively 1E+16). This is because there is no accurate representation of π/2
     * in base 2, and consequently {@code tan(π/2)} does not returns the infinite value.
     * </div>
     *
     * @author Martin Desruisseaux (MPO, IRD, Geomatys)
     * @author Rueben Schulz (UBC)
     * @module
     */
    static final class Spherical extends Mercator {
        /**
         * For cross-version compatibility.
         */
        private static final long serialVersionUID = 2383414176395616561L;

        /**
         * {@code true} if we are in the "Pseudo Mercator" case.
         */
        private final boolean pseudo;

        /**
         * Constructs a new map projection from the supplied parameters.
         *
         * @param method Description of the projection parameters.
         * @param values The parameter values of the projection to create.
         * @param pseudo {@code true} if we are in the "Pseudo Mercator" case.
         */
        Spherical(final OperationMethod method, final Parameters values, final boolean pseudo) {
            super(method, values);
            this.pseudo = pseudo;
            if (!pseudo) {
                ensureSpherical();
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public ParameterDescriptorGroup getParameterDescriptors() {
            return pseudo ? PseudoMercator.PARAMETERS : super.getParameterDescriptors();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Matrix transform(final double[] srcPts, final int srcOff,
                                final double[] dstPts, final int dstOff,
                                final boolean derivate) throws ProjectionException
        {
            final double λ = srcPts[srcOff  ];
            final double φ = srcPts[srcOff+1];
            /*
             * Projection of zero is zero. However the formulas below have a slight rounding error
             * which produce values close to 1E-10, so we will avoid them when y=0. In addition of
             * avoiding rounding error, this also preserve the sign (positive vs negative zero).
             */
            final double y;
            if (φ == 0) {
                y = φ;
            } else {
                // See class javadoc for a note about explicit check for poles.
                final double a = abs(φ);
                if (a < PI/2) {
                    y = log(tan(PI/4 + 0.5*φ));     // Snyder (7-2)
                } else {
                    y = copySign(a <= (PI/2 + ANGLE_TOLERANCE) ? POSITIVE_INFINITY : NaN, φ);
                }
            }
            Matrix derivative = null;
            if (derivate) {
                derivative = new Matrix2(1, 0, 0, 1/cos(φ));
            }
            /*
             * Following part is common to all spherical projections: verify, store and return.
             */
            assert pseudo || (Assertions.checkDerivative(derivative, super.transform(srcPts, srcOff, dstPts, dstOff, derivate))
                    && Assertions.checkTransform(dstPts, dstOff, λ, y)); // dstPts = result from ellipsoidal formulas.
            if (dstPts != null) {
                dstPts[dstOff  ] = λ;
                dstPts[dstOff+1] = y;
            }
            return derivative;
        }

        /**
         * {@inheritDoc}
         *
         * <div class="note"><b>Note:</b>
         * This method must be overridden because the {@link Mercator} class overrides the {@link UnitaryProjection}
         * default implementation.</div>
         */
        @Override
        public void transform(final double[] srcPts, int srcOff,
                              final double[] dstPts, int dstOff, int numPts)
                throws TransformException
        {
            if (srcPts != dstPts || srcOff != dstOff) {
                super.transform(srcPts, srcOff, dstPts, dstOff, numPts);
            } else {
                dstOff--;
                while (--numPts >= 0) {
                    double y = dstPts[dstOff += 2];     // Same as srcPts[srcOff …].
                    if (y != 0) {
                        final double a = abs(y);
                        if (a < PI/2) {
                            y = log(tan(PI/4 + 0.5*y));     // Snyder (7-2)
                        } else {
                            y = copySign(a <= (PI/2 + ANGLE_TOLERANCE) ? POSITIVE_INFINITY : NaN, y);
                        }
                        dstPts[dstOff] = y;
                    }
                }
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void inverseTransform(final double[] srcPts, final int srcOff,
                                        final double[] dstPts, final int dstOff)
                throws ProjectionException
        {
            double x = srcPts[srcOff  ];
            double y = srcPts[srcOff+1];
            y = PI/2 - 2 * atan(exp(-y));     // Snyder (7-4)
            assert pseudo || checkInverseTransform(srcPts, srcOff, dstPts, dstOff, x, y);
            dstPts[dstOff  ] = x;
            dstPts[dstOff+1] = y;
        }

        /**
         * Computes using ellipsoidal formulas and compare with the
         * result from spherical formulas. Used in assertions only.
         */
        private boolean checkInverseTransform(final double[] srcPts, final int srcOff,
                                              final double[] dstPts, final int dstOff,
                                              final double λ, final double φ)
                throws ProjectionException
        {
            super.inverseTransform(srcPts, srcOff, dstPts, dstOff);
            return Assertions.checkInverseTransform(dstPts, dstOff, λ, φ);
        }
    }
}
