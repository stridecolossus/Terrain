#version 450

layout(location=0) in vec2 inCoord[];

layout(vertices=4) out;

layout(location=0) out vec2 outCoord[4];

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
    outCoord[gl_InvocationID] = inCoord[gl_InvocationID];
}
