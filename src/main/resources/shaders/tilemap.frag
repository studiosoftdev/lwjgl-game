#version 330 core

out vec4 FragColor;
in vec2 TexCoord; // Comes from the VBO

uniform sampler2D texture_sampler;
uniform vec4 spriteUVs;
uniform bool useUVRemapping; // Our new switch

void main()
{
    vec2 finalUV;
    if (useUVRemapping) {
        // If it's a sprite, remap the generic 0-1 UVs
        finalUV = spriteUVs.xy + (TexCoord * spriteUVs.zw);
    } else {
        // If it's the tilemap, use the UVs from the VBO directly
        finalUV = TexCoord;
    }

    FragColor = texture(texture_sampler, finalUV);
}