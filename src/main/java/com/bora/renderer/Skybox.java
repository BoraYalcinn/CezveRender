package com.bora.renderer;

import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.InputStream;
import java.nio.*;

import static org.lwjgl.opengl.GL33C.*;

public class Skybox {

    private int cubemapTexture;
    private int VAO, VBO;

    // SKYBOX VERTICES
    private static final float[] vertices = {
        -1, -1,  1,   1, -1,  1,   1,  1,  1,   1,  1,  1,  -1,  1,  1,  -1, -1,  1,
        -1, -1, -1,  -1,  1, -1,   1,  1, -1,   1,  1, -1,   1, -1, -1,  -1, -1, -1,
        -1,  1, -1,  -1,  1,  1,   1,  1,  1,   1,  1,  1,   1,  1, -1,  -1,  1, -1,
        -1, -1, -1,   1, -1, -1,   1, -1,  1,   1, -1,  1,  -1, -1,  1,  -1, -1, -1,
         1, -1, -1,   1,  1, -1,   1,  1,  1,   1,  1,  1,   1, -1,  1,   1, -1, -1,
        -1, -1, -1,  -1, -1,  1,  -1,  1,  1,  -1,  1,  1,  -1,  1, -1,  -1, -1, -1
    };

    public Skybox(String folderPath) {
        String[] faces = {
            folderPath + "/right.jpg",
            folderPath + "/left.jpg",
            folderPath + "/top.jpg",
            folderPath + "/bottom.jpg",
            folderPath + "/front.jpg",
            folderPath + "/back.jpg"
        };
        loadCubemap(faces);
        setupVAO();
    }

    private void loadCubemap(String[] faces) {
        cubemapTexture = glGenTextures();
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapTexture);

        for (int i = 0; i < faces.length; i++) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                IntBuffer w = stack.mallocInt(1);
                IntBuffer h = stack.mallocInt(1);
                IntBuffer channels = stack.mallocInt(1);

                InputStream in = getClass().getClassLoader().getResourceAsStream(faces[i]);
                if (in == null) throw new RuntimeException("Skybox texture not found: " + faces[i]);

                byte[] bytes = in.readAllBytes();
                ByteBuffer buffer = MemoryUtil.memAlloc(bytes.length);
                buffer.put(bytes).flip();

                ByteBuffer texData = STBImage.stbi_load_from_memory(buffer, w, h, channels, 3);
                MemoryUtil.memFree(buffer);
                if (texData == null) throw new RuntimeException("Failed to load skybox face: " + faces[i]);

                glTexImage2D(GL_TEXTURE_CUBE_MAP_POSITIVE_X + i, 0, GL_RGB,
                        w.get(0), h.get(0), 0, GL_RGB, GL_UNSIGNED_BYTE, texData);

                STBImage.stbi_image_free(texData);
            } catch (Exception e) {
                throw new RuntimeException("Skybox load error: " + faces[i], e);
            }
        }

        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_CUBE_MAP, GL_TEXTURE_WRAP_R, GL_CLAMP_TO_EDGE);

        glBindTexture(GL_TEXTURE_CUBE_MAP, 0);
    }

    private void setupVAO() {
        VAO = glGenVertexArrays();
        VBO = glGenBuffers();

        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 3 * Float.BYTES, 0L);
        glEnableVertexAttribArray(0);

        glBindVertexArray(0);
    }

    public void render(Shader shader, Camera camera) {
        glDepthFunc(GL_LEQUAL);

        shader.useShader();

        org.joml.Matrix4f view = new org.joml.Matrix4f(camera.getViewMatrix());
        view.m30(0); view.m31(0); view.m32(0);

        shader.setUniformMat4f(shader.getUniformView(), view);
        shader.setUniformMat4f(shader.getUniformProjection(), camera.getProjectionMatrix());

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_CUBE_MAP, cubemapTexture);
        glUniform1i(glGetUniformLocation(shader.getProgramID(), "skybox"), 0);

        glBindVertexArray(VAO);
        glDrawArrays(GL_TRIANGLES, 0, 36);
        glBindVertexArray(0);

        glDepthFunc(GL_LESS);
    }
}