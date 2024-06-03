#version 450 core
layout (location = 0) in vec2 positionGeo;
layout (location = 1) in float direction;
layout (location = 2) in vec2 nextGeo;
layout (location = 3) in vec2 previousGeo;

uniform mat4 projection;
uniform mat4 transform;
uniform float aspect; //The aspect ratio of the viewport. Can be obtained from the camera object.
uniform vec2 resolution;

uniform float thickness;
uniform int miter; // 1 for mitter, 0 for no mitter

#define PI 3.1415926535897932384626433832795

//Takes in latitude and logitude in radians, returns web mercator coordinates at zoom level zero
vec2 latlngToCartesian(float lat, float lon) {
    float zoom = 0;

    float x = (1.0f / (2*PI)) * pow(2, zoom) * (PI - lon);
    float y = (1.0f / (2*PI)) * pow(2, zoom) * (PI - log(tan(PI/4 + lat/2)));

    return vec2(x, y);
}

void main() {
    vec3 position = vec3(latlngToCartesian(radians(positionGeo.x), radians(positionGeo.y)), 0);
    vec3 previous = vec3(latlngToCartesian(radians(previousGeo.x), radians(previousGeo.y)), 0);
    vec3 next = vec3(latlngToCartesian(radians(nextGeo.x), radians(nextGeo.y)), 0);

    vec2 aspectVec = vec2(aspect, 1.0);
    mat4 projViewModel = projection * transform;
    vec4 previousProjected = projViewModel * vec4(previous, 1.0);
    vec4 currentProjected = projViewModel * vec4(position, 1.0);
    vec4 nextProjected = projViewModel * vec4(next, 1.0);

    //vec4 finalPosition = projViewModel * vec4(position, 1.0);

    //get 2D screen space with W divide and aspect correction
    vec2 currentScreen = currentProjected.xy / currentProjected.w * aspectVec;
    vec2 previousScreen = previousProjected.xy / previousProjected.w * aspectVec;
    vec2 nextScreen = nextProjected.xy / nextProjected.w * aspectVec;

    float len = thickness;
    float orientation = direction;

    //starting point uses (next - current)
    vec2 dir = vec2(0.0);
    if (currentScreen == previousScreen) {
        dir = normalize(nextScreen - currentScreen);
    }
    //ending point uses (current - previous)
    else if (currentScreen == nextScreen) {
        dir = normalize(currentScreen - previousScreen);
    }
    //somewhere in middle, needs a join
    else {
        //get directions from (C - B) and (B - A)
        vec2 dirA = normalize((currentScreen - previousScreen));
        if (miter == 1) {
            vec2 dirB = normalize((nextScreen - currentScreen));
            //now compute the miter join normal and length
            vec2 tangent = normalize(dirA + dirB);
            vec2 perp = vec2(-dirA.y, dirA.x);
            vec2 miter = vec2(-tangent.y, tangent.x);
            dir = tangent;
            len = thickness / dot(miter, perp);
        } else {
            dir = dirA;
        }
    }
    vec2 normal = vec2(-dir.y, dir.x);
    normal *= len / 2.0;
    normal.x /= aspect;

    vec4 normal4 = vec4(normal, 0.0, 1.0);

    //SIZE ATTENUATION HERE
    normal4 *= projection;
    normal4.xy *= currentProjected.w;
    normal4.xy /= (vec4(resolution, 0.0, 1.0) * projection).xy;

    normal = normal4.xy;
    vec4 offset = vec4(normal * orientation * aspectVec, 0.0, 0.0);
    gl_Position = currentProjected + offset;
    gl_PointSize = 1.0;
}
