#version 330

#moj_import <minecraft:globals.glsl>

uniform sampler2D Sampler0;

in vec4 vertexColor;
in vec2 texCoord0;
in vec3 relativePosition;
in vec3 viewPosition;
in vec3 viewNormal;
flat in ivec2 packedA;
flat in ivec2 packedB;
out vec4 fragColor;

const float SECONDS_PER_DAY = 1200.0;
const float ORIGIN_UNITS = 512.0;
const float SCALE_UNITS = 256.0;

vec3 gradientColor(vec3 base) {
    int encoded = packedA.x & 0xFFFF;
    vec3 end = vec3(float(encoded & 0xFF), float((encoded >> 8) & 0xFF), float(packedA.y & 0xFF)) / 255.0;
    float span = float(packedB.y - packedB.x);
    float t = span > 0.0 ? clamp((float(packedB.y) - gl_FragCoord.y) / span, 0.0, 1.0) : 0.0;
    return mix(base, end, t);
}

vec3 localPosition() {
    vec3 origin = vec3(float(packedA.x), float(packedA.y), float(packedB.x)) / ORIGIN_UNITS;
    float scale = max(float(packedB.y & 0xFFFF) / SCALE_UNITS, 0.001);
    return (relativePosition - origin) / scale;
}

vec2 surfaceUv() {
    vec3 local = localPosition();
    return vec2(local.x + local.z, local.y);
}

#moj_import <adin:fill/common.glsl>
#moj_import <adin:fill/galaxy.glsl>
#moj_import <adin:fill/metallic.glsl>
#moj_import <adin:fill/aurora.glsl>
#moj_import <adin:fill/ocean.glsl>
#moj_import <adin:fill/lava.glsl>
#moj_import <adin:fill/sky.glsl>

void main() {
    if (texture(Sampler0, texCoord0).a < 0.1) discard;
    vec3 base = vertexColor.rgb;
    float time = GameTime * SECONDS_PER_DAY;
#if EFFECT == 1
    vec3 color = starNest(base, time);
#elif EFFECT == 2
    vec3 color = metallic(base, time);
#elif EFFECT == 3
    vec3 color = auroras(base, time);
#elif EFFECT == 4
    vec3 color = ocean(base, time);
#elif EFFECT == 5
    vec3 color = lava(base, time);
#elif EFFECT == 6
    vec3 color = sky(base, time);
#else
    vec3 color = gradientColor(base);
#endif
    fragColor = vec4(clamp(color, 0.0, 1.0), vertexColor.a);
}
