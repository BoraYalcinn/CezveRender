package com.bora.renderer;

import static org.lwjgl.opengl.GL33C.*;

public class ShadowMap {

	
	private int shadowWidth,shadowHeight,depthMapFBO,depthMap;
	
	public ShadowMap() {
		this.shadowHeight = 0;
		this.shadowWidth = 0;
	}
	
	public ShadowMap(int width , int height) {
		this.shadowHeight = height;
		this.shadowWidth = width;
	}
	
	public void InitiliazeShadows() {
		this.depthMap = glGenTextures();
		glBindTexture(GL_TEXTURE_2D,depthMap);
		
		
		
		glTexImage2D(GL_TEXTURE_2D, 0, GL_DEPTH_COMPONENT, shadowWidth, shadowHeight, 0, GL_DEPTH_COMPONENT, GL_FLOAT, 0L);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_BORDER);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_BORDER);
		
		this.depthMapFBO = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER,depthMapFBO);
		glFramebufferTexture2D(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,GL_TEXTURE_2D,depthMap,0);
		glDrawBuffer(GL_NONE);
		glReadBuffer(GL_NONE);
		glBindFramebuffer(GL_FRAMEBUFFER,0);
		
		
	}
	
	
	
	public int getDepthMap() {
		return depthMap;
	}
	
	public int getDepthMapFBO() {
		return depthMapFBO;
	}
	
}
