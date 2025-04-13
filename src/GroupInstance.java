import java.awt.Graphics2D;
import java.awt.Color;

public class GroupInstance extends GeometricObject {
    private GroupPrototype prototype;
    // Translation offset (the location of this instance relative to the group’s original coordinates)
    private int offsetX, offsetY;

    public GroupInstance(GroupPrototype prototype, int offsetX, int offsetY) {
        this.prototype = prototype;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }
    
    // Optionally, allow updating the offset.
    public void setOffset(int x, int y) {
        this.offsetX = x;
        this.offsetY = y;
    }
    
    public GroupPrototype getPrototype() {
        return prototype;
    }
    
    @Override
    public void draw(Graphics2D g2d) {
        // Save the current transform.
        g2d.translate(offsetX, offsetY);
        // Optionally, draw a bounding box or outline to indicate grouping.
        // g2d.setColor(Color.GRAY);
        // g2d.drawRect(...);
        
        // Draw each object in the shared prototype.
        for (GeometricObject obj : prototype.getObjects()) {
            obj.draw(g2d);
        }
        // Restore the transform.
        g2d.translate(-offsetX, -offsetY);
    }

    @Override
    public boolean contains(int x, int y) {
        // You can implement hit testing for the group if needed.
        // For simplicity, return false or test against the bounding box.
        return false;
    }

    @Override
    public String toString() {
        return "GroupInstance at (" + offsetX + ", " + offsetY + ")";
    }
}