import java.util.ArrayList;
import java.util.List;

public class GroupPrototype {
    // The underlying objects in the group (points, lines, arcs)
    private List<GeometricObject> objects;

    public GroupPrototype() {
        objects = new ArrayList<>();
    }

    public void addObject(GeometricObject obj) {
        if (!objects.contains(obj)) {
            objects.add(obj);
        }
    }

    public void removeObject(GeometricObject obj) {
        objects.remove(obj);
    }

    public List<GeometricObject> getObjects() {
        return objects;
    }
}
