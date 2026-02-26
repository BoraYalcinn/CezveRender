package com.bora.renderer;

import org.lwjgl.*;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33C.*;


public class Shader {

	private int shaderID;
	private int uniformProjection;
	private int uniformModel;
	
	public Shader() {
		shaderID = 0;
		uniformProjection = 0;
		uniformModel = 0;
	}
	
	public Shader(String vertPath,String fragPath) {
		String vertexResource = loadResource(vertPath);
		String fragmentResource = loadResource(fragPath);
		
			
		shaderID = glCreateProgram();
		
		
		
	}
	
	private void compileShader() {
		
		
		
	}
	
	private void addShader() {
		
	}
	
	public void useShader() {
		
		
	}
	
	public void clearShader() {
		
		
	}
	
	private String loadResource(String path) {
		try(InputStream in = getClass().getClassLoader().getResourceAsStream(path)){
			if(in == null)throw new RuntimeException("Shader File Not Found!");
			return new String(in.readAllBytes(),StandardCharsets.UTF_8);
		} catch(IOException e){
			throw new RuntimeException("Failed To Load Shader File: "+ path,e);
		}
		
	}
	
	
	
	
}
