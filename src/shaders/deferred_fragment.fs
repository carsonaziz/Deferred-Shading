#version 330 core

in vec2 TextCoord;

out vec4 FragColour;

uniform sampler2D gPosition;
uniform sampler2D gAlbedo;

void main() {
	FragColour = texture(gAlbedo, TextCoord);
}