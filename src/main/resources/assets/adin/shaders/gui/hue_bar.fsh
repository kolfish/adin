#version 330

#moj_import <adin:rect_coverage.glsl>

in vec4 vertexColor;
in vec2 localPosition;
flat in vec2 rectSize;
flat in float cornerRadius;
flat in int shape;

out vec4 fragColor;

vec3 hueToRgb(float hue) {
    vec3 p = abs(fract(hue + vec3(0.0, 2.0 / 3.0, 1.0 / 3.0)) * 6.0 - 3.0);
    return clamp(p - 1.0, 0.0, 1.0);
}

void main() {
    float coverage = rectCoverage(localPosition, rectSize, cornerRadius, shape);
    float hue = clamp(localPosition.x / rectSize.x, 0.0, 1.0);
    fragColor = vec4(hueToRgb(hue), vertexColor.a * coverage);
}
