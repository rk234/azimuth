#version 450 core
uniform sampler1D colortable;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

layout (location = 0) in vec2 pos;
layout (location = 1) in float data;

out float fragData;

void main() {
    gl_Position = projectionMatrix * transformMatrix * vec4(
        pos.x,
        pos.y,
        0.0,
        1.0
    );
    fragData = data;
}