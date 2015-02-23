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
package org.apache.sis.referencing.operation.transform;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.IdentityHashMap;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;
import org.opengis.util.NoSuchIdentifierException;
import org.opengis.referencing.operation.Conversion;
import org.opengis.referencing.operation.Projection;
import org.opengis.referencing.operation.SingleOperation;
import org.opengis.referencing.operation.OperationMethod;
import org.apache.sis.internal.referencing.provider.Affine;
import org.apache.sis.internal.util.Constants;
import org.apache.sis.test.DependsOnMethod;
import org.apache.sis.test.DependsOn;
import org.apache.sis.test.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.opengis.test.Assert.*;


/**
 * Tests the registration of operation methods in {@link DefaultMathTransformFactory}. This test uses the
 * providers registered in all {@code META-INF/services/org.opengis.referencing.operation.OperationMethod}
 * files found on the classpath.
 *
 * @author  Martin Desruisseaux (Geomatys)
 * @since   0.6
 * @version 0.6
 * @module
 */
@DependsOn({
    org.apache.sis.referencing.operation.DefaultOperationMethodTest.class,
    OperationMethodSetTest.class
})
public final strictfp class DefaultMathTransformFactoryTest extends TestCase {
    /**
     * The factory being tested.
     */
    private static DefaultMathTransformFactory factory;

    /**
     * Creates the factory to be tested.
     */
    @BeforeClass
    public static void createFactory() {
        factory = new DefaultMathTransformFactory();
    }

    /**
     * Releases the factory used for the tests.
     */
    @AfterClass
    public static void releaseFactory() {
        factory = null;
    }

    /**
     * Tests the {@link DefaultMathTransformFactory#getOperationMethod(String)} method.
     *
     * @throws NoSuchIdentifierException Should never happen.
     */
    @Test
    public void testGetOperationMethod() throws NoSuchIdentifierException {
        // A conversion which is not a projection.
        OperationMethod method = factory.getOperationMethod(Constants.AFFINE);
        assertInstanceOf("Affine", Affine.class, method);

        // Same than above, using EPSG code.
        assertSame("EPSG:9624", method, factory.getOperationMethod("EPSG:9624"));
    }

    /**
     * Tests non-existent operation method.
     */
    @Test
    @DependsOnMethod("testGetOperationMethod")
    public void testNonExistentCode() {
        try {
            factory.getOperationMethod("EPXX:9624");
            fail("Expected NoSuchIdentifierException");
        } catch (NoSuchIdentifierException e) {
            final String message = e.getLocalizedMessage();
            assertTrue(message, message.contains("EPXX:9624"));
        }
    }

    /**
     * Tests the {@link DefaultMathTransformFactory#getAvailableMethods(Class)} method.
     *
     * @throws NoSuchIdentifierException Should never happen.
     */
    @Test
    @DependsOnMethod("testGetOperationMethod")
    public void testGetAvailableMethods() throws NoSuchIdentifierException {
        final Set<OperationMethod> transforms  = factory.getAvailableMethods(SingleOperation.class);
        final Set<OperationMethod> conversions = factory.getAvailableMethods(Conversion.class);
        final Set<OperationMethod> projections = factory.getAvailableMethods(Projection.class);
        /*
         * Following tests should not cause loading of more classes than needed.
         */
        assertFalse(transforms .isEmpty());
        assertFalse(conversions.isEmpty());
        assertTrue (projections.isEmpty());
        assertTrue (conversions.contains(factory.getOperationMethod(Constants.AFFINE)));
        /*
         * Following tests will force instantiation of all remaining OperationMethod.
         */
        assertTrue("Conversions should be a subset of transforms.",  transforms .containsAll(conversions));
        assertTrue("Projections should be a subset of conversions.", conversions.containsAll(projections));
    }

    /**
     * Ensures that every parameter instance is unique. Actually this test is not strong requirement.
     * This is only for sharing existing resources by avoiding unnecessary objects duplication.
     */
    @Test
    @DependsOnMethod("testGetAvailableMethods")
    public void ensureParameterUniqueness() {
        final Map<GeneralParameterDescriptor, String> groupNames = new IdentityHashMap<>();
        final Map<GeneralParameterDescriptor, GeneralParameterDescriptor> existings = new HashMap<>();
        for (final OperationMethod method : factory.getAvailableMethods(SingleOperation.class)) {
            final ParameterDescriptorGroup group = method.getParameters();
            final String name = group.getName().getCode();
            for (final GeneralParameterDescriptor param : group.descriptors()) {
                assertFalse("Parameter declared twice in the same group.", name.equals(groupNames.put(param, name)));
                final GeneralParameterDescriptor existing = existings.put(param, param);
                if (existing != null && existing != param) {
                    fail("Parameter “" + param.getName().getCode() + "” defined in “" + name + '”'
                       + " was already defined in “" + groupNames.get(existing) + "”."
                       + " The same instance could be shared.");
                }
            }
        }
    }
}
