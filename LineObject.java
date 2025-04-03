import java.awt.Color;
import java.awt.Graphics2D;

public class LineObject extends GeometricObject{
    private int x1, x2, y1, y2; 

    public LineObject(int x1, int y1, int x2, int y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
    } 

    public int getx1() {
        return this.x1;
    }

    public int getx2() {
        return this.x2;
    }

    public int gety1() {
        return this.y1; 
    } 

    public int gety2() {
        return this.y2;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLUE);  // probably should be uniform 
        g2d.drawLine(x1, y1, x2, y2);
    }

    @Override
    public boolean contains(int px, int py) {
        final double tolerance = 10.0; // increased tolerance
        double dx = x2 - x1;
        double dy = y2 - y1;
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
        double dx = x2 - x1; 
        double dy = y2 - y1; 
        return Math.sqrt(dx*dx + dy*dy); 
    } 

    // keeps the starting point (x1, y1) fixed and scales the direction vector.
    public void setLength(double newLength) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double currentLength = getLength();
        if (currentLength == 0) return;   // no division by zero 
        double scale = newLength / currentLength;
        this.x2 = x1 + (int) Math.round(dx * scale);
        this.y2 = y1 + (int) Math.round(dy * scale);
    }
    
    @Override
    public String toString() {
        return "Line[(" + x1 + ", " + y1 + ") -> (" + x2 + ", " + y2 + ")]";
    }

}
