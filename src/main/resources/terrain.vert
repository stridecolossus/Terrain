#version 450

layout(location = 0) in vec3 pos;
layout(location = 1) in vec2 coords;

layout(binding = 0) uniform sampler2D texSampler;

layout(push_constant) uniform Matrices {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(location = 0) out vec2 outCoords;
layout(location = 1) out float height;

void main() {
    float h = texture(texSampler, coords).r;
    vec4 vertex = vec4(pos.x, pos.y + h * 5.0, pos.z, 1.0);
    gl_Position = projection * view * model * vertex;
    outCoords = coords;
    height = h;
}
