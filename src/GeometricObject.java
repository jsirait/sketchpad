import java.awt.Graphics2D;

public abstract class GeometricObject {
    public abstract void draw(Graphics2D g2d);
    public abstract boolean contains(int px, int py);
}
