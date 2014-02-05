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
package org.apache.sis.referencing.crs;

import org.opengis.test.Validators;
import org.opengis.referencing.cs.CoordinateSystem;
import org.opengis.referencing.cs.CoordinateSystemAxis;
import org.apache.sis.referencing.cs.DefaultCompoundCS;
import org.apache.sis.referencing.cs.AxesConvention;
import org.apache.sis.referencing.cs.HardCodedAxes;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.TestCase;
import org.junit.Test;

import static java.util.Collections.singletonMap;
import static org.opengis.referencing.cs.CoordinateSystem.NAME_KEY;
import static org.apache.sis.test.Assert.*;


/**
 * Tests the {@link DefaultCompoundCRS} class.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.4
 * @version 0.4
 * @module
 */
@DependsOn({
    SubTypesTest.class,
    DefaultGeographicCRSTest.class
})
public final strictfp class DefaultCompoundCRSTest extends TestCase {
    /**
     * The vertical CRS arbitrarily chosen in this class for the tests.
     */
    private static final DefaultVerticalCRS HEIGHT = HardCodedCRS.GRAVITY_RELATED_HEIGHT;

    /**
     * The temporal CRS arbitrarily chosen in this class for the tests.
     */
    private static final DefaultTemporalCRS TIME = HardCodedCRS.TIME;

    /**
     * Tolerance threshold for strict floating point comparisons.
     */
    private static final double STRICT = 0;

    /**
     * Tests construction and serialization of a {@link DefaultCompoundCRS}.
     */
    @Test
    public void testConstructionAndSerialization() {
        final DefaultGeographicCRS crs2 = HardCodedCRS.WGS84;
        final DefaultCompoundCRS   crs3 = new DefaultCompoundCRS(singletonMap(NAME_KEY, "3D"), crs2, HEIGHT);
        final DefaultCompoundCRS   crs4 = new DefaultCompoundCRS(singletonMap(NAME_KEY, "4D"), crs3, TIME);
        Validators.validate(crs4);
        /*
         * Verifies the coordinate system axes.
         */
        final CoordinateSystem cs = crs4.getCoordinateSystem();
        assertInstanceOf("coordinateSystem", DefaultCompoundCS.class, cs);
        assertEquals("dimension", 4, cs.getDimension());
        assertSame(HardCodedAxes.GEODETIC_LONGITUDE,     cs.getAxis(0));
        assertSame(HardCodedAxes.GEODETIC_LATITUDE,      cs.getAxis(1));
        assertSame(HardCodedAxes.GRAVITY_RELATED_HEIGHT, cs.getAxis(2));
        assertSame(HardCodedAxes.TIME,                   cs.getAxis(3));
        /*
         * Verifies the list of components, including after serialization
         * since readObject(ObjectInputStream) is expected to recreate it.
         */
        verifyComponents(crs2, crs3, crs4);
        verifyComponents(crs2, crs3, assertSerializedEquals(crs4));
    }

    /**
     * Verifies the components of the CRS created by {@link #testConstructionAndSerialization()}.
     *
     * @param crs2 The expected two-dimensional component (for the 2 first axes).
     * @param crs3 The expected three-dimensional component.
     * @param crs4 The four-dimensional compound CRS to test.
     */
    private static void verifyComponents(final DefaultGeographicCRS crs2,
                                         final DefaultCompoundCRS   crs3,
                                         final DefaultCompoundCRS   crs4)
    {
        assertArrayEquals(new AbstractCRS[] {crs3, TIME},         crs4.getComponents().toArray());
        assertArrayEquals(new AbstractCRS[] {crs2, HEIGHT, TIME}, crs4.getSingleComponents().toArray());
    }

    /**
     * Tests {@link DefaultCompoundCRS#forConvention(AxesConvention)} with {@link AxesConvention#RIGHT_HANDED}.
     */
    @Test
    public void testNormalization() {
        final DefaultGeographicCRS crs2 = HardCodedCRS.WGS84_φλ;
        final DefaultGeographicCRS rh2  = crs2.forConvention(AxesConvention.RIGHT_HANDED);
        final DefaultCompoundCRS   crs3 = new DefaultCompoundCRS(singletonMap(NAME_KEY, "3D"), crs2, HEIGHT);
        final DefaultCompoundCRS   crs4 = new DefaultCompoundCRS(singletonMap(NAME_KEY, "4D"), crs3, TIME);
        final DefaultCompoundCRS   rh4  = crs4.forConvention(AxesConvention.RIGHT_HANDED);
        assertNotSame(crs4, rh4);
        Validators.validate(rh4);
        verifyComponents(crs2, crs3, crs4);
        verifyComponents(rh2, new DefaultCompoundCRS(singletonMap(NAME_KEY, "3D"), rh2, HEIGHT), rh4);
    }

    /**
     * Tests {@link DefaultCompoundCRS#forConvention(AxesConvention)} with {@link AxesConvention#POSITIVE_RANGE}.
     */
    @Test
    public void testShiftLongitudeRange() {
        final DefaultGeographicCRS crs3 = HardCodedCRS.WGS84_3D;
        final DefaultCompoundCRS   crs4 = new DefaultCompoundCRS(singletonMap(NAME_KEY, "4D"), crs3, TIME);
        CoordinateSystemAxis axis = crs4.getCoordinateSystem().getAxis(0);
        assertEquals("longitude.minimumValue", -180.0, axis.getMinimumValue(), STRICT);
        assertEquals("longitude.maximumValue", +180.0, axis.getMaximumValue(), STRICT);

        assertSame("Expected a no-op.", crs4, crs4.forConvention(AxesConvention.RIGHT_HANDED));
        final DefaultCompoundCRS shifted =   crs4.forConvention(AxesConvention.POSITIVE_RANGE);
        assertNotSame("Expected a new CRS.", crs4, shifted);
        Validators.validate(shifted);

        axis = shifted.getCoordinateSystem().getAxis(0);
        assertEquals("longitude.minimumValue",      0.0, axis.getMinimumValue(), STRICT);
        assertEquals("longitude.maximumValue",    360.0, axis.getMaximumValue(), STRICT);
        assertSame("Expected a no-op.",         shifted, shifted.forConvention(AxesConvention.POSITIVE_RANGE));
        assertSame("Expected cached instance.", shifted, crs4   .forConvention(AxesConvention.POSITIVE_RANGE));
    }
}
