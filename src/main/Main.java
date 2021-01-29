package main;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import engine.Window;
import shaders.ShaderProgram;
import utilities.TransformationMatrix;
import utilities.Utils;

public class Main {

	public static void main(String[] args) {
		Window window = new Window(1600, 900, "Deferred Shading");
		window.init();
		Camera camera = new Camera(new Vector3f(0, 0, 5));
		ShaderProgram geometryPassShader = new ShaderProgram("src/shaders/gbuffer_vertex.vs", "src/shaders/gbuffer_fragment.fs");
		ShaderProgram lightingPassShader = new ShaderProgram("src/shaders/deferred_vertex.vs", "src/shaders/deferred_fragment.fs");
		
		//****Deferred Shading****//
		int gBuffer = glGenFramebuffers();
		glBindFramebuffer(GL_FRAMEBUFFER, gBuffer);
		
		// position buffer
		int gPosition = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, gPosition);
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, window.getWidth(), window.getHeight(), 0, GL_RGBA, GL_FLOAT, (ByteBuffer)null);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, gPosition, 0);
	    // color buffer
	    int gAlbedo = glGenTextures();
	    glBindTexture(GL_TEXTURE_2D, gAlbedo);
	    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA16F, window.getWidth(), window.getHeight(), 0, GL_RGBA, GL_FLOAT, (ByteBuffer)null);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
	    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
	    glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT1, GL_TEXTURE_2D, gAlbedo, 0);
	    // tell OpenGL which color attachments we'll use (of this framebuffer) for rendering 
	    int attachments[] = { GL_COLOR_ATTACHMENT0, GL_COLOR_ATTACHMENT1 };
	    IntBuffer buffer = Utils.storeDataInIntBuffer(attachments);
	    glDrawBuffers(buffer);
	    
	    // create and attach depth buffer (renderbuffer)
	    int rboDepth = glGenRenderbuffers();
	    glBindRenderbuffer(GL_RENDERBUFFER, rboDepth);
	    glRenderbufferStorage(GL_RENDERBUFFER, GL_DEPTH_COMPONENT, window.getWidth(), window.getHeight());
	    glFramebufferRenderbuffer(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_RENDERBUFFER, rboDepth);
	    // finally check if framebuffer is complete
	    if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE)
	        System.out.println("Framebuffer not complete");
	    glBindFramebuffer(GL_FRAMEBUFFER, 0);
	    
	    lightingPassShader.use();
	    lightingPassShader.loadInt("gPosition", 0);
	    lightingPassShader.loadInt("gAlbedo", 1);
		//************************//
		int cube = createCube();
		int quad = createQuad();
		float rotY = 0.0f;
		Texture texture = null;
		try {
			texture = new Texture("src/res/bow_texture.png");
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (!window.windowShouldClose()) {
			glEnable(GL_DEPTH_TEST);
			glClearColor(1, 1, 0, 1);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
			
			// 1. Geometry Pass
			glBindFramebuffer(GL_FRAMEBUFFER, gBuffer);
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
			
			Matrix4f model = null;
			Matrix4f view = TransformationMatrix.createViewMatrix(camera);
			Matrix4f projection = TransformationMatrix.createProjectionMatrix(45.0f, window.getWidth()/window.getHeight(), 0.1f, 1500.0f);
			
			geometryPassShader.use();
			geometryPassShader.loadMatrix("view", view);
			geometryPassShader.loadMatrix("projection", projection);
			rotY += 1.0f;
			model = TransformationMatrix.createModelMatrix(new Vector3f(0, 0, 0), rotY, rotY, 0, 1.0f);
			geometryPassShader.loadMatrix("model", model);
			glBindVertexArray(cube);
			glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_INT, 0);
			glBindFramebuffer(GL_FRAMEBUFFER, 0);
			
			// 2. Lighting Pass
			glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
	        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	        lightingPassShader.use();
	        glActiveTexture(GL_TEXTURE0);
	        glBindTexture(GL_TEXTURE_2D, gPosition);
	        glActiveTexture(GL_TEXTURE1);
	        glBindTexture(GL_TEXTURE_2D, gAlbedo);
	        glBindVertexArray(quad);
			glDrawElements(GL_TRIANGLE_STRIP, 6, GL_UNSIGNED_INT, 0);
			
			glfwSwapBuffers(window.getWindowHandle()); // swap the color buffers
			glfwPollEvents();
		}
	}
	
	public static int createQuad() {
		float[] vertices = {
				-0.5f, 0.5f, 0.0f,
				0.5f, 0.5f, 0.0f,
				0.5f, -0.5f, 0.0f,
				-0.5f, -0.5f, 0.0f,
				
			};
		
		int[] indices = {
			0, 1, 2,
			0, 2, 3
		};
		
		float[] textCoords = {
				0.0f, 1.0f,
				1.0f, 1.0f,
				1.0f, 0.0f,
				0.0f, 0.0f
		};
		
		int vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		
		int vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		FloatBuffer verticesBuffer = Utils.storeDataInFloatBuffer(vertices);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		int textureVboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, textureVboID);
		FloatBuffer textCoordsBuffer = Utils.storeDataInFloatBuffer(textCoords);
		glBufferData(GL_ARRAY_BUFFER, textCoordsBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(1);
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		
		int eboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		IntBuffer indicesBuffer = Utils.storeDataInIntBuffer(indices);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
		
		glBindVertexArray(0);
		
		return vaoID;
	}
	
	public static int createCube() {
		float[] vertices = {
				-0.5f, 0.5f, 0.5f,
				0.5f, 0.5f, 0.5f,
				0.5f, -0.5f, 0.5f,
				-0.5f, -0.5f, 0.5f,
				-0.5f, 0.5f, -0.5f,
				0.5f, 0.5f, -0.5f,
				0.5f, -0.5f, -0.5f,
				-0.5f, -0.5f, -0.5f
				
			};
		
		int[] indices = {
			0, 1, 2,
			0, 2, 3,
			4, 5, 6,
			4, 6, 7,
			1, 5, 6,
			1, 6, 2,
			0, 4, 7,
			0, 7, 3,
			0, 4, 5,
			0, 5, 1,
			3, 7, 6,
			3, 6, 2
		};
		
		int vaoID = glGenVertexArrays();
		glBindVertexArray(vaoID);
		
		int vboID = glGenBuffers();
		glBindBuffer(GL_ARRAY_BUFFER, vboID);
		FloatBuffer verticesBuffer = Utils.storeDataInFloatBuffer(vertices);
		glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
		glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(0);
		glBindBuffer(GL_ARRAY_BUFFER, 0);

		int eboID = glGenBuffers();
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, eboID);
		IntBuffer indicesBuffer = Utils.storeDataInIntBuffer(indices);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);
		
		glBindVertexArray(0);
		
		return vaoID;
	}

}
