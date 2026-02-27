package com.bora.renderer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;


public class Main {
	
	private static Mesh mesh;
	private static Shader shader;
	
	
		
	public static void main(String[] args) {
		System.out.println("Hello 3D renderer");
		
		// Create Window
		Window renderer = new Window();
		renderer.run();
		
		
		// Mesh Data
		int[] indices = {
		        0, 3, 1,
		        1, 3, 2,
		        2, 3, 0,
		        0, 2, 1
		    };
		
		float[] vertices = {
				// x,    y,    z,        u ,   v        normal
		        -1.0f, -1.0f, 0.0f,     0.0f, 0.0f,     0.0f,0.0f,0.0f,
				 0.0f, -1.0f, 1.0f,     0.5f, 0.0f,     0.0f,0.0f,0.0f,
				 1.0f, -1.0f, 0.0f,     1.0f, 0.0f,     0.0f,0.0f,0.0f,
				 0.0f,  1.0f, 0.0f,     0.5f, 1.0f,     0.0f,0.0f,0.0f
		    };
		
		// Create Mesh
		mesh = new Mesh();
		mesh.createMesh(vertices, indices);
		
		// Create Texture
		Texture brick = new Texture("textures/brick.png");
		brick.loadTexture();
		
		// Create Camera
		Camera camera = new Camera(70f, 800f/600f, 0.01f, 100f);
        camera.getTransform().position.z = 2f;
        
        Transform modelTransform = new Transform();
		
		// Create Shader
		shader = new Shader("shaders/shader.vert","shaders/shader.frag");
		
		// initialize input
		Input input = new Input(renderer.getWindow());
		float speed = 1.f;
		float sensitivity = 1.f;
		double lastTime = glfwGetTime();
		
		// RENDER LOOP
		while(!renderer.getShouldClose()) {
			
			double currentTime = glfwGetTime();
			float deltaTime = (float) (currentTime - lastTime);
			lastTime = currentTime;
			float velocity = speed * deltaTime;
			
			glfwPollEvents();
			renderer.clear();
			
			// Handle Camera Movements
			camera.handleMovement(velocity, camera, input, sensitivity);
		    
			// texture & shader
			shader.useShader();
			shader.setTextureUnit(0);
			brick.useTexture();
			
			
			shader.setUniformMat4f(shader.getUniformModel(), modelTransform.getModelMatrix());
            shader.setUniformMat4f(shader.getUniformView(), camera.getViewMatrix());
            shader.setUniformMat4f(shader.getUniformProjection(), camera.getProjectionMatrix());
            
			mesh.renderMesh();
			
			glUseProgram(0);
			renderer.swapBuffers();
			
			if (input.isKeyDown(GLFW_KEY_ESCAPE)) {
		        glfwSetWindowShouldClose(renderer.getWindow(), true);
		    }
		}
		
		
		shader.clearShader();
		mesh.clearMesh();
		renderer.destroy();
	}

}
