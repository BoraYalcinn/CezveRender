package com.bora.renderer;

import org.joml.*;
import static org.lwjgl.opengl.GL33C.*;

public class DirectionalLight extends Light{

	protected Vector3f direction;
	
	public DirectionalLight() {
		
		this.direction = new Vector3f(0,0,0);
		
	}
	
	public DirectionalLight(float red,float green,float blue,float aIntensity,float dIntensity,
							float xDir,float yDir,float zDir){
		super(aIntensity, dIntensity, red, green, blue);
		this.direction = new Vector3f(xDir,yDir,zDir);
		
	}
	
	public void UseDirectionalLight(int ambientIntensityLocation,int ambientColorLocation,int diffuseIntensityLocation,int directionLocation) {
		glUniform3f(ambientColorLocation,color.x,color.y,color.z);
		glUniform1f(ambientIntensityLocation,ambientIntensity);
		glUniform3f(directionLocation,direction.x,direction.y,direction.z);
		glUniform1f(diffuseIntensityLocation,diffuseIntensity);
		
	}
	
	public float getDiffuseIntensity() {
		return diffuseIntensity;
	}
	
	public float getAmbientIntensity() {
		return ambientIntensity;
	}
	
	public Vector3f getColor() {
		return color;
	}
	
	public Vector3f getDirection() {
		return direction;
	}
	
}
