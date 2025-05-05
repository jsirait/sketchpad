import java.awt.Graphics2D;


// for copying and pasting groups of objects (OOP)
public class GroupInstance extends GeometricObject {
    private GroupPrototype prototype;
    // Translation offset (the location of this instance relative to the group’s original coordinates)
    private int offsetX, offsetY;

    public GroupInstance(GroupPrototype prototype, int offsetX, int offsetY) {
        this.prototype = prototype;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    
    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
    }
    
    public GroupPrototype getPrototype() {
        return prototype;
    }
    
    @Override
    public void draw(Graphics2D g2d) {
        g2d.translate(offsetX, offsetY);
        
        // draw each object in the shared prototype
        for (GeometricObject obj : prototype.getObjects()) {
            obj.draw(g2d);
        }
        // restore the transform
        g2d.translate(-offsetX, -offsetY);
    }

    @Override
    public boolean contains(int x, int y) {
        // for removal -- TODO
        return false;
    }

    @Override
    public String toString() {
        return "GroupInstance at (" + offsetX + ", " + offsetY + ")";
    }
}