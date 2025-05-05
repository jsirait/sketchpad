// LineObject.java
import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Junita Sirait - COS583
 * 
 * Line Object defined by start point and end point. 
 * 
 */
public class LineObject extends GeometricObject {
    private PointObject startPoint;
    private PointObject endPoint;
    
    public LineObject(PointObject startPoint, PointObject endPoint) {
        this.startPoint = startPoint;
        this.endPoint = endPoint;
    }
    
    public PointObject getStartPoint() {
        return startPoint;
    }
    
    public PointObject getEndPoint() {
        return endPoint;
    }
    
    // Allow the endPoint startPoint to be replaced
    public void setEndPoint(PointObject pt)  { this.endPoint = pt; }
    public void setStartPoint(PointObject pt)  { this.startPoint = pt; }
    
    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.drawLine(startPoint.getX(), startPoint.getY(), endPoint.getX(), endPoint.getY());
    }
    
    @Override
    public boolean contains(int px, int py) {
        final double tolerance = 10.0;
        int x1 = startPoint.getX(), y1 = startPoint.getY();
        int x2 = endPoint.getX(), y2 = endPoint.getY();
        double dx = x2 - x1, dy = y2 - y1;
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared == 0) return false;
        double t = ((px - x1) * dx + (py - y1) * dy) / lengthSquared;
        t = Math.max(0, Math.min(1, t));
        double projX = x1 + t * dx;
        double projY = y1 + t * dy;
        double distance = Math.sqrt(Math.pow(px - projX, 2) + Math.pow(py - projY, 2));
        return distance <= tolerance;
    }
    
    public double getLength() {
        double dx = endPoint.getX() - startPoint.getX();
        double dy = endPoint.getY() - startPoint.getY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    // get delta x or y
    public double getDX() { return endPoint.getX() - startPoint.getX(); }
    public double getDY() { return endPoint.getY() - startPoint.getY(); } 

    // get slope 
    public double getSlope() { return this.getDX()/this.getDY(); }
    // get the angle from start to end point
    public double getAngle() { return this.getStartPoint().angleTo(this.getEndPoint()); }

    @Override
    public String toString() {
        return "Line[(" + startPoint + ") -> (" + endPoint + ")]";
    }

    @Override 
    public boolean equals(Object that) {
        if (this == that) return true; 
        if (!(that instanceof LineObject)) return false; 
        LineObject thatLine = (LineObject) that; 
        return this.startPoint.equals(thatLine.startPoint) && this.endPoint.equals(thatLine.endPoint);
    }
}
