package com.bora.renderer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

import imgui.ImGui;
import imgui.ImVec2;

public class Main {

    private static int debugQuadVAO = 0;

    

    // =========================================================================
    //  DEBUG FUNCTIONS
    // =========================================================================
    private static int initDebugQuad() {
        float[] v = {
             0.5f, 0.5f,  0f, 0f,
             1.0f, 0.5f,  1f, 0f,
             1.0f, 1.0f,  1f, 1f,
             0.5f, 1.0f,  0f, 1f,
        };
        int[] idx = {0,1,2, 0,2,3};
        int vao = glGenVertexArrays(), vbo = glGenBuffers(), ibo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, v, GL_STATIC_DRAW);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, idx, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4*Float.BYTES, 0L);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4*Float.BYTES, 2L*Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
        return vao;
    }

    private static void renderDebugQuad(Shader sh, int depthMap) {
        glDisable(GL_DEPTH_TEST);
        sh.useShader();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, depthMap);
        glUniform1i(glGetUniformLocation(sh.getProgramID(), "depthMap"), 0);
        glUniform1f(glGetUniformLocation(sh.getProgramID(), "nearPlane"), 0.1f);
        glUniform1f(glGetUniformLocation(sh.getProgramID(), "farPlane"), 100f);
        glBindVertexArray(debugQuadVAO);
        glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        glUseProgram(0);
    }
    

    // =========================================================================
    //  MAIN
    // =========================================================================
    public static void main(String[] args) {

        // Window
        Window window = new Window();
        window.run();
        

        // Shaders
        Shader mainShader       = new Shader("shaders/shader.vert",      "shaders/shader.frag");
        Shader shadowShader     = new Shader("shaders/shadow.vert",      "shaders/shadow.frag");
        Shader debugShader      = new Shader("shaders/debug_depth.vert", "shaders/debug_depth.frag");
        Shader omniShadowShader = new Shader("shaders/omni_shadow.vert", "shaders/omni_shadow.geom", "shaders/omni_shadow.frag");
        Shader skyboxShader     = new Shader("shaders/skybox.vert",      "shaders/skybox.frag");

        // Scene
        Scene scene = new Scene(mainShader, shadowShader, omniShadowShader, skyboxShader);

        // Skybox
        scene.setSkybox(new Skybox("textures/skybox"));

        // Camera
        Camera camera = new Camera(70f, (float)window.getWidth()/window.getHeight(), 0.01f, 100f);
        camera.getTransform().position.z = 4f;
        scene.setCamera(camera);

        // initiliaze floor
        Texture grass = new Texture("textures/grass.jpg"); grass.loadTexture();
        scene.setFloor(MeshFactory.createFloor(10f, -3f), grass, new Material(256f, 0f));

        // create initial model
        Model object = new Model("models/TaleWorlds.obj");
        object.getTransform().position.set(0f, -2f, 0f);
        scene.addModel(object);

         
        // set initial dirlight
        scene.setDirLight(new DirectionalLight(1f,1f,1f, 0.1f,0.8f, 0f,-1f,-0.5f));

        // Debug 
        debugQuadVAO = initDebugQuad();

        // Input 
        Input input = new Input(window.getWindow());

        // GUI
        SceneEditor editor = new SceneEditor(window.getWindow(), scene, window.getWidth(), window.getHeight());;

        float speed = 3f, sensitivity = 1f;
        double lastTime = glfwGetTime();

        // RENDER LOOP
        while (!window.getShouldClose()) {

            double now = glfwGetTime();
            float dt   = (float)(now - lastTime);
            lastTime   = now;

            // Shadow passes
            scene.renderShadowPasses(window.getWidth(), window.getHeight());

            glfwPollEvents();

            if (input.isKeyDown(GLFW_KEY_ESCAPE))
                glfwSetWindowShouldClose(window.getWindow(), true);
            
            if(input.isKeyDown(GLFW_KEY_F))
            	window.toggleFullScreen();
            
            if (!scene.useLightCamera && !editor.isGuiVisible())
                camera.handleMovement(speed * dt, camera, input, sensitivity);

            window.clear();
            scene.render();
            scene.renderSkybox();

            if (scene.showShadowMap && scene.getDirLight() != null)
                renderDebugQuad(debugShader, scene.getDirLight().getShadowMap().getDepthMap());

            editor.newFrame();
            editor.render();

            window.swapBuffers();
        }
        
        // CLEAR
        editor.dispose();
        mainShader.clearShader();
        shadowShader.clearShader();
        debugShader.clearShader();
        omniShadowShader.clearShader();
        skyboxShader.clearShader();
        window.destroy();
    }
}