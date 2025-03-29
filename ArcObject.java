import java.awt.Color;
import java.awt.Graphics2D; 

public class ArcObject extends GeometricObject {
    private int x, y, radius; 
    private int startAngle, arcAngle; 

    public ArcObject(int x, int y, int radius, int startAngle, int arcAngle) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.startAngle = startAngle;
        this.arcAngle = arcAngle;
    } 

    @Override
    public void draw(Graphics2D g2d) {
        g2d.setColor(Color.GREEN);  // should be uniform 
        // draw arc based on a bounding box defined by the center 
        // and radius 
        g2d.drawArc(x-radius, y-radius, 2*radius, 2*radius, startAngle, arcAngle);;
    }
}
