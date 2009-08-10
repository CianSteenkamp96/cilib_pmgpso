/**
 * Copyright (C) 2003 - 2009
 * Computational Intelligence Research Group (CIRG@UP)
 * Department of Computer Science
 * University of Pretoria
 * South Africa
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sourceforge.cilib.type.types.container;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.sourceforge.cilib.type.types.Type;
import net.sourceforge.cilib.util.Vectors;

/**
 * Representation of a Matrix. This class is immutable with the intention that
 * all opertions on a Matrix will result in a new resulting Matrix.
 */
public final class Matrix implements Type {
    private static final long serialVersionUID = 7726056815026772629L;

    private final double[][] contents;

    private Matrix(int x, int y) {
        Preconditions.checkArgument(x > 0, "Zero row length does not make sense.");
        Preconditions.checkArgument(y > 0, "Zero column length does not make sense.");

        this.contents = new double[x][y];
    }

    /**
     * Determine if the {@code Matrix} is square. In other words, if the number
     * of rows and columns are the same.
     * @return {@code true} if the matrix is square, {@code false} otherwise.
     */
    public boolean isSquare() {
        Preconditions.checkState(this.contents.length >= 1);
        return (contents.length == contents[0].length) ? true : false;
    }

    /**
     * Obtain the current value within the matrix at the provided co-ordinates.
     * @param row The row to lookup, indexed from 0.
     * @param col The column to lookup, indexed from 0.
     * @return The value located at the position {@code [row][col]}.
     */
    public double valueAt(int row, int col) {
        return this.contents[row][col];
    }

    /**
     * Obtain the row vector for the given row, indexed from 0.
     * @param row The row number to obtain.
     * @return A {@code Vector} representing the row.
     */
    public Vector getRow(int row) {
        List<Double> rowList = new ArrayList<Double>(this.contents[row].length);

        for (double d : this.contents[row])
            rowList.add(d);

        return Vectors.create(rowList);
    }

    /**
     * Get the number of rows within the matrix.
     * @return The number of rows.
     */
    public int getRows() {
        return this.contents.length;
    }

    /**
     * Get the number of columns within the matrix.
     * @return The number of columns.
     */
    public int getColumns() {
        return this.contents[0].length;
    }

    /**
     * Apply the addition operation on the current matrix and the provided
     * matrix. The result is a new matrix.
     * @param b The matrix to add.
     * @return A new {@code Matrix} representing the result of the addition.
     */
    public final Matrix plus(final Matrix b) {
        Preconditions.checkArgument((this.getRows() == b.getRows()) && (this.getColumns() == b.getColumns()),
            "Illegal matrix dimensions for matrix addition.");

        Matrix result = new Matrix(this.getRows(), this.getColumns());
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getColumns(); j++) {
                result.contents[i][j] = this.contents[i][j] + b.contents[i][j];
            }
        }
        return result;
    }

    /**
     * Apply the subtration operation on the current matrix and the provided
     * matrix. The result is a new matrix.
     * @param b The matrix to subtract.
     * @return A new {@code Matrix} representing the result of the subtraction.
     */
    public final Matrix minus(final Matrix b) {
        Preconditions.checkArgument((this.getRows() == b.getRows()) && (this.getColumns() == b.getColumns()),
            "Illegal matrix dimensions for matrix subtraction.");

        Matrix result = new Matrix(this.getRows(), this.getColumns());
        for (int i = 0; i < getRows(); i++) {
            for (int j = 0; j < getColumns(); j++) {
                result.contents[i][j] = this.contents[i][j] - b.contents[i][j];
            }
        }
        return result;
    }

    public Matrix multiply(Matrix b) {
        return this.times(b);
    }

    /**
     * Perform multiplication on the current {@code Matrix} and the provided {@code Matrix}.
     * Naturally, matrix multiplication can only be performed on matricies that adhere
     * to the required matrix multiplication rules.
     * <p>
     * The result of this operation is a new immutable matrix.
     * @param b The {@code Matrix} to to multiply the current {@code Matrix} with.
     * @return A new {@code Matrix} representing the result of the multiplication.
     */
    public Matrix times(Matrix b) {
        Preconditions.checkArgument(this.getRows() == b.getColumns(), "Illegal matrix dimensions for matrix multiplication.");

        Matrix result = new Matrix(this.getRows(), b.getColumns());
        for (int i = 0; i < result.getColumns(); i++) {
            for (int j = 0; j < result.getRows(); j++) {
                for (int k = 0; k < this.getRows(); k++)
                result.contents[i][j] += this.contents[i][k] * b.contents[k][j];
            }
        }
        return result;
    }

    /**
     * Obtain the transposition of the current {@code Matrix} instance.
     * @return A new {@code Matrix} that is the transpose of the current {@code Matrix}.
     */
    public Matrix transpose() {
        Matrix result = new Matrix(this.getColumns(), this.getRows());
        for (int i = 0; i < this.getRows(); i++)
            for (int j = 0; j < this.getColumns(); j++)
                result.contents[j][i] = this.contents[i][j];
        return result;
    }

    /**
     * 
     * @param angle The rotation angle defined in radians.
     * @return
     */
    public Matrix rotate(double angle) {
        Matrix rotation = new Matrix(2, 2);
        rotation.contents[0][0] = Math.cos(angle);
        rotation.contents[0][1] = -Math.sin(angle);
        rotation.contents[1][0] = Math.sin(angle);
        rotation.contents[1][1] = Math.cos(angle);

        return this.times(rotation);
    }

    /**
     * 
     * @return
     */
    public double determinant() {
        return contents[0][0]*contents[1][1] - contents[1][0]*contents[0][1];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Matrix getClone() {
        Matrix matrix = new Matrix(this.getRows(), this.getColumns());

        for (int i = 0; i < getRows(); i++)
            for (int j = 0; j < getColumns(); j++)
                matrix.contents[i][j] = this.contents[i][j];

        return matrix;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (getClass() != obj.getClass()) {
            return false;
        }

        final Matrix other = (Matrix) obj;
        return Arrays.deepEquals(this.contents, other.contents);
    }

    @Override
    public int hashCode() {
        int hash = 0;

        for (double[] array : this.contents)
            hash += Arrays.hashCode(array);

        return hash;
    }

    /**
     * Obtain a builder for {@code Matrix} instances.
     * @return A {@link Matrix.Builder} instance.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * A builder object to help with the construction of {@link Matrix} instances.
     */
    public static final class Builder {
        private int rowNumber;
        private int colNumber;
        private List<List<Double>> rows;
        private List<DataPoint> tuples;
        private boolean identity;

        private Builder() {
            reset();
        }

        /**
         * Define the number of rows that the built up {@code Matrix} will contain.
         * @param rows The required number of rows.
         * @return The current {@code Builder}.
         */
        public Builder rows(int rows) {
            this.rowNumber = rows;
            return this;
        }

        /**
         * Define the number of coloumns that the built up {@code Matrix}  will contain.
         * @param colummns The required number of columns.
         * @return The current {@code Builder}.
         */
        public Builder columns(int colummns) {
            this.colNumber = colummns;
            return this;
        }

        /**
         * Add a row vector, for inclusion in the built up {@code Matrix}.
         * @param columnValues The values for the columns.
         * @return the current {@code Builder}.
         * @throws IllegalArgumentException if the {@code columnValues} are not the same
         *         dimensions / length that is expected.
         */
        public Builder addRow(Double... columnValues) {
            Preconditions.checkArgument(columnValues.length == this.colNumber,
                "Cannot add a row with a differing column length. Expected: " + this.colNumber + ", got: " + columnValues.length);

            List<Double> rowVector = Arrays.asList(columnValues);
            this.rows.add(rowVector);
            return this;
        }

        /**
         * Add a row vector, for inclusion in the built up {@code Matrix}.
         * @param iterable The values for the columns.
         * @return the current {@code Builder}.
         * @throws IllegalArgumentException if the {@code columnValues} are not the same
         *         dimensions / length that is expected.
         */
        public Builder addRow(Iterable<Double> iterable) {
            List<Double> list = new ArrayList<Double>();
            Iterables.addAll(list, iterable);
            this.rows.add(list);
            return this;
        }

        /**
         * Define that the built up {@code Matrix} should be an identity {@code Matrix}.
         * All added row vectors or position values will be discarded if the built up {@code Matrix}
         * is to be an identity {@code Matrix}.
         * @return The current {@code Builder}.
         */
        public Builder identity() {
            Preconditions.checkState(this.rowNumber == this.colNumber, "Identity on non-sqaure matrix is not allowed.");
            this.identity = true;
            return this;
        }

        /**
         * Define that at a specific value for a defined grid point within the built
         * up {@code Matrix}.
         * <p>
         * This operation will override any values specified by a row vector addition.
         * @param row The row number, indexed from 0.
         * @param col The column number, indexed from 0.
         * @param value The value to be set at the defined location.
         * @return The curret {@code Builder}.
         */
        public Builder valueAt(int row, int col, double value) {
            this.tuples.add(new DataPoint(row, col, value));
            return this;
        }

        /**
         * Reset the builder into a clean state.
         */
        private void reset() {
            this.rowNumber = 0;
            this.colNumber = 0;
            this.rows = new ArrayList<List<Double>>();
            this.tuples = new ArrayList<DataPoint>();
            this.identity = false;
        }

        /**
         * Build the {@code Matrix} instance, based on the currently defined builder.
         * @return A new immutable {@code Matrix} instance.
         */
        public Matrix build() {
            Matrix matrix = new Matrix(rowNumber, colNumber);

            if (identity) {
                for (int i = 0; i < rowNumber; i++) {
                    for (int j = 0; j < colNumber; j++) {
                        matrix.contents[i][j] = (i == j) ? 1.0 : 0.0;
                    }
                }

                return matrix;
            }

            if (this.rows.size() >= 1) {
                for (int i = 0; i < rowNumber; i++) {
                    for (int j = 0; j < colNumber; j++) {
                        matrix.contents[i][j] = this.rows.get(i).get(j);
                    }
                }
            }

            if (this.tuples.size() >= 0) {
                for (DataPoint tuple : this.tuples) {
                    matrix.contents[tuple.getX()][tuple.getY()] = tuple.getValue();
                }
            }

            reset(); // Reset the builder to the default state, so that it may be used again, if needed.

            return matrix;
        }
    }

    /**
     * Class defining a [row, col, value] tuple for building up of a {@code Matrix}.
     */
    private static final class DataPoint {
        private int x;
        private int y;
        private double value;

        public DataPoint(int x, int y, double value) {
            this.x = x;
            this.y = y;
            this.value = value;
        }

        public double getValue() {
            return value;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }
}
