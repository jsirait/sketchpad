/**
 * Junita Sirait - COS583
 * 
 * Equal Length constraint is satisfied by moving both the start point and the end point of 
 * both of the lines, in their respective directions, by the amount of delta. Delta is 
 * half the initial length difference. Note that this is somewhat arbitrary as we can just set 
 * delta as the length difference itself. 
 * 
 */
public class EqualLengthConstraint implements Constraint { 
    private LineObject base; 
    private LineObject current; 

    public EqualLengthConstraint(LineObject base, LineObject current) {
        this.base = base; 
        this.current = current; 
    } 

    public LineObject baseline() { return this.base; } 
    public LineObject current() { return this.current; } 

    public double error() {
        return Math.abs(this.base.getLength() - this.current.getLength()); 
    }

    public void apply() {
        // System.out.println("* Applying equal length line constraints *"); 
        PointObject baseStartpoint = this.base.getStartPoint(); 
        PointObject baseEndpoint = this.base.getEndPoint(); 
        PointObject currentStartpoint = this.current.getStartPoint(); 
        PointObject currentEndpoint = this.current.getEndPoint(); 

        double baseLen = this.base.getLength(); 
        double currentLen = this.current.getLength(); 

        double delta = (currentLen - baseLen) / 2.0; 
        // System.out.println("delta: " + delta); 

        // TODO: what happens when sharing points? 
        // direction vectors 
        double uxBase = (baseEndpoint.getX() - baseStartpoint.getX()) / baseLen; 
        double uyBase = (baseEndpoint.getY() - baseStartpoint.getY()) / baseLen; 
        double uxCurrent = (currentEndpoint.getX() - currentStartpoint.getX()) / currentLen; 
        double uyCurrent = (currentEndpoint.getY() - currentStartpoint.getY()) / currentLen; 

        // move endpoints by +- delta along their line 
        baseStartpoint.moveBy((int) (-delta *uxBase), (int) (-delta * uyBase)); 
        baseEndpoint.moveBy((int) (delta * uxBase), (int) (delta * uyBase)); 
        currentStartpoint.moveBy((int) (delta *uxCurrent), (int) (delta * uyCurrent)); 
        currentEndpoint.moveBy((int) (-delta * uxCurrent), (int) (-delta * uyCurrent)); 
    }
}
