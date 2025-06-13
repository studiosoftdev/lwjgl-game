package studiosoft.project;

public class Actor {
    private Sprite sprite;

    //screen-space
    private float posX, posY;
    private float widthSF = 1f;
    private float heightSF = 1f;

    //in case things are logically intended to exist on separate layers, eg rendering or game logic
    private int zLayer;

    //layer 0: background
    //layer 1: collision entities (walls etc)
    //layer 2: interaction entities (items etc)
    //layer 3: npcs (friendly, enemies)
    //layer 4: player
    //layer 5: UI

    //define collision bounds in this class?
    //rendering groups (per texture to .bind is managed easily)?

    public Actor(Sprite spr){
        this.sprite = spr;
        this.posX = 0;
        this.posY = 0;
        this.zLayer = 0;
    }

    public Actor(Sprite spr, float posX, float posY, int zLayer){
        this.sprite = spr;
        this.posX = posX;
        this.posY = posY;
        this.zLayer = zLayer;
    }

    public Actor(Sprite spr, float posX, float posY, float widthSF, float heightSF){
        this.sprite = spr;
        this.posX = posX;
        this.posY = posY;
        this.widthSF = widthSF;
        this.heightSF = heightSF;
    }

    public Actor(Sprite spr, float posX, float posY){
        this.sprite = spr;
        this.posX = posX;
        this.posY = posY;
    }

    public void deltaPos(float x, float y){
        this.posX += x;
        this.posY += y;
    }

    public float getPosX() {
        return posX;
    }

    public float getPosY() {
        return posY;
    }

    public float getWidthSF() {
        return widthSF;
    }
    public float getHeightSF() {
        return heightSF;
    }

    public Sprite getSprite(){
        return sprite;
    }
}
