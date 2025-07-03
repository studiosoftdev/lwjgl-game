package studiosoft.project;

public class Camera {
    public float worldX;
    public float worldY;
    public float zoom = 1.0f;

    public Camera(float startX, float startY) {
        this.worldX = startX;
        this.worldY = startY;
    }

    public Camera(float startX, float startY, float zoom) {
        this.worldX = startX;
        this.worldY = startY;
        this.zoom = zoom;
    }
}
