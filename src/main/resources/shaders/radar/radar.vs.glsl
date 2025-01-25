#version 450 core

const float radiusMajor = 6378137.0;
const float PI = 3.14159265;

uniform sampler1D colortable;

uniform mat4 projectionMatrix;
uniform mat4 transformMatrix;

uniform vec2 stationLatLon;
uniform float elevation;

layout (location = 0) in vec2 azimuthRange;
layout (location = 1) in float data;

out float fragData;

vec2 antennaToCartesian(float azimuth, float range, float elevation) {
    float thetaE = radians(elevation);
    float thetaA = radians(azimuth);

    float R = 6371.0 * 1000.0 * 4.0 / 3.0;
    float r = range;

    float z = sqrt(r*r + R*R + 2*r*R*sin(thetaE)) - R;
    float s = R * asin(r * cos(thetaE) / (R + z));
    float x = s*sin(thetaA);
    float y = s*cos(thetaA);

    return vec2(x, y);
}

vec2 antennaRelativeCartesianToGeographic(vec2 cartesian) {
    float x = cartesian.x;
    float y = cartesian.y;

    float R = 6370997.0;
    float antennaLatRad = radians(stationLatLon.x);
    float antennaLonRad = radians(stationLatLon.y);

    float rho = sqrt(x*x+y*y);
    float c = rho / R;

    float latRad = asin(
        cos(c) * sin(antennaLatRad) + y * sin(c) * cos(antennaLatRad) / rho
    );
    float latDeg = degrees(latRad);

    float x1 = x * sin(c);
    float x2 = rho * cos(antennaLatRad) * cos(c) - y * sin(antennaLatRad) * sin(c);

    float lonRad = antennaLonRad + atan(x1, x2);
    float lonDeg = degrees(lonRad);

    return vec2(latDeg, lonDeg);
}


vec2 aerToGeo(float azimuth, float elevation, float range) {
    vec2 cartesian = antennaToCartesian(azimuth, range, elevation);
    vec2 latLon = antennaRelativeCartesianToGeographic(cartesian);
    return latLon;
}

vec2 mercator(vec2 latlon) {
    float x = radians(latlon.y) * radiusMajor;
    float y = log(tan(PI / 4 + radians(latlon.x) / 2)) * radiusMajor;
    return vec2(x, y);
}

void main() {
    vec2 p = mercator(aerToGeo(azimuthRange.x, elevation, azimuthRange.y));

    gl_Position = projectionMatrix * transformMatrix * vec4(
        p.x,
        p.y,
        0.0,
        1.0
    );
    fragData = data;
}