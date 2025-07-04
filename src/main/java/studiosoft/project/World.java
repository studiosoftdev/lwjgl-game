package studiosoft.project;

import java.util.*;

public class World {
    private int nextEntityID = 0;
    // this is the core data storage. it maps a component type
    // to a map of entity IDs and their actual component instances.
    private Map<Class<?>, Map<Integer, Object>> componentData = new HashMap<>();

    public Entity createEntity() {
        return new Entity(nextEntityID++, this);
    }

    // actually associate the component with the entity
    public <T> void addComponent(int entityID, T component) {
        // find the storage for this type of component, or create it if it doesn't exist.
        componentData.computeIfAbsent(component.getClass(), k -> new HashMap<>());
        // store the component instance, linked to the entity's ID.
        componentData.get(component.getClass()).put(entityID, component);
    }

    // A helper method for systems to get all components of a certain type
    public <T> Collection<T> getComponents(Class<T> componentType) {
        Map<Integer, Object> components = componentData.get(componentType);
        if (components == null) {
            return Collections.emptyList(); // Return empty list if no entities have this component
        }
        return (Collection<T>) components.values();
    }


    public <T> Collection<T> getComponent(Class<T> componentType, int entityID) {
        Map<Integer, Object> components = componentData.get(componentType);
        if (components == null) {
            return Collections.emptyList();
        }
        return Collections.singleton((T) components.get(entityID));
    }

    public List<Integer> queryEntitiesWith(Class<?>... componentTypes) {
        List<Integer> matchingEntities = new ArrayList<>();

        // Find all entities that have the *first* component type
        if (componentTypes.length == 0 || !componentData.containsKey(componentTypes[0])) {
            return matchingEntities; // Return empty list if none exist
        }

        // Start with a list of all entities that have the first component
        Set<Integer> entityCandidates = new HashSet<>(componentData.get(componentTypes[0]).keySet());

        // For the rest of the component types, remove any entities that don't have them
        for (int i = 1; i < componentTypes.length; i++) {
            Map<Integer, Object> components = componentData.get(componentTypes[i]);
            if (components == null) {
                return Collections.emptyList(); // If any component type has no entities, no match is possible
            }
            entityCandidates.retainAll(components.keySet()); // Keep only entities that are also in the next set
        }

        matchingEntities.addAll(entityCandidates);
        return matchingEntities;
    }

    public <T> void removeComponent(int entityID, Class<T> componentType){
        /// TODO: implement this
    }
}
