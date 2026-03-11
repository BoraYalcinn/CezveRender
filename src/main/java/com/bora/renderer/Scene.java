package com.bora.renderer;

import java.util.*;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL33C.*;

public class Scene {

    // necessary lists
    private final List<Mesh>    meshes       = new ArrayList<>();
    private final List<Model>   models       = new ArrayList<>();
    private final List<Texture> meshTextures = new ArrayList<>(); 
    private Mesh floor;

    // lights
    private DirectionalLight       dirLight;
    private final List<PointLight> pointLights = new ArrayList<>();
    private final List<SpotLight>  spotLights  = new ArrayList<>();

    // shader
    private final Shader mainShader;
    private final Shader shadowShader;
    private final Shader omniShadowShader;
    private final Shader skyboxShader;

    // texture & material
    private Texture  floorTexture;
    private Material floorMaterial;

    // camera ,skybox
    private Camera camera;
    private Skybox skybox;

    // gui states
    public float   shadowBias      = 0.005f;
    public boolean shadowDebugView = false;
    public boolean showShadowMap   = false;
    public boolean useLightCamera  = false;

    // ==========================================================================
    //  CONSTRUCTOR
    // ==========================================================================

    public Scene(Shader main, Shader shadow, Shader omni, Shader skyboxShader) {
        this.mainShader       = main;
        this.shadowShader     = shadow;
        this.omniShadowShader = omni;
        this.skyboxShader     = skyboxShader;
    }

    // ==========================================================================
    //  SHADOW PASSES
    // ==========================================================================

    public void renderShadowPasses(int windowWidth, int windowHeight) {

        // Directional Shaders
        if (dirLight != null) {
            glBindFramebuffer(GL_FRAMEBUFFER, dirLight.getShadowMap().getDepthMapFBO());
            glViewport(0, 0, 1024, 1024);
            glClear(GL_DEPTH_BUFFER_BIT);
            shadowShader.useShader();
            shadowShader.setUniformMat4f(shadowShader.getUniformLightSpaceMatrix(), dirLight.getLightSpaceMatrix());

            glEnable(GL_CULL_FACE); glCullFace(GL_FRONT);
            for (Model m : models) {
                shadowShader.setUniformMat4f(shadowShader.getUniformModel(), m.getTransform().getModelMatrix());
                m.Draw(shadowShader);
            }
            glDisable(GL_CULL_FACE);
            renderMeshesAndFloor(shadowShader);

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glViewport(0, 0, windowWidth, windowHeight);
        }

        // Spot Shaders
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

        // Point Shaders
        omniShadowShader.useShader();
        for (PointLight p : pointLights) {
            glBindFramebuffer(GL_FRAMEBUFFER, p.getCubeShadowMap().getDepthMapFBO());
            glViewport(0, 0, 1024, 1024);
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

    // ==========================================================================
    // RENDER
    // ==========================================================================

    public void render() {
        mainShader.useShader();

        if (dirLight != null) {
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, dirLight.getShadowMap().getDepthMap());
            glUniform1i(glGetUniformLocation(mainShader.getProgramID(), "shadowMap"), 1);
            mainShader.setUniformMat4f(mainShader.getUniformLightSpaceMatrix(), dirLight.getLightSpaceMatrix());
        }
        for (int i = 0; i < spotLights.size(); i++) {
            glActiveTexture(GL_TEXTURE2 + i);
            glBindTexture(GL_TEXTURE_2D, spotLights.get(i).getShadowMap().getDepthMap());
        }
        int pointBase = 2 + spotLights.size();
        for (int i = 0; i < pointLights.size(); i++) {
            glActiveTexture(GL_TEXTURE0 + pointBase + i);
            glBindTexture(GL_TEXTURE_CUBE_MAP, pointLights.get(i).getCubeShadowMap().getDepthMap());
            glUniform1i(glGetUniformLocation(mainShader.getProgramID(), "pointShadowMaps[" + i + "]"), pointBase + i);
        }

        glUniform1f(glGetUniformLocation(mainShader.getProgramID(), "farPlane"), 100f);
        glUniform1f(glGetUniformLocation(mainShader.getProgramID(), "shadowBiasMultiplier"), shadowBias);
        glUniform1i(glGetUniformLocation(mainShader.getProgramID(), "debugMode"), shadowDebugView ? 1 : 0);

        // lights
        if (dirLight != null) mainShader.setDirectionalLight(dirLight);
        mainShader.setPointLights(pointLights.toArray(new PointLight[0]));
        mainShader.setSpotLights(spotLights.toArray(new SpotLight[0]));

        // handle camera
        if (camera != null) {
            if (useLightCamera && dirLight != null) {
                mainShader.setUniformMat4f(mainShader.getUniformView(),       dirLight.getLightViewMatrix());
                mainShader.setUniformMat4f(mainShader.getUniformProjection(), dirLight.getLightProjectionMatrix());
            } else {
                mainShader.setUniformMat4f(mainShader.getUniformView(),       camera.getViewMatrix());
                mainShader.setUniformMat4f(mainShader.getUniformProjection(), camera.getProjectionMatrix());
            }
            glUniform3f(glGetUniformLocation(mainShader.getProgramID(), "viewPos"),
                camera.getTransform().position.x,
                camera.getTransform().position.y,
                camera.getTransform().position.z);
        }

        // Models
        for (Model m : models) m.Draw(mainShader);

        // Meshes
        for (int i = 0; i < meshes.size(); i++) {
            Mesh m = meshes.get(i);
            mainShader.setUniformMat4f(mainShader.getUniformModel(), m.getTransform().getModelMatrix());
            if (i < meshTextures.size() && meshTextures.get(i) != null) {
                meshTextures.get(i).useTexture();
            }
            m.renderMesh();
        }

        // Floor
        if (floor != null) {
            mainShader.setUniformMat4f(mainShader.getUniformModel(), floor.getTransform().getModelMatrix());
            if (floorTexture  != null) floorTexture.useTexture();
            if (floorMaterial != null) floorMaterial.useMaterial(
                mainShader.getUniformSpecularIntensity(), mainShader.getUniformShininess());
            floor.renderMesh();
        }

        glUseProgram(0);
    }

    public void renderSkybox() {
        if (skybox != null && camera != null) skybox.render(skyboxShader, camera);
    }

    // ==========================================================================
    //  PRIVATE HELPERS
    // ==========================================================================

    private void renderAllObjects(Shader shader) {
        for (Model m : models) {
            shader.setUniformMat4f(shader.getUniformModel(), m.getTransform().getModelMatrix());
            m.Draw(shader);
        }
        renderMeshesAndFloor(shader);
    }

    private void renderMeshesAndFloor(Shader shader) {
        for (Mesh m : meshes) {
            shader.setUniformMat4f(shader.getUniformModel(), m.getTransform().getModelMatrix());
            m.renderMesh();
        }
        if (floor != null) {
            shader.setUniformMat4f(shader.getUniformModel(), floor.getTransform().getModelMatrix());
            floor.renderMesh();
        }
    }

    // ==========================================================================
    //  SETTERS / ADDERS / GETTERS
    // ==========================================================================

    public void setDirLight(DirectionalLight d)     { this.dirLight    = d; }
    public void addPointLight(PointLight p)          { pointLights.add(p); }
    public void removePointLight(int i)              { if (i < pointLights.size()) pointLights.remove(i); }
    public void addSpotLight(SpotLight s)            { spotLights.add(s); }
    public void removeSpotLight(int i)               { if (i < spotLights.size())  spotLights.remove(i); }
    public void addModel(Model m)                    { models.add(m); }
    public void removeModel(int i)                   { if (i < models.size())      models.remove(i); }
    public void setSkybox(Skybox s)                  { this.skybox      = s; }
    public void setCamera(Camera c)                  { this.camera      = c; }
    public void setFloor(Mesh m, Texture t, Material mat) {
        floor = m;
        if (t   != null) floorTexture  = t;
        if (mat != null) floorMaterial = mat;
    }
    public void setFloorTexture(Texture t)           { this.floorTexture  = t; }
    public void setFloorMaterial(Material m)         { this.floorMaterial = m; }

    public void addMesh(Mesh m) {
        meshes.add(m);
        meshTextures.add(null);
    }

    
    public void setMeshTexture(int index, Texture t) {
        if (index >= 0 && index < meshTextures.size()) meshTextures.set(index, t);
    }

    public DirectionalLight getDirLight()    { return dirLight; }
    public List<PointLight> getPointLights() { return pointLights; }
    public List<SpotLight>  getSpotLights()  { return spotLights; }
    public List<Model>      getModels()      { return models; }
    public List<Mesh>       getMeshes()      { return meshes; }
    public Mesh             getFloor()       { return floor; }
    public Camera           getCamera()      { return camera; }
}