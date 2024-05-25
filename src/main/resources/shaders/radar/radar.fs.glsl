#version 450 core

in float fragData;
out vec4 fragColor;

uniform sampler1D colormap;

void main() {
    fragColor = vec4(texture(colormap, fragData).xyz, 1);
}
