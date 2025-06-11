package studiosoft.project;

public class Sprite {
    private Texture textureAtlas;

    // UV coords for start of texture
    private float texU1, texV1, texU2, texV2;
    private int sizeX, sizeY;

    // number for how many tiles of the texture to include. tile size is set by the texture.
    private int tileSpanX;
    private int tileSpanY;

    public Sprite(Texture srcTex, int tileU, int tileV, int tileSpanX, int tileSpanY) {
        float tileSizeFracY = (float) srcTex.getTileSize() / srcTex.getHeight();
        float tileSizeFracX = (float) srcTex.getTileSize() / srcTex.getWidth();

        this.textureAtlas = srcTex;

        this.texU1 = tileU * tileSizeFracX;
        this.texV1 = tileV * tileSizeFracY;

        this.texU2 = texU1 + (tileSpanX  * tileSizeFracX);
        this.texV2 = texV1 + (tileSpanY * tileSizeFracY);

        this.sizeX = tileSpanX * srcTex.getTileSize();
        this.sizeY = tileSpanY * srcTex.getTileSize();
    }

    public float getU1(){
        return texU1;
    }

    public float getV1(){
        return texV1;
    }

    public float getU2(){
        return texU2;
    }

    public float getV2(){
        return texV2;
    }

    public int getSizeX() {
        return sizeX;
    }
    public int getSizeY() {
        return sizeY;
    }
}
