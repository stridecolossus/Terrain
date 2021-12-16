#version 450

layout(quads, equal_spacing, ccw) in;

layout(location=0) in vec2 inCoords[];

layout(push_constant) uniform Matrices {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(set=0, binding=0) uniform sampler2D heightMap;

layout(location=0) out vec3 outPosition;
layout(location=1) out vec2 outCoords;
layout(location=2) out float outHeight;

void main() {
    // Interpolate texture coordinates
    vec2 coords1 = mix(inCoords[0], inCoords[1], gl_TessCoord.x);
    vec2 coords2 = mix(inCoords[3], inCoords[2], gl_TessCoord.x);
    outCoords = mix(coords1, coords2, gl_TessCoord.y);
    
    // Interpolate positions
    vec4 pos1 = mix(gl_in[0].gl_Position, gl_in[1].gl_Position, gl_TessCoord.x);
    vec4 pos2 = mix(gl_in[3].gl_Position, gl_in[2].gl_Position, gl_TessCoord.x);
    vec4 pos = mix(pos1, pos2, gl_TessCoord.y);
    
    float h = texture(heightMap, outCoords).r * 5.0;
    pos.y += h;
    
    gl_Position = projection * model * view * pos;
    outHeight = h;
}
