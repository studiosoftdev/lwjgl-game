package studiosoft.project;

public class Entity {
    private final World world;
    public final int id;

    public Entity(int id, World world) {
        this.world = world;
        this.id = id;
    }

    public <T> void addComponent(T component){
        world.addComponent(this.id, component);
    }
}
