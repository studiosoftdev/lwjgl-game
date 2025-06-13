package studiosoft.project;

public class Player extends Actor{

    private float moveSpeed = 1f;

    public Player(Sprite spr) {
        super(spr);
    }

    public Player(Sprite spr, float posX, float posY) {
        super(spr, posX, posY);
    }

    public Player(Sprite spr, float posX, float posY, int zLayer) {
        super(spr, posX, posY, zLayer);
    }

    public void moveX(boolean forwards) {
        deltaPos(moveSpeed * (forwards ? 1 : -1), 0f);
    }
    public void moveY(boolean forwards) {
        deltaPos(0f, moveSpeed * (forwards ? 1 : -1));
    }

    public void setMoveSpeed(float moveSpeed) {
        this.moveSpeed = moveSpeed;
    }

    public float getMoveSpeed() {
        return moveSpeed;
    }
}
