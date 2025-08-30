#version 450 core
layout (location = 0) in vec2 positionGeo;
layout (location = 1) in vec2 nextGeo;
layout (location = 2) in vec2 previousGeo;
layout (location = 3) in float direction;

uniform mat4 projection;
uniform mat4 transform;
uniform float aspect;
uniform vec2 resolution;
uniform float thickness;
uniform float outlineWidth;
uniform int miter;

out float normalizedDistanceFromCenter;

void main() {
    vec3 position = vec3(positionGeo, 0);
    vec3 previous = vec3(previousGeo, 0);
    vec3 next = vec3(nextGeo, 0);

    vec2 aspectVec = vec2(aspect, 1.0);
    mat4 projViewModel = projection * transform;
    vec4 previousProjected = projViewModel * vec4(previous, 1.0);
    vec4 currentProjected = projViewModel * vec4(position, 1.0);
    vec4 nextProjected = projViewModel * vec4(next, 1.0);

    vec2 currentScreen = currentProjected.xy / currentProjected.w * aspectVec;
    vec2 previousScreen = previousProjected.xy / previousProjected.w * aspectVec;
    vec2 nextScreen = nextProjected.xy / nextProjected.w * aspectVec;

    float totalThickness = thickness + outlineWidth * 2.0;
    float len = totalThickness;
    float orientation = direction;

    vec2 dir = vec2(0.0);
    if (currentScreen == previousScreen) {
        dir = normalize(nextScreen - currentScreen);
    }
    else if (currentScreen == nextScreen) {
        dir = normalize(currentScreen - previousScreen);
    }
    else {
        vec2 dirA = normalize((currentScreen - previousScreen));
        if (miter == 1) {
            vec2 dirB = normalize((nextScreen - currentScreen));
            vec2 tangent = normalize(dirA + dirB);
            vec2 perp = vec2(-dirA.y, dirA.x);
            vec2 miter = vec2(-tangent.y, tangent.x);
            dir = tangent;
            len = totalThickness / dot(miter, perp);
        } else {
            dir = dirA;
        }
    }

    vec2 normal = vec2(-dir.y, dir.x);
    normal *= len / 2.0;
    normal.x /= aspect;

    vec4 normal4 = vec4(normal, 0.0, 1.0);
    normal4 *= projection;
    normal4.xy *= currentProjected.w;
    normal4.xy /= (vec4(resolution, 0.0, 1.0) * projection).xy;

    normal = normal4.xy;
    vec4 offset = vec4(normal * orientation * aspectVec, 0.0, 0.0);
    gl_Position = currentProjected + offset;

    // Pass normalized distance: 0 = center, 1 = edge of total thickness
    // Since orientation is ±1, we map it to [0, 1] where 0.5 is center
    normalizedDistanceFromCenter = (orientation + 1.0) * 0.5;

    gl_PointSize = 1.0;
}
