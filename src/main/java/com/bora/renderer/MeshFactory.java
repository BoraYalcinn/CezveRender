package com.bora.renderer;

public class MeshFactory {
	

    // =========================================================================
    //  CREATE FLOOR
    // =========================================================================

    public static Mesh createFloor(float size, float y) {
        float[] vertices = {
            -size, y, -size,   0f,    0f,    0f, 1f, 0f,
             size, y, -size,   size,  0f,    0f, 1f, 0f,
             size, y,  size,   size,  size,  0f, 1f, 0f,
            -size, y,  size,   0f,    size,  0f, 1f, 0f
        };
        int[] indices = { 0, 1, 2,  0, 2, 3 };

        Mesh m = new Mesh();
        m.createMesh(vertices, indices);
        return m;
    }
    

    // =========================================================================
    //  CREATE SHAPE METHODS
    // =========================================================================

    public static Mesh createPyramid(float size) {
        float h  = size;          
        float b  = size / 2f;     
        float by = -size / 2f;    

        // edges
        float ax = 0,    ay = h,  az = 0;      
        float v0x = -b,  v0y = by, v0z = -b;   
        float v1x =  b,  v1y = by, v1z = -b;   
        float v2x =  b,  v2y = by, v2z =  b;   
        float v3x = -b,  v3y = by, v3z =  b;   

        float[] nFront  = faceNormal(v3x,v3y,v3z, v2x,v2y,v2z, ax,ay,az);
        float[] nRight  = faceNormal(v2x,v2y,v2z, v1x,v1y,v1z, ax,ay,az);
        float[] nBack   = faceNormal(v1x,v1y,v1z, v0x,v0y,v0z, ax,ay,az);
        float[] nLeft   = faceNormal(v0x,v0y,v0z, v3x,v3y,v3z, ax,ay,az);
        float[] nBottom = {0f, -1f, 0f};

        float[] vertices = {
            // FRONT
            v3x, v3y, v3z,   0f,   0f,   nFront[0], nFront[1], nFront[2],
            v2x, v2y, v2z,   1f,   0f,   nFront[0], nFront[1], nFront[2],
            ax,  ay,  az,    0.5f, 1f,   nFront[0], nFront[1], nFront[2],

            // RIGHT
            v2x, v2y, v2z,   0f,   0f,   nRight[0], nRight[1], nRight[2],
            v1x, v1y, v1z,   1f,   0f,   nRight[0], nRight[1], nRight[2],
            ax,  ay,  az,    0.5f, 1f,   nRight[0], nRight[1], nRight[2],

            // BACK
            v1x, v1y, v1z,   0f,   0f,   nBack[0], nBack[1], nBack[2],
            v0x, v0y, v0z,   1f,   0f,   nBack[0], nBack[1], nBack[2],
            ax,  ay,  az,    0.5f, 1f,   nBack[0], nBack[1], nBack[2],

            // LEFT
            v0x, v0y, v0z,   0f,   0f,   nLeft[0], nLeft[1], nLeft[2],
            v3x, v3y, v3z,   1f,   0f,   nLeft[0], nLeft[1], nLeft[2],
            ax,  ay,  az,    0.5f, 1f,   nLeft[0], nLeft[1], nLeft[2],

            // BOTTOM
            v0x, v0y, v0z,   0f, 0f,   nBottom[0], nBottom[1], nBottom[2],
            v1x, v1y, v1z,   1f, 0f,   nBottom[0], nBottom[1], nBottom[2],
            v2x, v2y, v2z,   1f, 1f,   nBottom[0], nBottom[1], nBottom[2],
            v3x, v3y, v3z,   0f, 1f,   nBottom[0], nBottom[1], nBottom[2],
        };

        int[] indices = {
            
             0,  1,  2,   
             3,  4,  5,   
             6,  7,  8,   
             9, 10, 11,   

            12, 13, 14,
            12, 14, 15
        };

        Mesh mesh = new Mesh();
        mesh.createMesh(vertices, indices);
        return mesh;
    }

    
    public static Mesh createTriangle(float size) {
        return createPyramid(size);
    }

    public static Mesh createCube(float size) {
        float h = size / 2f;
        float[] vertices = {
            // FRONT
            -h, -h,  h,   0f, 0f,   0f, 0f, 1f,
             h, -h,  h,   1f, 0f,   0f, 0f, 1f,
             h,  h,  h,   1f, 1f,   0f, 0f, 1f,
            -h,  h,  h,   0f, 1f,   0f, 0f, 1f,
            // BACK 
             h, -h, -h,   0f, 0f,   0f, 0f,-1f,
            -h, -h, -h,   1f, 0f,   0f, 0f,-1f,
            -h,  h, -h,   1f, 1f,   0f, 0f,-1f,
             h,  h, -h,   0f, 1f,   0f, 0f,-1f,
            //TOP
            -h,  h,  h,   0f, 0f,   0f, 1f, 0f,
             h,  h,  h,   1f, 0f,   0f, 1f, 0f,
             h,  h, -h,   1f, 1f,   0f, 1f, 0f,
            -h,  h, -h,   0f, 1f,   0f, 1f, 0f,
            // BOTTOM
            -h, -h, -h,   0f, 0f,   0f,-1f, 0f,
             h, -h, -h,   1f, 0f,   0f,-1f, 0f,
             h, -h,  h,   1f, 1f,   0f,-1f, 0f,
            -h, -h,  h,   0f, 1f,   0f,-1f, 0f,
            // RIGHT
             h, -h,  h,   0f, 0f,   1f, 0f, 0f,
             h, -h, -h,   1f, 0f,   1f, 0f, 0f,
             h,  h, -h,   1f, 1f,   1f, 0f, 0f,
             h,  h,  h,   0f, 1f,   1f, 0f, 0f,
            // LEFT
            -h, -h, -h,   0f, 0f,  -1f, 0f, 0f,
            -h, -h,  h,   1f, 0f,  -1f, 0f, 0f,
            -h,  h,  h,   1f, 1f,  -1f, 0f, 0f,
            -h,  h, -h,   0f, 1f,  -1f, 0f, 0f,
        };
        int[] indices = {
             0,  1,  2,   0,  2,  3,
             4,  5,  6,   4,  6,  7,
             8,  9, 10,   8, 10, 11,
            12, 13, 14,  12, 14, 15,
            16, 17, 18,  16, 18, 19,
            20, 21, 22,  20, 22, 23
        };
        Mesh mesh = new Mesh();
        mesh.createMesh(vertices, indices);
        return mesh;
    }

    
    private static float[] faceNormal(float ax, float ay, float az,
                                      float bx, float by, float bz,
                                      float cx, float cy, float cz) {
        
        float e1x = bx - ax, e1y = by - ay, e1z = bz - az;
        float e2x = cx - ax, e2y = cy - ay, e2z = cz - az;
        float nx = e1y * e2z - e1z * e2y;
        float ny = e1z * e2x - e1x * e2z;
        float nz = e1x * e2y - e1y * e2x;
        // normalize
        float len = (float) Math.sqrt(nx*nx + ny*ny + nz*nz);
        if (len < 0.0001f) return new float[]{0f, 1f, 0f};
        return new float[]{ nx/len, ny/len, nz/len };
    }
}