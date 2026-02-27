#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 normal; // for lights
layout(location = 2) in vec2 texCoord; // for texture

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;

out vec3 fragPos;
out vec3 fragNormal;
out vec2 fragTex;

void main(){
	fragPos = vec3(model * vec4(aPos,1.));
	fragNormal = mat3(transpose(inverse(model))) * normal;
	fragTex = texCoord;
	gl_Position = projection * view * model * vec4(aPos,1.0);
	 	

}