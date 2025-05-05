import java.util.List;

/**
 * Junita Sirait - COS583
 * 
 * Parallel Line constraint is satisfied by `forcing` the second line to be parallel to the 
 * first line that user clicks on. We do this by rotating the second line as appropriate. 
 * 
 */
public class ParallelLineConstraint implements Constraint { 

    public LineObject base; 
    public LineObject current; 
    private double error; 

    public ParallelLineConstraint(LineObject line1, LineObject line2) {
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

        // two valid parallels 0 or pi 
        double d1 = normalizeAngle(baseAngle - currentAngle); 
        double d2 = normalizeAngle(baseAngle + Math.PI - currentAngle); 
        double diff = (Math.abs(d1) < Math.abs(d2) ? d1 : d2) * 0.5;  // half the diff arbiitrarily  
        // rotation by half the difference 
        // double diff = 0.5 * (baseAngle - currentAngle); 
        double c = Math.cos(diff); 
        double s = Math.sin(diff); 

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
