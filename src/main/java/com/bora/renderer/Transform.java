package com.bora.renderer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Transform {
    public Vector3f position = new Vector3f(0,0,0);
    public Vector3f rotation = new Vector3f(0,0,0);
    public Vector3f scale = new Vector3f(1,1,1);

    private Matrix4f modelMatrix = new Matrix4f();

    public Matrix4f getModelMatrix() {
    	// System.out.println("getModelMatrix position: " + position.x + ", " + position.y + ", " + position.z);
        modelMatrix.identity()
            .translate(position)
            .rotateX((float)Math.toRadians(rotation.x))
            .rotateY((float)Math.toRadians(rotation.y))
            .rotateZ((float)Math.toRadians(rotation.z))
            .scale(scale);
        return modelMatrix;
    }
}