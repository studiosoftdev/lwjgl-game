package studiosoft.project.systems;

import studiosoft.project.*;
import studiosoft.project.components.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glTexCoord2f;
import static org.lwjgl.opengl.GL11.glVertex2f;

public class RenderSystem {
    private World world;
    private Camera camera;
    private final int windowWidth;
    private final int windowHeight;

    public RenderSystem(World world, Camera camera, int windowWidth, int windowHeight) {
        this.world = world;
        this.camera = camera;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
    }

    public Sprite createSprite(Texture srcTex, int tileU, int tileV, int tileSpanX, int tileSpanY){
        return new Sprite(srcTex, tileU, tileV, tileSpanX, tileSpanY);
    }

    public void update(){

        List<Integer> entitiesToRender = world.queryEntitiesWith(Renderable.class, Position.class);
        for(Integer entityID : entitiesToRender){
            Sprite rendSprite = world.getComponent(Renderable.class, entityID).stream().findFirst().get().getSprite();
            Position pos = world.getComponent(Position.class, entityID).stream().findFirst().get();

            /// translate from world to screen space -- update for zoom factor
            float screenX = (pos.x - camera.worldX) + (this.windowWidth / (camera.zoom * 2.0f));
            float screenY = (pos.y - camera.worldY) + (this.windowHeight / (camera.zoom * 2.0f));

            // camera.zoom increases with zoom in
            float h = rendSprite.getSizeY() * camera.zoom;
            float w = rendSprite.getSizeX() * camera.zoom;

            glBegin(GL_QUADS);
            glTexCoord2f(rendSprite.getU1(), rendSprite.getV1()); glVertex2f(screenX, screenY);
            glTexCoord2f(rendSprite.getU1(), rendSprite.getV2()); glVertex2f(screenX, screenY + h);
            glTexCoord2f(rendSprite.getU2(), rendSprite.getV2()); glVertex2f(screenX + w, screenY + h);
            glTexCoord2f(rendSprite.getU2(), rendSprite.getV1()); glVertex2f(screenX + w, screenY);
            glEnd();

        }

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
