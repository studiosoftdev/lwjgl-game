package studiosoft.project;

import org.joml.Matrix4f;
import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.*;
import studiosoft.project.components.*;
import studiosoft.project.systems.PlayerInputSystem;
import studiosoft.project.systems.RenderSystem;
import studiosoft.project.systems.TilemapRenderSystem;

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

        // We want to use a modern OpenGL version, 3.3 is a good choice
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        // We want to use the modern "core" profile, not the old "compatibility" one
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        // This is required for macOS, but good practice for all systems
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

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
        GL.createCapabilities();
        GLUtil.setupDebugMessageCallback(System.err);
        glfwSwapInterval(1); // Enable v-sync
        glfwShowWindow(window);
    }


    private void loop() {
        // NEW: Enable 2D texturing and alpha blending.
        glEnable(GL_TEXTURE_2D);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        // create projection matrix that does same thing as previous glOrtho
        Matrix4f projectionMatrix = new Matrix4f().ortho(0, WINDOW_WIDTH, WINDOW_HEIGHT, 0, 1, -1);

        // shader setup
        ShaderProgram shaderProgram;
        try{
            String vertexSource = loadResource("/shaders/tilemap.vert");
            String fragmentSource = loadResource("/shaders/tilemap.frag");
            shaderProgram = new ShaderProgram(vertexSource, fragmentSource);
            // create uniforms for the proj matrix and tex sampler
            shaderProgram.createUniform("projection");
            shaderProgram.createUniform("view");
            shaderProgram.createUniform("model");
            shaderProgram.createUniform("texture_sampler");
            shaderProgram.createUniform("spriteUVs");
            shaderProgram.createUniform("useUVRemapping");
        } catch (Exception e){
            e.printStackTrace();
            return;
        }

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
            URL testAtlasURL = Main.class.getClassLoader().getResource("textures/bgatlas.png");
            if (testAtlasURL == null) {
                throw new IOException("Resource not found: textures/bgatlas.png");
            }
            testAtlas = new Texture(testAtlasURL, 16);
        } catch (IOException e) {
            System.err.println("Failed to load bgatlas Texture");
            throw new RuntimeException(e);
        }


        // Set the clear color to a dark gray
        glClearColor(0.1f, 0.2f, 0.1f, 0.0f);

        // create test sprites
        Sprite testSprite = new Sprite(frogTex, 0, 0, 1, 1);
        Sprite testSprite2 = new Sprite(frogTex, 1, 0, 1, 1);
        Sprite playerSprite = new Sprite(frogTex, 0, 1, 1, 1);

        //Player player = new Player(playerSprite, 250f, 250f, 2f, 2f);


        /// TESTING NEW ECS SETUPS
        World world = new World();
        Camera camera = new Camera(0f, 0f, 2f);

        // Systems
        RenderSystem renderSystem = new RenderSystem(world, camera, WINDOW_WIDTH, WINDOW_HEIGHT, shaderProgram);
        PlayerInputSystem playerInputSystem = new PlayerInputSystem(world, window);

        System.out.println("Context at start of loop(): " + org.lwjgl.glfw.GLFW.glfwGetCurrentContext());
        TilemapRenderSystem tilemapRenderSystem = new TilemapRenderSystem(world, testAtlas, shaderProgram, camera);

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

        Entity testLevelE = world.createEntity();
        testLevelE.addComponent(new TilemapRenderable(0));
        testLevelE.addComponent(new LevelRenderData());


        while (!glfwWindowShouldClose(window)) {

            // get start time for deltaTime
            double loopStartTime = glfwGetTime();

            // --- INPUT LOGIC STARTS HERE ---

            // --- INPUT LOGIC ENDS HERE ---

            // --- RENDER LOGIC STARTS HERE ---

            // 1. Clear the screen
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

            // 2. bind shader and set uniforms
            shaderProgram.bind();
            shaderProgram.setUniform("projection", projectionMatrix);
            shaderProgram.setUniform("view", camera.getViewMatrix());
            shaderProgram.setUniform("texture_sampler", 0); //use tex unit 0

            // 3. Update core systems (movement etc)
            playerInputSystem.update((float) deltaTime);


            // 4. Draw world tiles for current frame
            tilemapRenderSystem.update((float) deltaTime);

            // 5. Draw entities for current frame
            //testAtlas.bind();
            renderSystem.update((float) deltaTime);

            // 6. unbind shader
            shaderProgram.unbind();

            // --- RENDER LOGIC ENDS HERE ---

            // 7. Swap the buffers to display what we've drawn
            glfwSwapBuffers(window);

            // 8. Poll for events (like closing the window)
            glfwPollEvents();

            // 9. calc deltaTime
            double loopEndTime = glfwGetTime();
            deltaTime = loopEndTime - loopStartTime;
            //System.out.println(1/deltaTime);
        }
    }

    public static String loadResource(String fileName) throws Exception {
        String result;
        try (var in = Main.class.getResourceAsStream(fileName);
             var scanner = new java.util.Scanner(in, java.nio.charset.StandardCharsets.UTF_8)) {
            result = scanner.useDelimiter("\\A").next();
        }
        return result;
    }

    public static void main(String[] args) {
        new Main().run();
    }
}