package com.bora.renderer;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
	
	private Transform transform = new Transform();
	private Matrix4f projection = new Matrix4f();
	
	private Vector3f front = new Vector3f(0,0,-1);
	private Vector3f right = new Vector3f();
	private Vector3f worldUp = new Vector3f(0,1,0);
	
	float yaw = (float) Math.toRadians(transform.rotation.y);
    float pitch = (float) Math.toRadians(transform.rotation.x);
	
	public Camera(float fov,float aspect,float near,float far) {
		
		projection = new Matrix4f().perspective((float)Math.toRadians(fov), aspect, near, far);
		
	}
	
	public Matrix4f getViewMatrix() {
	    
	    

	    Vector3f direction = new Vector3f();
	    direction.x = (float) (Math.cos(pitch) * Math.sin(yaw));
	    direction.y = (float) Math.sin(pitch); 
	    direction.z = (float) (-Math.cos(pitch) * Math.cos(yaw));

	    Vector3f center = new Vector3f(transform.position).add(direction);

	    return new Matrix4f().lookAt(transform.position, center, new Vector3f(0, 1, 0));
	}
	
	public void update() {
	    yaw = (float)Math.toRadians(transform.rotation.y);
	    pitch = (float)Math.toRadians(transform.rotation.x);

	    front.x = (float)(Math.cos(pitch) * Math.sin(yaw));
	    front.y = (float)(Math.sin(pitch));
	    front.z = (float)(-Math.cos(pitch) * Math.cos(yaw));
	    front.normalize();

	    right = new Vector3f(front).cross(worldUp).normalize();
	}
	
	public void handleMovement(float velocity, Camera camera,Input input,float sensitivity) {
		camera.getTransform().rotation.y += input.getXChange() * sensitivity;
		camera.getTransform().rotation.x += input.getYChange() * sensitivity;

		camera.update();

		if (input.isKeyDown(GLFW_KEY_W)) camera.getTransform().position.add(new Vector3f(camera.getFront()).mul(velocity));
		if (input.isKeyDown(GLFW_KEY_S)) camera.getTransform().position.sub(new Vector3f(camera.getFront()).mul(velocity));
		if (input.isKeyDown(GLFW_KEY_A)) camera.getTransform().position.sub(new Vector3f(camera.getRight()).mul(velocity));
		if (input.isKeyDown(GLFW_KEY_D)) camera.getTransform().position.add(new Vector3f(camera.getRight()).mul(velocity));
	}
	
	public Matrix4f getProjectionMatrix() {
        return projection;
    }

    public Transform getTransform() {
        return transform;
    }
	
	public Vector3f getFront() {
		return front;
	}
	
	public Vector3f getRight() {
		return right;
	}
}
