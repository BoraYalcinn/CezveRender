package com.bora.renderer;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.*;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import imgui.type.ImBoolean;
import imgui.type.ImInt;
import imgui.type.ImString;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL33C.*;
import static org.lwjgl.glfw.GLFW.*;

public class SceneEditor {

    private final ImGuiImplGlfw imGuiGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3  imGuiGl3  = new ImGuiImplGl3();

    private final Scene scene;
    private final long  windowHandle;

    private boolean guiVisible    = true;
    private boolean tabWasPressed = false;

    // Dir light inputs
    private final float[] dirColor = {1f, 1f, 1f};
    private final float[] dirDir   = {0f, -1f, -0.5f};
    private final float[] dirAmb   = {0.1f};
    private final float[] dirDiff  = {0.8f};
    private final float[] dirOrtho = {20f};
    private boolean dirExists = true;

    // Point light inputs
    private final float[] plColor = {1f, 1f, 1f};
    private final float[] plPos   = {0f, 2f, 0f};
    private final float[] plAmb   = {0.5f};
    private final float[] plDiff  = {0.7f};
    private final float[] plConst = {0.3f};
    private final float[] plLin   = {0.1f};
    private final float[] plExp   = {0.1f};
    private int selectedPL = -1;

    // Spot light inputs
    private final float[] slColor = {1f, 1f, 1f};
    private final float[] slPos   = {0f, 8f, 0f};
    private final float[] slDir   = {0f, -1f, 0f};
    private final float[] slAmb   = {0f};
    private final float[] slDiff  = {2f};
    private final float[] slConst = {0.3f};
    private final float[] slLin   = {0.1f};
    private final float[] slExp   = {0.1f};
    private final float[] slEdge  = {20f};
    private int selectedSL = -1;

    // Textures
    private final List<Texture> textures     = new ArrayList<>();
    private final List<String>  texNames     = new ArrayList<>();
    private final ImString      texInput     = new ImString(256);
    private String              texError     = "";

    // Floor
    private final float[] floorSz  = {10f};
    private final float[] floorY   = {-3f};
    private int           floorTex = -1;

    // Handle Mesh
    private int           meshType = 1;
    private final float[] meshSz   = {1f};
    private final float[] meshY    = {-3f};
    private int           meshTex  = -1;

    //Handle Model 
    private final ImString modelInput = new ImString(256);
    private String         modelError = "";

    
    private int selMesh  = -1;
    private int selModel = -1;

    // Debug
    private final float[]   bias        = {0.005f};
    private final ImBoolean shadowMap   = new ImBoolean(false);
    private final ImBoolean lightCam    = new ImBoolean(false);
    private final ImBoolean shadowDebug = new ImBoolean(false);

    
    public SceneEditor(long windowHandle, Scene scene) {
        this.scene        = scene;
        this.windowHandle = windowHandle;

        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);

        applyTheme();
        imGuiGlfw.init(windowHandle, true);
        imGuiGl3.init("#version 330 core");
        syncFromScene();
    }

    public void newFrame() {
        boolean tabDown = glfwGetKey(windowHandle, GLFW_KEY_TAB) == GLFW_PRESS;
        if (tabDown && !tabWasPressed) {
            guiVisible = !guiVisible;
            glfwSetInputMode(windowHandle, GLFW_CURSOR,
                guiVisible ? GLFW_CURSOR_NORMAL : GLFW_CURSOR_DISABLED);
        }
        tabWasPressed = tabDown;
        imGuiGlfw.newFrame();
        ImGui.newFrame();
    }

    public void render() {
        if (guiVisible) {
            panelLights();
            panelScene();
            panelDebug();
            panelTextures();
        }
        ImGui.render();
        imGuiGl3.renderDrawData(ImGui.getDrawData());
    }

    public void dispose() {
        imGuiGl3.dispose();
        imGuiGlfw.dispose();
        ImGui.destroyContext();
    }

    public boolean isGuiVisible() { return guiVisible; }

    // =========================================================================
    //  LIGHTS USER INTERFACE
    // =========================================================================
    private void panelLights() {
        ImGui.setNextWindowPos(10, 10, ImGuiCond.Once);
        ImGui.setNextWindowSize(340, 600, ImGuiCond.Once);
        ImGui.begin("Lights");

        // DIRECTIONAL LIGHTS
        if (ImGui.collapsingHeader("Directional Light", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.colorEdit3("Color##d",    dirColor);
            ImGui.sliderFloat3("Dir##d",    dirDir, -1f, 1f);
            ImGui.sliderFloat("Ambient##d", dirAmb,   0f, 1f);
            ImGui.sliderFloat("Diffuse##d", dirDiff,  0f, 2f);
            ImGui.sliderFloat("Ortho##d",   dirOrtho, 1f, 200f);

            if (ImGui.button("Apply##d"))  buildAndSetDirLight();
            ImGui.sameLine();
            if (ImGui.button("Reset##d")) {
                dirColor[0]=1;dirColor[1]=1;dirColor[2]=1;
                dirDir[0]=0;dirDir[1]=-1;dirDir[2]=-0.5f;
                dirAmb[0]=0.1f; dirDiff[0]=0.8f; dirOrtho[0]=20f;
                buildAndSetDirLight();
            }
            ImGui.sameLine();
            if (dirExists) {
                if (ImGui.button("Remove##d")) { scene.setDirLight(null); dirExists=false; }
            } else {
                if (ImGui.button("Restore##d")) buildAndSetDirLight();
            }
        }

        ImGui.separator();

        // POINT LIGHTS
        if (ImGui.collapsingHeader("Point Lights", ImGuiTreeNodeFlags.DefaultOpen)) {
            List<PointLight> pls = scene.getPointLights();
            int toRemove = -1;
            for (int i = 0; i < pls.size(); i++) {
                
                ImGui.pushID(i + 1000);
                boolean sel = (selectedPL == i);
                if (ImGui.button(sel ? "[" + i + "]" : " " + i + " ")) {
                    selectedPL = i;
                    loadPLForm(pls.get(i));
                }
                ImGui.sameLine();
                ImGui.text("Point Light " + i);
                ImGui.sameLine();
                if (ImGui.button("X##rpl")) toRemove = i;
                ImGui.popID();
            }
            if (toRemove >= 0) {
                scene.removePointLight(toRemove);
                if (selectedPL == toRemove)      selectedPL = -1;
                else if (selectedPL > toRemove)  selectedPL--;
            }

            ImGui.separator();
            ImGui.textColored(0.3f, 0.9f, 0.3f, 1f, "Add / Edit");
            ImGui.colorEdit3("Color##pl",   plColor);
            ImGui.dragFloat3("Pos##pl",     plPos, 0.1f);
            ImGui.sliderFloat("Amb##pl",    plAmb,   0f, 1f);
            ImGui.sliderFloat("Diff##pl",   plDiff,  0f, 4f);
            ImGui.sliderFloat("Const##pl",  plConst, 0.01f, 2f);
            ImGui.sliderFloat("Linear##pl", plLin,   0f, 1f);
            ImGui.sliderFloat("Exp##pl",    plExp,   0f, 1f);
            if (ImGui.button("Add##pl")) { scene.addPointLight(buildPL()); }
            if (selectedPL >= 0 && selectedPL < pls.size()) {
                ImGui.sameLine();
                if (ImGui.button("Update##pl")) {
                    scene.removePointLight(selectedPL);
                    scene.getPointLights().add(selectedPL, buildPL());
                }
            }
        }

        ImGui.separator();

        // SPOT LIGHTS
        if (ImGui.collapsingHeader("Spot Lights", ImGuiTreeNodeFlags.DefaultOpen)) {
            List<SpotLight> sls = scene.getSpotLights();
            int toRemove = -1;
            for (int i = 0; i < sls.size(); i++) {
                ImGui.pushID(i + 2000);
                boolean sel = (selectedSL == i);
                if (ImGui.button(sel ? "[" + i + "]" : " " + i + " ")) {
                    selectedSL = i;
                    loadSLForm(sls.get(i));
                }
                ImGui.sameLine();
                ImGui.text("Spot Light " + i);
                ImGui.sameLine();
                if (ImGui.button("X##rsl")) toRemove = i;
                ImGui.popID();
            }
            if (toRemove >= 0) {
                scene.removeSpotLight(toRemove);
                if (selectedSL == toRemove)      selectedSL = -1;
                else if (selectedSL > toRemove)  selectedSL--;
            }

            ImGui.separator();
            ImGui.textColored(0.3f, 0.7f, 1f, 1f, "Add / Edit");
            ImGui.colorEdit3("Color##sl",  slColor);
            ImGui.dragFloat3("Pos##sl",    slPos, 0.1f);
            ImGui.dragFloat3("Dir##sl",    slDir, 0.01f);
            ImGui.sliderFloat("Amb##sl",   slAmb,   0f, 1f);
            ImGui.sliderFloat("Diff##sl",  slDiff,  0f, 4f);
            ImGui.sliderFloat("Const##sl", slConst, 0.01f, 2f);
            ImGui.sliderFloat("Lin##sl",   slLin,   0f, 1f);
            ImGui.sliderFloat("Exp##sl",   slExp,   0f, 1f);
            ImGui.sliderFloat("Edge##sl",  slEdge,  1f, 89f);
            if (ImGui.button("Add##sl")) { scene.addSpotLight(buildSL()); }
            if (selectedSL >= 0 && selectedSL < sls.size()) {
                ImGui.sameLine();
                if (ImGui.button("Update##sl")) {
                    scene.removeSpotLight(selectedSL);
                    scene.getSpotLights().add(selectedSL, buildSL());
                }
            }
        }

        ImGui.end();
    }

    // =========================================================================
    //  SCENE PANEL
    // =========================================================================
    private void panelScene() {
        ImGui.setNextWindowPos(10, 620, ImGuiCond.Once);
        ImGui.setNextWindowSize(340, 420, ImGuiCond.Once);
        ImGui.begin("Scene");

        //Floor
        if (ImGui.collapsingHeader("Floor", ImGuiTreeNodeFlags.DefaultOpen)) {
            ImGui.sliderFloat("Size##fl", floorSz, 1f, 100f);
            ImGui.sliderFloat("Y##fl",    floorY,  -20f, 5f);
            String[] txOpts = texComboItems();
            ImInt fi = new ImInt(floorTex + 1);
            if (ImGui.combo("Tex##fl", fi, txOpts)) floorTex = fi.get() - 1;
            if (ImGui.button("Rebuild Floor")) {
                Mesh nf = MeshFactory.createFloor(floorSz[0], floorY[0]);
                Texture t = (floorTex >= 0 && floorTex < textures.size()) ? textures.get(floorTex) : null;
                scene.setFloor(nf, t, null);
            }
        }

        ImGui.separator();

        // Add Mesh
        if (ImGui.collapsingHeader("Add Mesh", ImGuiTreeNodeFlags.DefaultOpen)) {
            String[] types = {"Floor Plane","Pyramid","Cube"};
            ImInt mt = new ImInt(meshType);
            if (ImGui.combo("Type##am", mt, types)) meshType = mt.get();
            ImGui.sliderFloat("Size##am", meshSz, 0.1f, 20f);
            if (meshType == 0) ImGui.sliderFloat("Y##am", meshY, -20f, 5f);
            String[] txOpts = texComboItems();
            ImInt mi = new ImInt(meshTex + 1);
            if (ImGui.combo("Tex##am", mi, txOpts)) meshTex = mi.get() - 1;
            if (ImGui.button("Create & Add")) createAndAddMesh();
        }

        ImGui.separator();

        //Handle Mesh List
        if (ImGui.collapsingHeader("Meshes", ImGuiTreeNodeFlags.DefaultOpen)) {
            List<Mesh> meshes = scene.getMeshes();
            int toRemove = -1;
            for (int i = 0; i < meshes.size(); i++) {
                ImGui.pushID(i + 3000);
                if (ImGui.button(selMesh == i ? "[" + i + "]" : " " + i + " ")) selMesh = i;
                ImGui.sameLine();
                ImGui.text("Mesh " + i);
                ImGui.sameLine();
                if (ImGui.button("X##rmsh")) toRemove = i;
                ImGui.popID();
            }
            if (toRemove >= 0) {
                scene.getMeshes().remove(toRemove);
                if (selMesh == toRemove)     selMesh = -1;
                else if (selMesh > toRemove) selMesh--;
            }
        }

        ImGui.separator();

        //Load Model
        if (ImGui.collapsingHeader("Load OBJ Model")) {
            ImGui.inputText("Path##moi", modelInput);
            if (!modelError.isEmpty()) ImGui.textColored(1f,0.3f,0.3f,1f, modelError);
            if (ImGui.button("Load##mo")) {
                String path = modelInput.get().trim();
                if (!path.isEmpty()) {
                    try {
                        scene.addModel(new Model(path));
                        modelInput.set("");
                        modelError = "";
                    } catch (Exception e) {
                        modelError = "Failed: " + e.getMessage();
                    }
                }
            }
        }

        ImGui.separator();

        if (ImGui.collapsingHeader("Models", ImGuiTreeNodeFlags.DefaultOpen)) {
            List<Model> models = scene.getModels();
            int toRemove = -1;
            for (int i = 0; i < models.size(); i++) {
                ImGui.pushID(i + 4000);
                if (ImGui.button(selModel == i ? "[" + i + "]" : " " + i + " ")) selModel = i;
                ImGui.sameLine();
                ImGui.text("Model " + i);
                ImGui.sameLine();
                if (ImGui.button("X##rmdl")) toRemove = i;
                ImGui.popID();
            }
            if (toRemove >= 0) {
                scene.removeModel(toRemove);
                if (selModel == toRemove)     selModel = -1;
                else if (selModel > toRemove) selModel--;
            }

            if (selModel >= 0 && selModel < models.size()) {
                ImGui.separator();
                Model m = models.get(selModel);
                float[] p = {m.getTransform().position.x, m.getTransform().position.y, m.getTransform().position.z};
                float[] r = {m.getTransform().rotation.x, m.getTransform().rotation.y, m.getTransform().rotation.z};
                float[] s = {m.getTransform().scale.x,    m.getTransform().scale.y,    m.getTransform().scale.z};
                if (ImGui.dragFloat3("Pos",   p, 0.01f)) m.getTransform().position.set(p[0],p[1],p[2]);
                if (ImGui.dragFloat3("Rot",   r, 0.5f))  m.getTransform().rotation.set(r[0],r[1],r[2]);
                if (ImGui.dragFloat3("Scale", s, 0.01f)) m.getTransform().scale.set(s[0],s[1],s[2]);
            }
        }

        ImGui.end();
    }

    // =========================================================================
    //  DEBUG PANEL
    // =========================================================================
    private void panelDebug() {
        ImGui.setNextWindowPos(1270, 10, ImGuiCond.Once);
        ImGui.setNextWindowSize(320, 230, ImGuiCond.Once);
        ImGui.begin("Debug");

        ImGui.checkbox("Shadow Debug Colors", shadowDebug);
        ImGui.sliderFloat("Shadow Bias", bias,     0f, 0.05f);
        ImGui.sliderFloat("Ortho Size",  dirOrtho, 1f, 200f);
        if (scene.getDirLight() != null) scene.getDirLight().setOrthoSize(dirOrtho[0]);

        ImGui.separator();
        Camera cam = scene.getCamera();
        if (cam != null) ImGui.text(String.format("Cam: %.1f %.1f %.1f",
            cam.getTransform().position.x,
            cam.getTransform().position.y,
            cam.getTransform().position.z));
        ImGui.text("PL: " + scene.getPointLights().size() +
                   "  SL: " + scene.getSpotLights().size() +
                   "  M: "  + scene.getMeshes().size() +
                   "  Mo: " + scene.getModels().size());

        
        scene.shadowDebugView = shadowDebug.get();
        scene.shadowBias      = bias[0];

        ImGui.end();
    }

    // =========================================================================
    //  TEXTURE PANEL
    // =========================================================================
    private void panelTextures() {
        ImGui.setNextWindowPos(1270, 250, ImGuiCond.Once);
        ImGui.setNextWindowSize(320, 200, ImGuiCond.Once);
        ImGui.begin("Textures");

        for (int i = 0; i < texNames.size(); i++)
            ImGui.bulletText(i + ": " + texNames.get(i));

        ImGui.separator();
        ImGui.inputText("Path##tx", texInput);
        ImGui.textDisabled("e.g. textures/brick.png");
        if (!texError.isEmpty()) ImGui.textColored(1f,0.3f,0.3f,1f, texError);

        if (ImGui.button("Load Texture")) {
            String raw = texInput.get().trim();
            if (!raw.isEmpty()) {
                // Classpath'e uygun path — backslash → forward slash
                String path = raw.replace('\\', '/');
                try {
                    Texture t = new Texture(path);
                    t.loadTexture();
                    textures.add(t);
                    // İsim: son / 'dan sonrası
                    int slash = path.lastIndexOf('/');
                    texNames.add(slash >= 0 ? path.substring(slash + 1) : path);
                    texInput.set("");
                    texError = "";
                } catch (Exception e) {
                    texError = "Failed: " + e.getMessage();
                    System.err.println("[Tex] " + e.getMessage());
                }
            }
        }

        ImGui.end();
    }

    // =========================================================================
    //  BUILDERS
    // =========================================================================
    private void buildAndSetDirLight() {
        DirectionalLight dl = new DirectionalLight(
            dirColor[0], dirColor[1], dirColor[2],
            dirAmb[0], dirDiff[0],
            dirDir[0], dirDir[1], dirDir[2]);
        dl.setOrthoSize(dirOrtho[0]);
        scene.setDirLight(dl);
        dirExists = true;
    }

    private PointLight buildPL() {
        return new PointLight(plColor[0],plColor[1],plColor[2],
            plAmb[0],plDiff[0], plPos[0],plPos[1],plPos[2],
            plConst[0],plLin[0],plExp[0]);
    }

    private SpotLight buildSL() {
        return new SpotLight(slColor[0],slColor[1],slColor[2],
            slAmb[0],slDiff[0], slPos[0],slPos[1],slPos[2],
            slDir[0],slDir[1],slDir[2], slConst[0],slLin[0],slExp[0],slEdge[0]);
    }

    private void loadPLForm(PointLight p) {
        plColor[0]=p.getColor().x; plColor[1]=p.getColor().y; plColor[2]=p.getColor().z;
        plPos[0]=p.getPosition().x; plPos[1]=p.getPosition().y; plPos[2]=p.getPosition().z;
        plAmb[0]=p.getAmbientIntensity(); plDiff[0]=p.getDiffuseIntensity();
        plConst[0]=p.getConstant(); plLin[0]=p.getLinear(); plExp[0]=p.getExponent();
    }

    private void loadSLForm(SpotLight s) {
        slColor[0]=s.getColor().x; slColor[1]=s.getColor().y; slColor[2]=s.getColor().z;
        slPos[0]=s.getPosition().x; slPos[1]=s.getPosition().y; slPos[2]=s.getPosition().z;
        slDir[0]=s.direction.x; slDir[1]=s.direction.y; slDir[2]=s.direction.z;
        slAmb[0]=s.getAmbientIntensity(); slDiff[0]=s.getDiffuseIntensity();
        slConst[0]=s.getConstant(); slLin[0]=s.getLinear(); slExp[0]=s.getExponent();
        double c = Math.max(-1.0, Math.min(1.0, s.procEdge));
        slEdge[0] = (float) Math.toDegrees(Math.acos(c));
    }

    private void createAndAddMesh() {
        Mesh m;
        if (meshType == 0)      m = MeshFactory.createFloor(meshSz[0], meshY[0]);
        else if (meshType == 1) m = MeshFactory.createPyramid(meshSz[0]);
        else                    m = MeshFactory.createCube(meshSz[0]);
        scene.addMesh(m);
        Texture t = (meshTex >= 0 && meshTex < textures.size()) ? textures.get(meshTex) : null;
        scene.setMeshTexture(scene.getMeshes().size() - 1, t);
    }

    private String[] texComboItems() {
        String[] items = new String[texNames.size() + 1];
        items[0] = "-- keep current --";
        for (int i = 0; i < texNames.size(); i++) items[i+1] = i + ": " + texNames.get(i);
        return items;
    }

    private void syncFromScene() {
        DirectionalLight dl = scene.getDirLight();
        if (dl != null) {
            dirColor[0]=dl.getColor().x; dirColor[1]=dl.getColor().y; dirColor[2]=dl.getColor().z;
            dirDir[0]=dl.getDirection().x; dirDir[1]=dl.getDirection().y; dirDir[2]=dl.getDirection().z;
            dirAmb[0]=dl.getAmbientIntensity(); dirDiff[0]=dl.getDiffuseIntensity();
            dirOrtho[0]=dl.getOrthoSize(); dirExists=true;
        } else { dirExists=false; }
    }

    private void applyTheme() {
        ImGui.styleColorsDark();
        float[][] c = ImGui.getStyle().getColors();
        c[ImGuiCol.WindowBg]      = new float[]{0.08f,0.09f,0.12f,0.95f};
        c[ImGuiCol.TitleBgActive] = new float[]{0.16f,0.20f,0.35f,1.00f};
        c[ImGuiCol.Header]        = new float[]{0.20f,0.25f,0.40f,0.80f};
        c[ImGuiCol.HeaderHovered] = new float[]{0.25f,0.35f,0.55f,1.00f};
        c[ImGuiCol.Button]        = new float[]{0.20f,0.28f,0.48f,1.00f};
        c[ImGuiCol.ButtonHovered] = new float[]{0.30f,0.40f,0.65f,1.00f};
        c[ImGuiCol.FrameBg]       = new float[]{0.14f,0.16f,0.22f,1.00f};
        c[ImGuiCol.SliderGrab]    = new float[]{0.40f,0.55f,0.85f,1.00f};
        c[ImGuiCol.CheckMark]     = new float[]{0.40f,0.80f,0.60f,1.00f};
        ImGui.getStyle().setColors(c);
    }
}