import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import org.lwjgl.system.MemoryStack;
import org.joml.*;

import static org.lwjgl.opengl.GL33.*;

public class ShaderReader {
    // Reads GLSL shader files from the disk. The file type need not actually be .glsl for it to be read in.
    
    int shaderProgram;
    String vertexCode = "", fragmentCode = ""; // To prevent the strings from starting with "null", I need to initialise them as empty strings
    String vertexPath, fragmentPath;

    ShaderReader(String vertexPath, String fragmentPath) { // constructor
        try {
            // Saving the file path for later use in compilation check
            this.vertexPath = vertexPath; // Is this poor variable naming?
            this.fragmentPath = fragmentPath;

            FileReader vShaderFile = new FileReader(vertexPath);
            FileReader fShaderFile = new FileReader(fragmentPath);
       
            int vData = vShaderFile.read(); 
            int fData = fShaderFile.read();
            while (vData != -1) { 
                vertexCode += (char) vData; // Taking the data from the file and putting it into a string. Needs to be cast from bytes to characters.
                vData = vShaderFile.read();
            }
            vShaderFile.close(); // Remember to close the file -> I suppose you could alternatively use a try-with-resources block?
            while (fData != -1) {
                fragmentCode += (char) fData;
                fData = fShaderFile.read();
            }
            fShaderFile.close();
        }
        catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        
        // Creating shaders
        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexCode);
        glCompileShader(vertexShader);

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentCode);
        glCompileShader(fragmentShader);

        // Checking shaders have compiled properly
        // Vertex shader
        int success = glGetShaderi(vertexShader, GL_COMPILE_STATUS);
        if (success != 1) {
            int len = glGetShaderi(vertexShader, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: `"+vertexPath+"' Vertex shader compilation failed");
            System.out.println(glGetShaderInfoLog(vertexShader, len));
        }
        success = glGetShaderi(fragmentShader, GL_COMPILE_STATUS);
        // Fragment shader
        if (success != 1) {
            int len = glGetShaderi(fragmentShader, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: `"+fragmentPath+"' Fragment shader compilation failed");
            System.out.println(glGetShaderInfoLog(fragmentShader, len));
        }

        shaderProgram = glCreateProgram(); // Creating shader program
        glAttachShader(shaderProgram, vertexShader);
        glAttachShader(shaderProgram, fragmentShader);
        glLinkProgram(shaderProgram);

        success = glGetProgrami(shaderProgram, GL_LINK_STATUS); // Checking program linkage
        if (success != 1) {
            int len = glGetProgrami(shaderProgram, GL_INFO_LOG_LENGTH);
            System.out.println("ERROR: Shader program compilation failed!");
            System.out.println(glGetProgramInfoLog(shaderProgram, len));
        }

        glDeleteShader(vertexShader); // Deleting redundant shaders
        glDeleteShader(fragmentShader);

    }
    

    void use() {
        glUseProgram(shaderProgram);
    }

    void setMat4f(String uniform, Matrix4f matrix) { // Custom function makes setting view, model and projection matrices easier. 
        int uniformLoc = glGetUniformLocation(shaderProgram, uniform);
        try (MemoryStack stack = MemoryStack.stackPush()) { // Nice to have try-resources block hidden away here instead of bloating up other files. Still better would be C++ where such a thing is not needed.
            glUniformMatrix4fv(uniformLoc, false, matrix.get(stack.mallocFloat(16)));
        }

    }

    void print() {
        System.out.println(vertexCode);
        System.out.println(fragmentCode);
    }
        
}