/**
 * Junita Sirait - COS583
 * 
 * Vertical constraint is satisfied by moving both the start point and end point of the input line 
 * up or down, to make them vertical (reference point is midpoint). 
 * 
 * TODO: perhaps need to reconsider making length constant 
 * 
 */
public class VerticalConstraint implements Constraint {
    private final LineObject line; 
    private double error; 

    public VerticalConstraint(LineObject line) {
        this.line = line; 
    } 

    public void apply() {
        // System.out.println("Applying VERTICAL constraint"); 
        PointObject p1 = line.getStartPoint(), p2 = line.getEndPoint(); 
        int dx = p2.getX() - p1.getX(); 
        int delta = dx/2; 
        p1.moveBy(delta, 0); 
        p2.moveBy(-1*delta, 0); 
        this.error = Math.abs(dx); 
    } 

    public double error() {
        return this.error; 
    }
}
