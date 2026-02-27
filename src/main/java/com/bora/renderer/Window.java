package com.bora.renderer;

import java.nio.*;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

public class Window {

	private int width;
	private int height;
	private long mainWindow;
	private IntBuffer bufferHeight;
	private IntBuffer bufferWidth;
	
	
	
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
	    
	    glEnable(GL_DEPTH_TEST);
	}
	
	public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }
	
	public void swapBuffers() {
		glfwSwapBuffers(this.mainWindow);
	}
	
	public boolean getShouldClose() {
		return glfwWindowShouldClose(mainWindow);
	}
	
	public int getWidth() { return width; }
    public int getHeight() { return height; }
	
	public IntBuffer getBufferWidth() {
		return bufferWidth;
	}
	
	public IntBuffer getBufferHeight() {
		return bufferHeight;
	}
	
	public long getWindow() {
		return mainWindow;
	}
	
	public void destroy() {
		glfwDestroyWindow(mainWindow);
		glfwTerminate();
	}
	
}
