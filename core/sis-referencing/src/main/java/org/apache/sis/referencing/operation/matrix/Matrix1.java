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

import org.opengis.referencing.operation.Matrix;
import org.apache.sis.internal.util.Numerics;


/**
 * A matrix of fixed {@value #SIZE}×{@value #SIZE} size,
 * typically resulting from {@linkplain org.opengis.referencing.operation.MathTransform1D} derivative computation.
 * The matrix member is:
 *
 * <blockquote><pre> ┌     ┐
 * │ {@link #m00} │
 * └     ┘</pre></blockquote>
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.4 (derived from geotk-2.2)
 * @version 0.4
 * @module
 *
 * @see Matrix2
 * @see Matrix3
 * @see Matrix4
 */
public final class Matrix1 extends MatrixSIS {
    /**
     * Serial number for inter-operability with different versions.
     */
    private static final long serialVersionUID = -4829171016106097031L;

    /**
     * The matrix size, which is {@value}.
     */
    public static final int SIZE = 1;

    /**
     * The only element in this matrix.
     */
    public double m00;

    /**
     * Creates a new identity matrix.
     */
    public Matrix1() {
        m00 = 1;
    }

    /**
     * Creates a new matrix filled with only zero values.
     *
     * @param ignore Shall always be {@code false} in current version.
     */
    Matrix1(final boolean ignore) {
    }

    /**
     * Creates a new matrix initialized to the specified value.
     *
     * @param m00 The element in this matrix.
     */
    public Matrix1(final double m00) {
        this.m00 = m00;
    }

    /**
     * Creates a new matrix initialized to the specified values.
     * The length of the given array must be 1.
     *
     * @param elements Elements of the matrix.
     * @throws IllegalArgumentException If the given array does not have the expected length.
     */
    public Matrix1(final double[] elements) throws IllegalArgumentException {
        setElements(elements);
    }

    /**
     * Creates a new matrix initialized to the same value than the specified one.
     * The specified matrix size must be {@value #SIZE}×{@value #SIZE}.
     * This is not verified by this constructor, since it shall be verified by {@link Matrices}.
     *
     * @param  matrix The matrix to copy.
     */
    Matrix1(final Matrix matrix) {
        m00 = matrix.getElement(0,0);
    }

    /*
     * The 'final' modifier in following method declarations is redundant with the 'final' modifier
     * in this class declaration, but we keep them as a reminder of which methods should stay final
     * if this class was modified to a non-final class. Some methods should stay final because:
     *
     *  - returning a different value would make no-sense for this class (e.g. 'getNumRow()');
     *  - they are invoked by a constructor or by an other method expecting this exact semantic.
     */

    /**
     * Returns the number of rows in this matrix, which is always {@value #SIZE} in this implementation.
     *
     * @return Always {@value SIZE}.
     */
    @Override
    public final int getNumRow() {
        return SIZE;
    }

    /**
     * Returns the number of columns in this matrix, which is always {@value #SIZE} in this implementation.
     *
     * @return Always {@value SIZE}.
     */
    @Override
    public final int getNumCol() {
        return SIZE;
    }

    /**
     * Retrieves the value at the specified row and column of this matrix.
     * This method can be invoked when the matrix size or type is unknown.
     * If the matrix is known to be an instance of {@code Matrix1},
     * then the {@link #m00} field can be read directly for efficiency.
     *
     * @param row    The row index, which can only be 0.
     * @param column The column index, which can only be 0.
     * @return       The current value.
     */
    @Override
    public final double getElement(final int row, final int column) {
        if (row == 0 && column == 0) {
            return m00;
        } else {
            throw indexOutOfBounds(row, column);
        }
    }

    /**
     * Modifies the value at the specified row and column of this matrix.
     * This method can be invoked when the matrix size or type is unknown.
     * If the matrix is known to be an instance of {@code Matrix1},
     * then the {@link #m00} field can be set directly for efficiency.
     *
     * @param row    The row index, which can only be 0.
     * @param column The column index, which can only be 0.
     * @param value  The new value to set.
     */
    @Override
    public final void setElement(final int row, final int column, final double value) {
        if (row == 0 && column == 0) {
            m00 = value;
        } else {
            throw indexOutOfBounds(row, column);
        }
    }

    /**
     * Returns all matrix elements in a flat, row-major (column indices vary fastest) array.
     * The array length is 1.
     */
    @Override
    public final double[] getElements() {
        return new double[] {m00};
    }

    /**
     * Sets all matrix elements from a flat, row-major (column indices vary fastest) array.
     * The array length shall be 1.
     */
    @Override
    public final void setElements(final double[] elements) {
        ensureLengthMatch(SIZE*SIZE, elements);
        m00 = elements[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isAffine() {
        return m00 == 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final boolean isIdentity() {
        return m00 == 1;
    }

    /**
     * For a 1×1 matrix, this method does nothing.
     */
    @Override
    public void transpose() {
        // Nothing to do for a 1x1 matrix.
    }

    /**
     * Normalizes all columns in-place.
     * For a 1×1 matrix, this method just sets unconditionally the {@link #m00} value to 1.
     */
    @Override
    public void normalizeColumns() {
        m00 = 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatrixSIS inverse() throws NoninvertibleMatrixException {
        if (m00 == 0) {
            throw new NoninvertibleMatrixException();
        }
        return new Matrix1(1 / m00);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatrixSIS solve(final Matrix matrix) throws MismatchedMatrixSizeException, NoninvertibleMatrixException {
        final int nc = matrix.getNumCol();
        ensureNumRowMatch(SIZE, matrix, nc);
        if (m00 == 0) {
            throw new NoninvertibleMatrixException();
        }
        if (nc != SIZE) {
            final NonSquareMatrix m = new NonSquareMatrix(SIZE, nc, false);
            for (int i=0; i<nc; i++) {
                m.elements[i] = matrix.getElement(0, i) / m00;
            }
            return m;
        }
        return new Matrix1(matrix.getElement(0,0) / m00);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MatrixSIS multiply(final Matrix matrix) {
        final int nc = matrix.getNumCol();
        ensureNumRowMatch(SIZE, matrix, nc);
        if (nc != SIZE) {
            return new NonSquareMatrix(this, matrix);
        }
        return new Matrix1(m00 * matrix.getElement(0,0));
    }

    /**
     * Returns {@code true} if the specified object is of type {@code Matrix1} and
     * all of the data members are equal to the corresponding data members in this matrix.
     *
     * @param object The object to compare with this matrix for equality.
     * @return {@code true} if the given object is equal to this matrix.
     */
    @Override
    public boolean equals(final Object object) {
        if (object instanceof Matrix1) {
            final Matrix1 that = (Matrix1) object;
            return Numerics.equals(m00, that.m00);
        }
        return false;
    }

    /**
     * Returns a hash code value based on the data values in this object.
     */
    @Override
    public int hashCode() {
        final long code = Double.doubleToLongBits(m00) ^ serialVersionUID;
        return ((int) code) ^ ((int) (code >>> 32));
    }
}