#version 330

float roundedDistance(vec2 position, vec2 rectSize, float cornerRadius) {
    vec2 halfSize = rectSize * 0.5;
    vec2 q = abs(position - halfSize) - halfSize + cornerRadius;
    return length(max(q, 0.0)) + min(max(q.x, q.y), 0.0) - cornerRadius;
}

float rectCoverage(vec2 localPosition, vec2 rectSize, float cornerRadius) {
    vec2 dx = dFdx(localPosition);
    vec2 dy = dFdy(localPosition);
    float distanceToEdge = roundedDistance(localPosition, rectSize, cornerRadius);
    float footprint = max(length(dx) + length(dy), 0.0001);
    float coverage = distanceToEdge < 0.0 ? 1.0 : 0.0;
    float filterWidth = max(1.1 * length(vec2(dFdx(distanceToEdge), dFdy(distanceToEdge))), 0.0001);
    if (abs(distanceToEdge) < footprint) {
        coverage = 0.0;
        for (int y = 0; y < 4; ++y) {
            for (int x = 0; x < 4; ++x) {
                vec2 offset = (float(x) - 1.5) * 0.25 * dx + (float(y) - 1.5) * 0.25 * dy;
                float d = roundedDistance(localPosition + offset, rectSize, cornerRadius);
                coverage += 1.0 - smoothstep(-filterWidth, filterWidth, d);
            }
        }
        coverage *= 0.0625;
    }
    return coverage;
}
