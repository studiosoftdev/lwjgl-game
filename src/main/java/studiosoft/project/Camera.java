package studiosoft.project;

import org.joml.Matrix4f;

public class Camera {
    public float worldX;
    public float worldY;
    public float zoom = 1.0f;
    private Matrix4f viewMatrix;

    public Camera(float startX, float startY) {
        this.worldX = startX;
        this.worldY = startY;
        this.viewMatrix = new Matrix4f();
    }

    public Camera(float startX, float startY, float zoom) {
        this.worldX = startX;
        this.worldY = startY;
        this.zoom = zoom;
    }

    public Matrix4f getViewMatrix() {
        viewMatrix = new Matrix4f().translate(-this.worldX, -this.worldY, 0).scale(this.zoom);
        return viewMatrix;
    }
}
