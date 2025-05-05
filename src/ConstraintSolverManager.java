import java.util.List;

/**
 * Junita Sirait - COS583
 * 
 * To manage constraints satisfaction through iterative process. 
 */
public class ConstraintSolverManager { 
    private static final int ITER_MAX = 100; 
    private static final double TOLERANCE = 3;  

    // run relaxation until all constraints are under tolerance or we hit the iteration cap 
    public void solve(List<Constraint> constraints) {
        System.out.println("Number of constraints to be satisfied: " + constraints.size()); 
        for (int iter = 0; iter < ITER_MAX; iter++ ) {
            double maxError = 0; 
            for (Constraint c : constraints) {
                // System.out.println("constraint c: " + c); 
                c.apply(); 
                maxError = Math.max(maxError, c.error()); 
            }
            System.out.println("Iter " + iter + ", error: " + maxError);
            if (maxError < TOLERANCE) break; 
        }
    }
}
