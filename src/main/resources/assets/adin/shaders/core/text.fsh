#version 330

uniform sampler2D Sampler0;

in vec2 texCoord;
in vec4 vertexColor;
out vec4 fragColor;

float median(float r, float g, float b) {
    return max(min(r, g), min(max(r, g), b));
}

void main() {
    vec3 msdf = texture(Sampler0, texCoord).rgb;
    float distance = median(msdf.r, msdf.g, msdf.b) - 0.5;
    vec2 unitRange = vec2(MSDF_RANGE) / vec2(textureSize(Sampler0, 0));
    vec2 screenTexSize = 1.0 / max(fwidth(texCoord), vec2(0.000001));
    float screenPxRange = max(0.5 * dot(unitRange, screenTexSize), 1.0);
    float coverage = clamp(distance * screenPxRange + 0.5, 0.0, 1.0);
    fragColor = vec4(vertexColor.rgb, vertexColor.a * coverage);
}
