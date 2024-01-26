#version 330 core
out vec4 FragColour;

//in vec3 ourColour;
in vec2 TexCoord;

uniform sampler2D ourTexture; // sampler2D is type for dealing with 2D textures

void main()
{
    FragColour = texture(ourTexture, TexCoord); // 1st arg - texture sampler; 2nd arg - texture coordinates
}
