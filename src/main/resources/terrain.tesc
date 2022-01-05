#version 450

layout(location=0) in vec2 inCoord[];

layout(set=0, binding=0) uniform sampler2D heightMap;

layout(push_constant) uniform Matrices {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(vertices=4) out;

layout(location=0) out vec2 outCoord[4];

layout(constant_id=0) const float TesselationLevel = 20;

float factor(float a, float b) {
    float dist = min(a, b);
    return max(1.0, TesselationLevel - dist);
}

void main() {
    if(gl_InvocationID == 0) {
        // Determine distance to each vertex of the quad
        float distance[4];
        for(int n = 0; n < 4; ++n) {
            float h = texture(heightMap, inCoord[n]).r;
            vec4 pos = view * model * (gl_in[gl_InvocationID].gl_Position + vec4(0, h, 0, 0));
            distance[n] = dot(pos, pos);
        }

        // Calculate outer tesselation factor for each edge
        gl_TessLevelOuter[0] = factor(distance[3], distance[0]);
        gl_TessLevelOuter[1] = factor(distance[0], distance[1]);
        gl_TessLevelOuter[2] = factor(distance[1], distance[2]);
        gl_TessLevelOuter[3] = factor(distance[2], distance[3]);
        
        // Calculate inner tesselation factors
        gl_TessLevelInner[0] = mix(gl_TessLevelOuter[0], gl_TessLevelOuter[3], 0.5);
        gl_TessLevelInner[1] = mix(gl_TessLevelOuter[2], gl_TessLevelOuter[1], 0.5);
    }
    
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    
    outCoord[gl_InvocationID] = inCoord[gl_InvocationID];
}
