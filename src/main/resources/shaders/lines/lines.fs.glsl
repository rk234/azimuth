#version 450 core
uniform vec4 color;
uniform vec4 outlineColor;
uniform float thickness;
uniform float outlineWidth;

in float normalizedDistanceFromCenter;

out vec4 fragColor;

void main() {
    float totalThickness = thickness + outlineWidth * 2.0;

    // Calculate the actual distance from center [0, 1] where 0.5 is the center
    float distFromCenter = abs(normalizedDistanceFromCenter - 0.5) * 2.0;

    // Calculate threshold: main line occupies the center portion
    float mainLineThreshold = thickness / totalThickness;

    // Determine which region we're in
    if (distFromCenter <= mainLineThreshold) {
        // We're in the main line region
        fragColor = color;
    } else {
        // We're in the outline region
        fragColor = outlineColor;
    }
}
