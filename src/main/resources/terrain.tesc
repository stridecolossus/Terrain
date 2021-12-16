#version 450

layout(set = 0, binding = 1) uniform sampler2D heightMap;
layout(location = 0) in vec2 coords[];

layout(vertices = 4) out;
layout(location = 0) out vec2 outCoords[4];

void main() {
    if(gl_InvocationID == 0) {
        gl_TessLevelInner[0] = 1.0;
        gl_TessLevelInner[1] = 1.0;
        gl_TessLevelOuter[0] = 1.0;
        gl_TessLevelOuter[1] = 1.0;
        gl_TessLevelOuter[2] = 1.0;
        gl_TessLevelOuter[3] = 1.0;
    }
    
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    outCoords[gl_InvocationID] = coords[gl_InvocationID];
}
