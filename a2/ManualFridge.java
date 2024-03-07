package a2;
import tage.*;
import tage.shapes.*;

public class ManualFridge extends ManualObject {
private float[] vertices = new float[] {
    // Front face
    -0.5f, -0.25f, 0.5f,  0.5f, -0.25f, 0.5f,  0.5f, 0.25f, 0.5f, // Triangle 1
    -0.5f, -0.25f, 0.5f,  0.5f, 0.25f, 0.5f,  -0.5f, 0.25f, 0.5f, // Triangle 2
    // Right face
    0.5f, -0.25f, 0.5f,  0.5f, -0.25f, -0.5f,  0.5f, 0.25f, -0.5f, // Triangle 1
    0.5f, -0.25f, 0.5f,  0.5f, 0.25f, -0.5f,  0.5f, 0.25f, 0.5f, // Triangle 2
    // Back face
    0.5f, -0.25f, -0.5f,  -0.5f, -0.25f, -0.5f,  -0.5f, 0.25f, -0.5f, // Triangle 1
    0.5f, -0.25f, -0.5f,  -0.5f, 0.25f, -0.5f,  0.5f, 0.25f, -0.5f, // Triangle 2
    // Left face
    -0.5f, -0.25f, -0.5f,  -0.5f, -0.25f, 0.5f,  -0.5f, 0.25f, 0.5f, // Triangle 1
    -0.5f, -0.25f, -0.5f,  -0.5f, 0.25f, 0.5f,  -0.5f, 0.25f, -0.5f, // Triangle 2
    // Bottom face
    -0.5f, -0.25f, -0.5f,  0.5f, -0.25f, -0.5f,  0.5f, -0.25f, 0.5f, // Triangle 1
    -0.5f, -0.25f, -0.5f,  0.5f, -0.25f, 0.5f,  -0.5f, -0.25f, 0.5f  // Triangle 2
};

    private float[] texcoords = new float[] {
        0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, // Front face
        0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, // Right face
        0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, // Back face
        0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f, // Left face
        0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 0.0f, 0.0f, 1.0f, 1.0f, 0.0f, 1.0f  // Bottom face
    };

    private float[] normals = new float[] {
    // Front face (z positive)
    0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
    0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f,
    // Right face (x positive)
    1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
    1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 0.0f,
    // Back face (z negative)
    0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
    0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f,
    // Left face (x negative)
    -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
    -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f,
    // Bottom face (y negative)
    0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f,
    0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f, 0.0f, -1.0f, 0.0f
};


    public ManualFridge() { 
        super();
        setNumVertices(36); // 6 faces * 2 triangles per face * 3 vertices per triangle
        setVertices(vertices);
        setTexCoords(texcoords);
        setNormals(normals);
        // Material properties would be set here if needed
    }
}
