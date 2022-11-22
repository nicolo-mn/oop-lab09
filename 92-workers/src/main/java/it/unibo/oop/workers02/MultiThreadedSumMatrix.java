package it.unibo.oop.workers02;

import java.util.ArrayList;
import java.util.List;

public class MultiThreadedSumMatrix implements SumMatrix{
    private final int nthread;

    /**
     * 
     * @param nthread
     *            no. of thread performing the sum.
     */
    public MultiThreadedSumMatrix(final int nthread) {
        if (nthread < 1) {
            throw new IllegalArgumentException("Error: you must use at least 1 thread to do this operation");
        }
        this.nthread = nthread;
    }

    private static class Worker extends Thread {
        private final double[][] matrix;
        private final int startElem;
        private final int nelem;
        private long res;

        /**
         * Build a new worker.
         * 
         * @param list
         *            the list to sum
         * @param startpos
         *            the initial position for this worker
         * @param nelem
         *            the no. of elems to sum up for this worker
         */
        Worker(final double[][] matrix, final int startElem, final int nelem) {
            super();
            this.matrix = matrix;
            this.startElem = startElem;
            this.nelem = nelem;
        }

        @Override
        public void run() {
            //System.out.println("Working from position " + startpos + " to position " + (startpos + nelem - 1));
            for (int i = 0; i < nelem; i++) {
                int curRow = (this.startElem + i) / matrix[0].length;
                int curCol = (this.startElem + i) % matrix[0].length;
                if (curRow < matrix.length && curCol < matrix[0].length) {
                    this.res += matrix[curRow][curCol];
                }
            }
        }

        /**
         * Returns the result of summing up the integers within the list.
         * 
         * @return the sum of every element in the array
         */
        public long getResult() {
            return this.res;
        }

    }

    @Override
    public double sum(double[][] matrix) {
        final int totalElems = matrix.length * matrix[0].length;
        final int size = totalElems % nthread + totalElems / nthread;
        /*
         * Build a list of workers
         */
        final List<Worker> workers = new ArrayList<>(nthread);
        for (int start = 0; start < totalElems; start += size) {
            workers.add(new Worker(matrix, start, size));
        }
        /*
         * Start them
         */
        for (final Worker w: workers) {
            w.start();
        }
        /*
         * Wait for every one of them to finish. This operation is _way_ better done by
         * using barriers and latches, and the whole operation would be better done with
         * futures.
         */
        long sum = 0;
        for (final Worker w: workers) {
            try {
                w.join();
                sum += w.getResult();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        }
        /*
         * Return the sum
         */
        return sum;
    }
    
}
