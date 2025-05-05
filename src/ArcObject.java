import java.awt.*;


/**
 * Junita Sirait - COS583
 * 
 * ArcObject objects as drawn on the original Sketchpad by Ian Sutherland. 
 * Method: we calculate the angle between the first and second click, and keep on calculating 
 * angle as we move our mouse along and finalize this angle when we 'flick'. 
 * 
 * TODO: record start and end points of arc to allow for moving and merging, similar to LineObject. 
 */
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
        // normalize
        double dx = (px - cx) / rx;
        double dy = (py - cy) / ry;
        double norm = Math.sqrt(dx * dx + dy * dy);
        if (Math.abs(norm - 1.0) > 0.2) return false;
        
        // angle from center to the point 
        double angle = Math.toDegrees(Math.atan2(-dy, dx)); // adjust for screen coordinates
        if (angle < 0) angle += 360;

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
