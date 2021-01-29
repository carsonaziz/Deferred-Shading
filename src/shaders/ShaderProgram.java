package shaders;

import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.*;

import java.nio.FloatBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;

import utilities.Utils;

public class ShaderProgram {
	private final int programID;
	private final int vertexShaderID;
	private final int fragmentShaderID;
	
	public ShaderProgram(String vertexShaderPath, String fragmentShaderPath) {
		vertexShaderID = createShader(vertexShaderPath, GL_VERTEX_SHADER);
		fragmentShaderID = createShader(fragmentShaderPath, GL_FRAGMENT_SHADER);
		programID = createProgram(vertexShaderID, fragmentShaderID);
	}
	
	public void use() {
		glUseProgram(programID);
	}
	
	private int createProgram(int vertexID, int fragmentID) {
		int id = glCreateProgram();
		
		glAttachShader(id, vertexID);
		glAttachShader(id, fragmentID);
		glLinkProgram(id);
		
		if(glGetProgrami(id, GL_LINK_STATUS) == GL_FALSE) {
			System.err.println(glGetProgramInfoLog(id, 512));
			System.exit(-1);
		}
		
		glDetachShader(id, vertexID);
		glDetachShader(id, fragmentID);
		glDeleteShader(vertexID);
		glDeleteShader(fragmentID);
		
		return id;
	}
	
	private int createShader(String fileName, int type) {
		StringBuilder source = null;
		try {
			source = Utils.loadResource(fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		int shaderID = glCreateShader(type);
		glShaderSource(shaderID, source);
		glCompileShader(shaderID);
		
		if(glGetShaderi(shaderID, GL_COMPILE_STATUS) == GL_FALSE) {
			System.err.println(glGetShaderInfoLog(shaderID, 512));
			System.exit(-1);
		}
		
		return shaderID;
	}
	
	public void loadMatrix(String name, Matrix4f matrix) {
		int location = glGetUniformLocation(programID, name);
		try(MemoryStack stack = MemoryStack.stackPush()) {
			FloatBuffer fb = stack.mallocFloat(16);
			matrix.get(fb);
			glUniformMatrix4fv(location, false, fb);
		}
	}
	
	public void loadMatrices(String name, Matrix4f[] matrices) {
		int location = glGetUniformLocation(programID, name);
		
		try(MemoryStack stack = MemoryStack.stackPush()) {
			int length = matrices != null ? matrices.length : 0;
			FloatBuffer fb = stack.mallocFloat(16 * length);
			
			for(int i = 0; i < length; i++) {
				matrices[i].get(16 * i, fb);
			}
			glUniformMatrix4fv(location, false, fb);
		}
	}
	
	public void loadVector3f(String name, Vector3f vec) {
		int location = glGetUniformLocation(programID, name);
		glUniform3f(location, vec.x, vec.y, vec.z);
	}
	
	public void loadFloat(String name, float value) {
		int location = glGetUniformLocation(programID, name);
		glUniform1f(location, value);
	}
	
	public void loadInt(String name, int value) {
		int location = glGetUniformLocation(programID, name);
		glUniform1i(location, value);
	}
	
	public void loadBoolean(String name, boolean value) {
		int location = glGetUniformLocation(programID, name);
		if(value) {
			glUniform1i(location, 1);
		} else {
			glUniform1i(location, 0);
		}
	}
}
