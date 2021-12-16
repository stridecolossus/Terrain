#version 450

layout(binding = 0) uniform sampler2D texSampler;

layout(location = 0) in vec2 fragCoords;
layout(location = 1) in float height;

layout(location = 0) out vec4 outColour;

void main() {
    const vec4 green = vec4(0.2, 0.5, 0.1, 1.0);
    const vec4 brown = vec4(0.6, 0.5, 0.2, 1.0);
    const vec4 white = vec4(1.0);
    
    vec4 col = mix(green, brown, smoothstep(0.0, 0.4, height));
    outColour = mix(col, white, smoothstep(0.5, 0.8, height));
}
