package com.bora.renderer;

import org.joml.Matrix4f;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;
import java.nio.charset.StandardCharsets;

import static org.lwjgl.opengl.GL33C.*;


public class Shader {

	private int shaderID;
	
	private int uniformProjection;
	private int uniformModel;
	private int uniformView;
	private int uniformTexture;
	
	
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
		uniformView = glGetUniformLocation(shaderID, "view");
		uniformTexture = glGetUniformLocation(shaderID, "theTexture");
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
	
	public void setUniformMat4f(int uniformLocation, Matrix4f matrix) {
        try(MemoryStack stack = MemoryStack.stackPush()) {
            FloatBuffer fb = stack.mallocFloat(16);
            matrix.get(fb);
            glUniformMatrix4fv(uniformLocation, false, fb);
        }
    }
	
	public void setTextureUnit(int unit) {
	    glUniform1i(uniformTexture, unit);
	}
	
	// --- EKLENDİ: DirectionalLight setter ---
	public void setDirectionalLight(DirectionalLight dLight) {
	    // Program aktif değilse aktif et
	    useShader();

	    glUniform3f(glGetUniformLocation(shaderID, "directionalLight.base.color"),
	                dLight.getColor().x, dLight.getColor().y, dLight.getColor().z);
	    glUniform1f(glGetUniformLocation(shaderID, "directionalLight.base.ambientIntensity"),
	                dLight.getAmbientIntensity());
	    glUniform1f(glGetUniformLocation(shaderID, "directionalLight.base.diffuseIntensity"),
	                dLight.getDiffuseIntensity());
	    glUniform3f(glGetUniformLocation(shaderID, "directionalLight.dir"),
	                dLight.getDirection().x, dLight.getDirection().y, dLight.getDirection().z);
	}

	// --- EKLENDİ: PointLight array setter ---
	public void setPointLights(PointLight[] lights) {
	    useShader();

	    int count = Math.min(lights.length, 10); // MAX_POINT_LIGHTS = 10
	    glUniform1i(glGetUniformLocation(shaderID, "pointLightCount"), count);

	    for (int i = 0; i < count; i++) {
	        PointLight p = lights[i];
	        String prefix = "pointLights[" + i + "].";

	        glUniform3f(glGetUniformLocation(shaderID, prefix + "base.color"),
	                    p.getColor().x, p.getColor().y, p.getColor().z);
	        glUniform1f(glGetUniformLocation(shaderID, prefix + "base.ambientIntensity"),
	                    p.getAmbientIntensity());
	        glUniform1f(glGetUniformLocation(shaderID, prefix + "base.diffuseIntensity"),
	                    p.getDiffuseIntensity());

	        glUniform3f(glGetUniformLocation(shaderID, prefix + "position"),
	                    p.getPosition().x, p.getPosition().y, p.getPosition().z);
	        glUniform1f(glGetUniformLocation(shaderID, prefix + "constant"), p.getConstant());
	        glUniform1f(glGetUniformLocation(shaderID, prefix + "linear"), p.getLinear());
	        glUniform1f(glGetUniformLocation(shaderID, prefix + "exponent"), p.getExponent());
	    }
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
	
	public int getUniformModel() {
		return uniformModel;
	}
	
	public int getUniformView() {
		return uniformView;
	}
	
	public int getUniformProjection() {
		return uniformProjection;
	}
	
	public int getProgramID() {
	    return shaderID;
	}
	
}
