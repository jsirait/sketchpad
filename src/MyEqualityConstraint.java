// MyEqualityConstraint.java
import org.pybee.cassowary.AbstractConstraint;
import org.pybee.cassowary.CassowaryError;
import org.pybee.cassowary.Expression;
import org.pybee.cassowary.Strength;

public class MyEqualityConstraint extends AbstractConstraint {

    private Expression expression;

    /**
     * Create an equality constraint that enforces expression == 0.
     */
    public MyEqualityConstraint(Expression expression, Strength strength) throws CassowaryError {
        super(strength);
        this.expression = expression;
    }

    @Override
    public Expression getExpression() {
        return expression;
    }
}
