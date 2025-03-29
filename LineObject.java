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
    
}
