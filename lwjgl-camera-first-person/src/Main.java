import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWFramebufferSizeCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWScrollCallback;

import java.nio.*;
import org.lwjgl.BufferUtils;
import java.lang.Math;
import org.joml.*; // Use for vectors and matrices

import static org.lwjgl.BufferUtils.*; // Direct buffers only!
import static org.lwjgl.opengl.GL.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL33.*; // This corresponds to OpenGL ver 3.3
import static org.lwjgl.system.MemoryUtil.NULL; // Needed for GLFW functions
import static org.lwjgl.stb.STBImage.stbi_image_free;

// note: When using GLFW functions, they need to be within a class' method (such as main()), or Java thinks I'm defining a method.

public class Main {
    public static void main(String[] args) {
        Window screen = new Window();
        screen.run();    
    }
}

// Seemed redundant to give Window class its own file in this project
class Window {

    public static Window screen;
    static long window;

    static int SCR_WIDTH = 1280, SCR_HEIGHT = 720; // Width and height in px
    String title = "Miss Bernard can't stop spinning!";
    
    // Camera
    public static Camera camera = new Camera();
    static float lastX = SCR_WIDTH/2.0f; // Width and height need to have been instantiated prior for this to make sense.
    static float lastY = SCR_HEIGHT/2.0f;
    static boolean firstMouse = true;

    // Timing
    static float deltaTime = 0.0f, lastTime = 0.0f;

    Cube cubes = new Cube();

    void init() {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE); // Forward compatibility

    
        window = glfwCreateWindow(Window.SCR_WIDTH, Window.SCR_HEIGHT, title, NULL, NULL); 
        if (window == NULL) {
        throw new IllegalStateException("Failed to create GLFW window");
        }
         glfwMakeContextCurrent(window);

         GL.createCapabilities(); // Necessary for bindings to work with LWJGL. Has to be placed after context has been made.
       
        
        // Framebuffer size callback
        Resize framebuffer_size_callback = new Resize();
        glfwSetFramebufferSizeCallback(window, framebuffer_size_callback);

        Mouse mousePosCallback = new Mouse();
        glfwSetCursorPosCallback(window, mousePosCallback);
        glfwSetCursorPos(window, Window.lastX, Window.lastY); // Defaulting mouse position to centre of screen
        Scroll mouseScrollCallback = new Scroll();
        glfwSetScrollCallback(window, mouseScrollCallback);

        glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED); // Capturing mouse
        
        glEnable(GL_DEPTH_TEST); // Enabling z-buffer
        //glfwSwapInterval(1); // V-sync

        cubes.init();
    }

    void loop() {
        while (!glfwWindowShouldClose(window)) {
            float currentFrame = (float) glfwGetTime();
            deltaTime = currentFrame - lastTime;
            lastTime = currentFrame; // Should this be here and not the bottom of the loop?

            // Will setup checking for inputs later
            camera.processInput(screen);
    
            // Render loop: All of the rendering should be done in this block
            glClearColor(0.5f, 0.2f, 0.6f, 1.0f); // Need to use floats; cannot use doubles
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // Vertical bar is bitwise OR
            
            cubes.draw();
                       
            // These two bits come last
            glfwSwapBuffers(window);
            glfwPollEvents();
           }          
    }
    
    void run() {
        init(); // Is it preferable to make these methods static and then call them as Window.init() and Window.loop()?
        loop();
        glfwTerminate();
    }

}

class Mouse extends GLFWCursorPosCallback {
    public void invoke(long window, double xPosIn, double yPosIn) {
        float xPos = (float) xPosIn;
        float yPos = (float) yPosIn;
        
        if (Window.firstMouse) {
            Window.lastX = xPos;
            Window.lastY = yPos;
            Window.firstMouse = false;
        }

        float xOffset = xPos - Window.lastX;
        float yOffset = Window.lastY - yPos; // Reverse order here because screen coordinate starts with top left as (0,0) and bottom left as (0,1)

        Window.lastX = xPos;
        Window.lastY = yPos;

        Window.camera.ProcessMouseMovement(xOffset, yOffset, true);
   }
}

class Scroll extends GLFWScrollCallback {
    public void invoke(long window, double xOffset, double yOffset) {
        Window.camera.ProcessMouseScroll((float) yOffset);
    }
}

class Resize extends GLFWFramebufferSizeCallback { // Callback method has to be defined in its own class so that it can be invoked properly
    public void invoke(long window, int width, int height) {
        glViewport(0,0,width,height);
    }
}