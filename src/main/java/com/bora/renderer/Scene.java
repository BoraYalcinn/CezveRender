package com.bora.renderer;

import java.util.*;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*; 

public class Scene {
		
	private List<Mesh> meshes = new ArrayList<>();
	private List<Model> models = new ArrayList<>();
	private List<Material> materials = new ArrayList<>();
	
	private Mesh floor;
	
	//Lights
	private DirectionalLight dirLight;
	private List<PointLight> pointLights = new ArrayList<>();
    private List<SpotLight> spotLights = new ArrayList<>();
	
    //Shader
    private Shader mainShader;
    private Shader shadowShader;
    private Shader omniShadowShader;
    private Shader skyboxShader;
    
    //Texture & Material
    private Texture floorTexture;
    private Material floorMaterial;

    public float shadowBias = 0.005f;
    public boolean shadowDebugView = false;
    public boolean showShadowMap = false;
    public boolean useLightCamera = false;
	
	private Skybox skybox;
	private Camera camera;
	
	// SCENE
	public Scene(Shader main,Shader shadow, Shader omni ,Shader skybox) {
		this.mainShader = main;
		this.shadowShader = shadow;
		this.omniShadowShader = omni;
		this.skyboxShader = skybox;
		
	}
	
	public void runScene() {
		
	}
	
	// MODEL 	
	public void addModel(Model m) {
		models.add(m);
	}
	
	// MESH 
	public void addMesh(Mesh m) {
		meshes.add(m);
	}
	
	// RENDER
	public void renderShadowPasses(int windowWidth,int windowHeight) { 
	
		// DIRECTIONAL LIGHT SHADOW PASS
		glBindFramebuffer(GL_FRAMEBUFFER,dirLight.getShadowMap().getDepthMapFBO());
		glViewport(0,0,1024,1024);
		glClear(GL_DEPTH_BUFFER_BIT);
		
		shadowShader.useShader();
		shadowShader.setUniformMat4f(shadowShader.getUniformLightSpaceMatrix(), dirLight.getLightSpaceMatrix());
		
		glEnable(GL_CULL_FACE); glCullFace(GL_FRONT);
	    renderAllObjects(shadowShader);
	    glDisable(GL_CULL_FACE);
	    glBindFramebuffer(GL_FRAMEBUFFER, 0);
	    glViewport(0, 0, windowWidth, windowHeight);
	    
	    // SPOT LIGHT SHADOW PASS
	    shadowShader.useShader();
	    for (SpotLight s : spotLights) {
	        glBindFramebuffer(GL_FRAMEBUFFER, s.getShadowMap().getDepthMapFBO());
	        glViewport(0, 0, 1024, 1024);
	        glClear(GL_DEPTH_BUFFER_BIT);
	        shadowShader.setUniformMat4f(shadowShader.getUniformLightSpaceMatrix(), s.getLightSpaceMatrix());
	        renderAllObjects(shadowShader);
	        glBindFramebuffer(GL_FRAMEBUFFER, 0);
	        glViewport(0, 0, windowWidth, windowHeight);
	    }
	    
	    // POINT LIGHT SHADOW PASS
	    for (PointLight p : pointLights) {
	    	glBindFramebuffer(GL_FRAMEBUFFER,p.getCubeShadowMap().getDepthMapFBO());
	    	glViewport(0,0,1024,1024);
	    	glClear(GL_DEPTH_BUFFER_BIT);
	    	
	    	Matrix4f[] matrices = p.getLightSpaceMatrices();
	        for (int face = 0; face < 6; face++) {
	            int loc = glGetUniformLocation(omniShadowShader.getProgramID(), "lightSpaceMatrices[" + face + "]");
	            omniShadowShader.setUniformMat4f(loc, matrices[face]);
	        }
	        
	        glUniform3f(glGetUniformLocation(omniShadowShader.getProgramID(), "lightPos"),
	            p.getPosition().x, p.getPosition().y, p.getPosition().z);
	        glUniform1f(glGetUniformLocation(omniShadowShader.getProgramID(), "farPlane"), 100f);
	        renderAllObjects(omniShadowShader);
	        glBindFramebuffer(GL_FRAMEBUFFER, 0);
	        glViewport(0, 0, windowWidth, windowHeight);
	    
	    }
	    
	}
	
	private void renderAllObjects(Shader shader) {
	    for (Model m : models) {
	        shader.setUniformMat4f(shader.getUniformModel(), m.getTransform().getModelMatrix());
	        m.Draw(shader);
	    }
	    for (Mesh m : meshes) {
	        shader.setUniformMat4f(shader.getUniformModel(), m.getTransform().getModelMatrix());
	        m.renderMesh();
	    }
	    if (floor != null) {
	        shader.setUniformMat4f(shader.getUniformModel(), floor.getTransform().getModelMatrix());
	        floor.renderMesh();
	    }
	}
	
	public void render() {
	    mainShader.useShader();

	    // Directional shadow map
	    glActiveTexture(GL_TEXTURE1);
	    glBindTexture(GL_TEXTURE_2D, dirLight.getShadowMap().getDepthMap());
	    glUniform1i(glGetUniformLocation(mainShader.getProgramID(), "shadowMap"), 1);
	    mainShader.setUniformMat4f(mainShader.getUniformLightSpaceMatrix(), dirLight.getLightSpaceMatrix());

	    // Spot shadow maps
	    for (int i = 0; i < spotLights.size(); i++) {
	        glActiveTexture(GL_TEXTURE2 + i);
	        glBindTexture(GL_TEXTURE_2D, spotLights.get(i).getShadowMap().getDepthMap());
	    }

	    // Point shadow maps
	    for (int i = 0; i < pointLights.size(); i++) {
	        glActiveTexture(GL_TEXTURE2 + spotLights.size() + i);
	        glBindTexture(GL_TEXTURE_CUBE_MAP, pointLights.get(i).getCubeShadowMap().getDepthMap());
	        glUniform1i(glGetUniformLocation(mainShader.getProgramID(), "pointShadowMaps[" + i + "]"), 2 + spotLights.size() + i);
	    }

	    glUniform1f(glGetUniformLocation(mainShader.getProgramID(), "farPlane"), 100f);
	    glUniform1f(glGetUniformLocation(mainShader.getProgramID(), "shadowBiasMultiplier"), shadowBias);
	    glUniform1i(glGetUniformLocation(mainShader.getProgramID(), "debugMode"), shadowDebugView ? 1 : 0);
	   
	    // Lights
	    mainShader.setDirectionalLight(dirLight);
	    mainShader.setPointLights(pointLights.toArray(new PointLight[0]));
	    mainShader.setSpotLights(spotLights.toArray(new SpotLight[0]));

	    // Camera
	    if (useLightCamera) {
	        mainShader.setUniformMat4f(mainShader.getUniformView(), dirLight.getLightViewMatrix());
	        mainShader.setUniformMat4f(mainShader.getUniformProjection(), dirLight.getLightProjectionMatrix());
	    } else {
	        mainShader.setUniformMat4f(mainShader.getUniformView(), camera.getViewMatrix());
	        mainShader.setUniformMat4f(mainShader.getUniformProjection(), camera.getProjectionMatrix());
	    }

	    glUniform3f(glGetUniformLocation(mainShader.getProgramID(), "viewPos"),
	        camera.getTransform().position.x,
	        camera.getTransform().position.y,
	        camera.getTransform().position.z);

	    // Models
	    for (Model m : models) m.Draw(mainShader);
	    // Meshes
	    for (Mesh m : meshes) {
	        mainShader.setUniformMat4f(mainShader.getUniformModel(), m.getTransform().getModelMatrix());
	        m.renderMesh();
	    }
	    // Floor
	    if (floor != null) {
	        mainShader.setUniformMat4f(mainShader.getUniformModel(), floor.getTransform().getModelMatrix());
	        if (floorTexture != null) floorTexture.useTexture();
	        if (floorMaterial != null) floorMaterial.useMaterial(
	            mainShader.getUniformSpecularIntensity(),
	            mainShader.getUniformShininess());
	        floor.renderMesh();
	    }
	    glUseProgram(0);
	}
    
    public void renderSkybox() { 
    	if(skybox != null)skybox.render(skyboxShader, camera);
    }
	
    // getters and setters
    public void setDirLight(DirectionalLight d) { this.dirLight = d; }
    public void addPointLight(PointLight p) { pointLights.add(p); }
    public void removePointLight(int i) { if(i < pointLights.size()) pointLights.remove(i); }
    public void addSpotLight(SpotLight s) { spotLights.add(s); }
    public void removeSpotLight(int i) { if(i < spotLights.size()) spotLights.remove(i); }
    public void removeModel(int i) { if(i < models.size()) models.remove(i); }
    public void setSkybox(Skybox s) { this.skybox = s; }
    public void setCamera(Camera c) { this.camera = c; }
    public void setFloor(Mesh m, Texture t, Material mat) { floor = m; floorTexture = t; floorMaterial = mat; }
    public void setFloorTexture(Texture t) { this.floorTexture = t; }
    public DirectionalLight getDirLight() { return dirLight; }
    public List<PointLight> getPointLights() { return pointLights; }
    public List<SpotLight> getSpotLights() { return spotLights; }
    public List<Model> getModels() { return models; }
    public List<Mesh> getMeshes() { return meshes; }
    public Mesh getFloor() { return floor; }
    public Camera getCamera() { return camera; }
}
