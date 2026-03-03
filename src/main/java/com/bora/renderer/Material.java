package com.bora.renderer;

import static org.lwjgl.opengl.GL33C.*;

public class Material {
	
	
	float specularIntensity;
	float shininess;
	
	public Material() {
		this.specularIntensity = 0.f;
		this.shininess = 0.f;
	}
	
	public Material(float shine, float sIntensity) {
		this.specularIntensity = sIntensity;
		this.shininess = shine;
	}
	
	public void useMaterial(int specularIntensityLocation,int shininessLocation) {
		glUniform1f(specularIntensityLocation,specularIntensity);
		glUniform1f(shininessLocation,shininess);
	}
	
	
}
