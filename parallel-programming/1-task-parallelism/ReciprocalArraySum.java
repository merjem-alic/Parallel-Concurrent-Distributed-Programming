import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class ReciprocalArraySum {
    // sequential version
    public static double seqArraySum(double[] X) {
        long startTime = System.nanoTime();
        double sum = 0;

        for (int i = 0; i < X.length; i++) {
            sum += 1 / X[i]; // calculating reciprocals 
        }

        long timeInNanos = System.nanoTime() - startTime;
        printResults("seqArraySum", timeInNanos, sum);

        return sum; 
    }

    // parallel wrapper
    public static double parArraySum(double[] X) {
        long startTime = System.nanoTime();

        // create the initial task for the whole array 
        SumArray t = new SumArray(X, 0, X.length);
        
        // push the task into the ForkJoin pool
        ForkJoinPool.commonPool().invoke(t);

        double sum = t.ans; // get the result stored in the task pool
        long timeInNanos = System.nanoTime() - startTime;
        printResults("parArraySum", timeInNanos, sum);
        return sum; 
    }

    // parallel logic
    private static class SumArray extends RecursiveAction {
        static int SEQUENTIAL_THRESHOLD = 5; // smallest unit of work
        int lo, hi;
        double[] arr;
        double ans = 0; 

        SumArray(double[] a, int l, int h) {
            lo = l;
            hi = h;
            arr = a;
        }

        protected void compute() {
            // base case, if array segment is small, sum it sequentially
            if (hi - lo <= SEQUENTIAL_THRESHOLD) {
                for (int i = lo; i < hi; i++) {
                    ans += 1 / arr[i];
                }
            }

            // recursive step: split the work
            else {
                int mid = (hi + lo) / 2;
                SumArray left = new SumArray(arr, lo, mid);
                SumArray right = new SumArray(arr, mid, hi);

                left.fork(); // start left on a different thread
                right.compute(); // current thread works on right
                left.join(); // wait for left to finish

                ans = left.ans + right.ans; // combine results
            }
        }
    }

    private static void printResults(String name, long timeInNanos, double sum) {
        System.out.printf(" %s completed in %8.3f milliseconds, with sum = %8.5f \n", 
                          name, timeInNanos / 1e6, sum);
    }
}