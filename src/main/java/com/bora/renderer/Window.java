package com.bora.renderer;

import java.nio.*;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

public class Window {

	private int width;
	private int height;
	private long mainWindow;
	private IntBuffer bufferHeight;
	private IntBuffer bufferWidth;
	private boolean[] keys = new boolean[1024];
	
	
	public Window() {
		
		this.width = 800;
		this.height = 600;
	}
	
	public Window(int windowWidth,int windowHeight) {
		this.width = windowWidth;
		this.height = windowHeight;
	}
	
	public void run() {
		initializeWindow();
		glfwSetKeyCallback(mainWindow, (window, key, scancode, action, mods) -> {
		    handleKeys(window, key, scancode, action, mods);
		});
	}
	
	
	public void initializeWindow() {
		
		if(!glfwInit()) throw new IllegalStateException("Window couldn't be initlialized");
		

	    // Setup GLFW window properties
	    // OpenGL version
	    glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
	    glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
	    // Core profile = No backwards compatibility
	    glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
	    // Allow forward compatibility
	    glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
		
	    mainWindow = glfwCreateWindow(width,height,"3D Renderer",0,0);
	    if(mainWindow == 0) throw new RuntimeException("Failed to create the glfw window");
	    
	    this.bufferWidth = BufferUtils.createIntBuffer(1);
	    this.bufferHeight = BufferUtils.createIntBuffer(1);
	    
	    glfwGetWindowSize(mainWindow,bufferWidth,bufferHeight);
	    
	    GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
	    glfwSetWindowPos(
				mainWindow,
				(vidmode.width() - bufferWidth.get(0)) / 2,
				(vidmode.height() - bufferHeight.get(0)) / 2
			);
	    
	    glfwMakeContextCurrent(mainWindow);
	    GL.createCapabilities();
	    glfwShowWindow(mainWindow);
	}
	
	private void handleKeys(long window, int key, int scancode, int action, int mods) {
	    
	    if (key == GLFW_KEY_ESCAPE && action == GLFW_PRESS) {
	        glfwSetWindowShouldClose(window, true);
	    }

	    
	    if (key == GLFW_KEY_W && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
	        System.out.println("W pressed!");
	    }

	    
	    if (key == GLFW_KEY_A && (action == GLFW_PRESS || action == GLFW_REPEAT)) {
	        System.out.println("A pressed!");
	    }

	    
	}
	
	public boolean[] getKeys() {
		return keys;
	}
	
	public void swapBuffers() {
		glfwSwapBuffers(this.mainWindow);
	}
	
	public boolean getShouldClose() {
		return glfwWindowShouldClose(mainWindow);
	}
	
	public IntBuffer getBufferWidth() {
		return bufferWidth;
	}
	
	public IntBuffer getBufferHeight() {
		return bufferHeight;
	}
	
	
}
