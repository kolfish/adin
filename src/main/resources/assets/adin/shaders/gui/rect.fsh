#version 330

#moj_import <adin:rect_coverage.glsl>

in vec4 vertexColor;
in vec2 localPosition;
flat in vec2 rectSize;
flat in float cornerRadius;

out vec4 fragColor;

void main() {
    float coverage = rectCoverage(localPosition, rectSize, cornerRadius);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * coverage);
}
