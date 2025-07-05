package studiosoft.project;

import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class ShaderProgram {

    private final int programId;
    private int vertexShaderId;
    private int fragmentShaderId;
    private final Map<String, Integer> uniforms;

    public ShaderProgram(String vertexSource, String fragmentSource) {
        programId = glCreateProgram();
        if(programId == 0) {
            throw new RuntimeException("Could not create shader program");
        }
        uniforms = new HashMap<>();

        createVertexShader(vertexSource);
        createFragmentShader(fragmentSource);
        link();
    }

    public void createVertexShader(String shaderCode) {
        vertexShaderId = createShader(shaderCode, GL_VERTEX_SHADER);
    }

    public void createFragmentShader(String shaderCode) {
        fragmentShaderId = createShader(shaderCode, GL_FRAGMENT_SHADER);
    }

    protected int createShader(String shaderCode, int shaderType) {
        int shaderId = glCreateShader(shaderType);
        if(shaderId == 0) {
            throw new RuntimeException("Could not create shader of type: " + shaderType);
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if(glGetShaderi(shaderId, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error compiling shader code: " + glGetShaderInfoLog(shaderId, 1024));
        }

        glAttachShader(programId, shaderId);
        return shaderId;
    }

    private void link() {
        glLinkProgram(programId);
        if(glGetProgrami(programId, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException("Error linking program: " + glGetProgramInfoLog(programId, 1024));
        }

        if(vertexShaderId != 0){
            glDetachShader(programId, vertexShaderId);
        }
        if(fragmentShaderId != 0){
            glDetachShader(programId, fragmentShaderId);
        }

        //glValidateProgram(programId);
        //if(glGetProgrami(programId, GL_VALIDATE_STATUS) == GL_FALSE) {
        //    System.err.println("Error validating program: " + glGetProgramInfoLog(programId, 1024));
        //}
    }

    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if(uniformLocation < 0){
            throw new RuntimeException("Could not get uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
    }

    public void createUniform(String uniformName, Matrix4f value) {
        int uniformLocation = glGetUniformLocation(programId, uniformName);
        if(uniformLocation < 0){
            throw new RuntimeException("Could not get uniform: " + uniformName);
        }
        uniforms.put(uniformName, uniformLocation);
        setUniform(uniformName, value);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        //dump matrix into float buffer
        try (MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            value.get(fb);
            glUniformMatrix4fv(uniforms.get(uniformName), false, fb);
        }
    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, Vector4f value) {
        glUniform4f(uniforms.get(uniformName), value.x, value.y, value.z, value.w);
    }

    public void setUniform(String uniformName, boolean value) {
        // A false is 0, a true is 1
        glUniform1i(uniforms.get(uniformName), value ? 1 : 0);
    }

    public void bind(){
        glUseProgram(programId);
    }

    public void unbind(){
        glUseProgram(0);
    }

    public void cleanup(){
        unbind();
        if(programId != 0){
            glDeleteProgram(programId);
        }
    }
}
