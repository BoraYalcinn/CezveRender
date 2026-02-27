#version 330 core

out vec4 FragColor;

in vec2 fragTex;
uniform sampler2D theTexture;

void main(){

	FragColor = texture(theTexture,fragTex);

}
