import java.awt.Graphics2D;

/**
 * Junita Sirait - COS583
 * 
 * Points, lines, and arcs extend GeometricObject. 
 */
public abstract class GeometricObject {
    public abstract void draw(Graphics2D g2d);
    public abstract boolean contains(int px, int py);
}
