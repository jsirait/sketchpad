import java.awt.*;

public class ArcObject extends GeometricObject {
    private int x, y, width, height, startAngle, arcAngle;
    
    public ArcObject(int x, int y, int width, int height, int startAngle, int arcAngle) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
    }
    
    @Override
    public void draw(Graphics2D g) {
        g.drawArc(x, y, width, height, startAngle, arcAngle);
    }
    
    // Approximate hit-testing for an arc -- for deleting purposes 
    @Override
    public boolean contains(int px, int py) {
        // compute center and radius
        double cx = x + width / 2.0;
        double cy = y + height / 2.0;
        double rx = width / 2.0;
        double ry = height / 2.0;
        // Normalize the point with respect to the ellipse.
        double dx = (px - cx) / rx;
        double dy = (py - cy) / ry;
        double norm = Math.sqrt(dx * dx + dy * dy);
        // Check if the point is near the ellipse boundary.
        if (Math.abs(norm - 1.0) > 0.2) return false;
        
        // Compute the angle from the center to the point.
        double angle = Math.toDegrees(Math.atan2(-dy, dx)); // adjust for screen coordinates
        if (angle < 0) angle += 360;
        
        // Determine if angle lies within the arc's angle range.
        double start = startAngle;
        double end = startAngle + arcAngle;
        // Normalize angles.
        if (arcAngle >= 0) {
            return (angle >= start && angle <= end);
        } else {
            return (angle >= end && angle <= start);
        }
    }
    
    @Override
    public String toString() {
        return "Arc[(" + x + ", " + y + "), w=" + width + ", h=" + height + 
               ", start=" + startAngle + ", arc=" + arcAngle + "]";
    }
}
