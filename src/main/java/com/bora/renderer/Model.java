package com.bora.renderer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.*;
import java.util.*;

import static org.lwjgl.opengl.GL33C.*;

public class Model {

	private String fileLocation;
	private String modelDir;
	
	private List<Mesh> meshes = new ArrayList<>();
	private List<Texture> textures = new ArrayList<>();
	private Map<String,String> materialTextures = new HashMap<>();
	
	private Transform transform = new Transform();
	
	
	
	public Model(String filePath) {
		this.fileLocation = filePath;
		int lastSlash = filePath.lastIndexOf('/');
	    this.modelDir = (lastSlash >= 0) ? filePath.substring(0, lastSlash + 1) : "";
	    LoadModel();		
	}
	
	public void Draw(Shader shader) {
		glBindTexture(GL_TEXTURE_2D,0);
	    shader.setUniformMat4f(shader.getUniformModel(), transform.getModelMatrix());
	    for(int i = 0; i < meshes.size(); i++) {
	        if(i < textures.size() && textures.get(i) != null) {
	            textures.get(i).useTexture();
	        }
	        meshes.get(i).renderMesh();
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
	                Assimp.aiProcess_CalcTangentSpace |
	                Assimp.aiProcess_GenNormals,
	                "obj"
	        );

	        if (scene == null || scene.mRootNode() == null) {
	            throw new RuntimeException("Error Loading Model: " + Assimp.aiGetErrorString());
	        }
	        
	        String mtlPath = fileLocation.replace(".obj", ".mtl");
	        parseMTL(mtlPath);
	        processNode(scene.mRootNode(), scene);

	        MemoryUtil.memFree(buffer);

	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private void parseMTL(String mtlPath) {
		InputStream in = getClass().getClassLoader().getResourceAsStream(mtlPath);
		if (in == null) {
		    in = getClass().getResourceAsStream("/" + mtlPath);
		}
	    try {
	        String currentMaterial = null;
	        for (String line : new String(in.readAllBytes()).split("\n")) {
	            line = line.trim();
	            if (line.startsWith("newmtl ")) {
	                currentMaterial = line.substring(7).trim();
	            } else if (line.startsWith("map_Kd ") && currentMaterial != null) {
	                String texFile = line.substring(7).trim();
	                
	                texFile = texFile.replace("\\", "/");
	                if (texFile.contains("/")) {
	                    texFile = texFile.substring(texFile.lastIndexOf('/') + 1);
	                }
	                materialTextures.put(currentMaterial, texFile);
	            }
	        }
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
	    int numVertices = mesh.mNumVertices();

	    AIVector3D.Buffer positions = mesh.mVertices();
	    AIVector3D.Buffer normals   = mesh.mNormals();
	    AIVector3D.Buffer uvs       = mesh.mTextureCoords(0);
	    AIFace.Buffer     faces     = mesh.mFaces();

	    boolean hasUV   = uvs != null;
	    boolean hasNorm = normals != null;

	    
	    float[] vertexArray = new float[numVertices * 8];

	    for (int i = 0; i < numVertices; i++) {
	        AIVector3D pos = positions.get(i);
	        vertexArray[i*8]   = pos.x();
	        vertexArray[i*8+1] = pos.y();
	        vertexArray[i*8+2] = pos.z();

	        if (hasUV) {
	            AIVector3D uv = uvs.get(i);
	            vertexArray[i*8+3] = uv.x();
	            vertexArray[i*8+4] = uv.y();
	        } else {
	            vertexArray[i*8+3] = 0f;
	            vertexArray[i*8+4] = 0f;
	        }

	        if (hasNorm) {
	            AIVector3D n = normals.get(i);
	            vertexArray[i*8+5] = n.x();
	            vertexArray[i*8+6] = n.y();
	            vertexArray[i*8+7] = n.z();
	        } else {
	            vertexArray[i*8+5] = 0f;
	            vertexArray[i*8+6] = 1f;
	            vertexArray[i*8+7] = 0f;
	        }
	    }

	    int[] indices = new int[mesh.mNumFaces() * 3];
	    for (int i = 0; i < mesh.mNumFaces(); i++) {
	        AIFace face = faces.get(i);
	        IntBuffer ib = face.mIndices();
	        indices[i*3]   = ib.get(0);
	        indices[i*3+1] = ib.get(1);
	        indices[i*3+2] = ib.get(2);
	    }

	    Texture texture = loadMeshTexture(mesh, scene);
	    textures.add(texture);

	    Mesh newMesh = new Mesh();
	    newMesh.createMesh(vertexArray, indices);
	    return newMesh;
	}
	
	private Texture loadMeshTexture(AIMesh mesh, AIScene scene) {
	    
	    
	    int matIndex = mesh.mMaterialIndex();
	    if (matIndex < 0 || scene.mMaterials() == null) return null;

	    
	    AIMaterial material = AIMaterial.create(scene.mMaterials().get(matIndex));
	    AIString matName = AIString.calloc();
	    Assimp.aiGetMaterialString(material, Assimp.AI_MATKEY_NAME, 0, 0, matName);
	    String name = matName.dataString();
	    
	    String fileName = materialTextures.get(name);
	    if (fileName == null) {
	        System.out.println("Texture cannot be found: " + name);
	        return null;
	    }

	    
	    String modelName = fileLocation
	        .substring(fileLocation.lastIndexOf('/') + 1)
	        .replace(".obj", "");

	    String fullPath = "textures/" + modelName + "/" + fileName;
	    System.out.println("Texture downloading: " + fullPath);

	    Texture texture = new Texture(fullPath);
	    texture.loadTexture();
	    return texture;
	}
	
	public Transform getTransform() {
		return transform;
	}
}
