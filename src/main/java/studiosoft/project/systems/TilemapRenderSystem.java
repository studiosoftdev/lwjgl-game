package studiosoft.project.systems;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.system.MemoryStack;
import studiosoft.project.Camera;
import studiosoft.project.ShaderProgram;
import studiosoft.project.Texture;
import studiosoft.project.World;
import studiosoft.project.components.LevelRenderData;
import studiosoft.project.components.TilemapRenderable;

import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class TilemapRenderSystem implements ECSSystem{
    private World world;
    private Texture textureAtlas;
    private ShaderProgram shaderProgram;
    private Camera camera;

    public TilemapRenderSystem(World world, Texture textureAtlas, ShaderProgram shaderProgram, Camera camera) {
        this.world = world;
        this.textureAtlas = textureAtlas;
        this.shaderProgram = shaderProgram;
        this.camera = camera;
    }

    @Override
    public void update(float deltaTime){

        //System.out.println("Context in TilemapRenderSystem: " + org.lwjgl.glfw.GLFW.glfwGetCurrentContext()); // ADD THIS LINE
        // get all tilemap entities
        List<Integer> tilemapEntities = world.queryEntitiesWith(TilemapRenderable.class);

        //set camera view matrix to view uniform here somehow
        shaderProgram.setUniform("view", camera.getViewMatrix());

        for(Integer entID : tilemapEntities){
            // get data for the tilemap
            TilemapRenderable tilemap = world.getComponent(TilemapRenderable.class, entID).stream().findFirst().get();
            LevelRenderData renderData = world.getComponent(LevelRenderData.class, entID).stream().findFirst().get();
            //System.out.println("upd");

            // if tilemap changed or doesn't exist, need to rebuild VBO
            boolean needsRebuild = renderData == null || tilemap.isDirty; //what flag is this?

            if(needsRebuild) {
                if(renderData == null){
                    renderData = new LevelRenderData();
                    world.addComponent(entID, renderData);
                }
                buildTilemapVBO(tilemap, renderData);
                tilemap.isDirty = false;
            }

            // render the tilemap
            renderTilemap(renderData);
        }
    }

    private void buildTilemapVBO(TilemapRenderable tilemap, LevelRenderData renderData) {
        /*// each tile is a quad (2 tri, 4 vert)
        // each vert needs (x,y) and (u,v) for pos and tex coords
        // 4 floats per vert => 16 floats per quad
        int numTiles = tilemap.tilemapWidth * tilemap.tilemapHeight;
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(numTiles * 4 * (2+2) * Float.BYTES);

        for(int y = 0; y < tilemap.tilemapHeight; y++){
            for(int x = 0; x < tilemap.tilemapWidth; x++){
                int tileID = tilemap.tileMap[y][x];
                float[] uvs = textureAtlas.getTileUVs(tileID); // get UVs for this tile

                float xPos = (float) x * tilemap.tileWidth;
                float yPos = (float) y * tilemap.tileHeight;

                // define the 4 verts for quad
                // order matters for winding (front/back face culling)
                // assumes tex coords match vert order
                // so using common order for quad:
                // v1 (bottom left)
                vertexBuffer.put(xPos).put(yPos);
                vertexBuffer.put(uvs[0]).put(uvs[1]);

                // v2 (bottom right)
                vertexBuffer.put(xPos + tilemap.tileWidth).put(yPos);
                vertexBuffer.put(uvs[2]).put(uvs[1]);

                // v3 (top right)
                vertexBuffer.put(xPos + tilemap.tileWidth).put(yPos + tilemap.tileHeight);
                vertexBuffer.put(uvs[2]).put(uvs[3]);

                // v4 (top left)
                vertexBuffer.put(xPos).put(yPos + tilemap.tileHeight);
                vertexBuffer.put(uvs[0]).put(uvs[2]);
            }
        }
        vertexBuffer.flip();

        /// openGL stuff
        // generate VAO (stores the state needed to render obj)
        if(renderData.vaoID == 0){
            renderData.vaoID = glGenVertexArrays();
        }
        glBindVertexArray(renderData.vaoID);

        // generate VBO
        if(renderData.vboID == 0){
            renderData.vboID = glGenBuffers();
        }
        glBindBuffer(GL_ARRAY_BUFFER, renderData.vboID);
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer, GL_STATIC_DRAW); //GL_DYNAMIC_DRAW if updated frequently

        // configure vertex attributes
        // position attr (layout 0 in shader)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, (2+2), 0);
        glEnableVertexAttribArray(0);

        // texture coord attr (layout 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false,
                (2+2) * Float.BYTES, 2*Float.BYTES);
        glEnableVertexAttribArray(1);

        // unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // store vertex count for drawing
        renderData.vertexCount = numTiles * 4; //4 vert per quad*/

        /// triangle
        int numTiles = tilemap.tilemapWidth * tilemap.tilemapHeight;
        // Each tile is a quad, which we'll make from two triangles (6 vertices).
        // Each vertex has position (2 floats) and UVs (2 floats).
        FloatBuffer vertexBuffer = BufferUtils.createFloatBuffer(numTiles * 6 * 4);

        for (int y = 0; y < tilemap.tilemapHeight; y++) {
            for (int x = 0; x < tilemap.tilemapWidth; x++) {
                int tileID = tilemap.tileMap[y][x];
                if (tileID == -1) continue; // Optional: skip empty tiles

                float[] uvs = textureAtlas.getTileUVs(tileID);
                float xPos = (float) x * tilemap.tileWidth;
                float yPos = (float) y * tilemap.tileHeight;

                // Vertex data for a quad, ordered for two triangles
                // Triangle 1: Top-left, Bottom-left, Bottom-right
                // Triangle 2: Bottom-right, Top-right, Top-left
                float[] verts = {
                        // Position      // UVs
                        xPos, yPos,                           uvs[6], uvs[7], // Top-left
                        xPos, yPos + tilemap.tileHeight,      uvs[0], uvs[1], // Bottom-left
                        xPos + tilemap.tileWidth, yPos + tilemap.tileHeight, uvs[2], uvs[3], // Bottom-right

                        xPos + tilemap.tileWidth, yPos + tilemap.tileHeight, uvs[2], uvs[3], // Bottom-right
                        xPos + tilemap.tileWidth, yPos,       uvs[4], uvs[5], // Top-right
                        xPos, yPos,                           uvs[6], uvs[7]  // Top-left
                };
                vertexBuffer.put(verts);
            }
        }
        vertexBuffer.flip();

        // Generate and bind VAO
        if (renderData.vaoID == 0) {
            renderData.vaoID = glGenVertexArrays();
        }
        glBindVertexArray(renderData.vaoID);

        // Generate and bind VBO
        if (renderData.vboID == 0) {
            renderData.vboID = glGenBuffers();
        }
        glBindBuffer(GL_ARRAY_BUFFER, renderData.vboID);
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

        renderData.vertexCount = numTiles * 6;
    }

    private void renderTilemap(LevelRenderData renderData){
        // The Model matrix is now an identity matrix because the vertex positions
        // are already in world space. The camera's view matrix will handle positioning.
        shaderProgram.setUniform("model", new Matrix4f()); // Set a neutral model matrix
        shaderProgram.setUniform("useUVRemapping", false);

        textureAtlas.bind();
        glBindVertexArray(renderData.vaoID);

        glDrawArrays(GL_TRIANGLES, 0, renderData.vertexCount);

        // Unbind after drawing
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    private void updateTexCoords(int vaoID, float[] uvs) {
        // only uvs are being changed, which are at offset 2 floats in each vertex (8 bytes)
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer uvBuffer = stack.mallocFloat(2);

            // assuming vbo is still bound from setup - can rebind if needed
            // calc offset where UVs start (2 floats into each vert)
            for(int i = 0; i < 4; i++){
                // each vert is 4 bytes, wanting to update floats 2 and 3 of each
                long offsetBytes = (i * 4 + 2) * Float.BYTES;

                uvBuffer.clear();
                uvBuffer.put(uvs[i*2]);
                uvBuffer.put(uvs[i*2 + 1]);
                uvBuffer.flip();

                //wrte u and v (2 floats) for this vert
                glBufferSubData(GL_ARRAY_BUFFER, offsetBytes, uvBuffer);
            }

            glBindVertexArray(0);
        }
    }

    // Dispose resources when game ends or level changes completely
    public void dispose(int entityID){
        LevelRenderData renderData = world.getComponent(LevelRenderData.class, entityID).stream()
                .findFirst().orElse(null);
        if(renderData != null){
            glDeleteBuffers(renderData.vboID);
            glDeleteVertexArrays(renderData.vaoID);

            //remove component from entity if no longer needed
            /// method does not yet have implementation
            world.removeComponent(entityID, LevelRenderData.class);
        }
    }
}
