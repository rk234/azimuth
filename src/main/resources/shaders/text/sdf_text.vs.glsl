#version 450 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

out vec2 TexCoord;


void main() {
    gl_Position = projectionMatrix * transformMatrix * vec4(aPos, 0.0, 1.0);
    TexCoord = aTexCoord;
}
