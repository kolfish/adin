#version 330

uniform sampler2D InSampler;

layout(std140) uniform OutlineConfig {
    float Width;
    vec4 Color;
    float Fill;
    float Layers;
    float Glow;
};

in vec2 texCoord;

out vec4 fragColor;

void main() {
    ivec2 size = textureSize(InSampler, 0);
    ivec2 at = ivec2(gl_FragCoord.xy);
    float reach = Width * (2.0 * Layers + 1.0);
    int taps = int(ceil(reach));
    float nearestSq = -1.0;
    vec3 color = vec3(0.0);
    for (int dy = -taps; dy <= taps; dy++) {
        int y = at.y + dy;
        if (y < 0 || y >= size.y) continue;
        vec4 row = texelFetch(InSampler, ivec2(at.x, y), 0);
        if (row.a <= 0.0) continue;
        float dx = (1.0 - row.a) * (reach + 1.0);
        float distSq = dx * dx + float(dy * dy);
        if (nearestSq < 0.0 || distSq < nearestSq) {
            nearestSq = distSq;
            color = row.rgb;
        }
    }
    if (nearestSq < 0.0) discard;
    float nearest = sqrt(nearestSq);
    if (nearest > reach + 0.5) discard;
    vec3 rgb = Color.a > 0.0 ? Color.rgb : color;
    if (nearest < 0.5) {
        if (Fill <= 0.0) discard;
        fragColor = vec4(rgb, Fill);
        return;
    }
    float t = (nearest - 0.5) / Width;
    int band = int(floor(t));
    if (band > int(2.0 * Layers) || (band & 1) == 1) discard;
    float inner = band == 0 ? 1.0 : clamp((t - float(band)) * Width, 0.0, 1.0);
    float outer = clamp((float(band) + 1.0 - t) * Width, 0.0, 1.0);
    rgb = mix(rgb, vec3(1.0), min(Glow, 3.0) * 0.15);
    fragColor = vec4(rgb, min(inner, outer));
}
