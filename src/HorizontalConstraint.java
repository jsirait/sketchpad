/**
 * Junita Sirait - COS583
 * 
 * Horizontal constraint is satisfied by moving both the start point and end point of the input line 
 * up or down, to make them horizontal (reference point is midpoint). 
 * 
 * TODO: perhaps need to reconsider the need to keep length constant
 * 
 */
public class HorizontalConstraint implements Constraint { 
    private final LineObject line; 
    private double error; 

    public HorizontalConstraint(LineObject line) { 
        this.line = line; 
    } 

    public void apply() { 
        // System.out.println("Applying HORIZONTAL constraint"); 
        PointObject p1 = line.getStartPoint(), p2 = line.getEndPoint(); 
        int dy = p2.getY() - p1.getY(); 
        int delta = dy/2; 
        p1.moveBy(0, delta); 
        p2.moveBy(0, -1*delta); 
        this.error = Math.abs(dy); 
    } 

    public double error() {
        return this.error; 
    }     
}
