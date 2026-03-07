package com.bora.renderer;

import org.joml.*;
import static org.lwjgl.opengl.GL33C.*;

public class DirectionalLight extends Light{

	protected Vector3f direction;
	private ShadowMap shadowMap;
	private float orthoSize = 10f;
	
	public DirectionalLight() {
		
		this.direction = new Vector3f(0,0,0);
		this.shadowMap =  new ShadowMap();
		
	}
	
	public DirectionalLight(float red,float green,float blue,float aIntensity,float dIntensity,
							float xDir,float yDir,float zDir){
		super(aIntensity, dIntensity, red, green, blue);
		this.shadowMap =  new ShadowMap(1024,1024);
		this.shadowMap.InitiliazeShadows();
		this.direction = new Vector3f(xDir,yDir,zDir);
		
	}
	
	public void UseDirectionalLight(int ambientIntensityLocation,int ambientColorLocation,int diffuseIntensityLocation,int directionLocation) {
		glUniform3f(ambientColorLocation,color.x,color.y,color.z);
		glUniform1f(ambientIntensityLocation,ambientIntensity);
		glUniform3f(directionLocation,direction.x,direction.y,direction.z);
		glUniform1f(diffuseIntensityLocation,diffuseIntensity);
		
	}
	
	public Matrix4f getLightSpaceMatrix() {
		
		Matrix4f lightProjection = new Matrix4f().ortho(-orthoSize,orthoSize,-orthoSize,orthoSize,0.1f,100f);
		Vector3f lightPos = new Vector3f(direction).mul(-20f);
		Matrix4f lightView = new Matrix4f().lookAt(lightPos,new Vector3f(0,0,0),new Vector3f(0,1,0));
		
		return new Matrix4f(lightProjection).mul(lightView);
	}

	public Matrix4f getLightViewMatrix() {
		Vector3f lightPos = new Vector3f(direction).mul(-20f);
		return new Matrix4f().lookAt(lightPos, new Vector3f(0,0,0), new Vector3f(0,1,0));
	}

	public Matrix4f getLightProjectionMatrix() {
		return new Matrix4f().ortho(-orthoSize,orthoSize,-orthoSize,orthoSize,0.1f,100f);
	}

	public float getOrthoSize() { return orthoSize; }
	public void setOrthoSize(float size) { this.orthoSize = java.lang.Math.max(1f, size); }
	
	public ShadowMap getShadowMap() {
		return shadowMap;
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
