package studiosoft.project.systems;
import studiosoft.project.*;
import studiosoft.project.components.*;

import java.util.Collection;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.glfwGetKey;

public class PlayerInputSystem implements ECSSystem {
    // The system knows about the world so it can query for data.
    private World world;
    private long window;

    public PlayerInputSystem(World world, long window) {
        this.world = world;
        this.window = window;
    }

    public void update(){
        return;
    }

    public void update(float deltaTime) {
        // 1. The System queries the World for all components of a certain type.
        //    (A more advanced system would query for entities with a COMBINATION of components).
        Collection<Position> positions = world.getComponents(Position.class);
        Collection<PlayerInput> playerInputs = world.getComponents(PlayerInput.class);

        List<Integer> entitiesPlayers = world.queryEntitiesWith(PlayerInput.class, Position.class);

        for(Integer entityID : entitiesPlayers){
            Position pos = world.getComponent(Position.class, entityID).stream().findFirst().get();
            PlayerInput playerInput = world.getComponent(PlayerInput.class, entityID).stream().findFirst().get();

            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
                pos.y -= playerInput.moveSpeed * deltaTime;
            }
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                pos.y += playerInput.moveSpeed * deltaTime;
            }
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
                pos.x -= playerInput.moveSpeed * deltaTime;
            }
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
                pos.x += playerInput.moveSpeed * deltaTime;
            }
        }

        // This is a simplified check. We assume if a player input component exists,
        // the corresponding position component is the one we want to modify.
        if (!playerInputs.isEmpty() && !positions.isEmpty()) {

            // 2. The System iterates through the data and applies its logic.
            for (Position pos : positions) {
                // Fake input for demonstration
                // In a real game, this would check glfwGetKey()
                if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
                    pos.y += 1 * deltaTime;
                }
                if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                    pos.y -= 1 * deltaTime;
                }
                if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
                    pos.x += 1 * deltaTime;
                }
                if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
                    pos.x -= 1 * deltaTime;
                }
            }
        }
    }
}
