package com.bora.renderer;

import java.nio.*;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;


public class Main {
	
	private static Mesh mesh;
	private static Shader shader;
	
		
	public static void main(String[] args) {
		System.out.println("Hello 3D renderer");
		
		Window renderer = new Window();
		renderer.run();
		
		int[] indices = {0, 1, 2};
		
		float[] vertices = {
		    -0.5f, -0.5f, 0.0f,
		     0.5f, -0.5f, 0.0f,
		     0.0f,  0.5f, 0.0f
		};
		
		
		mesh = new Mesh();
		mesh.createMesh(vertices, indices);
		
		shader = new Shader("shaders/shader.vert","shaders/shader.frag");
		
		
		
		while(!renderer.getShouldClose()) {
			
			glfwPollEvents();
			glClear(GL_COLOR_BUFFER_BIT);
			
			shader.useShader();
			mesh.renderMesh();
			
			 
			glUseProgram(0);
			renderer.swapBuffers();
			
		}
		
		shader.clearShader();
		mesh.clearMesh();
	}

}
