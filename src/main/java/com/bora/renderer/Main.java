package com.bora.renderer;

import java.nio.*;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;




public class Main {
	
	int[] indices = {
			
			
	};
	
	float[] vertices = {
			-1.f,-1.f,0.f,
			0.f,-1.f,1.f,
			1.0f,-1.0f,0.f
	};
	
	
	public static void main(String[] args) {
		System.out.println("Hello 3D renderer");
		Window renderer = new Window();
		
		renderer.run();
		
		
		
		
		while(!renderer.getShouldClose()) {
			
			 glfwPollEvents();
			
			
			glUseProgram(0);
			renderer.swapBuffers();
			
		}
	}

}
