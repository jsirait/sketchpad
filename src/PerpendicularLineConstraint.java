import java.util.List;

/**
 * Junita Sirait - COS583
 * 
 * PerpendicularLineConstraint is satisfied by `forcing` the second line to be perpendicular to the 
 * first line that user clicks on. We do this by rotating the second line as appropriate. 
 * 
 */
public class PerpendicularLineConstraint implements Constraint { 

    public LineObject base; 
    public LineObject current; 
    private double error; 

    public PerpendicularLineConstraint(LineObject line1, LineObject line2) {
        this.base = line1; 
        this.current = line2; 
    } 

    public LineObject baseline() { return this.base; } 
    public LineObject current() { return this.current; }

    public int compareTo(Constraint c) {
        // must satisfy parallel, hold, then same length 
        // so parallel > equal length 
        // i.e. make them equal first then at end parallel 
        if (c.getClass().equals(EqualLengthConstraint.class)) return 1; 
        // compare to other constraints later 
        // ParallelLineConstraint p = (ParallelLineConstraint) c; 
        return 0; 
    } 

    public double error() {
        // double dx1 = this.baseline.getLength();
        return this.error; 
    }

    public void apply() {
        // System.out.println("* Applying PARALLEL constraint *"); 
        // this.baseline.makeParallelTo(this.current); 
        // get endpoints 
        PointObject base1 = this.base.getStartPoint(); 
        PointObject base2 = this.base.getEndPoint(); 
        PointObject current1 = this.current.getStartPoint(); 
        PointObject current2 = this.current.getEndPoint(); 

        // compute midpoints 
        double currentMX = (current1.getX() + current2.getX()) / 2; 
        double currentMY = (current1.getY() + current2.getY()) / 2; 

        // measure angles 
        double baseAngle = Math.atan2(base2.getY()-base1.getY(), base2.getX()-base1.getX()); 
        double currentAngle = Math.atan2(current2.getY()-current1.getY(), current2.getX()-current1.getX()); 

        double target1 = baseAngle + Math.PI/2;
        double target2 = baseAngle - Math.PI/2;
        double diff1   = normalizeAngle(target1 - currentAngle);
        double diff2   = normalizeAngle(target2 - currentAngle);
        double diff    = (Math.abs(diff1) < Math.abs(diff2) ? diff1 : diff2); 

        // desired new angle = baseAngle plus 90 degrees 
        // double targetAngle = baseAngle + Math.PI/2; 
        // double diff = targetAngle - currentAngle; 
        // normalize to -pi,pi 
        // diff = (diff+Math.PI) % (2*Math.PI) - Math.PI; 
        // relaxation: rotate by half the difference 
        double step = diff * 0.5; 
        double s = Math.sin(step), c = Math.cos(step); 

        // rotate each endpoints of current line around midpoint 
        for (PointObject p : List.of(current1, current2)) {
            double x = p.getX() - currentMX, y = p.getY() - currentMY; 
            double xRotate = x*c - y*s;
            double yRotate = x*s + y*c; 
            p.setX((int) (currentMX + xRotate)); 
            p.setY((int) (currentMY + yRotate)); 
        } 

        // the err is calculated as the component of the current line that is in the direction of `diff` 
        this.error = Math.abs(this.current.getLength() * Math.abs(diff)); 
    }

    // normalize into [-pi, pi]
    public double normalizeAngle(double angle) {
        double a = (angle + Math.PI) % (2*Math.PI); 
        if (a < 0) a += 2*Math.PI; 
        return a - Math.PI; 
    }
    
}
