package com.bora.renderer;

import org.joml.*;
import org.joml.Math;

import static org.lwjgl.opengl.GL33C.*;

public class SpotLight extends PointLight{
	
	float edge;
	float procEdge;
	Vector3f direction;
	
	public SpotLight() {
		this.direction = new Vector3f(0f,0f,0f);
		this.edge = 0.f;
		this.procEdge = Math.cos(Math.toRadians(edge)); 
	}
	
	public SpotLight(float red,float green,float blue,
						float aIntensity,float dIntensity,
						float xPos,float yPos,float zPos,
						float xDir,float yDir,float zDir,
						float con,float lin,float exp,
						float edg) {
		super( red, green, blue,
				aIntensity,dIntensity,
				xPos,yPos,zPos,
				con,lin,exp);
		direction = new Vector3f(xDir,yDir,zDir).normalize();
		this.edge = edg;
		procEdge = Math.cos(Math.toRadians(edge));
		
	}
	
	
	public void UseSpotLight(int ambientIntensityLocation,int ambientColorLocation,
								int diffuseIntensityLocation,int positionLocation,int directionLocation,
								int constantLocation,int linearLocation,int exponentLocation,int edgeLocation) {
		
		glUniform3f(ambientColorLocation,color.x,color.y,color.z);
		glUniform1f(ambientIntensityLocation,ambientIntensity);
		glUniform1f(diffuseIntensityLocation,diffuseIntensity);
		
		glUniform3f(directionLocation,direction.x,direction.y,direction.z);
		glUniform3f(positionLocation,position.x,position.y,position.z);
		
		glUniform1f(constantLocation,constant);
		glUniform1f(linearLocation,linear);
		glUniform1f(exponentLocation,exponent);
		
		glUniform1f(edgeLocation,procEdge);
	}
	
}
