package com.bora.renderer;

import static org.lwjgl.glfw.GLFW.*;

public class Input {

    @SuppressWarnings("unused")
	private long window;

    private boolean[] keys = new boolean[GLFW_KEY_LAST];
    private double mouseX, mouseY;
    private double lastMouseX, lastMouseY;
    private float xChange, yChange;
    private boolean firstMouse = true;

    public Input(long window) {
        this.window = window;

        
        glfwSetKeyCallback(window, (win, key, scancode, action, mods) -> {
            if (key >= 0 && key < keys.length) {
                keys[key] = action != GLFW_RELEASE;
            }
        });

        
        glfwSetCursorPosCallback(window, (win, xpos, ypos) -> {
            mouseX = xpos;
            mouseY = ypos;

            if (firstMouse) {
                lastMouseX = xpos;
                lastMouseY = ypos;
                firstMouse = false;
            }

            xChange = (float)(xpos - lastMouseX);
            yChange = (float)(lastMouseY - ypos); 

            lastMouseX = mouseX;
            lastMouseY = mouseY;
        });

        
        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    public boolean isKeyDown(int key) {
        return keys[key];
    }

    
    public float getXChange() {
        float temp = xChange;
        xChange = 0; 
        return temp;
    }

    public float getYChange() {
        float temp = yChange;
        yChange = 0; 
        return temp;
    }
}