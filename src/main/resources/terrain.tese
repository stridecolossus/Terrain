#version 450

layout(quads, equal_spacing, ccw) in;

layout(location=0) in vec2 inCoord[];

layout(set=0, binding=0) uniform sampler2D heightMap;

layout(push_constant) uniform Matrices {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(constant_id=1) const float HeightScale = 2.5;

layout(location=0) out vec2 outCoord;

void main() {
    // Interpolate texture coordinate
    vec2 coords1 = mix(inCoord[1], inCoord[0], gl_TessCoord.x);
    vec2 coords2 = mix(inCoord[2], inCoord[3], gl_TessCoord.x);
    vec2 coord = mix(coords1, coords2, gl_TessCoord.y);

    // Interpolate position
    vec4 pos1 = mix(gl_in[1].gl_Position, gl_in[0].gl_Position, gl_TessCoord.x);
    vec4 pos2 = mix(gl_in[2].gl_Position, gl_in[3].gl_Position, gl_TessCoord.x);
    vec4 pos = mix(pos1, pos2, gl_TessCoord.y);

    // Lookup vertex height
    pos.y += texture(heightMap, coord).r * HeightScale;

    // Output vertex
    gl_Position = projection * view * model * pos;
    outCoord = coord;
}
