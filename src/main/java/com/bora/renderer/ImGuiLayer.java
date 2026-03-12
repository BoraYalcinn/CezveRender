package com.bora.renderer;

import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import imgui.glfw.ImGuiImplGlfw;
import imgui.gl3.ImGuiImplGl3;

public class ImGuiLayer {

    private final ImGuiImplGlfw implGlfw = new ImGuiImplGlfw();
    private final ImGuiImplGl3 implGl3 = new ImGuiImplGl3();

    public void init(long windowHandle) {
        ImGui.createContext();
        ImGuiIO io = ImGui.getIO();
        io.setIniFilename("imgui.ini");
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        implGlfw.init(windowHandle, true);
        implGl3.init("#version 330 core");
    }

    public void startFrame() {
        implGlfw.newFrame();
        ImGui.newFrame();
    }

    public void render() {
        ImGui.render();
        implGl3.renderDrawData(ImGui.getDrawData());
    }

    public void dispose() {
        implGl3.dispose();
        implGlfw.dispose();
        ImGui.destroyContext();
    }
}
