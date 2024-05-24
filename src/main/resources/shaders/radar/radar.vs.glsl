#version 450 core
uniform sampler1D colortable;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

layout (location = 0) in float azimuth;
layout (location = 1) in float range;
layout (location = 2) in float data;

out vec4 vertColor;

void main() {
    vertColor = texture(colortable, data);
    gl_Position = projectionMatrix * transformMatrix * vec3(
            range * cos(azimuth),
            range * sin(azimuth)
    );
}