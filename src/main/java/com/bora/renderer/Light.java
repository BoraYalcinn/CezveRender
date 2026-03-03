package com.bora.renderer;

import org.joml.*;

public class Light {

	protected float ambientIntensity;
	protected float diffuseIntensity;
	protected Vector3f color;
	protected float red,green,blue;
	
	
	public Light() {
		color = new Vector3f(0,0,0);
		this.ambientIntensity = 1.f;
		this.diffuseIntensity = 0.f;
	}
	
	public Light(float aIntensity,float dIntensity,float r,float g,float b) {
		this.color = new Vector3f(r,g,b);
		this.ambientIntensity = aIntensity;
		this.diffuseIntensity = dIntensity;
	}
		
}
