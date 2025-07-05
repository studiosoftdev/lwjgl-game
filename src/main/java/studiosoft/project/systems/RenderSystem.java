package studiosoft.project.systems;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import studiosoft.project.*;
import studiosoft.project.components.*;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;

public class RenderSystem implements ECSSystem {
    private World world;
    private Camera camera;
    private final int windowWidth;
    private final int windowHeight;

    private ShaderProgram shaderProgram;
    private int vboID;
    private int vaoID;
    private int vertexCount;
    private FloatBuffer vertexBuffer;

    public RenderSystem(World world, Camera camera, int windowWidth, int windowHeight, ShaderProgram shaderProgram) {
        this.world = world;
        this.camera = camera;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.shaderProgram = shaderProgram;

        createInitialVBO();
    }

    private void createInitialVBO() {
        /// triangle
        // create single VBO we can manipulate when drawing sprites
        vertexBuffer = BufferUtils.createFloatBuffer(6 * 4);

        float[] uvs = {0f,0f,1f,0f,1f,1f,0f,1f};

        // Vertex data for a quad, ordered for two triangles
        // Triangle 1: Top-left, Bottom-left, Bottom-right
        // Triangle 2: Bottom-right, Top-right, Top-left
        float[] verts = {
                // Position      // UVs
                0f, 1f,          uvs[6], uvs[7], // Top-left
                0f, 0f,          uvs[0], uvs[1], // Bottom-left
                1f, 0f,          uvs[2], uvs[3], // Bottom-right

                1f, 0f,          uvs[2], uvs[3], // Bottom-right
                1f, 1f,          uvs[4], uvs[5], // Top-right
                0f, 1f,          uvs[6], uvs[7]  // Top-left
        };
        vertexBuffer.put(verts);
        vertexBuffer.flip();

        // Generate and bind VAO
        if (vaoID == 0) {
            vaoID = glGenVertexArrays();
        }
        glBindVertexArray(vaoID);

        // Generate and bind VBO
        if (vboID == 0) {
            vboID = glGenBuffers();
        }
        glBindBuffer(GL_ARRAY_BUFFER, vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW);

        // Configure vertex attributes
        int stride = 4 * Float.BYTES;
        // Position attribute (location = 0)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        // Texture coordinate attribute (location = 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        // Unbind VAO to be safe
        glBindVertexArray(0);

        vertexCount = 6;
    }

    public Sprite createSprite(Texture srcTex, int tileU, int tileV, int tileSpanX, int tileSpanY){
        return new Sprite(srcTex, tileU, tileV, tileSpanX, tileSpanY);
    }

    @Override
    public void update(float deltaTime){
        List<Integer> entitiesToRender = world.queryEntitiesWith(Renderable.class, Position.class);

        // Bind the shared geometry ONCE before the loop
        glBindVertexArray(vaoID);

        for(Integer entityID : entitiesToRender){
            Sprite rendSprite = world.getComponent(Renderable.class, entityID).stream().findFirst().get().getSprite();
            Position pos = world.getComponent(Position.class, entityID).stream().findFirst().get();

            float spriteWidth = rendSprite.getSizeX();
            float spriteHeight = rendSprite.getSizeY();

            // build the model matrix for this specific sprite
            Matrix4f modelMatrix = new Matrix4f()
                    .translate(pos.x, pos.y, 0) // move it to the entity's position
                    .scale(spriteWidth, spriteHeight, 1);   // scale the 1x1 quad to the correct pixel size


            // send this unique matrix to the shader
            shaderProgram.setUniform("model", modelMatrix);

            // send off sprite UVs and ensure that it can do the UV remapping
            shaderProgram.setUniform("useUVRemapping", true);
            shaderProgram.setUniform("spriteUVs", rendSprite.getUVsAsVector());

            rendSprite.getTextureAtlas().bind();

            glDrawArrays(GL_TRIANGLES, 0, vertexCount);

        }

        // Unbind the VAO once after the loop is done
        glBindVertexArray(0);

        /*Collection<Renderable> renderables = world.getComponents(Renderable.class);


        for(Renderable renderable : renderables){
            Sprite rendSprite = renderable.getSprite();
            //mult these by height/width factors for stretch
            float h = renderable.getSprite().getSizeY();
            float w = renderable.getSprite().getSizeX();

            glBegin(GL_QUADS);
            glTexCoord2f(rendSprite.getU1(), rendSprite.getV1()); glVertex2f(x, y);
            glTexCoord2f(rendSprite.getU1(), rendSprite.getV2()); glVertex2f(x, y + h);
            glTexCoord2f(rendSprite.getU2(), rendSprite.getV2()); glVertex2f(x + w, y + h);
            glTexCoord2f(rendSprite.getU2(), rendSprite.getV1()); glVertex2f(x + w, y);
            glEnd();
        }*/
    }
}
