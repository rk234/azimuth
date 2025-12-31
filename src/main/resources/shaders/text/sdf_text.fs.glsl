#version 450 core

in vec2 TexCoord;

uniform vec4 color;
uniform vec4 borderColor;
uniform float borderWidth;
uniform sampler2D fontBitmap;
uniform float pxrange;
uniform float fontSize;

out vec4 fragColor;

void main() {
    // Sample single-channel SDF texture (value 0-1, with 0.5 being the edge)
    float dist = texture(fontBitmap, TexCoord).r;

    // Calculate smoothing based on pixel range and font size
    float smoothing = clamp(pxrange / fontSize, 0.001, 0.5);

    // Edge threshold (0.5 is the glyph boundary in SDF)
    const float edge = 0.5;


    float alpha;
    vec3 outColor;

    if (borderWidth > 0.0) {
        // Border/outline rendering
        // Scale border width to SDF space (typical range 0.0-0.5)
        float scaledBorderWidth = borderWidth * smoothing;
        float borderEdge = edge - scaledBorderWidth;

        // Ensure borderEdge doesn't go below 0
        borderEdge = max(borderEdge, smoothing);

        // Outer edge alpha (includes border)
        float outerAlpha = smoothstep(borderEdge - smoothing, borderEdge + smoothing, dist);
        // Inner edge alpha (fill only)
        float innerAlpha = smoothstep(edge - smoothing, edge + smoothing, dist);

        // Blend between border color and fill color
        outColor = mix(borderColor.rgb, color.rgb, innerAlpha);
        alpha = outerAlpha * mix(borderColor.a, color.a, innerAlpha);
    } else {
        alpha = smoothstep(edge - smoothing, edge + smoothing, dist);
        outColor = color.rgb;
        alpha *= color.a;
    }

    fragColor = vec4(outColor, alpha);
}
