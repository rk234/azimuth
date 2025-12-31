#version 450 core

layout (location = 0) in vec2 aPos;
layout (location = 1) in vec2 aTexCoord;
layout (location = 2) in vec2 anchor; // world space anchor point

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;
uniform float zoom;

out vec2 TexCoord;

void main() {
    vec4 screenPos = transformMatrix * vec4(anchor, 0.0, 1.0);
    gl_Position = projectionMatrix * (vec4((aPos / zoom) + screenPos.xy, 0.0, 1.0));
    TexCoord = aTexCoord;
}
