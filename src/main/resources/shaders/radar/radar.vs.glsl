#version 450 core
uniform sampler1D colortable;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

layout (location = 0) in float azimuth;
layout (location = 1) in float range;
layout (location = 2) in float data;

out float fragData;

void main() {
    gl_Position = projectionMatrix * transformMatrix * vec4(
            range * cos(azimuth),
            range * sin(azimuth),
            0.0,
            1.0
    );
    fragData = data;
}