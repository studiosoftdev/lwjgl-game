#version 330 core

// A vertex's position (e.g., {x, y})
// location = 0 corresponds to your first glVertexAttribPointer call
layout (location = 0) in vec2 aPos;

// A vertex's texture coordinate (e.g., {u, v})
// location = 1 corresponds to your second glVertexAttribPointer call
layout (location = 1) in vec2 aTexCoord;

// The projection matrix we'll send from our Java code
uniform mat4 projection;

// This allows camera to pan/zoom around the world space
uniform mat4 view;

// whatever this is, something to do with textures idk
uniform mat4 model;

// An output variable to pass the texture coordinate to the fragment shader
out vec2 TexCoord;

void main()
{
    // Calculate the final screen position of the vertex
    gl_Position = projection * view * model * vec4(aPos, 0.0, 1.0);

    // Pass the texture coordinate to the next stage
    TexCoord = aTexCoord;
}