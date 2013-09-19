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
package org.apache.sis.referencing.operation.matrix;

import org.opengis.geometry.Envelope;
import org.opengis.referencing.cs.AxisDirection;
import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.geometry.MismatchedDimensionException;
import org.apache.sis.util.Static;
import org.apache.sis.util.CharSequences;
import org.apache.sis.util.ComparisonMode;
import org.apache.sis.util.ArgumentChecks;
import org.apache.sis.util.resources.Errors;
import org.apache.sis.internal.util.Numerics;
import org.apache.sis.internal.referencing.AxisDirections;

// Related to JDK7
import java.util.Objects;


/**
 * {@link Matrix} factory methods and utilities.
 * This class provides the following methods:
 *
 * <ul>
 *   <li>Creating new matrices of arbitrary size:
 *       {@link #createIdentity createIdentity},
 *       {@link #createDiagonal createDiagonal},
 *       {@link #createZero     createZero},
 *       {@link #create         create}.
 *   </li>
 *   <li>Creating new matrices for coordinate operation steps:
 *       {@link #createTransform(Envelope, AxisDirection[], Envelope, AxisDirection[]) createTransform},
 *       {@link #createDimensionSelect createDimensionSelect},
 *       {@link #createPassThrough     createPassThrough}.
 *   </li>
 *   <li>Copies matrices to a SIS implementation:
 *       {@link #copy       copy},
 *       {@link #castOrCopy castOrCopy}.
 *   </li>
 *   <li>Information:
 *       {@link #isAffine   isAffine},
 *       {@link #isIdentity isIdentity},
 *       {@link #equals(Matrix, Matrix, double, boolean) equals},
 *       {@link #equals(Matrix, Matrix, ComparisonMode)  equals}.
 *   </li>
 * </ul>
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.4 (derived from geotk-2.2)
 * @version 0.4
 * @module
 */
public final class Matrices extends Static {
    /**
     * Number of spaces to put between columns formatted by {@link #toString(Matrix)}.
     */
    private static final int MARGIN = 2;

    /**
     * Do not allows instantiation of this class.
     */
    private Matrices() {
    }

    /**
     * Creates a square identity matrix of size {@code size} × {@code size}.
     * Elements on the diagonal (<var>j</var> == <var>i</var>) are set to 1.
     *
     * <blockquote><font size=-1><b>Implementation note:</b>
     * For sizes between {@value org.apache.sis.referencing.operation.matrix.Matrix1#SIZE} and
     * {@value org.apache.sis.referencing.operation.matrix.Matrix4#SIZE} inclusive, the matrix
     * is guaranteed to be an instance of one of {@link Matrix1} … {@link Matrix4} subtypes.
     * </font></blockquote>
     *
     * @param  size Numbers of row and columns. For an affine transform matrix, this is the number of
     *         {@linkplain MathTransform#getSourceDimensions() source} and
     *         {@linkplain MathTransform#getTargetDimensions() target} dimensions + 1.
     * @return An identity matrix of the given size.
     */
    public static MatrixSIS createIdentity(final int size) {
        switch (size) {
            case 1:  return new Matrix1();
            case 2:  return new Matrix2();
            case 3:  return new Matrix3();
            case 4:  return new Matrix4();
            default: return new GeneralMatrix(size, size, true);
        }
    }

    /**
     * Creates a matrix of size {@code numRow} × {@code numCol}.
     * Elements on the diagonal (<var>j</var> == <var>i</var>) are set to 1.
     * The result is an identity matrix if {@code numRow} = {@code numCol}.
     *
     * <blockquote><font size=-1><b>Implementation note:</b>
     * For {@code numRow} == {@code numCol} with a value between
     * {@value org.apache.sis.referencing.operation.matrix.Matrix1#SIZE} and
     * {@value org.apache.sis.referencing.operation.matrix.Matrix4#SIZE} inclusive, the matrix
     * is guaranteed to be an instance of one of {@link Matrix1} … {@link Matrix4} subtypes.
     * </font></blockquote>
     *
     * @param numRow For a math transform, this is the number of {@linkplain MathTransform#getTargetDimensions() target dimensions} + 1.
     * @param numCol For a math transform, this is the number of {@linkplain MathTransform#getSourceDimensions() source dimensions} + 1.
     * @return An identity matrix of the given size.
     */
    public static MatrixSIS createDiagonal(final int numRow, final int numCol) {
        if (numRow == numCol) {
            return createIdentity(numRow);
        } else {
            return new NonSquareMatrix(numRow, numCol, true);
        }
    }

    /**
     * Creates a matrix of size {@code numRow} × {@code numCol} filled with zero values.
     * This constructor is convenient when the caller want to initialize the matrix elements himself.
     *
     * <blockquote><font size=-1><b>Implementation note:</b>
     * For {@code numRow} == {@code numCol} with a value between
     * {@value org.apache.sis.referencing.operation.matrix.Matrix1#SIZE} and
     * {@value org.apache.sis.referencing.operation.matrix.Matrix4#SIZE} inclusive, the matrix
     * is guaranteed to be an instance of one of {@link Matrix1} … {@link Matrix4} subtypes.
     * </font></blockquote>
     *
     * @param numRow For a math transform, this is the number of {@linkplain MathTransform#getTargetDimensions() target dimensions} + 1.
     * @param numCol For a math transform, this is the number of {@linkplain MathTransform#getSourceDimensions() source dimensions} + 1.
     * @return A matrix of the given size with only zero values.
     */
    public static MatrixSIS createZero(final int numRow, final int numCol) {
        if (numRow == numCol) switch (numRow) {
            case 1:  return new Matrix1(false);
            case 2:  return new Matrix2(false);
            case 3:  return new Matrix3(false);
            case 4:  return new Matrix4(false);
            default: return new GeneralMatrix(numRow, numCol, false);
        }
        return new NonSquareMatrix(numRow, numCol, false);
    }

    /**
     * Creates a matrix of size {@code numRow} × {@code numCol} initialized to the given elements.
     * The elements array size must be equals to {@code numRow*numCol}. Column indices vary fastest.
     *
     * <blockquote><font size=-1><b>Implementation note:</b>
     * For {@code numRow} == {@code numCol} with a value between
     * {@value org.apache.sis.referencing.operation.matrix.Matrix1#SIZE} and
     * {@value org.apache.sis.referencing.operation.matrix.Matrix4#SIZE} inclusive, the matrix
     * is guaranteed to be an instance of one of {@link Matrix1} … {@link Matrix4} subtypes.
     * </font></blockquote>
     *
     * @param  numRow   Number of rows.
     * @param  numCol   Number of columns.
     * @param  elements The matrix elements in a row-major array. Column indices vary fastest.
     * @return A matrix initialized to the given elements.
     *
     * @see MatrixSIS#setElements(double[])
     */
    public static MatrixSIS create(final int numRow, final int numCol, final double[] elements) {
        if (numRow == numCol) switch (numRow) {
            case 1:  return new Matrix1(elements);
            case 2:  return new Matrix2(elements);
            case 3:  return new Matrix3(elements);
            case 4:  return new Matrix4(elements);
            default: return new GeneralMatrix(numRow, numCol, elements);
        }
        return new NonSquareMatrix(numRow, numCol, elements);
    }

    /**
     * Implementation of {@code createTransform(…)} public methods expecting envelopes and/or axis directions.
     *
     * @param useEnvelopes {@code true} if source and destination envelopes shall be taken in account.
     *        If {@code false}, then source and destination envelopes will be ignored and may be null.
     */
    private static MatrixSIS createTransform(final Envelope srcEnvelope, final AxisDirection[] srcAxes,
                                             final Envelope dstEnvelope, final AxisDirection[] dstAxes,
                                             final boolean useEnvelopes)
    {
        final MatrixSIS matrix = createZero(dstAxes.length+1, srcAxes.length+1);
        /*
         * Maps source axes to destination axes. If no axis is moved (for example if the user
         * want to transform (NORTH,EAST) to (SOUTH,EAST)), then source and destination index
         * will be equal.   If some axes are moved (for example if the user want to transform
         * (NORTH,EAST) to (EAST,NORTH)), then ordinates at index {@code srcIndex} will have
         * to be moved at index {@code dstIndex}.
         */
        for (int dstIndex = 0; dstIndex < dstAxes.length; dstIndex++) {
            boolean hasFound = false;
            final AxisDirection dstDir = dstAxes[dstIndex];
            final AxisDirection search = AxisDirections.absolute(dstDir);
            for (int srcIndex = 0; srcIndex < srcAxes.length; srcIndex++) {
                final AxisDirection srcDir = srcAxes[srcIndex];
                if (search.equals(AxisDirections.absolute(srcDir))) {
                    if (hasFound) {
                        throw new IllegalArgumentException(Errors.format(
                                Errors.Keys.ColinearAxisDirections_2, srcDir, dstDir));
                    }
                    hasFound = true;
                    /*
                     * Set the matrix elements. Some matrix elements will never be set.
                     * They will be left to zero, which is their desired value.
                     */
                    final boolean same = srcDir.equals(dstDir);
                    double scale = same ? +1 : -1;
                    double translate = 0;
                    if (useEnvelopes) {
                        translate  = same ? dstEnvelope.getMinimum(dstIndex)
                                          : dstEnvelope.getMaximum(dstIndex);
                        scale     *= dstEnvelope.getSpan(dstIndex) /
                                     srcEnvelope.getSpan(srcIndex);
                        translate -= srcEnvelope.getMinimum(srcIndex) * scale;
                    }
                    matrix.setElement(dstIndex, srcIndex,       scale);
                    matrix.setElement(dstIndex, srcAxes.length, translate);
                }
            }
            if (!hasFound) {
                throw new IllegalArgumentException(Errors.format(
                        Errors.Keys.CanNotMapAxisToDirection_2, "srcAxes", dstAxes[dstIndex]));
            }
        }
        matrix.setElement(dstAxes.length, srcAxes.length, 1);
        return matrix;
    }

    /**
     * Creates a transform matrix mapping the given source envelope to the given destination envelope.
     * The given envelopes can have any dimensions, which are handled as below:
     *
     * <ul>
     *   <li>If the two envelopes have the same {@linkplain Envelope#getDimension() dimension},
     *       then the transform is {@linkplain #isAffine(Matrix) affine}.</li>
     *   <li>If the destination envelope has less dimensions than the source envelope,
     *       then trailing dimensions are silently dropped.</li>
     *   <li>If the target envelope has more dimensions than the source envelope,
     *       then the transform will append trailing ordinates with the 0 value.</li>
     * </ul>
     *
     * @param  srcEnvelope The source envelope.
     * @param  dstEnvelope The destination envelope.
     * @return The transform from the given source envelope to target envelope.
     *
     * @see #createTransform(AxisDirection[], AxisDirection[])
     * @see #createTransform(Envelope, AxisDirection[], Envelope, AxisDirection[])
     */
    public static MatrixSIS createTransform(final Envelope srcEnvelope, final Envelope dstEnvelope) {
        final int srcDim = srcEnvelope.getDimension();
        final int dstDim = dstEnvelope.getDimension();
        final MatrixSIS matrix = createZero(dstDim+1, srcDim+1);
        for (int i = Math.min(srcDim, dstDim); --i >= 0;) {
            double scale     = dstEnvelope.getSpan(i)    / srcEnvelope.getSpan(i);
            double translate = dstEnvelope.getMinimum(i) - srcEnvelope.getMinimum(i)*scale;
            matrix.setElement(i, i,         scale);
            matrix.setElement(i, srcDim, translate);
        }
        matrix.setElement(dstDim, srcDim, 1);
        return matrix;
    }

    /**
     * Creates a transform matrix changing axis order and/or direction. For example the transform may convert
     * (<i>northing</i>, <i>westing</i>) coordinates into (<i>easting</i>, <i>northing</i>) coordinates.
     * This method tries to associate each {@code dstAxes} direction to either an equals {@code srcAxis}
     * direction, or to an opposite {@code srcAxis} direction.
     *
     * <ul>
     *   <li>If some {@code srcAxes} directions can not be mapped to {@code dstAxes} directions, then the transform
     *       will silently drops the ordinates associated to those extra source axis directions.</li>
     *   <li>If some {@code dstAxes} directions can not be mapped to {@code srcAxes} directions,
     *       then an exception will be thrown.</li>
     * </ul>
     *
     * {@example It is legal to transform from (<i>easting</i>, <i>northing</i>, <i>up</i>) to
     *           (<i>easting</i>, <i>northing</i>) - this is the first above case, but illegal
     *           to transform (<i>easting</i>, <i>northing</i>) to (<i>easting</i>, <i>up</i>).}
     *
     * <b>Code example:</b> the following method call:
     *
     * {@preformat java
     *   matrix = Matrices.createTransform(
     *           new AxisDirection[] {AxisDirection.NORTH, AxisDirection.WEST},
     *           new AxisDirection[] {AxisDirection.EAST, AxisDirection.NORTH});
     * }
     *
     * will return the following square matrix, which can be used in coordinate conversions as below:
     *
     * {@preformat math
     *   ┌    ┐   ┌         ┐   ┌    ┐
     *   │ +x │   │ 0 -1  0 │   │  y │
     *   │  y │ = │ 1  0  0 │ × │ -x │
     *   │  1 │   │ 0  0  1 │   │  1 │
     *   └    ┘   └         ┘   └    ┘
     * }
     *
     * @param  srcAxes The ordered sequence of axis directions for source coordinate system.
     * @param  dstAxes The ordered sequence of axis directions for destination coordinate system.
     * @return The transform from the given source axis directions to the given target axis directions.
     * @throws IllegalArgumentException If {@code dstAxes} contains at least one axis not found in {@code srcAxes},
     *         or if some colinear axes were found.
     *
     * @see #createTransform(Envelope, Envelope)
     * @see #createTransform(Envelope, AxisDirection[], Envelope, AxisDirection[])
     */
    public static MatrixSIS createTransform(final AxisDirection[] srcAxes, final AxisDirection[] dstAxes) {
        return createTransform(null, srcAxes, null, dstAxes, false);
    }

    /**
     * Creates a transform matrix mapping the given source envelope to the given destination envelope,
     * combined with changes in axis order and/or direction.
     * Invoking this method is equivalent to concatenating the following matrix transforms:
     *
     * <ul>
     *   <li><code>{@linkplain #createTransform(Envelope, Envelope) createTransform}(srcEnvelope, dstEnvelope)</code></li>
     *   <li><code>{@linkplain #createTransform(AxisDirection[], AxisDirection[]) createTransform}(srcAxes, dstAxes)</code></li>
     * </ul>
     *
     * @param  srcEnvelope The source envelope.
     * @param  srcAxes     The ordered sequence of axis directions for source coordinate system.
     * @param  dstEnvelope The destination envelope.
     * @param  dstAxes     The ordered sequence of axis directions for destination coordinate system.
     * @return The transform from the given source envelope and axis directions
     *         to the given envelope and target axis directions.
     * @throws MismatchedDimensionException If an envelope {@linkplain Envelope#getDimension() dimension} does not
     *         match the length of the axis directions sequence.
     * @throws IllegalArgumentException If {@code dstAxes} contains at least one axis not found in {@code srcAxes},
     *         or if some colinear axes were found.
     *
     * @see #createTransform(Envelope, Envelope)
     * @see #createTransform(AxisDirection[], AxisDirection[])
     */
    public static MatrixSIS createTransform(final Envelope srcEnvelope, final AxisDirection[] srcAxes,
                                            final Envelope dstEnvelope, final AxisDirection[] dstAxes)
    {
        ensureDimensionMatch("srcEnvelope", srcEnvelope, srcAxes.length);
        ensureDimensionMatch("dstEnvelope", dstEnvelope, dstAxes.length);
        return createTransform(srcEnvelope, srcAxes, dstEnvelope, dstAxes, true);
    }

    /**
     * Convenience method for checking object dimension validity.
     * This method is invoked for argument checking.
     *
     * @param  name      The name of the argument to check.
     * @param  envelope  The envelope to check.
     * @param  dimension The expected dimension for the object.
     * @throws MismatchedDimensionException if the envelope doesn't have the expected dimension.
     */
    private static void ensureDimensionMatch(final String name, final Envelope envelope,
            final int dimension) throws MismatchedDimensionException
    {
        ArgumentChecks.ensureNonNull(name, envelope);
        final int dim = envelope.getDimension();
        if (dimension != dim) {
            throw new MismatchedDimensionException(Errors.format(
                    Errors.Keys.MismatchedDimension_3, name, dimension, dim));
        }
    }

    /**
     * Creates a matrix for a transform that keep only a subset of source ordinate values.
     * The matrix size will be ({@code selectedDimensions.length}+1) × ({@code sourceDimensions}+1).
     * The matrix will contain only zero elements, except for the following cells which will contain 1:
     *
     * <ul>
     *   <li>The last column in the last row.</li>
     *   <li>For any row <var>j</var> other than the last row, the column {@code selectedDimensions[j]}.</li>
     * </ul>
     *
     * <b>Example:</b> Given (<var>x</var>,<var>y</var>,<var>z</var>,<var>t</var>) ordinate values, if one wants to
     * keep (<var>y</var>,<var>x</var>,<var>t</var>) ordinates (note the <var>x</var> ↔ <var>y</var> swapping)
     * and discard the <var>z</var> values, then the indices of source ordinates to select are 1 for <var>y</var>,
     * 0 for <var>x</var> and 3 for <var>t</var>. One can use the following method call:
     *
     * {@preformat java
     *   matrix = Matrices.createDimensionSelect(4, new int[] {1, 0, 3});
     * }
     *
     * The above method call will create the following 4×5 matrix,
     * which can be used for converting coordinates as below:
     *
     * {@preformat math
     *   ┌   ┐   ┌           ┐   ┌   ┐
     *   │ y │   │ 0 1 0 0 0 │   │ x │
     *   │ x │   │ 1 0 0 0 0 │   │ y │
     *   │ t │ = │ 0 0 0 1 0 │ × │ z │
     *   │ 1 │   │ 0 0 0 0 1 │   │ t │
     *   └   ┘   └           ┘   │ 1 │
     *                           └   ┘
     * }
     *
     * @param  sourceDimensions The number of dimensions in source coordinates.
     * @param  selectedDimensions The 0-based indices of source ordinate values to keep.
     *         The length of this array will be the number of dimensions in target coordinates.
     * @return An affine transform matrix keeping only the given source dimensions, and discarding all others.
     * @throws IllegalArgumentException if a value of {@code selectedDimensions} is lower than 0
     *         or not smaller than {@code sourceDimensions}.
     *
     * @see org.apache.sis.referencing.operation.MathTransforms#dimensionFilter(int, int[])
     */
    public static MatrixSIS createDimensionSelect(final int sourceDimensions, final int[] selectedDimensions) {
        final int numTargetDim = selectedDimensions.length;
        final MatrixSIS matrix = createZero(numTargetDim+1, sourceDimensions+1);
        for (int j=0; j<numTargetDim; j++) {
            final int i = selectedDimensions[j];
            ArgumentChecks.ensureValidIndex(sourceDimensions, i);
            matrix.setElement(j, i, 1);
        }
        matrix.setElement(numTargetDim, sourceDimensions, 1);
        return matrix;
    }

    /**
     * Creates a matrix which converts a subset of ordinates with another matrix.
     * For example giving (<var>latitude</var>, <var>longitude</var>, <var>height</var>) coordinates,
     * a pass through operation can convert the height values from feet to metres without affecting
     * the (<var>latitude</var>, <var>longitude</var>) values.
     *
     * <p>The given sub-matrix shall have the following properties:</p>
     * <ul>
     *   <li>The last column contains translation terms, except in the last row.</li>
     *   <li>The last row often (but not necessarily) contains 0 values except in the last column.</li>
     * </ul>
     *
     * A square matrix complying with the above conditions is often {@linkplain #isAffine(Matrix) affine},
     * but this is not mandatory
     * (for example a <cite>perspective transform</cite> may contain non-zero values in the last row).
     *
     * <p>This method builds a new matrix with the following content:</p>
     * <ul>
     *   <li>An amount of {@code firstAffectedOrdinate} rows and columns are inserted before the first
     *       row and columns of the sub-matrix. The elements for the new rows and columns are set to 1
     *       on the diagonal, and 0 elsewhere.</li>
     *   <li>The sub-matrix - except for its last row and column - is copied in the new matrix starting
     *       at index ({@code firstAffectedOrdinate}, {@code firstAffectedOrdinate}).</li>
     *   <li>An amount of {@code numTrailingOrdinates} rows and columns are appended after the above sub-matrix.
     *       Their elements are set to 1 on the pseudo-diagonal ending in the lower-right corner, and 0 elsewhere.</li>
     *   <li>The last sub-matrix row is copied in the last row of the new matrix, and the last sub-matrix column
     *       is copied in the last column of the sub-matrix.</li>
     * </ul>
     *
     * <b>Example:</b> Given the following sub-matrix which converts height values from feet to metres,
     * then subtract 25 metres:
     *
     * {@preformat math
     *   ┌    ┐   ┌             ┐   ┌   ┐
     *   │ z' │ = │ 0.3048  -25 │ × │ z │
     *   │ 1  │   │ 0         1 │   │ 1 │
     *   └    ┘   └             ┘   └   ┘
     * }
     *
     * Then a call to {@code Matrices.createPassThrough(2, subMatrix, 1)} will return the following matrix,
     * which can be used for converting the height (<var>z</var>) without affecting the other ordinate values
     * in (<var>x</var>,<var>y</var>,<var>t</var>) coordinates:
     *
     * {@preformat math
     *   ┌    ┐   ┌                      ┐   ┌   ┐
     *   │ x  │   │ 1  0  0       0    0 │   │ x │
     *   │ y  │   │ 0  1  0       0    0 │   │ y │
     *   │ z' │ = │ 0  0  0.3048  0  -25 │ × │ z │
     *   │ t  │   │ 0  0  0       1    0 │   │ t │
     *   │ 1  │   │ 0  0  0       0    1 │   │ 1 │
     *   └    ┘   └                      ┘   └   ┘
     * }
     *
     * @param  firstAffectedOrdinate The lowest index of the affected ordinates.
     * @param  subMatrix The matrix to use for affected ordinates.
     * @param  numTrailingOrdinates Number of trailing ordinates to pass through.
     * @return A matrix
     *
     * @see org.apache.sis.referencing.operation.DefaultMathTransformFactory#createPassThroughTransform(int, MathTransform, int)
     */
    public static MatrixSIS createPassThrough(final int firstAffectedOrdinate,
            final Matrix subMatrix, final int numTrailingOrdinates)
    {
        ArgumentChecks.ensurePositive("firstAffectedOrdinate", firstAffectedOrdinate);
        ArgumentChecks.ensurePositive("numTrailingOrdinates",  numTrailingOrdinates);
        final int  expansion = firstAffectedOrdinate + numTrailingOrdinates;
        int sourceDimensions = subMatrix.getNumCol();
        int targetDimensions = subMatrix.getNumRow();
        final MatrixSIS matrix = createZero(targetDimensions-- + expansion,
                                            sourceDimensions-- + expansion);
        /*
         * Following code process for upper row to lower row.
         * First, set the diagonal elements on leading new dimensions.
         */
        for (int j=0; j<firstAffectedOrdinate; j++) {
            matrix.setElement(j, j, 1);
        }
        /*
         * Copy the sub-matrix, with special case for the translation terms
         * which are unconditionally stored in the last column.
         */
        final int lastColumn = sourceDimensions + expansion;
        for (int j=0; j<targetDimensions; j++) {
            for (int i=0; i<sourceDimensions; i++) {
                matrix.setElement(firstAffectedOrdinate + j, firstAffectedOrdinate + i, subMatrix.getElement(j, i));
            }
            matrix.setElement(firstAffectedOrdinate + j, lastColumn, subMatrix.getElement(j, sourceDimensions));
        }
        /*
         * Set the pseudo-diagonal elements on the trailing new dimensions.
         * 'diff' is zero for a square matrix and non-zero for rectangular matrix.
         */
        final int diff = targetDimensions - sourceDimensions;
        for (int i=lastColumn - numTrailingOrdinates; i<lastColumn; i++) {
            matrix.setElement(diff + i, i, 1);
        }
        /*
         * Copy the last row from the sub-matrix. In the usual affine transform,
         * this row contains only 0 element except for the last one, which is 1.
         */
        final int lastRow = targetDimensions + expansion;
        for (int i=0; i<sourceDimensions; i++) {
            matrix.setElement(lastRow, i + firstAffectedOrdinate, subMatrix.getElement(targetDimensions, i));
        }
        matrix.setElement(lastRow, lastColumn, subMatrix.getElement(targetDimensions, sourceDimensions));
        return matrix;
    }

    /**
     * Creates a new matrix which is a copy of the given matrix.
     *
     * <blockquote><font size=-1><b>Implementation note:</b>
     * For square matrix with a size between {@value org.apache.sis.referencing.operation.matrix.Matrix1#SIZE}
     * and {@value org.apache.sis.referencing.operation.matrix.Matrix4#SIZE} inclusive, the returned matrix is
     * guaranteed to be an instance of one of {@link Matrix1} … {@link Matrix4} subtypes.
     * </font></blockquote>
     *
     * @param matrix The matrix to copy, or {@code null}.
     * @return A copy of the given matrix, or {@code null} if the given matrix was null.
     *
     * @see MatrixSIS#clone()
     */
    public static MatrixSIS copy(final Matrix matrix) {
        if (matrix == null) {
            return null;
        }
        final int size = matrix.getNumRow();
        if (size == matrix.getNumCol()) {
            switch (size) {
                case 1: return new Matrix1(matrix);
                case 2: return new Matrix2(matrix);
                case 3: return new Matrix3(matrix);
                case 4: return new Matrix4(matrix);
            }
        }
        return new GeneralMatrix(matrix);
    }

    /**
     * Casts or copies the given matrix to a SIS implementation. If {@code matrix} is already
     * an instance of {@code MatrixSIS}, then it is returned unchanged. Otherwise all elements
     * are copied in a new {@code MatrixSIS} object.
     *
     * @param  matrix The matrix to cast or copy, or {@code null}.
     * @return The matrix argument if it can be safely casted (including {@code null} argument),
     *         or a copy of the given matrix otherwise.
     */
    public static MatrixSIS castOrCopy(final Matrix matrix) {
        if (matrix instanceof MatrixSIS) {
            return (MatrixSIS) matrix;
        }
        return copy(matrix);
    }

    /**
     * Returns {@code true} if the given matrix represents an affine transform.
     * A transform is affine if the matrix is square and its last row contains
     * only zeros, except in the last column which contains 1.
     *
     * @param matrix The matrix to test.
     * @return {@code true} if the matrix represents an affine transform.
     *
     * @see MatrixSIS#isAffine()
     * @see AffineTransforms2D#castOrCopy(Matrix)
     */
    public static boolean isAffine(final Matrix matrix) {
        if (matrix instanceof MatrixSIS) {
            return ((MatrixSIS) matrix).isAffine();
        }
        // Following is executed only if the given matrix is not a SIS implementation.
        int j = matrix.getNumRow();
        int i = matrix.getNumCol();
        if (i != j--) {
            return false; // Matrix is not square.
        }
        double e = 1;
        while (--i >= 0) {
            if (matrix.getElement(j, i) != e) {
                return false;
            }
            e = 0;
        }
        return true;
    }

    /**
     * Returns {@code true} if the given matrix is close to an identity matrix, given a tolerance threshold.
     * This method is equivalent to computing the difference between the given matrix and an identity matrix
     * of identical size, and returning {@code true} if and only if all differences are smaller than or equal
     * to {@code tolerance}.
     *
     * @param  matrix The matrix to test for identity.
     * @param  tolerance The tolerance value, or 0 for a strict comparison.
     * @return {@code true} if this matrix is close to the identity matrix given the tolerance threshold.
     *
     * @see MatrixSIS#isIdentity()
     */
    public static boolean isIdentity(final Matrix matrix, final double tolerance) {
        final int numRow = matrix.getNumRow();
        final int numCol = matrix.getNumCol();
        if (numRow != numCol) {
            return false;
        }
        for (int j=0; j<numRow; j++) {
            for (int i=0; i<numCol; i++) {
                double e = matrix.getElement(j,i);
                if (i == j) {
                    e--;
                }
                if (!(Math.abs(e) <= tolerance)) {  // Uses '!' in order to catch NaN values.
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Compares the given matrices for equality, using the given relative or absolute tolerance threshold.
     * The matrix elements are compared as below:
     *
     * <ul>
     *   <li>{@link Double#NaN} values are considered equals to all other NaN values</li>
     *   <li>Infinite values are considered equal to other infinite values of the same sign</li>
     *   <li>All other values are considered equal if the absolute value of their difference is
     *       smaller than or equals to the threshold described below.</li>
     * </ul>
     *
     * If {@code relative} is {@code true}, then for any pair of values <var>v1</var><sub>j,i</sub>
     * and <var>v2</var><sub>j,i</sub> to compare, the tolerance threshold is scaled by
     * {@code max(abs(v1), abs(v2))}. Otherwise the threshold is used as-is.
     *
     * @param  m1       The first matrix to compare, or {@code null}.
     * @param  m2       The second matrix to compare, or {@code null}.
     * @param  epsilon  The tolerance value.
     * @param  relative If {@code true}, then the tolerance value is relative to the magnitude
     *         of the matrix elements being compared.
     * @return {@code true} if the values of the two matrix do not differ by a quantity
     *         greater than the given tolerance threshold.
     *
     * @see MatrixSIS#equals(Matrix, double)
     */
    public static boolean equals(final Matrix m1, final Matrix m2, final double epsilon, final boolean relative) {
        if (m1 != m2) {
            if (m1 == null || m2 == null) {
                return false;
            }
            final int numRow = m1.getNumRow();
            if (numRow != m2.getNumRow()) {
                return false;
            }
            final int numCol = m1.getNumCol();
            if (numCol != m2.getNumCol()) {
                return false;
            }
            for (int j=0; j<numRow; j++) {
                for (int i=0; i<numCol; i++) {
                    final double v1 = m1.getElement(j, i);
                    final double v2 = m2.getElement(j, i);
                    double tolerance = epsilon;
                    if (relative) {
                        tolerance *= Math.max(Math.abs(v1), Math.abs(v2));
                    }
                    if (!(Math.abs(v1 - v2) <= tolerance)) {
                        if (Numerics.equals(v1, v2)) {
                            // Special case for NaN and infinite values.
                            continue;
                        }
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Compares the given matrices for equality, using the given comparison strictness level.
     * To be considered equal, the two matrices must meet the following conditions, which depend
     * on the {@code mode} argument:
     *
     * <ul>
     *   <li>{@link ComparisonMode#STRICT STRICT}:
     *       the two matrices must be of the same class, have the same size and the same element values.</li>
     *   <li>{@link ComparisonMode#BY_CONTRACT BY_CONTRACT}:
     *       the two matrices must have the same size and the same element values,
     *       but are not required to be the same implementation class (any {@link Matrix} is okay).</li>
     *   <li>{@link ComparisonMode#IGNORE_METADATA IGNORE_METADATA}: same as {@code BY_CONTRACT}.
     *   <li>{@link ComparisonMode#APPROXIMATIVE APPROXIMATIVE}:
     *       the two matrices must have the same size, but the element values can differ up to some threshold.
     *       The threshold value is determined empirically and may change in any future SIS versions.</li>
     * </ul>
     *
     * @param  m1  The first matrix to compare, or {@code null}.
     * @param  m2  The second matrix to compare, or {@code null}.
     * @param  mode The strictness level of the comparison.
     * @return {@code true} if both matrices are equal.
     *
     * @see MatrixSIS#equals(Object, ComparisonMode)
     */
    public static boolean equals(final Matrix m1, final Matrix m2, final ComparisonMode mode) {
        switch (mode) {
            case STRICT:          return Objects.equals(m1, m2);
            case BY_CONTRACT:     // Fall through
            case IGNORE_METADATA: return equals(m1, m2, 0, false);
            case DEBUG:           // Fall through
            case APPROXIMATIVE:   return equals(m1, m2, Numerics.COMPARISON_THRESHOLD, true);
            default: throw new IllegalArgumentException(Errors.format(Errors.Keys.UnknownEnumValue_1, mode));
        }
    }

    /**
     * Returns a unlocalized string representation of the given matrix.
     * For each column, the numbers are aligned on the decimal separator.
     *
     * {@note Formatting on a per-column basis is convenient for the kind of matrices used in referencing by coordinates,
     *        because each column is typically a displacement vector in a different dimension of the source coordinate
     *        reference system. In addition, the last column is often a translation vector having a magnitude very
     *        different than the other columns.}
     *
     * @param  matrix The matrix for which to get a string representation.
     * @return A string representation of the given matrix.
     */
    public static String toString(final Matrix matrix) {
        final int numRow = matrix.getNumRow();
        final int numCol = matrix.getNumCol();
        final String[] elements = new String[numRow * numCol];
        final int[] columnWidth = new int[numCol];
        final int[] maximumFractionDigits = new int[numCol];
        final int[] maximumRemainingWidth = new int[numCol]; // Minus sign (if any) + integer digits + decimal separator + 2 spaces.
        int totalWidth = 1;
        /*
         * Create now the string representation of all matrix elements and measure the width
         * of the integer field and the fraction field, then the total width of each column.
         */
        for (int i=0; i<numCol; i++) {
            for (int j=0; j<numRow; j++) {
                final String element = Double.toString(matrix.getElement(j,i));
                elements[j*numCol + i] = element;
                int width = element.length();
                int s = element.lastIndexOf('.');
                if (s >= 0) {
                    width = (maximumRemainingWidth[i] = Math.max(maximumRemainingWidth[i], ++s + MARGIN))
                          + (maximumFractionDigits[i] = Math.max(maximumFractionDigits[i], width - s));
                } else {
                    // NaN or Infinity.
                    width += MARGIN;
                }
                columnWidth[i] = Math.max(columnWidth[i], width);
            }
            totalWidth += columnWidth[i];
        }
        /*
         * Now append the formatted elements with the appropriate amount of spaces
         * and trailling zeros for each column.
         */
        final String lineSeparator = System.lineSeparator();
        final CharSequence whiteLine = CharSequences.spaces(totalWidth);
        final StringBuffer buffer = new StringBuffer((totalWidth + 2 + lineSeparator.length()) * (numRow + 2));
        buffer.append('┌').append(whiteLine).append('┐').append(lineSeparator);
        for (int k=0,j=0; j<numRow; j++) {
            buffer.append('│');
            for (int i=0; i<numCol; i++) {
                final String element = elements[k++];
                final int width = element.length();
                int s = element.lastIndexOf('.');
                buffer.append(CharSequences.spaces(s >= 0 ? maximumRemainingWidth[i] - ++s : columnWidth[i] - width)).append(element);
                s += maximumFractionDigits[i] - width;
                while (--s >= 0) {
                    buffer.append('0');
                }
            }
            buffer.append(" │").append(lineSeparator);
        }
        return buffer.append('└').append(whiteLine).append('┘').append(lineSeparator).toString();
    }
}
