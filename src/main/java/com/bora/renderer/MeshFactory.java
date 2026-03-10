package com.bora.renderer;

public class MeshFactory {

	public static Mesh createFloor(float size, float y) {
		
		float[] vertices = {
				-size, y, -size,  0f,    0f,    0f,1f,0f,
	             size, y, -size,  size,  0f,    0f,1f,0f,
	             size, y,  size,  size,  size,  0f,1f,0f,
	            -size, y,  size,  0f,    size,  0f,1f,0f
		};
		
		int[] indices = {
				0,1,2,
				0,2,3
		};
		
		Mesh m =  new Mesh();
		m.createMesh(vertices, indices);
		return m;
	}
	
	public static Mesh createTriangle(float size,float height) {
		
		float h = height/2f;
		
		
		float[] vertices = {
				-h, 0f, -h,   0f,  0f,  0f,1f,0f,
		         h, 0f, -h,   1f,  0f,  0f,1f,0f,
		         h, 0f,  h,   1f,  1f,  0f,1f,0f,
		        -h, 0f,  h,   0f,  1f,  0f,1f,0f,
		        0f, height, 0f,  0.5f, 0.5f,  0f,1f,0f
		};
		
		int[] indices = {
				0,3,1,
				1,3,2,
				2,3,0,
				0,2,1
		};
		
		Mesh t = new Mesh();
		t.createMesh(vertices,indices);
		return t;
	}
	
	public static Mesh createCube(float size) {
		float h = size/2f;
		float[] vertices = {
				 // front
	            -h,-h, h,  0f,0f,  0f,0f,1f,
	             h,-h, h,  1f,0f,  0f,0f,1f,
	             h, h, h,  1f,1f,  0f,0f,1f,
	            -h, h, h,  0f,1f,  0f,0f,1f,
	            // back
	            -h,-h,-h,  1f,0f,  0f,0f,-1f,
	            -h, h,-h,  1f,1f,  0f,0f,-1f,
	             h, h,-h,  0f,1f,  0f,0f,-1f,
	             h,-h,-h,  0f,0f,  0f,0f,-1f,
	            // top
	            -h, h,-h,  0f,1f,  0f,1f,0f,
	            -h, h, h,  0f,0f,  0f,1f,0f,
	             h, h, h,  1f,0f,  0f,1f,0f,
	             h, h,-h,  1f,1f,  0f,1f,0f,
	            // bottom
	            -h,-h,-h,  0f,0f,  0f,-1f,0f,
	             h,-h,-h,  1f,0f,  0f,-1f,0f,
	             h,-h, h,  1f,1f,  0f,-1f,0f,
	            -h,-h, h,  0f,1f,  0f,-1f,0f,
	            // right
	             h,-h,-h,  1f,0f,  1f,0f,0f,
	             h, h,-h,  1f,1f,  1f,0f,0f,
	             h, h, h,  0f,1f,  1f,0f,0f,
	             h,-h, h,  0f,0f,  1f,0f,0f,
	            // left
	            -h,-h,-h,  0f,0f, -1f,0f,0f,
	            -h,-h, h,  1f,0f, -1f,0f,0f,
	            -h, h, h,  1f,1f, -1f,0f,0f,
	            -h, h,-h,  0f,1f, -1f,0f,0f
		};
		
		int[] indices = {
				 0, 1, 2,  0, 2, 3,
	             4, 5, 6,  4, 6, 7,
	             8, 9,10,  8,10,11,
	            12,13,14, 12,14,15,
	            16,17,18, 16,18,19,
	            20,21,22, 20,22,23
		};
		
		Mesh c = new Mesh();
		c.createMesh(vertices, indices);
		return c;
	}
	
	
}
