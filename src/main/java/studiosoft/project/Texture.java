package studiosoft.project;

import de.matthiasmann.twl.utils.PNGDecoder;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

public class Texture {

    public final int id;
    public final int target = GL_TEXTURE_2D;
    // NEW: Store width and height for potential future use
    private final int width;
    private final int height;
    private final int tileSize;

    public Texture(URL textureSource, int tileSize) throws IOException {
        this.tileSize = tileSize;
        try (InputStream input = textureSource.openStream()) {
            PNGDecoder decoder = new PNGDecoder(input);
            this.width = decoder.getWidth();
            this.height = decoder.getHeight();

            final int bpp = 4; // 4 bytes per pixel for RGBA
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * height * bpp);
            decoder.decode(buffer, width * bpp, PNGDecoder.Format.RGBA);

            // IMPORTANT FIX: Flip the buffer.
            // This resets the position to 0 and sets the limit to the last position written.
            // OpenGL now knows how much data to read from the buffer.
            buffer.flip();

            id = glGenTextures();
            bind();

            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
            glTexParameteri(target, GL_TEXTURE_MIN_FILTER, GL_NEAREST); // Use NEAREST for sharp pixels in roguelikes
            glTexParameteri(target, GL_TEXTURE_MAG_FILTER, GL_NEAREST); // Use NEAREST for sharp pixels
            glTexParameteri(target, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
            glTexParameteri(target, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);

            glTexImage2D(target, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        }
    }

    public void bind() {
        glBindTexture(target, id);
    }

    public int getTileSize(){
        return tileSize;
    }

    public int getWidth(){
        return width;
    }

    public int getHeight(){
        return height;
    }
}