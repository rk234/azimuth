#version 450 core

in vec2 TexCoord;

uniform vec4 color;
uniform vec4 borderColor;
uniform float borderWidth;
uniform sampler2D fontBitmap;

out vec4 fragColor;

void main() {
    vec4 sampled = vec4(1.0, 1.0, 1.0, texture(text, TexCoords).r);
    color = vec4(textColor, 1.0) * sampled;
    fragColor = color;
}
