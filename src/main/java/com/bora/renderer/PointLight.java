package com.bora.renderer;

import org.joml.*;
import static org.lwjgl.opengl.GL33C.*;

public class PointLight extends Light{

	protected float constant,linear,exponent;
	protected Vector3f position;
	
	public PointLight() {
		
	}
		
	public PointLight(float red,float green,float blue,
			float aIntensity,float dIntensity,
			float xPos,float yPos,float zPos,
			float con,float lin,float exp) {
		super(aIntensity,dIntensity,red,green,blue);
		this.constant = con;
		this.linear = lin;
		this.exponent = exp;
		this.position = new Vector3f(xPos,yPos,zPos);
	
	}
	
	public void UsePointLight(int ambientIntensityLocation,int ambientColorLocation,int diffuseIntensityLocation,int positionLocation,
							int constantLocation,int linearLocation,int exponentLocation) {
		glUniform3f(ambientColorLocation,color.x,color.y,color.z);
		glUniform1f(ambientIntensityLocation,ambientIntensity);
		glUniform1f(diffuseIntensityLocation,diffuseIntensity);
		glUniform3f(positionLocation,position.x,position.y,position.z);
		
		glUniform1f(constantLocation,constant);
		glUniform1f(linearLocation,linear);
		glUniform1f(exponentLocation,exponent);
	}
	
	public float getExponent() {
		return exponent;
	}
	
	public float getLinear() {
		return linear;
	}
	
	public float getConstant() {
		return constant;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public float getAmbientIntensity() {
		return ambientIntensity;
	}
	
	public float getDiffuseIntensity() {
		return diffuseIntensity;
	}
	
	public Vector3f getColor() {
		return color;
	}
	
}
