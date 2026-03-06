package com.bora.renderer;

import java.io.InputStream;
import java.nio.*;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import static org.lwjgl.opengl.GL33C.*;

public class Texture {

	private int textureID;
	int width;
	int height;
	int bitDepth;
	
	String fileLocation;
	
	public Texture() {
		this.width = 0;
		this.height = 0;
		this.fileLocation = "";
	}
	
	public Texture(String texturePath) {
		this.fileLocation = texturePath;
		this.textureID = glGenTextures();
		glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
	}
	
	public void useTexture() {
	    glActiveTexture(GL_TEXTURE0);
	    glBindTexture(GL_TEXTURE_2D, textureID);
	    
	        
	}
	
	public void loadTexture() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            
            InputStream in = getClass().getClassLoader().getResourceAsStream(fileLocation);
            if (in == null) throw new RuntimeException("Texture File Not Found: " + fileLocation);

            byte[] bytes = in.readAllBytes();
            ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
            buffer.put(bytes).flip();

            
            ByteBuffer texData = STBImage.stbi_load_from_memory(buffer, w, h, channels, 4);
            MemoryUtil.memFree(buffer);

            if (texData == null) throw new RuntimeException("Failed to load texture: " + fileLocation);

            width = w.get(0);
            height = h.get(0);
            bitDepth = channels.get(0);

            glBindTexture(GL_TEXTURE_2D, textureID);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0,
                    GL_RGBA, GL_UNSIGNED_BYTE, texData);

            glGenerateMipmap(GL_TEXTURE_2D);

            STBImage.stbi_image_free(texData);
            glBindTexture(GL_TEXTURE_2D, 0);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load texture: " + fileLocation, e);
        }
    }
	
	public void clearTexture() {
		glDeleteTextures(textureID);
	}
	
}
