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

import org.opengis.referencing.operation.Matrix;
import org.opengis.referencing.operation.MathTransform;


/**
 * A usually affine, or otherwise a projective transform, which convert coordinates using only linear equations.
 * A projective transform is capable of mapping an arbitrary quadrilateral into another arbitrary quadrilateral,
 * while preserving the straightness of lines. In the special case where the transform is affine, the parallelism
 * of lines in the source is preserved in the output.
 *
 * <p>Such a coordinate transformation can be represented by a matrix of arbitrary size, which is given by the
 * {@link #getMatrix()} method. The relationship between matrix size and transform dimensions is as below:</p>
 *
 * <ul>
 *   <li>The {@linkplain Matrix#getNumCol() number of columns} in the matrix is equal to
 *       the number of {@linkplain #getSourceDimensions() source dimensions} plus 1</li>
 *   <li>The {@linkplain Matrix#getNumRow() number of rows} in the matrix is equal to
 *       the number of {@linkplain #getTargetDimensions() target dimensions} plus 1.</li>
 * </ul>
 *
 * <div class="section">Affine transform</div>
 * In most cases the transform in affine. For such transforms, the last matrix row contains only zero values
 * except in the last column, which contains 1. For example a conversion from projected coordinates (metres)
 * to display coordinates (pixel) can be done as below:
 *
 * <center>
 * <img src="../matrix/doc-files/AffineTransform.png" alt="Matrix representation of an affine transform">
 * </center>
 *
 * <div class="section">Projective transform</div>
 * If the last matrix row does not met the above constraints, then the transform is not affine.
 * A <cite>projective</cite> transform can be used as a generalization of affine transforms.
 * In such case the computation performed by SIS is similar to {@code PerspectiveTransform}
 * in <cite>Java Advanced Imaging</cite>.
 * For example a square matrix of size 4×4 is used for transforming three-dimensional coordinates.
 * The transformed points {@code (x',y',z')} are computed as below:
 *
 * <center>{@include formulas.html#ProjectiveTransform}</center>
 *
 * <div class="section">Instantiation</div>
 * The easiest way to instantiate a {@code LinearTransform} is to use the {@link MathTransforms#linear(Matrix)}
 * convenience method.
 *
 * @author  Martin Desruisseaux (IRD, Geomatys)
 * @since   0.4
 * @version 0.6
 * @module
 *
 * @see org.apache.sis.referencing.operation.transform.MathTransforms#linear(Matrix)
 * @see org.apache.sis.referencing.operation.builder.LinearTransformBuilder
 * @see java.awt.geom.AffineTransform
 * @see <a href="http://mathworld.wolfram.com/AffineTransformation.html">Affine transformation on MathWorld</a>
 */
public interface LinearTransform extends MathTransform {
    /**
     * Returns {@code true} if this transform is affine.
     * An affine transform preserves parallelism.
     *
     * @return {@code true} if this transform is affine.
     *
     * @see org.apache.sis.referencing.operation.matrix.MatrixSIS#isAffine()
     *
     * @since 0.6
     */
    boolean isAffine();

    /**
     * Returns the coefficients of this linear transform as a matrix.
     * Converting a coordinate with this {@code MathTransform} is equivalent to multiplying the
     * returned matrix by a vector containing the ordinate values with an additional 1 in the last row.
     * See {@link LinearTransform} class Javadoc for more details.
     *
     * @return The coefficients of this linear transform as a matrix.
     *
     * @see MathTransforms#getMatrix(MathTransform)
     */
    Matrix getMatrix();
}
