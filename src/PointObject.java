import java.awt.Color;
import java.awt.Graphics2D;

public class PointObject extends GeometricObject {
    private int x,y;
    private static final int SIZE = 5; 

    public PointObject(int x, int y) {
        this.x = x;
        this.y = y;
    } 

    // getters and setters 
    public int getX() { return x; } 
    public int getY() { return y; } 
    public void setX(int x) { this.x = x; } 
    public void setY(int y) { this.y = y; } 

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x-SIZE/2, y-SIZE/2, SIZE, SIZE);
    }

    @Override 
    public boolean contains(int px, int py) {
        int tolerance = 10; 
        int dx = px - x; 
        int dy = py - y; 
        return dx*dx + dy*dy <= tolerance*tolerance; 
    } 

    @Override 
    public int hashCode() {
        return 31 * x + y; 
    } 

    @Override 
    public boolean equals(Object o) {
        if (this == o) return true; 
        if (!(o instanceof PointObject)) return false; 
        PointObject p = (PointObject) o; 
        return this.x == p.x && this.y == p.y; 
    }

    @Override 
    public String toString() {
        return "Point(" + x + ", " + y + ")"; 
    }
}