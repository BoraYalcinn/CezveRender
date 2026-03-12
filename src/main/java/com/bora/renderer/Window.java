package com.bora.renderer;

import java.nio.*;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;

public class Window {

	private int winWidth;
	private int winHeight;
	private int monWidth;
	private int monHeight;
	
	private boolean isFullScreen = false;
	private long mainWindow;
	
	private IntBuffer bufferHeight;
	private IntBuffer bufferWidth;
	
	
	
	public Window() {
		
		this.winWidth = 800;
		this.winHeight = 600;
	}
	
	public Window(int windowWidth,int windowHeight) {
		this.winWidth = windowWidth;
		this.winHeight = windowHeight;
	}
	
	public void run() {
		initializeWindow();
	}
	
	public void toggleFullScreen() {
	    long monitor = glfwGetPrimaryMonitor();
	    GLFWVidMode vid = glfwGetVideoMode(monitor);
	    if (!isFullScreen) {
	        glfwSetWindowMonitor(mainWindow, monitor, 0, 0, vid.width(), vid.height(), vid.refreshRate());
	        isFullScreen = true;
	    } else {
	        glfwSetWindowMonitor(mainWindow, 0,
	            (monWidth - winWidth) / 2, (monHeight - winHeight) / 2,
	            winWidth, winHeight, 0);
	        isFullScreen = false;
	    }
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
		
	    
	    
	    // get monitor res
	    GLFWVidMode vid= glfwGetVideoMode(glfwGetPrimaryMonitor());
	    
	    monWidth  = vid.width();
	    monHeight = vid.height();
	    // scale it so that it will fill out 80 percent of the users screen 
	    // press F to to toggle FullScreen checkout main
	    winWidth  = (int)(monWidth  * 0.8f);
	    winHeight = (int)(monHeight * 0.8f);
	    
	    mainWindow = glfwCreateWindow(winWidth, winHeight, "3D Renderer", 0, 0);
	    glfwSetWindowPos(mainWindow, (monWidth - winWidth) / 2, (monHeight - winHeight) / 2);
	    
	    
	    
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
	    glDepthFunc(GL_LESS);
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
	
	public int getWidth()  { int[] w = new int[1], h = new int[1]; glfwGetWindowSize(mainWindow, w, h); return w[0]; }
	public int getHeight() { int[] w = new int[1], h = new int[1]; glfwGetWindowSize(mainWindow, w, h); return h[0]; }

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
