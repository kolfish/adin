#version 330

#moj_import <adin:rect_coverage.glsl>

in vec4 vertexColor;
in vec2 localPosition;
flat in vec2 rectSize;
flat in float cornerRadius;

out vec4 fragColor;

void main() {
    float coverage = rectCoverage(localPosition, rectSize, cornerRadius);
    float saturation = clamp(localPosition.x / rectSize.x, 0.0, 1.0);
    float value = 1.0 - clamp(localPosition.y / rectSize.y, 0.0, 1.0);
    vec3 rgb = value * mix(vec3(1.0), vertexColor.rgb, saturation);
    fragColor = vec4(rgb, vertexColor.a * coverage);
}
