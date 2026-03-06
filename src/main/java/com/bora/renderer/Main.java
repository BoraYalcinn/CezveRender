package com.bora.renderer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;


public class Main {
	
	private static Mesh[]  meshes = new Mesh[2];;
	
	
		
	public static void main(String[] args) {
		System.out.println("Hello 3D renderer");
		
		// Create Materials
		Material shinyMaterial = new Material(256f, 4.0f);
		Material dullMaterial  = new Material(8f, 0.3f);
		
		
		// Create Window
		Window renderer = new Window(1600,1080);
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
		        -1.0f, -1.0f, 0.0f,     0.0f, 0.0f,     0.0f,1.0f,0.0f,
		         0.0f, -1.0f, 1.0f,     0.5f, 0.0f,     0.0f,1.0f,0.0f,
		         1.0f, -1.0f, 0.0f,     1.0f, 0.0f,     0.0f,1.0f,0.0f,
		         0.0f,  1.0f, 0.0f,     0.5f, 1.0f,     0.0f,1.0f,0.0f
		};
		
		float[] fVertices = {
				 -10.0f, -3.0f, -10.0f,	 	0.0f, 0.0f,     0.0f,1.0f,0.0f,
	              10.0f, -3.0f, -10.0f, 	 	10.0f, 0.0f,    0.0f,1.0f,0.0f,
				  10.0f, -3.0f,  10.0f, 	 	10.0f,10.0f,    0.0f,1.0f,0.0f,
				 -10.0f, -3.0f,  10.0f,  	0.0f,10.0f,     0.0f,1.0f,0.0f
		};
		
		int[] fIndices = {
				0,1,2,
				0,2,3
		};
		
		// Create Mesh
		meshes[0] = new Mesh();
		meshes[0].createMesh(vertices, indices);
		// Create Floor Mesh
		meshes[1] = new Mesh();
		meshes[1].createMesh(fVertices, fIndices);
		
		

		// Create Model
		Model xwing = new Model("models/xwing.obj");
		xwing.getTransform().position.set(0f, -2.f, 0f);
		
		// Create Texture
		Texture brick = new Texture("textures/brick.png");
		brick.loadTexture();
		Texture plain = new Texture("textures/plain.png");
		plain.loadTexture();
		Texture grass = new Texture("textures/grass.jpg");
		grass.loadTexture();
		
		
		
		// Create Camera
		Camera camera = new Camera(70f, 800f/600f, 0.01f, 100f);
        camera.getTransform().position.z = 2f;
        
		
		// Create Shader
		Shader shader = new Shader("shaders/shader.vert","shaders/shader.frag");
		Shader shadowShader = new Shader("shaders/shadow.vert", "shaders/shadow.frag");
		
		
		
		// Create Lights
		DirectionalLight dirLight = new DirectionalLight(1f, 1f, 1f, 0.1f, 0.8f, 0f, -1f, -0.5f);

		PointLight[] pointLights = new PointLight[2];
		pointLights[0] = new PointLight(0f, 0f, 1f,
										0.5f, 0.9f,
										4.0f, 0f, 0f,
										0.3f, 0.2f, 0.1f);
		
		pointLights[1] = new PointLight(0f, 1f, 0f,
										0.6f, 1f, 
										-4.f, 2.0f, 0f, 
										0.3f, 0.1f, 0.1f);
		
		SpotLight[] spotLights = new SpotLight[2];

		spotLights[0] = new SpotLight(
		        0f, 0f, 1f,        
		        0.0f, 2.0f,        
		        0f, 8f, 2f,        
		        0f, -1f, 0f,       
		        0.3f, 0.1f, 0.1f,  
		        20f                
		);

		spotLights[1] = new SpotLight(
		        1f, 0f, 0f,        
		        0.0f, 2.0f,
		        -2f, 8f, 0f,
		        0f, -1f, 0f,
		        0.3f, 0.1f, 0.1f,
		        20f
		);
		
		
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
			
			
			
			// ─── SHADOW PASS ───────────────────────────────────────────
			glBindFramebuffer(GL_FRAMEBUFFER, dirLight.getShadowMap().getDepthMapFBO());
			glViewport(0, 0, 1024, 1024);
			glClear(GL_DEPTH_BUFFER_BIT);

			shadowShader.useShader();
			shadowShader.setUniformMat4f(shadowShader.getUniformLightSpaceMatrix(), dirLight.getLightSpaceMatrix());

			shadowShader.setUniformMat4f(shadowShader.getUniformModel(), xwing.getTransform().getModelMatrix());
			xwing.Draw(shadowShader);

			shadowShader.setUniformMat4f(shadowShader.getUniformModel(), meshes[0].getTransform().getModelMatrix());
			meshes[0].renderMesh();

			shadowShader.setUniformMat4f(shadowShader.getUniformModel(), meshes[1].getTransform().getModelMatrix());
			meshes[1].renderMesh();

			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			glViewport(0, 0, 1600, 1080);
			// ─── SHADOW PASS BİTTİ ─────────────────────────────────────
			
			glfwPollEvents();
			renderer.clear();
			
			// Handle Camera Movements
			camera.handleMovement(velocity, camera, input, sensitivity);
			
			shader.useShader();

			// shadow map'i bind et
			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, dirLight.getShadowMap().getDepthMap());
			glUniform1i(glGetUniformLocation(shader.getProgramID(), "shadowMap"), 1);
			shader.setUniformMat4f(shader.getUniformLightSpaceMatrix(), dirLight.getLightSpaceMatrix());

			shader.setDirectionalLight(dirLight);
			
			// set lights 
			shader.setDirectionalLight(dirLight);
			// shader.setPointLights(pointLights);
			// shader.setSpotLights(spotLights);
			
			// view position
			glUniform3f(glGetUniformLocation(shader.getProgramID(), "viewPos"),
				    camera.getTransform().position.x,
				    camera.getTransform().position.y,
				    camera.getTransform().position.z);
			
			
			// matrices
			
            shader.setUniformMat4f(shader.getUniformView(), camera.getViewMatrix());
            shader.setUniformMat4f(shader.getUniformProjection(), camera.getProjectionMatrix());
            
            // Draw Models
            
            xwing.Draw(shader);
            
            
            // Triangle
            shader.setUniformMat4f(shader.getUniformModel(), meshes[0].getTransform().getModelMatrix());
            brick.useTexture();
            dullMaterial.useMaterial(
                    shader.getUniformSpecularIntensity(),   
                    shader.getUniformShininess()
                    );
            meshes[0].renderMesh();

            // floor
            shader.setUniformMat4f(shader.getUniformModel(), meshes[1].getTransform().getModelMatrix());
            grass.useTexture();
            shinyMaterial.useMaterial(               
                    shader.getUniformSpecularIntensity(),
                    shader.getUniformShininess()
                    );
            meshes[1].renderMesh();
            
			
			glUseProgram(0);
			
			renderer.swapBuffers();
			renderer.clear();
			
			if (input.isKeyDown(GLFW_KEY_ESCAPE)) {
		        glfwSetWindowShouldClose(renderer.getWindow(), true);
		    }
		}
		
		// clear
		shader.clearShader();
		meshes[0].clearMesh();
		meshes[1].clearMesh();
		renderer.destroy();
	}

}