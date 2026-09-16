#version 330

#moj_import <adin:rect_coverage.glsl>

uniform sampler2D SharpSampler;
uniform sampler2D BlurSampler;

layout(std140) uniform GlassConfig {
    vec4 Rect;
    vec4 Light;
    vec4 Tint;
    vec4 Style;
};

in vec2 texCoord;
flat in float brightness;

out vec4 fragColor;

const int ALL_CORNERS = 15;
const float REFRACTIVE_INDEX = 1.5;
const float DISPERSION = 0.18;
const float BEZEL = 14.0;
const float BEZEL_FRACTION = 0.25;
const float THICKNESS = 1.8;
const float SATURATION = 1.2;
const float TINT_REGULAR = 0.22;
const float TINT_CLEAR = 0.08;
const float REFLECTANCE = 0.04;
const float RIM = 0.9;
const float RIM_AMBIENT = 0.3;
const float HIGHLIGHT = 1.0;
const float COUNTER_HIGHLIGHT = 0.4;
const float SUPERSAMPLE_BAND = 0.35;
const float SHADOW_DROP = 1.5;
const float SHADOW_SPREAD = 0.3;
const vec3 DARK_TINT = vec3(20.0 / 255.0);
const vec3 LUMA = vec3(0.2126, 0.7152, 0.0722);

vec2 screenSize() {
    return vec2(textureSize(SharpSampler, 0));
}

vec3 material(vec2 local) {
    vec2 size = screenSize();
    vec2 pixel = Rect.xy + local * Light.w;
    vec2 uv = vec2(pixel.x, size.y - pixel.y) / size;
    return Style.x > 0.5 ? texture(SharpSampler, uv).rgb : texture(BlurSampler, uv).rgb;
}

float surface(float x) {
    return sqrt(max(1.0 - (1.0 - x) * (1.0 - x), 0.0));
}

float surfaceSlope(float x) {
    return (1.0 - x) / max(surface(x), 0.001);
}

vec3 surfaceNormal(float edge, float bezel, vec2 outward) {
    if (edge >= bezel) return vec3(0.0, 0.0, 1.0);
    float x = clamp(edge / bezel, 0.002, 1.0);
    return normalize(vec3(outward * surfaceSlope(x) * THICKNESS, 1.0));
}

vec3 refracted(vec2 local, float edge, float bezel, vec2 outward) {
    if (edge >= bezel) return material(local);
    vec3 ray = refract(vec3(0.0, 0.0, -1.0), surfaceNormal(edge, bezel, outward), 1.0 / REFRACTIVE_INDEX);
    vec2 shift = ray.xy / max(-ray.z, 0.001) * bezel * THICKNESS * surface(clamp(edge / bezel, 0.002, 1.0));
    return vec3(material(local + shift * (1.0 - DISPERSION)).r, material(local + shift).g,
            material(local + shift * (1.0 + DISPERSION)).b);
}

vec2 boxGradient(vec2 position, vec2 rectSize, float radius) {
    vec2 centered = position - rectSize * 0.5;
    vec2 q = abs(centered) - rectSize * 0.5 + radius;
    vec2 direction = q.x > 0.0 && q.y > 0.0 ? normalize(q) : (q.x > q.y ? vec2(1.0, 0.0) : vec2(0.0, 1.0));
    return direction * sign(centered);
}

void main() {
    vec2 size = screenSize();
    vec2 pixel = texCoord * size;
    vec2 local = (vec2(pixel.x, size.y - pixel.y) - Rect.xy) / Light.w;
    vec2 rectSize = Rect.zw / Light.w;
    float shortSide = min(rectSize.x, rectSize.y);
    float corner = min(Style.z, shortSide * 0.5);
    float bezel = min(shortSide * BEZEL_FRACTION, BEZEL);
    float lift = min(shortSide * 0.5, BEZEL);
    float coverage = rectCoverage(local, rectSize, corner, ALL_CORNERS);
    float alpha = 0.0;
    vec3 color = vec3(0.0);
    if (coverage < 1.0) {
        float shadowDistance = boxDistance(local - vec2(0.0, SHADOW_DROP), rectSize, corner);
        float spread = SHADOW_SPREAD * lift;
        float strength = 0.16 + 0.12 * (1.0 - brightness);
        float shadow = shadowDistance > 0.0 ? strength * exp(-(shadowDistance * shadowDistance) / (2.0 * spread * spread)) : strength;
        alpha += shadow * (1.0 - coverage);
    }
    if (coverage > 0.0) {
        vec2 outward = boxGradient(local, rectSize, corner);
        float edge = max(-boxDistance(local, rectSize, corner), 0.0);
        vec3 glass;
        if (edge < bezel * SUPERSAMPLE_BAND) {
            float step = 0.25 / Light.w;
            glass = 0.5 * (refracted(local - outward * step, edge + step, bezel, outward)
                    + refracted(local + outward * step, max(edge - step, 0.0), bezel, outward));
        } else {
            glass = refracted(local, edge, bezel, outward);
        }
        float grey = dot(glass, LUMA);
        glass = clamp(grey + (glass - grey) * SATURATION, 0.0, 1.0);
        bool clear = Style.x > 0.5;
        glass = mix(glass, mix(DARK_TINT, vec3(1.0), smoothstep(0.30, 0.60, brightness)), clear ? TINT_CLEAR : TINT_REGULAR);
        glass = mix(glass, Tint.rgb, Tint.a);
        vec3 normal = surfaceNormal(edge, bezel, outward);
        float fresnel = (1.0 - REFLECTANCE) * pow(1.0 - normal.z, 5.0);
        float facing = dot(outward, normalize(Light.xy));
        float lit = RIM_AMBIENT + HIGHLIGHT * max(facing, 0.0) + COUNTER_HIGHLIGHT * max(-facing, 0.0);
        glass = mix(glass, vec3(1.0), clamp(fresnel * RIM * lit * (1.0 + 0.5 * Light.z), 0.0, 1.0));
        color = glass * coverage;
        alpha += coverage;
    }
    fragColor = vec4(color, alpha) * Style.y;
}
