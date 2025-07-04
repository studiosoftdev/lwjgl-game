package studiosoft.project.systems;

import org.lwjgl.BufferUtils;
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

    public TilemapRenderSystem(World world, Texture textureAtlas) {
        this.world = world;
        this.textureAtlas = textureAtlas;
    }

    @Override
    public void update(float deltaTime){

        //System.out.println("Context in TilemapRenderSystem: " + org.lwjgl.glfw.GLFW.glfwGetCurrentContext()); // ADD THIS LINE
        // get all tilemap entities
        List<Integer> tilemapEntities = world.queryEntitiesWith(TilemapRenderable.class);

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
            renderTilemap(renderData, textureAtlas.id);
        }
    }

    private void buildTilemapVBO(TilemapRenderable tilemap, LevelRenderData renderData) {
        System.out.println("bTVBO");
        // each tile is a quad (2 tri, 4 vert)
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
        glVertexAttribPointer(0, 2, GL_FLOAT, false, (2+2) * Float.BYTES, 0);
        glEnableVertexAttribArray(0);

        // texture coord attr (layout 1)
        glVertexAttribPointer(1, 2, GL_FLOAT, false,
                (2+2) * Float.BYTES, 2*Float.BYTES);
        glEnableVertexAttribArray(1);

        // unbind VBO and VAO
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);

        // store vertex count for drawing
        renderData.vertexCount = numTiles * 4; //4 vert per quad
    }

    private void renderTilemap(LevelRenderData renderData, int textureID){
        System.out.println("rT");
        // bind texture
        glActiveTexture(GL_TEXTURE0); //activate texture unit 0
        glBindTexture(GL_TEXTURE_2D, textureID);

        // bind VAO
        glBindVertexArray(renderData.vaoID);

        // enable vert attr arrays
        glEnableVertexAttribArray(0); //position
        glEnableVertexAttribArray(1); //tex UVs

        // draw quads -- investigate replacing with GL_TRIANGLES by using IBO/EBO for efficiency
        glDrawArrays(GL_QUADS, 0, renderData.vertexCount);

        // disable vert attr arrays
        glDisableVertexAttribArray(1);
        glDisableVertexAttribArray(0);

        // unbind VAO and tex
        glBindVertexArray(0);
        glBindTexture(GL_TEXTURE_2D, 0);
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

    /// test load level data, remove when real loading system done
    public void testLoad(){
        int[][] testLevel = {{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,1,1,1,1,1,1,1,1,1,1,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0},
                {0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,0},
                {1,1,1,1,1,1,1,1,0,1,1,1,1,1,1,1},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0},
                {1,1,1,1,0,1,1,1,1,1,1,0,0,1,1,1},
                {1,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0},
                {1,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0},
                {1,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0},
                {1,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0},
                {1,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0},
                {1,1,1,1,1,1,1,1,1,1,1,0,0,1,0,0}};

    }
}
