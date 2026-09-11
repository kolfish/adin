#version 330

const int FILLET = 16;
const int CORNER_MASK = 15;

float cornerRadiusAt(vec2 position, vec2 rectSize, float cornerRadius, int corners) {
    bool right = position.x > rectSize.x * 0.5;
    bool bottom = position.y > rectSize.y * 0.5;
    int corner = right ? (bottom ? 4 : 2) : (bottom ? 8 : 1);
    return (corners & corner) != 0 ? cornerRadius : 0.0;
}

float boxDistance(vec2 position, vec2 rectSize, float radius) {
    vec2 halfSize = rectSize * 0.5;
    vec2 q = abs(position - halfSize) - halfSize + radius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - radius;
}

vec2 filletCenter(vec2 rectSize, int corner) {
    return vec2((corner & 6) != 0 ? rectSize.x : 0.0, (corner & 12) != 0 ? rectSize.y : 0.0);
}

float shapeDistance(vec2 position, vec2 rectSize, float cornerRadius, int shape) {
    if ((shape & FILLET) != 0) {
        float outsideCircle = cornerRadius - length(position - filletCenter(rectSize, shape & CORNER_MASK));
        return max(boxDistance(position, rectSize, 0.0), outsideCircle);
    }
    return boxDistance(position, rectSize, cornerRadiusAt(position, rectSize, cornerRadius, shape & CORNER_MASK));
}

#ifndef AA_SOFTNESS
#define AA_SOFTNESS 1.0
#endif

const float SOFTEN_FULL_RADIUS = 4.0;
const float SOFTEN_RAMP = 1.0;

float arcDepth(vec2 position, vec2 rectSize, float cornerRadius, int shape) {
    if ((shape & FILLET) != 0) {
        float outsideCircle = cornerRadius - length(position - filletCenter(rectSize, shape & CORNER_MASK));
        return outsideCircle - boxDistance(position, rectSize, 0.0);
    }
    float radius = cornerRadiusAt(position, rectSize, cornerRadius, shape & CORNER_MASK);
    if (radius <= 0.0) return -1.0;
    vec2 q = abs(position - rectSize * 0.5) - rectSize * 0.5 + radius;
    return min(q.x, q.y);
}

float rectCoverage(vec2 localPosition, vec2 rectSize, float cornerRadius, int shape) {
    float distanceToEdge = shapeDistance(localPosition, rectSize, cornerRadius, shape);
    float pixel = max(length(vec2(dFdx(distanceToEdge), dFdy(distanceToEdge))), 0.0001);
    float arc = clamp(arcDepth(localPosition, rectSize, cornerRadius, shape) / (pixel * SOFTEN_RAMP), 0.0, 1.0)
            * clamp(cornerRadius / (pixel * SOFTEN_FULL_RADIUS), 0.0, 1.0);
    float crisp = clamp(0.5 - distanceToEdge / pixel, 0.0, 1.0);
    float soft = 1.0 - smoothstep(-0.5, 0.5, distanceToEdge / (pixel * AA_SOFTNESS));
    return mix(crisp, soft, arc);
}
