package com.bora.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;


public class Main {
	
	private static Mesh[]  meshes = new Mesh[2];;

	// Debug quad VAO for shadow map overlay
	private static int debugQuadVAO = 0;

	private static int initDebugQuad() {
		float[] quadVertices = {
			// positions   // texcoords  (bottom-right corner overlay)
			 0.5f,  0.5f,   0.0f, 0.0f,
			 1.0f,  0.5f,   1.0f, 0.0f,
			 1.0f,  1.0f,   1.0f, 1.0f,
			 0.5f,  1.0f,   0.0f, 1.0f,
		};
		int[] quadIndices = { 0, 1, 2, 0, 2, 3 };

		int vao = glGenVertexArrays();
		int vbo = glGenBuffers();
		int ibo = glGenBuffers();

		glBindVertexArray(vao);

		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		glBufferData(GL_ARRAY_BUFFER, quadVertices, GL_STATIC_DRAW);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, quadIndices, GL_STATIC_DRAW);

		glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0L);
		glEnableVertexAttribArray(0);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2L * Float.BYTES);
		glEnableVertexAttribArray(1);

		glBindVertexArray(0);
		return vao;
	}

	private static void renderDebugQuad(Shader debugShader, int depthMap) {
		glDisable(GL_DEPTH_TEST);
		debugShader.useShader();

		glActiveTexture(GL_TEXTURE0);
		glBindTexture(GL_TEXTURE_2D, depthMap);
		glUniform1i(glGetUniformLocation(debugShader.getProgramID(), "depthMap"), 0);
		glUniform1f(glGetUniformLocation(debugShader.getProgramID(), "nearPlane"), 0.1f);
		glUniform1f(glGetUniformLocation(debugShader.getProgramID(), "farPlane"), 100f);

		glBindVertexArray(debugQuadVAO);
		glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
		glBindVertexArray(0);

		glEnable(GL_DEPTH_TEST);
		glUseProgram(0);
	}

		
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
		Shader debugShader = new Shader("shaders/debug_depth.vert", "shaders/debug_depth.frag");
		Shader omniShadowShader = new Shader("shaders/omni_shadow.vert", "shaders/omni_shadow.geom", "shaders/omni_shadow.frag");
		
		// Init debug quad for shadow map overlay
		debugQuadVAO = initDebugQuad();

		// Toggle states
		boolean useLightCamera = false;
		boolean showShadowMap = true; // Show overlay by default for debugging
		boolean f1WasPressed = false;
		boolean f2WasPressed = false;
		boolean f3WasPressed = false;
		boolean shadowDebugView = false;
		float shadowBias = 0.005f;
		
		
		
		// Create Lights
		DirectionalLight dirLight = new DirectionalLight(1f, 1f, 1f, 0.1f, 0.8f, 0f, -1f, -0.5f);

		// Debug: print light camera info
		Vector3f lightPos = new Vector3f(dirLight.getDirection()).mul(-20f);
		System.out.println("[DEBUG] Light direction: " + dirLight.getDirection());
		System.out.println("[DEBUG] Light camera pos: " + lightPos);
		System.out.println("[DEBUG] Light ortho: +-" + dirLight.getOrthoSize() + ", near=0.1, far=100");
		System.out.println("[DEBUG] Light lookAt target: (0,0,0)");
		System.out.println("[DEBUG] F1 = toggle light camera | F2 = toggle shadow overlay");
		System.out.println("[DEBUG] In light-cam mode: W/S = increase/decrease ortho size");
		System.out.println("[DEBUG] P = increase bias | O = decrease bias (step 0.001)");
		System.out.println("[DEBUG] F3 = toggle shadow debug view (green=lit, red=shadow)");

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
		        1f, 1f, 1f,        
		        0.0f, 2.0f,        
		        0f, 8f, 2f,        
		        0f, -1f, 0f,       
		        0.3f, 0.1f, 0.1f,  
		        20f                
		);

		spotLights[1] = new SpotLight(
		        1f, 1f, 1f,        
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

			// Draw xwing with front-face culling (closed mesh — has back faces)
			glEnable(GL_CULL_FACE);
			glCullFace(GL_FRONT);
			shadowShader.setUniformMat4f(shadowShader.getUniformModel(), xwing.getTransform().getModelMatrix());
			xwing.Draw(shadowShader);
			glDisable(GL_CULL_FACE);

			// Draw single-sided geometry WITHOUT culling (floor, pyramid have no back faces)
			shadowShader.setUniformMat4f(shadowShader.getUniformModel(), meshes[0].getTransform().getModelMatrix());
			meshes[0].renderMesh();

			shadowShader.setUniformMat4f(shadowShader.getUniformModel(), meshes[1].getTransform().getModelMatrix());
			meshes[1].renderMesh();

			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			glViewport(0, 0, 1600, 1080);
			// ─── SHADOW PASS DONE ─────────────────────────────────────
			
			// ─── SPOT SHADOW PASS ─────────────────────────────────
			shadowShader.useShader();
			for(int i = 0; i < spotLights.length; i++) {
			    glBindFramebuffer(GL_FRAMEBUFFER, spotLights[i].getShadowMap().getDepthMapFBO());
			    glViewport(0, 0, 1024, 1024);
			    glClear(GL_DEPTH_BUFFER_BIT);
			    shadowShader.setUniformMat4f(shadowShader.getUniformLightSpaceMatrix(), spotLights[i].getLightSpaceMatrix());
			    shadowShader.setUniformMat4f(shadowShader.getUniformModel(), xwing.getTransform().getModelMatrix());
			    xwing.Draw(shadowShader);
			    shadowShader.setUniformMat4f(shadowShader.getUniformModel(), meshes[0].getTransform().getModelMatrix());
			    meshes[0].renderMesh();
			    shadowShader.setUniformMat4f(shadowShader.getUniformModel(), meshes[1].getTransform().getModelMatrix());
			    meshes[1].renderMesh();
			}
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			glViewport(0, 0, 1600, 1080);
			// ─── SPOT SHADOW PASS BİTTİ ──────────────────────────
			
			// ─── POINT SHADOW PASS ────────────────────────────────
			omniShadowShader.useShader();
			for(int i = 0; i < pointLights.length; i++) {
			    glBindFramebuffer(GL_FRAMEBUFFER, pointLights[i].getCubeShadowMap().getDepthMapFBO());
			    glViewport(0, 0, 1024, 1024);
			    glClear(GL_DEPTH_BUFFER_BIT);
			    
			    Matrix4f[] matrices = pointLights[i].getLightSpaceMatrices();
			    for(int face = 0; face < 6; face++) {
			        int loc = glGetUniformLocation(omniShadowShader.getProgramID(), "lightSpaceMatrices[" + face + "]");
			        omniShadowShader.setUniformMat4f(loc, matrices[face]);
			    }
			    glUniform3f(glGetUniformLocation(omniShadowShader.getProgramID(), "lightPos"),
			        pointLights[i].getPosition().x, pointLights[i].getPosition().y, pointLights[i].getPosition().z);
			    glUniform1f(glGetUniformLocation(omniShadowShader.getProgramID(), "farPlane"), 100f);
			    
			    omniShadowShader.setUniformMat4f(omniShadowShader.getUniformModel(), xwing.getTransform().getModelMatrix());
			    xwing.Draw(omniShadowShader);
			    omniShadowShader.setUniformMat4f(omniShadowShader.getUniformModel(), meshes[0].getTransform().getModelMatrix());
			    meshes[0].renderMesh();
			    omniShadowShader.setUniformMat4f(omniShadowShader.getUniformModel(), meshes[1].getTransform().getModelMatrix());
			    meshes[1].renderMesh();
			}
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			glViewport(0, 0, 1600, 1080);
			// ─── POINT SHADOW PASS BİTTİ ──────────────────────────
			
			glfwPollEvents();
			renderer.clear();

			// --- Toggle keys (edge-detect) ---
			boolean f1Down = input.isKeyDown(GLFW_KEY_F1);
			if (f1Down && !f1WasPressed) {
				useLightCamera = !useLightCamera;
				System.out.println("[DEBUG] Camera: " + (useLightCamera ? "LIGHT" : "PLAYER"));
			}
			f1WasPressed = f1Down;

			boolean f2Down = input.isKeyDown(GLFW_KEY_F2);
			if (f2Down && !f2WasPressed) {
				showShadowMap = !showShadowMap;
				System.out.println("[DEBUG] Shadow map overlay: " + (showShadowMap ? "ON" : "OFF"));
			}
			f2WasPressed = f2Down;

			boolean f3Down = input.isKeyDown(GLFW_KEY_F3);
			if (f3Down && !f3WasPressed) {
				shadowDebugView = !shadowDebugView;
				System.out.println("[DEBUG] Shadow debug view: " + (shadowDebugView ? "ON (green=lit, red=shadow)" : "OFF"));
			}
			f3WasPressed = f3Down;

			// --- Bias controls: P = increase, O = decrease ---
			if (input.isKeyDown(GLFW_KEY_P)) {
				shadowBias += 0.001f;
				System.out.println("[DEBUG] shadowBias = " + String.format("%.4f", shadowBias));
			}
			if (input.isKeyDown(GLFW_KEY_O)) {
				shadowBias -= 0.001f;
				System.out.println("[DEBUG] shadowBias = " + String.format("%.4f", shadowBias));
			}

			// --- Frustum controls (only in light camera mode): W/S = size, A/D = size ---
			if (useLightCamera) {
				if (input.isKeyDown(GLFW_KEY_W)) {
					dirLight.setOrthoSize(dirLight.getOrthoSize() + 0.5f);
					System.out.println("[DEBUG] orthoSize = " + String.format("%.1f", dirLight.getOrthoSize()));
				}
				if (input.isKeyDown(GLFW_KEY_S)) {
					dirLight.setOrthoSize(dirLight.getOrthoSize() - 0.5f);
					System.out.println("[DEBUG] orthoSize = " + String.format("%.1f", dirLight.getOrthoSize()));
				}
			}
			
			// Handle Camera Movements (only when using player camera)
			if (!useLightCamera) {
				camera.handleMovement(velocity, camera, input, sensitivity);
			}
			
			shader.useShader();

			// shadow map'i bind et
			glActiveTexture(GL_TEXTURE1);
			glBindTexture(GL_TEXTURE_2D, dirLight.getShadowMap().getDepthMap());
			glUniform1i(glGetUniformLocation(shader.getProgramID(), "shadowMap"), 1);
			shader.setUniformMat4f(shader.getUniformLightSpaceMatrix(), dirLight.getLightSpaceMatrix());

			// set dir light
			// shader.setDirectionalLight(dirLight);
			// set spot light
			for(int i = 0; i < spotLights.length; i++) {
			    glActiveTexture(GL_TEXTURE2 + i);
			    glBindTexture(GL_TEXTURE_2D, spotLights[i].getShadowMap().getDepthMap());
			}
			// shader.setSpotLights(spotLights);
			// set point lights
			for(int i = 0; i < pointLights.length; i++) {
			    glActiveTexture(GL_TEXTURE2 + spotLights.length + i);
			    glBindTexture(GL_TEXTURE_CUBE_MAP, pointLights[i].getCubeShadowMap().getDepthMap());
			    glUniform1i(glGetUniformLocation(shader.getProgramID(), "pointShadowMaps[" + i + "]"), 2 + spotLights.length + i);
			}
			glUniform1f(glGetUniformLocation(shader.getProgramID(), "farPlane"), 100f);
			shader.setPointLights(pointLights);
			
			
			
			// Set shadow bias uniform
			glUniform1f(glGetUniformLocation(shader.getProgramID(), "shadowBiasMultiplier"), shadowBias);

			// Set debug mode
			glUniform1i(glGetUniformLocation(shader.getProgramID(), "debugMode"), shadowDebugView ? 1 : 0);
			
			// view position
			glUniform3f(glGetUniformLocation(shader.getProgramID(), "viewPos"),
				    camera.getTransform().position.x,
				    camera.getTransform().position.y,
				    camera.getTransform().position.z);
			
			
			// matrices — switch between player cam and light cam
			if (useLightCamera) {
				shader.setUniformMat4f(shader.getUniformView(), dirLight.getLightViewMatrix());
				shader.setUniformMat4f(shader.getUniformProjection(), dirLight.getLightProjectionMatrix());
			} else {
				shader.setUniformMat4f(shader.getUniformView(), camera.getViewMatrix());
				shader.setUniformMat4f(shader.getUniformProjection(), camera.getProjectionMatrix());
			}
            
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

			// --- Draw shadow map debug overlay ---
			if (showShadowMap) {
				renderDebugQuad(debugShader, dirLight.getShadowMap().getDepthMap());
			}
			
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