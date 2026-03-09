package com.bora.renderer;

import static org.lwjgl.opengl.GL33C.*;


public class CubeShadowMap {
	
	private int shadowWidth, shadowHeight,depthMapFBO,depthMap;
	
	public CubeShadowMap() {
		this.shadowWidth = 0;
		this.shadowHeight = 0;
		
	}

	public CubeShadowMap(int width,int height) {
		this.shadowWidth = width;
		this.shadowHeight = height;
	}
	
	public void InitializeShadows() {
		depthMap = glGenTextures();
		glBindTexture(GL_TEXTURE_CUBE_MAP,depthMap);
		
		for(int i = 0; i<6;i++) {
			glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i ,0,GL_DEPTH_COMPONENT,shadowWidth,shadowHeight,0,GL_DEPTH_COMPONENT,GL_FLOAT,0L);
		}
		glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_CUBE_MAP,GL_TEXTURE_MAG_FILTER,GL_NEAREST);
		glTexParameteri(GL_TEXTURE_CUBE_MAP,GL_TEXTURE_WRAP_S,GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP,GL_TEXTURE_WRAP_T,GL_CLAMP_TO_EDGE);
		glTexParameteri(GL_TEXTURE_CUBE_MAP,GL_TEXTURE_WRAP_R,GL_CLAMP_TO_EDGE);
		
		depthMapFBO = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER,depthMapFBO);
		glFramebufferTexture(GL_FRAMEBUFFER,GL_DEPTH_ATTACHMENT,depthMap,0);
		
		glDrawBuffer(GL_NONE);
		glReadBuffer(GL_NONE);
		
		glBindFramebuffer(GL_FRAMEBUFFER,0);
	}
	
	public int getDepthMap() { return depthMap; }
	public int getDepthMapFBO() { return depthMapFBO; }
	
}
