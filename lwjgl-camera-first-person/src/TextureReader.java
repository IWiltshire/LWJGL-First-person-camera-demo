import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL33.*; // For RGB
import static org.lwjgl.stb.STBImage.stbi_load;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_set_flip_vertically_on_load;
import static org.lwjgl.BufferUtils.createByteBuffer;
import static org.lwjgl.BufferUtils.createIntBuffer;

public class TextureReader { // Renamed from ImageReader so as to avoid confusion with nio package of a similar name

    String imagePath;
    ByteBuffer data;
    int width, height, channels;

    TextureReader(String imagePath) {
        
        this.imagePath = imagePath;

        // stbi is included in lwjgl
        IntBuffer width = createIntBuffer(1); // It seems like stbi gets the width height and channels behind the scenes, so these variables can just be declared and not assigned.
        IntBuffer height = createIntBuffer(1);
        IntBuffer channels = createIntBuffer(1);
        stbi_set_flip_vertically_on_load(true); // Flipping image right way around
        data = stbi_load(imagePath, width, height, channels, 0);
        if (data == null) {
            System.out.println("Failed to read image: " + stbi_failure_reason()); // Even the docs say this is fairly unhelpful
        }

        this.width = width.get(0);
        this.height = height.get(0);
        this.channels = channels.get(0);
        
        if (this.channels == 3) {
            this.channels = GL_RGB;
        }
        else if (this.channels == 4) {
            this.channels = GL_RGBA;
        }
        else {
            assert false: "Error: Unknown number of channels " +this.channels;
        }
    }
}

// I wanted to read the image data in using FileInputStream, but I couldn't get things to work properly.
