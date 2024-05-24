#version 450 core

layout (location = 0) in vec3 aPos;
layout (location = 1) in vec3 color;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

out vec4 vertColor;

void main() {
    gl_Position = projectionMatrix * transformMatrix * vec4(aPos, 1.0);
    vertColor = vec4(color, 1.0f);
}