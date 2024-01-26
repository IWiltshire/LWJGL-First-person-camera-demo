import java.lang.Math;

import org.joml.*;
import static org.lwjgl.glfw.GLFW.*;

enum Camera_Movement {
    FORWARD,
    BACKWARD,
    LEFT,
    RIGHT;
}

// Since this class is for a camera, it only affects the the view matrix
public class Camera { 
    // Default camera values
    float SPEED = 2.5f;
    float SENSITIVITY = 0.1f;
    float ZOOM = 45.0f;

    // Camera attributes
    Vector3f Position = new Vector3f();
    Vector3f Front = new Vector3f();
    Vector3f Right = new Vector3f();
    Vector3f WorldUp = new Vector3f(0,1,0);

    // Camera options
    float MovementSpeed;
    float MouseSensitivity;
    float Zoom;

    // Camera deals in quaternions rather than Euler angles -> prevents issues with veering at extreme angles
    Quaternionf Orientation = new Quaternionf(0.f,0.0f,0.0f,-1.0f);
    float RightAngle = 0.0f; // Yaw - right is +ve. Calculations work out so that this is in degrees.
    float UpAngle = 0.0f; // Pitch - up is +ve


    // Constructor
    // Java does not support default parameters for constructors, so I will, in this case, rely on overloaded methods
    Camera() { // Default constructor
        this.Position = new Vector3f(0.0f, 0.0f, 3.0f);
        this.Front = new Vector3f(0.0f, 0.0f, -1.0f);
        this.Orientation = new Quaternionf(Front.x, Front.y, Front.z, 0.0f);
        this.RightAngle = 0.0f;
        this.UpAngle = 0.0f;
        this.MovementSpeed = SPEED;
        this.MouseSensitivity = SENSITIVITY;
        this.Zoom = ZOOM;
        updateCameraVectors();
    }

    Camera(Vector3f position) {// Constructor taking just position
        this.Position = position;
        this.Front = new Vector3f(0.0f, 0.0f, -1.0f);
        this.Orientation = new Quaternionf(Front.x, Front.y, Front.z, 0.0f);
        this.RightAngle = 0.0f;
        this.UpAngle = 0.0f;
        this.MovementSpeed = SPEED;
        this.MouseSensitivity = SENSITIVITY;
        this.Zoom = ZOOM;
        updateCameraVectors();
    }

    Camera(Vector3f position, Vector3f front, float RightAngle, float UpAngle, float MovementSpeed, float MouseSensitivity, float Zoom) { // Constructor needing all values
        this.Position = position;
        this.Front = front;
        this.Orientation = new Quaternionf(Front.x, Front.y, Front.z, 0.0f);
        this.RightAngle = 0.0f;
        this.UpAngle = 0.0f;
        this.MovementSpeed = MovementSpeed;
        this.MouseSensitivity = MouseSensitivity;
        this.Zoom = Zoom;    
        updateCameraVectors();
    }


    Matrix4f getViewMatrix() {
        Quaternionf revOrientation = new Quaternionf();
        Orientation.conjugate(revOrientation); // Conjugating because world moves to simulate camera movement, e.g. world moves -z to simulate camera moving +z.

        Matrix4f Rotation = new Matrix4f();
        Rotation.set(revOrientation); // Rotation matrix is derived automatically from conjugate of orientation quaternion

        Matrix4f Translation = new Matrix4f();
        Vector3f temp = new Vector3f();
        Translation.translate(Position.mul(-1.0f, temp));

        Matrix4f View = new Matrix4f();
        Rotation.mul(Translation, View);
        return View;
    }

    void ProcessKeyboard(Camera_Movement direction, float deltaTime) {
        float velocity = MovementSpeed * deltaTime;

        Quaternionf origFacing = new Quaternionf(0f, 0f, -1f, 0f); // We are originally facing in the (0,0,-1) direction. We want to rotate this vector into the direction we are looking at. 
        Quaternionf tempQuat = new Quaternionf();
        Quaternionf qF = new Quaternionf(); // This will store actual facing direction
        Orientation.mul(origFacing.mul(Orientation.conjugate(tempQuat)), qF); // Storing result in qF

        Front = new Vector3f(qF.x, qF.y, qF.z).normalize();
        Vector3f temp = new Vector3f();
        Front.cross(WorldUp, temp).normalize(Right);

        if (direction == Camera_Movement.FORWARD) {
            Position.add(Front.mul(velocity, temp)); // Can reuse temp here because it is constantly being overwritten.
        }
        if (direction == Camera_Movement.BACKWARD) {
            Position.sub(Front.mul(velocity, temp));
        }
        if (direction == Camera_Movement.LEFT) {
            Position.sub(Right.mul(velocity, temp));
        }
        if (direction == Camera_Movement.RIGHT) {
            Position.add(Right.mul(velocity, temp));
        }
    }

    void ProcessMouseMovement(float xOffset, float yOffset, boolean constrainPitch) {
        xOffset *= MouseSensitivity;
        yOffset *= MouseSensitivity;
        
        RightAngle += xOffset;
        UpAngle += yOffset;

        if (constrainPitch) { // Can constrain yaw in a similar manner
            if (UpAngle > 89.0f) { 
                UpAngle = 89.0f;
            }
            if (UpAngle < -89.0f) { // If constrainPitch is true and this is set to -90, there seems to be a glitch where pointing straight down and moving forward-left/right will transport you(?)/mess up somehow. 
                UpAngle = -89.0f;
            }
        }
        updateCameraVectors();
    }

    void ProcessMouseScroll(float yOffset) {
        Zoom -= yOffset; // Scrolling up lowers the yOffset
        if (Zoom < 1.0f) Zoom = 1.0f; // Zooming in
        if (Zoom > 45.0f) Zoom = 45.0f; // Zooming out
    }

    void updateCameraVectors() {
        
        Quaternionf aroundY = new Quaternionf(); // Yaw
        AxisAngle4f y = new AxisAngle4f((float)Math.toRadians(-RightAngle), new Vector3f(0.0f, 1.0f, 0.0f));
        aroundY.set(y);

        Quaternionf aroundX = new Quaternionf(); // Pitch
        AxisAngle4f x = new AxisAngle4f((float)Math.toRadians(UpAngle), new Vector3f(1.0f, 0.0f, 0.0f));
        aroundX.set(x);

        aroundY.mul(aroundX, Orientation);
    }

    void processInput(Window screen) {
    if (glfwGetKey(Window.window, GLFW_KEY_W) == GLFW_PRESS) {
       Window.camera.ProcessKeyboard(Camera_Movement.FORWARD, Window.deltaTime);
       //System.out.println("The W key is being pressed!");
    }
    if (glfwGetKey(Window.window, GLFW_KEY_S) == GLFW_PRESS) {
       Window.camera.ProcessKeyboard(Camera_Movement.BACKWARD, Window.deltaTime);
    }
    if (glfwGetKey(Window.window, GLFW_KEY_A) == GLFW_PRESS) {
       Window.camera.ProcessKeyboard(Camera_Movement.LEFT, Window.deltaTime);
    }
    if (glfwGetKey(Window.window, GLFW_KEY_D) == GLFW_PRESS) {
       Window.camera.ProcessKeyboard(Camera_Movement.RIGHT, Window.deltaTime);
    }

    if (glfwGetKey(Window.window, GLFW_KEY_ESCAPE) == GLFW_PRESS) { // Quit game
       glfwSetWindowShouldClose(Window.window, true);
    }
}

}
