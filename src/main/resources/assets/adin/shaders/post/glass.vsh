#version 330

uniform sampler2D SharpSampler;
uniform sampler2D BlurSampler;

layout(std140) uniform GlassConfig {
    vec4 Rect;
    vec4 Light;
    vec4 Tint;
    vec4 Style;
};

out vec2 texCoord;
flat out float brightness;

const int PROBES_X = 9;
const int PROBES_Y = 3;
const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

void main() {
    vec2 corner = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
    gl_Position = vec4(corner * 2.0 - 1.0, 0.0, 1.0);
    texCoord = corner;
    vec2 size = vec2(textureSize(SharpSampler, 0));
    float sum = 0.0;
    for (int j = 0; j < PROBES_Y; j++) {
        for (int i = 0; i < PROBES_X; i++) {
            vec2 pixel = Rect.xy + Rect.zw * vec2((float(i) + 0.5) / float(PROBES_X), (float(j) + 0.5) / float(PROBES_Y));
            vec2 uv = vec2(pixel.x, size.y - pixel.y) / size;
            vec3 color = Style.x > 0.5 ? textureLod(SharpSampler, uv, 0.0).rgb : textureLod(BlurSampler, uv, 0.0).rgb;
            sum += dot(color, LUMA);
        }
    }
    brightness = sum / float(PROBES_X * PROBES_Y);
}
