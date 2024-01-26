import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import java.nio.*;
import org.lwjgl.BufferUtils;
import java.lang.Math;
import org.joml.*;

import static org.lwjgl.BufferUtils.*; // Direct buffers only!
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*; // This corresponds to OpenGL ver 3.3
import static org.lwjgl.system.MemoryUtil.NULL; // Needed for GLFW functions
import static org.lwjgl.stb.STBImage.stbi_image_free;

public class Cube {

    int EBO, VAO, VBO, texture;
    ShaderReader rectShader;
    TextureReader image;
    Matrix4f model = new Matrix4f(); // Instantiating matrices as identity
    Matrix4f view = new Matrix4f();
    Matrix4f projection = new Matrix4f(); 
    Vector3f[] cubePositions = new Vector3f[10];

    void init() {

        rectShader = new ShaderReader("src/rectVertex.glsl", "src/rectFragment.glsl"); // Loading shaders
        image = new TextureReader("src/images/appleseed mad.png"); // Loading image for texture. Some jpgs and pngs seem troublesome and will cause program to automatically close.

        float[] vertices = { // Could save on performance by using EBOs, but I did not do that for this demo.
            // positions            texture coord.
             -0.5f, -0.5f, -0.5f,   0.0f, 0.0f,
             0.5f, -0.5f, -0.5f,    1.0f, 0.0f,
             0.5f,  0.5f, -0.5f,    1.0f, 1.0f,
             0.5f,  0.5f, -0.5f,    1.0f, 1.0f,
            -0.5f,  0.5f, -0.5f,    0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,    0.0f, 0.0f,

            -0.5f, -0.5f,  0.5f,    0.0f, 0.0f,
             0.5f, -0.5f,  0.5f,    1.0f, 0.0f,
             0.5f,  0.5f,  0.5f,    1.0f, 1.0f,
             0.5f,  0.5f,  0.5f,    1.0f, 1.0f,
            -0.5f,  0.5f,  0.5f,    0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,    0.0f, 0.0f,

            -0.5f,  0.5f,  0.5f,    1.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,    1.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,    0.0f, 1.0f,
            -0.5f, -0.5f, -0.5f,    0.0f, 1.0f,
            -0.5f, -0.5f,  0.5f,    0.0f, 0.0f,
            -0.5f,  0.5f,  0.5f,    1.0f, 0.0f,

             0.5f,  0.5f,  0.5f,    1.0f, 0.0f,
             0.5f,  0.5f, -0.5f,    1.0f, 1.0f,
             0.5f, -0.5f, -0.5f,    0.0f, 1.0f,
             0.5f, -0.5f, -0.5f,    0.0f, 1.0f,
             0.5f, -0.5f,  0.5f,    0.0f, 0.0f,
             0.5f,  0.5f,  0.5f,    1.0f, 0.0f,

            -0.5f, -0.5f, -0.5f,    0.0f, 1.0f,
             0.5f, -0.5f, -0.5f,    1.0f, 1.0f,
             0.5f, -0.5f,  0.5f,    1.0f, 0.0f,
             0.5f, -0.5f,  0.5f,    1.0f, 0.0f,
            -0.5f, -0.5f,  0.5f,    0.0f, 0.0f,
            -0.5f, -0.5f, -0.5f,    0.0f, 1.0f,

            -0.5f,  0.5f, -0.5f,    0.0f, 1.0f,
             0.5f,  0.5f, -0.5f,    1.0f, 1.0f,
             0.5f,  0.5f,  0.5f,    1.0f, 0.0f,
             0.5f,  0.5f,  0.5f,    1.0f, 0.0f,
            -0.5f,  0.5f,  0.5f,    0.0f, 0.0f,
            -0.5f,  0.5f, -0.5f,    0.0f, 1.0f
        };

        // Extra cubes world position
        cubePositions[0] = new Vector3f( 0.0f,  0.0f,  0.0f);
        cubePositions[1] = new Vector3f( 2.0f,  5.0f, -15.0f); 
        cubePositions[2] = new Vector3f(-1.5f, -2.2f, -2.5f);  
        cubePositions[3] = new Vector3f(-3.8f, -2.0f, -12.3f);  
        cubePositions[4] = new Vector3f( 2.4f, -0.4f, -3.5f);  
        cubePositions[5] = new Vector3f(-1.7f,  3.0f, -7.5f);  
        cubePositions[6] = new Vector3f( 1.3f, -2.0f, -2.5f);  
        cubePositions[7] = new Vector3f( 1.5f,  2.0f, -2.5f); 
        cubePositions[8] = new Vector3f( 1.5f,  0.2f, -1.5f); 
        cubePositions[9] = new Vector3f(-1.3f,  1.0f, -1.5f);  
        //

        FloatBuffer verticesBuffer = createFloatBuffer(vertices.length);
        verticesBuffer.put(vertices).flip();
        
        VAO = glGenVertexArrays(); 
        glBindVertexArray(VAO);

        VBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, VBO);
        glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW);
        
        texture = glGenTextures();        
        glBindTexture(GL_TEXTURE_2D, texture);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE); // Texture coord. "s" is the horizontal direction
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE); // Texture coord. "t" is the vertical direction
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST); // Texture upscales with nearest neighbour
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR); // Texture downscales with nearest neighbour

        if (image.data != null) { 
            glTexImage2D(GL_TEXTURE_2D, 0, image.channels, image.width, image.height, 0, image.channels, GL_UNSIGNED_BYTE, image.data);
            // Use image.channels to automatically fill in whether texture is RGB or RGBA
            // 6th argument should be left as 0 (legacy)
            glGenerateMipmap(GL_TEXTURE_2D);
            stbi_image_free(image.data); // Freeing up image memory
        }
        else {
            System.out.println("Substituting texture with black box");
            glBindTexture(GL_TEXTURE_2D, 0);
        }

        glVertexAttribPointer(0, 3, GL_FLOAT, false, 5*Float.BYTES, 0); // Vertex positions
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 5*Float.BYTES, 12); // Texture coordinates
        glEnableVertexAttribArray(1);

        glBindVertexArray(0);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0); // Have to unbind Element Array Buffer after Vertex Array Buffer, or it will crash. Why?
        glBindBuffer(GL_ARRAY_BUFFER, 0);

        //glPolygonMode(GL_FRONT_AND_BACK, GL_LINE); // Curiously, the cubes are still textured even when shown as wireframes.
    }
    
    Vector3f rotationAxis = new Vector3f(0.5f, 1.0f, 0.0f).normalize(); // Normalise vector to a unit vector. This prevents strange shearing and distortion when used to rotate matrices.

    void transformPerspective() {
        rectShader.setMat4f("model", model); // Can use my own function instead of using the try-with-resources block; makes code neater
        rectShader.setMat4f("projection", projection); // In our case, the projection matrix does not actually change each frame, and could really be placed outside the render loop.
        rectShader.setMat4f("view", view);     
    }

    void draw() {
        rectShader.use(); // Using shader

        glActiveTexture(GL_TEXTURE0); // Some drivers (not mine) require us to activate texture units before binding textures. We activate it inside the render loop.
        glBindTexture(GL_TEXTURE_2D, texture); // Binding texture
        
        transformPerspective();

        // projection matrix
        projection = new Matrix4f();
        projection.perspective((float) Math.toRadians(Window.camera.Zoom), (float)Window.SCR_WIDTH/(float)Window.SCR_HEIGHT, 0.1f, 100.0f);
        rectShader.setMat4f("projection", projection);

        // camera/view matrix
        view = Window.camera.getViewMatrix();
        rectShader.setMat4f("view", view);

        glBindVertexArray(VAO);

        // Rendering boxes
        for (int i=0; i<10; i++) {
            model = new Matrix4f();
            model.translate(cubePositions[i]);
            float angle = 20f * i;
            model.rotate((float) glfwGetTime() * (float)Math.toRadians(angle), rotationAxis);
            rectShader.setMat4f("model", model);

            glDrawArrays(GL_TRIANGLES, 0, 36);
        }
    }


}
