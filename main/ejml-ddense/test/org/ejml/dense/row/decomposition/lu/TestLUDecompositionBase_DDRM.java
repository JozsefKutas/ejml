/*
 * Copyright (c) 2021, Peter Abeles. All Rights Reserved.
 *
 * This file is part of Efficient Java Matrix Library (EJML).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ejml.dense.row.decomposition.lu;

import org.ejml.EjmlStandardJUnit;
import org.ejml.UtilEjml;
import org.ejml.data.DMatrixRMaj;
import org.ejml.dense.row.CommonOps_DDRM;
import org.ejml.dense.row.RandomMatrices_DDRM;
import org.ejml.dense.row.misc.DeterminantFromMinor_DDRM;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestLUDecompositionBase_DDRM extends EjmlStandardJUnit {
    /**
     * Compare the determinant computed from LU to the value computed from the minor
     * matrix method.
     */
    @Test
    public void testDeterminant()
    {
        Random rand = new Random(0xfff);

        int width = 10;

        DMatrixRMaj A = RandomMatrices_DDRM.rectangle(width,width,rand);

        DeterminantFromMinor_DDRM minor = new DeterminantFromMinor_DDRM(width);
        double minorVal = minor.compute(A);

        LUDecompositionAlt_DDRM alg = new LUDecompositionAlt_DDRM();
        alg.decompose(A);
        double luVal = alg.computeDeterminant().real;

        assertEquals(minorVal,luVal, UtilEjml.TEST_F64_SQ);
    }

    @Test
    public void _solveVectorInternal() {
        int width = 10;
        DMatrixRMaj LU = RandomMatrices_DDRM.rectangle(width,width,rand);

        DMatrixRMaj L = new DMatrixRMaj(width,width);
        DMatrixRMaj U = new DMatrixRMaj(width,width);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                double real = LU.get(i, j);
                if( j <= i ) {
                    if( j == i )
                        L.set(i,j,1);
                    else
                        L.set(i,j,real);
                }
                if( i <= j ) {
                    U.set(i,j,real);
                }
            }
        }

        DMatrixRMaj x = RandomMatrices_DDRM.rectangle(width, 1, -1, 1, rand);
        DMatrixRMaj tmp = new DMatrixRMaj(width,1);
        DMatrixRMaj b = new DMatrixRMaj(width,1);

        CommonOps_DDRM.mult(U, x, tmp);
        CommonOps_DDRM.mult(L,tmp,b);


        DebugDecompose alg = new DebugDecompose(width);
        for( int i = 0; i < width; i++ ) alg.getIndx()[i] = i;
        alg.setLU(LU);

        alg._solveVectorInternal(b.data);

        for( int i = 0; i < width; i++ ) {
            assertEquals(x.data[i],b.data[i],UtilEjml.TEST_F64_SQ);
        }
    }

    private static class DebugDecompose extends LUDecompositionBase_DDRM
    {
        public DebugDecompose(int width) {
            setExpectedMaxSize(width, width);
            m = n = width;
        }

        void setLU( DMatrixRMaj LU ) {
            this.LU = LU;
            this.dataLU = LU.data;
        }

        @Override
        public boolean decompose(DMatrixRMaj orig) {
            return false;
        }
    }
}