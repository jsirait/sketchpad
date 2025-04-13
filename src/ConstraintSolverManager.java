import org.pybee.cassowary.Variable;
import org.pybee.cassowary.SimplexSolver;
import org.pybee.cassowary.Expression;
import org.pybee.cassowary.CassowaryError;
import org.pybee.cassowary.Strength;
import org.pybee.cassowary.AbstractConstraint;
import java.util.HashMap;
import java.util.Map;

public class ConstraintSolverManager {
    private SimplexSolver solver;
    // Map each PointObject to a pair of solver variables.
    private Map<PointObject, PointVars> pointVarsMap;

    public ConstraintSolverManager() {
        solver = new SimplexSolver();
        pointVarsMap = new HashMap<>();
    }
    
    public static class PointVars {
        public Variable x, y;
        public PointVars(Variable x, Variable y) {
            this.x = x;
            this.y = y;
        }
    }
    
    // Register a point if not already done.
    public void addPoint(PointObject pt) {
        if (pointVarsMap.containsKey(pt))
            return;
        // Use the unique id instead of hashCode() based on mutable values.
        Variable vx = new Variable("x" + pt.getId());
        Variable vy = new Variable("y" + pt.getId());
        solver.addEditVar(vx, Strength.STRONG);
        solver.addEditVar(vy, Strength.STRONG);
        try {
            solver.suggestValue(vx, pt.getX());
            solver.suggestValue(vy, pt.getY());
        } catch (CassowaryError error) {
            error.printStackTrace();
        }
        pointVarsMap.put(pt, new PointVars(vx, vy));
    }
    
    // Update a point's suggested values.
    public void updatePoint(PointObject pt) {
        if (!pointVarsMap.containsKey(pt))
            addPoint(pt);
        PointVars pv = pointVarsMap.get(pt);
        try {
            solver.suggestValue(pv.x, pt.getX());
            solver.suggestValue(pv.y, pt.getY());
        } catch (CassowaryError error) {
            error.printStackTrace();
        }
    }
    
    // Remove a point from the constraint solver's mapping.
    public void removePoint(PointObject pt) {
        if (!pointVarsMap.containsKey(pt)) {
            return;
        }
        // If the solver supported removal of edit variables, you could do that here.
        pointVarsMap.remove(pt);
    }
    
    // Update all points from the solver's current values.
    public void updateAllPointsFromSolver() {
        for (Map.Entry<PointObject, PointVars> entry : pointVarsMap.entrySet()) {
            PointObject pt = entry.getKey();
            PointVars pv = entry.getValue();
            pt.setX((int)Math.round(pv.x.getValue()));
            pt.setY((int)Math.round(pv.y.getValue()));
        }
    }
    
    public PointVars getPointVars(PointObject pt) {
        return pointVarsMap.get(pt);
    }
    
    // Add a constraint to the solver.
    public void addConstraint(AbstractConstraint constraint) {
        try {
            solver.addConstraint(constraint);
        } catch (Exception e) { // e.g., RequiredFailure
            e.printStackTrace();
        }
    }
    
    // Solve the constraint system and update points.
    public void solve() {
        solver.resolve();
        updateAllPointsFromSolver();
    }
}
