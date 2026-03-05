package com.bora.renderer;

import static org.lwjgl.opengl.GL33C.*;

public class Mesh {
	
	public int VBO;
	public int VAO;
	public int IBO;
	public int indexCount;
	
	float[] vertices;
	int[] indices;
	
	
	private Transform transform = new Transform();
	
	public Mesh() {
		
		this.VBO = 0;
		this.VAO = 0;
		this.IBO = 0;
		this.indexCount = 0;
	
	}
	
	public void createMesh(float vertices[],int indices[]) {
		
		this.vertices = vertices;
		this.indices = indices;
		this.indexCount = indices.length;
		
		this.VAO =  glGenVertexArrays();
		glBindVertexArray(this.VAO);
		
		this.IBO = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,IBO);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER,indices,GL_STATIC_DRAW);
		
		this.VBO = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER,VBO);
		glBufferData(GL_ARRAY_BUFFER,vertices,GL_STATIC_DRAW);
		
		// position
		glVertexAttribPointer(0,3,GL_FLOAT,false,8*Float.BYTES,0L);
		glEnableVertexAttribArray(0);
		
		// normal
		glVertexAttribPointer(1,3,GL_FLOAT,false,8*Float.BYTES,5*Float.BYTES);
		glEnableVertexAttribArray(1);
		
		// texCoord
		glVertexAttribPointer(2,2,GL_FLOAT,false,8*Float.BYTES,3*Float.BYTES);
		glEnableVertexAttribArray(2);
		
		glBindBuffer(GL_ARRAY_BUFFER,0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,0);
		
		glBindVertexArray(0);
		
	}
	
	public Transform getTransform() {
		return transform;
	}
	
	public void renderMesh() {
		glBindVertexArray(VAO);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,IBO);
		glDrawElements(GL_TRIANGLES,indexCount,GL_UNSIGNED_INT,0);
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER,0);
		glBindVertexArray(0);
		
	}
	
	public void clearMesh() {
		
		if(VBO != 0 )glDeleteBuffers(VBO);
		if(IBO != 0)glDeleteBuffers(IBO);
		if(VAO != 0)glDeleteVertexArrays(VAO);
		indexCount = 0;
	}
	
	
}
