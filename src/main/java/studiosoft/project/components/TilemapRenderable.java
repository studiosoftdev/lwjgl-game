package studiosoft.project.components;

public class TilemapRenderable {
    public int[][] tileMap;
    public int tileWidth;
    public int tileHeight;
    public int tilemapWidth;
    public int tilemapHeight;
    public boolean isDirty;

    public TilemapRenderable(int[][] tileMap, int tileWidth, int tileHeight, int tilemapWidth, int tilemapHeight,
                             boolean isDirty) {
        this.tileWidth = tileWidth;
        this.tileHeight = tileHeight;
        this.tilemapWidth = tilemapWidth;
        this.tilemapHeight = tilemapHeight;
        this.tileMap = tileMap;
        this.isDirty = isDirty;
    }

    //call thsi constructure for test setup
    public TilemapRenderable(int test){
        tileMap = new int[][]{{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
                {0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 1},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
                {1, 1, 1, 1, 0, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0},
                {1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0, 0},
                {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 0, 0}};
        tileWidth = 16;
        tileHeight = 16;
        tilemapWidth = 3;
        tilemapHeight = 3;
        isDirty = true;
    }
}
