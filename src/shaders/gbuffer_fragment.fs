#version 330 core

layout(location = 0) out vec3 gPosition;
layout(location = 1) out vec3 gAlbedo;

in vec3 FragPos;
in vec3 colour;

void main() {
	gPosition = FragPos;
	gAlbedo = colour;
}