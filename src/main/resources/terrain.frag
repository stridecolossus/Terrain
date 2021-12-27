#version 450

layout(binding=0) uniform sampler2D heightMap;

layout(location=0) in vec2 inCoord;

layout(location=0) out vec4 outColour;

/*
void main() {
    const vec4 green = vec4(0.2, 0.5, 0.1, 1.0);
    const vec4 brown = vec4(0.6, 0.5, 0.2, 1.0);
    const vec4 white = vec4(1.0);

    outColour = texture(heightMap, inCoord);
//    float height = texture(heightMap, inCoord).r;
    
 //   vec4 col = mix(green, brown, smoothstep(0.0, 0.4, height));
   // outColour = mix(col, white, smoothstep(0.6, 0.9, height));
}
*/

void main() {
    outColour = texture(heightMap, inCoord);
//    outColour = vec4(inCoord.x, inCoord.y, 0.0, 1.0);
}
