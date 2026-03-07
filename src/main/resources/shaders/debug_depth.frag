#version 330 core

in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D depthMap;
uniform float nearPlane;
uniform float farPlane;

void main(){
    float depth = texture(depthMap, TexCoord).r;
    // Depth is already linear in ortho projection, but let's visualize it clearly
    // Show raw depth: 0=black (near), 1=white (far/empty)
    FragColor = vec4(vec3(depth), 1.0);
}
