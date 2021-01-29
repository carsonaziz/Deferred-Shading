package main;

import org.joml.Vector3f;

public class Camera {
	private Vector3f position;
	
	public Camera() {
		this.position = new Vector3f(0, 0, 0);
	}
	
	public Camera(Vector3f position) {
		this.position = position;
	}
	
	public Vector3f getPosition() {
		return position;
	}
	
	public void setPosition(float x, float y, float z) {
		this.position = new Vector3f(x, y, z);
	}
	
	public void changePosition(float x, float y, float z) {
		position.x += x;
		position.y += y;
		position.z += z;
	}
}
