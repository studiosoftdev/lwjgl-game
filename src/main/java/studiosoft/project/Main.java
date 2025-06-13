package studiosoft.project;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;

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

        Player player = new Player(playerSprite, 250f, 250f, 2f, 2f);

        while (!glfwWindowShouldClose(window)) {

            // --- INPUT LOGIC STARTS HERE ---

            if (glfwGetKey(window, GLFW_KEY_W) == GLFW_PRESS) {
                player.moveY(false);
            }
            if (glfwGetKey(window, GLFW_KEY_S) == GLFW_PRESS) {
                player.moveY(true);
            }
            if (glfwGetKey(window, GLFW_KEY_A) == GLFW_PRESS) {
                player.moveX(false);
            }
            if (glfwGetKey(window, GLFW_KEY_D) == GLFW_PRESS) {
                player.moveX(true);
            }

            // --- INPUT LOGIC ENDS HERE ---

            // --- RENDER LOGIC STARTS HERE ---

            // 1. Clear the screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 2. Draw everything for the current frame
            // Let's draw a few frogs at different grid positions to test.
            drawTile(frogTex, 0, 0); // Top-left corner
            drawTile(frogTex, 8, 3);
            drawTile(frogTex, 10, 15);
            drawTile(frogTex, GRID_COLS - 1, GRID_ROWS - 1); // Bottom-right corner

            testAtlas.bind();

            // draw test sprites
            drawSpriteQuad(testSprite, 120, 80, 4, 4);
            drawSpriteQuad(testSprite2, 120, 144, 4, 4);

            //draw player
            drawActorQuad(player, player.getPosX(), player.getPosY());

            // --- RENDER LOGIC ENDS HERE ---


            // 3. Swap the buffers to display what we've drawn
            glfwSwapBuffers(window);

            // 4. Poll for events (like closing the window)
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }
}