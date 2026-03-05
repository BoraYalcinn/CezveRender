package com.bora.renderer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.*;
import java.util.ArrayList;
import java.util.List;


public class Model {

	private String fileLocation;
	private List<Mesh> meshes = new ArrayList<>();
	
	public Model(String filePath) {
		this.fileLocation = filePath;
		LoadModel();
		
	}
	
	public void Draw() {
		for(Mesh mesh : meshes) {
			mesh.renderMesh();
		}
		
	}
	
	private void LoadModel() {
	    InputStream in = getClass().getClassLoader().getResourceAsStream(fileLocation);
	    if (in == null) throw new RuntimeException("Model File Not Found: " + fileLocation);

	    try {
	        byte[] bytes = in.readAllBytes();
	        ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
	        buffer.put(bytes).flip();

	        AIScene scene = Assimp.aiImportFileFromMemory(
	                buffer,
	                Assimp.aiProcess_Triangulate |
	                Assimp.aiProcess_FlipUVs |
	                Assimp.aiProcess_CalcTangentSpace,
	                fileLocation
	        );

	        if (scene == null || scene.mRootNode() == null) {
	            throw new RuntimeException("Error Loading Model: " + Assimp.aiGetErrorString());
	        }

	        processNode(scene.mRootNode(), scene);

	        MemoryUtil.memFree(buffer);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private void processNode(AINode node,AIScene scene) {
		int meshCount = node.mNumMeshes();
		IntBuffer meshIndices = node.mMeshes();
		for(int i = 0; i < meshCount; i++) {
			int meshIndex = meshIndices.get(i);
			AIMesh mesh = AIMesh.create(scene.mMeshes().get(meshIndex));
			meshes.add(processMesh(mesh, scene));
		}
		
		int childrenCount = node.mNumChildren();
		PointerBuffer children = node.mChildren();
		for(int i = 0; i < childrenCount;i++) {
			processNode(AINode.create(children.get(i)),scene);
		}
	}
	
	private Mesh processMesh(AIMesh mesh, AIScene scene) {

	    AIVector3D.Buffer vertices = mesh.mVertices();
	    AIFace.Buffer faces = mesh.mFaces();

	    float[] vertexArray = new float[mesh.mNumVertices() * 3];

	    for(int i = 0; i < mesh.mNumVertices(); i++) {

	        AIVector3D vertex = vertices.get(i);
	        float scale = 1.f;

	        vertexArray[i*3] = vertex.x() * scale;
	        vertexArray[i*3+1] = vertex.y() * scale;
	        vertexArray[i*3+2] = vertex.z() * scale; 
	    }

	    int[] indices = new int[mesh.mNumFaces() * 3];

	    for(int i = 0; i < mesh.mNumFaces(); i++) {

	        AIFace face = faces.get(i);
	        IntBuffer indexBuffer = face.mIndices();

	        indices[i*3] = indexBuffer.get(0);
	        indices[i*3+1] = indexBuffer.get(1);
	        indices[i*3+2] = indexBuffer.get(2);
	    }

	    Mesh newMesh = new Mesh();
	    newMesh.createMesh(vertexArray, indices);

	    return newMesh;
	}
	
}
