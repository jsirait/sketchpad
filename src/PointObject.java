import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Junita Sirait - COS583
 * 
 * Point object 
 * 
 */
public class PointObject extends GeometricObject {
    private int x,y; 
    private final int id; 
    private static int counter = 0; 
    private static final int SIZE = 5; 

    public PointObject(int x, int y) {
        this.x = x;
        this.y = y; 
        this.id = counter++; 
    } 

    // getters and setters 
    public int getX() { return x; } 
    public int getY() { return y; } 
    public int getId()  { return this.id; } 
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
        // return 31 * x + y;  // problem when moving points 
        return id;  
    } 

    @Override 
    public boolean equals(Object o) {
        if (this == o) return true; 
        if (!(o instanceof PointObject)) return false; 
        PointObject p = (PointObject) o; 
        // return this.x == p.x && this.y == p.y; 
        return this.id == p.id;  
    }

    @Override 
    public String toString() {
        return "Point(" + x + ", " + y + ")"; 
    }

    public double angleTo(PointObject base) {
        return Math.atan2((base.y - this.y) * 1.0, (base.x - this.y) * 1.0);
    }

    public void moveTo(PointObject basePoint, double distance, double angle) {
        // to `distance` away from `basePoint` at angle `angle` 
        this.x = (int) (basePoint.x + Math.cos(angle) * distance); 
        this.y = (int) (basePoint.x + Math.sin(angle) * distance); 
    }

    public void moveBy(int x, int y) {
        // translation 
        this.x += x; 
        this.y += y; 
    } 

    public void moveOptimally() {
        // move in x and y directions that brings error down 

    }
}
