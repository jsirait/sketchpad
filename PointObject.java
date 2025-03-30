import java.awt.Color;
import java.awt.Graphics2D;

public class PointObject extends GeometricObject {
    private int x,y;
    private static final int SIZE = 6; 

    public PointObject(int x, int y) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.BLACK);
        g2d.fillOval(x-SIZE/2, y-SIZE/2, SIZE, SIZE);
    }

    @Override 
    public boolean contains(int px, int py) {
        int tolerance = 5; 
        int dx = px - x; 
        int dy = py - y; 
        return dx*dx + dy*dy <= tolerance*tolerance; 
    }

    @Override 
    public String toString() {
        return "Point(" + x + ", " + y + ")"; 
    }
}