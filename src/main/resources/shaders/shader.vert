#version 330 core

layout(location = 0) in vec3 aPos;
layout(location = 1) in vec3 normal; // for lights
layout(location = 2) in vec2 texCoord; // for texture

uniform mat4 model;
uniform mat4 projection;
uniform mat4 view;
uniform mat4 lightSpaceMatrix;

out vec3 fragPos;
out vec3 fragNormal;
out vec2 fragTex;
out vec4 fragPosLightSpace;

void main(){

	fragPos = vec3(model * vec4(aPos,1.));
	fragNormal = mat3(transpose(inverse(model))) * normal;
	fragTex = texCoord;
	fragPosLightSpace = lightSpaceMatrix * vec4(fragPos, 1.0);
	gl_Position = projection * view * model * vec4(aPos,1.0);
	 	

}