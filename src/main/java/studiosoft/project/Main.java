package studiosoft.project;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import studiosoft.project.components.PlayerInput;
import studiosoft.project.components.Position;
import studiosoft.project.components.Renderable;
import studiosoft.project.systems.PlayerInputSystem;
import studiosoft.project.systems.RenderSystem;

import java.io.IOException;
import java.net.URL;
import java.nio.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    // The window handle
    private long window;

    // NEW: Constants for our grid and window dimensions
    // Let's define a grid of 25x20 tiles
    private static final int GRID_COLS = 25;
    private static final int GRID_ROWS = 20;

    // Each tile will be 32x32 pixels
    private static final int TILE_WIDTH = 32;
    private static final int TILE_HEIGHT = 32;

    // The final window size is derived from our grid dimensions
    private static final int WINDOW_WIDTH = GRID_COLS * TILE_WIDTH;   // 800
    private static final int WINDOW_HEIGHT = GRID_ROWS * TILE_HEIGHT; // 640

    // used for precise framerate calcs eg proper move speed;
    private double deltaTime = 0;


    public void run() {
        System.out.println("Hello LWJGL " + Version.getVersion() + "!");
        init();
        loop();

        // Free the window callbacks and destroy the window
        glfwFreeCallbacks(window);
        glfwDestroyWindow(window);

        // Terminate GLFW and free the error callback
        glfwTerminate();
        glfwSetErrorCallback(null).free();
    }

    private void init() {
        GLFWErrorCallback.createPrint(System.err).set();

        if (!glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE); // CHANGED: Let's make it non-resizable for a fixed grid

        // CHANGED: Use our new constants for window creation
        window = glfwCreateWindow(WINDOW_WIDTH, WINDOW_HEIGHT, "pep gaming", NULL, NULL);
        if (window == NULL)
            throw new RuntimeException("Failed to create the GLFW window");

        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true);
        });

        try (MemoryStack stack = stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            glfwGetWindowSize(window, pWidth, pHeight);
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1); // Enable v-sync
        glfwShowWindow(window);
    }

    /**
     *
     * @param sprite selected sprite to draw
     * @param x x coord (screen) to draw on ... replace with grid coord ?
     * @param y y coord (screen) to draw on ... replace with grid coord ?
     * @param width normally 1. this is to keep consistent pixel size, but can be broken for special effects if desired.
     * @param height normally 1. same as bove
     */
    public static void drawSpriteQuad(Sprite sprite, float x, float y, float width, float height) {
        float h = height * sprite.getSizeY();
        float w = width * sprite.getSizeX();

        glBegin(GL_QUADS);
            glTexCoord2f(sprite.getU1(), sprite.getV1()); glVertex2f(x, y);
            glTexCoord2f(sprite.getU1(), sprite.getV2()); glVertex2f(x, y + h);
            glTexCoord2f(sprite.getU2(), sprite.getV2()); glVertex2f(x + w, y + h);
            glTexCoord2f(sprite.getU2(), sprite.getV1()); glVertex2f(x + w, y);
        glEnd();

    }

    public static void drawActorQuad(Actor actor, float x, float y) {
        float h = actor.getHeightSF() * actor.getSprite().getSizeY();
        float w = actor.getWidthSF() * actor.getSprite().getSizeX();

        glBegin(GL_QUADS);
        glTexCoord2f(actor.getSprite().getU1(), actor.getSprite().getV1()); glVertex2f(x, y);
        glTexCoord2f(actor.getSprite().getU1(), actor.getSprite().getV2()); glVertex2f(x, y + h);
        glTexCoord2f(actor.getSprite().getU2(), actor.getSprite().getV2()); glVertex2f(x + w, y + h);
        glTexCoord2f(actor.getSprite().getU2(), actor.getSprite().getV1()); glVertex2f(x + w, y);
        glEnd();

    }

    // NEW: A dedicated function to draw a textured quad at specific pixel coordinates.
    // This is what your 'debugTexture' function has been refactored into.
    public static void drawQuad(Texture tex, float x, float y, float width, float height) {
        // Bind the texture before rendering it
        tex.bind();

        // Immediate mode is deprecated, but fine for this simple example.
        // We assume the projection is already set up for the frame.
        glBegin(GL_QUADS);
        glTexCoord2f(0, 0); glVertex2f(x, y);
        glTexCoord2f(0, 1); glVertex2f(x, y + height);
        glTexCoord2f(1, 1); glVertex2f(x + width, y + height);
        glTexCoord2f(1, 0); glVertex2f(x + width, y);
        glEnd();
    }

    // NEW: A helper function to draw a texture on our grid.
    // It translates grid coordinates (e.g., 5, 3) to pixel coordinates.
    public static void drawTile(Texture tex, int gridX, int gridY) {
        float pixelX = gridX * TILE_WIDTH;
        float pixelY = gridY * TILE_HEIGHT;
        drawQuad(tex, pixelX, pixelY, TILE_WIDTH, TILE_HEIGHT);
    }


    private void loop() {
        GL.createCapabilities();

        // NEW: Set up the projection matrix once.
        // This defines our 2D coordinate system.
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        // The coordinate system will go from (0,0) at the top-left corner
        // to (WINDOW_WIDTH, WINDOW_HEIGHT) at the bottom-right.
        glOrtho(0, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 1, -1);
        glMatrixMode(GL_MODELVIEW);
        glLoadIdentity();

        // NEW: Enable 2D texturing and alpha blending.
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // for deltaTime calc
        double lastFrameTime = glfwGetTime();


        // Load the texture
        Texture frogTex = null;
        try {
            // Make sure "textures/frog.png" is in your resources folder.
            URL frogURL = Main.class.getClassLoader().getResource("textures/frog.png");
            if (frogURL == null) {
                throw new IOException("Resource not found: textures/frog.png");
            }
            frogTex = new Texture(frogURL, 32);
        } catch (IOException e) {
            System.err.println("Failed to load Frog Texture");
            throw new RuntimeException(e);
        }

        // load test atlas
        Texture testAtlas = null;
        try {
            // Make sure "textures/frog.png" is in your resources folder.
            URL testAtlasURL = Main.class.getClassLoader().getResource("textures/atlas.png");
            if (testAtlasURL == null) {
                throw new IOException("Resource not found: textures/frog.png");
            }
            testAtlas = new Texture(testAtlasURL, 16);
        } catch (IOException e) {
            System.err.println("Failed to load Frog Texture");
            throw new RuntimeException(e);
        }


        // Set the clear color to a dark gray
        glClearColor(0.1f, 0.2f, 0.1f, 0.0f);

        // create test sprites
        Sprite testSprite = new Sprite(testAtlas, 0, 0, 1, 1);
        Sprite testSprite2 = new Sprite(testAtlas, 1, 0, 1, 1);
        Sprite playerSprite = new Sprite(testAtlas, 2, 1, 1, 1);

        //Player player = new Player(playerSprite, 250f, 250f, 2f, 2f);


        /// TESTING NEW ECS SETUPS
        World world = new World();
        Camera camera = new Camera(0f, 0f, 2f);

        // Systems
        RenderSystem renderSystem = new RenderSystem(world, camera, WINDOW_WIDTH, WINDOW_HEIGHT);
        PlayerInputSystem playerInputSystem = new PlayerInputSystem(world, window);

        // Initial entities
        Entity player = world.createEntity();
        player.addComponent(new Position(0, 0));
        player.addComponent(new PlayerInput(100f));
        player.addComponent(new Renderable(playerSprite));

        Entity testE1 = world.createEntity();
        testE1.addComponent(new Position(48, 64));
        testE1.addComponent(new Renderable(testSprite));

        Entity testE2 = world.createEntity();
        testE2.addComponent(new Position(64, 48));
        testE2.addComponent(new Renderable(testSprite2));


        while (!glfwWindowShouldClose(window)) {

            // get start time for deltaTime
            double loopStartTime = glfwGetTime();

            // --- INPUT LOGIC STARTS HERE ---



            // --- INPUT LOGIC ENDS HERE ---

            // --- RENDER LOGIC STARTS HERE ---

            // 1. Clear the screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 2. Update core systems (movement etc)
            playerInputSystem.update((float) deltaTime);

            // 2. Draw world tiles for current frame

            // 3. Draw entities for current frame
            testAtlas.bind();
            renderSystem.update();


            // --- RENDER LOGIC ENDS HERE ---


            // 3. Swap the buffers to display what we've drawn
            glfwSwapBuffers(window);

            // 4. Poll for events (like closing the window)
            glfwPollEvents();

            //calc deltaTime
            double loopEndTime = glfwGetTime();
            deltaTime = loopEndTime - loopStartTime;
            //System.out.println(1/deltaTime);
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}