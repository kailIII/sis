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

import java.util.Collections;
import org.opengis.parameter.ParameterDescriptor;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.datum.Ellipsoid;
import org.opengis.referencing.operation.Matrix;
import org.apache.sis.referencing.operation.DefaultOperationMethod;
import org.apache.sis.parameter.ParameterBuilder;
import org.apache.sis.parameter.Parameters;
import org.apache.sis.internal.util.Constants;
import org.apache.sis.test.mock.GeodeticDatumMock;
import org.apache.sis.util.Workaround;


/**
 * A simple implementation of {@link NormalizedProjection} as a "no-operation".
 * This is used for testing methods other than {@code transform(…)} and {@code inverseTransform(…)}.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.6
 * @version 0.6
 * @module
 */
@SuppressWarnings("serial")
final strictfp class NoOp extends NormalizedProjection {
    /**
     * Creates a new "no-operation".
     *
     * @param ellipsoidal {@code true} for an ellipsoidal case, or {@code false} for a spherical case.
     */
    NoOp(final boolean ellipsoidal) {
        this(parameters(ellipsoidal));
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    @Workaround(library="JDK", version="1.7")
    private NoOp(final Parameters parameters) {
        super(new DefaultOperationMethod(
                Collections.singletonMap(DefaultOperationMethod.NAME_KEY, parameters.getDescriptor().getName()),
                2, 2, parameters.getDescriptor()), parameters,
                Collections.<ParameterRole, ParameterDescriptor<Double>>emptyMap());
    }

    /**
     * Work around for RFE #4093999 in Sun's bug database
     * ("Relax constraint on placement of this()/super() call in constructors").
     */
    @Workaround(library="JDK", version="1.7")
    private static Parameters parameters(final boolean ellipse) {
        final ParameterValueGroup group = new ParameterBuilder()
                .addName("No-operation").createGroupForMapProjection().createValue();
        final Ellipsoid ellipsoid = (ellipse ? GeodeticDatumMock.WGS84 : GeodeticDatumMock.SPHERE).getEllipsoid();
        group.parameter(Constants.SEMI_MAJOR).setValue(ellipsoid.getSemiMajorAxis());
        group.parameter(Constants.SEMI_MINOR).setValue(ellipsoid.getSemiMinorAxis());
        return (Parameters) group;
    }

    /**
     * Do nothing.
     *
     * @return {@code null}.
     */
    @Override
    public Matrix transform(double[] srcPts, int srcOff, double[] dstPts, int dstOff, boolean derivate) {
        return null;
    }

    /**
     * Do nothing.
     */
    @Override
    protected void inverseTransform(double[] srcPts, int srcOff, double[] dstPts, int dstOff) {
    }
}
