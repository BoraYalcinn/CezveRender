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
	private int uniformView;
	
	
	
	public Shader() {
		shaderID = 0;
		uniformProjection = 0;
		uniformModel = 0;
	}
	
	public Shader(String vertPath,String fragPath) {
		
		shaderID = glCreateProgram();
		
		String vertexResource = loadResource(vertPath);
		String fragmentResource = loadResource(fragPath);
		
		
		compileShader(vertexResource,fragmentResource);
		
		
	}
	
	private void compileShader(String verResource,String fragResource) {
		
		addShader(verResource,GL_VERTEX_SHADER);
		addShader(fragResource,GL_FRAGMENT_SHADER);
		
		glLinkProgram(shaderID);
		
		if (glGetProgrami(shaderID, GL_LINK_STATUS) == GL_FALSE) {
            throw new RuntimeException(
                    "Error linking shader program:\n" +
                    glGetProgramInfoLog(shaderID));
        }

        glValidateProgram(shaderID);
        
		
		uniformModel = glGetUniformLocation(shaderID,"model");
		uniformProjection = glGetUniformLocation(shaderID,"projection");
		// uniformView = glGetUniformLocation(shaderID, "view");
		
	}
	
	private void addShader(String source,int type) {
		int theShader = glCreateShader(type);
		
		glShaderSource(theShader,source);
		glCompileShader(theShader);
		
		if (glGetShaderi(theShader, GL_COMPILE_STATUS) == GL_FALSE) {
            throw new RuntimeException(
                    "Error compiling shader:\n" +
                    glGetShaderInfoLog(theShader));
        }

        glAttachShader(shaderID, theShader);

        glDeleteShader(theShader);
	}
	
	public void useShader() {
		glUseProgram(shaderID);
	}
	
	public void clearShader() {
		glDeleteProgram(shaderID);
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
