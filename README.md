<div align="center">
<img width="512" height="1024" alt="CezveRenderer" src="https://github.com/user-attachments/assets/2d34fe7c-6040-4277-932d-07ffe0909d4b" />

# CezveRender

**A real-time 3D renderer built from scratch in Java using OpenGL 3.3 and LWJGL.**

[![Java](https://img.shields.io/badge/Java-17+-orange?style=flat-square)](https://www.java.com/)
[![OpenGL](https://img.shields.io/badge/OpenGL-3.3-blue?style=flat-square)](https://www.opengl.org/)
[![Maven](https://img.shields.io/badge/Maven-3.9.9-red?style=flat-square)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)](LICENSE)

</div>

---

## About

CezveRender is my first large-scale personal project — a real-time 3D renderer written entirely in Java. It's not perfect, there are rough edges, known bugs, and plenty of room to grow. I built it to learn graphics programming from the ground up: shadow mapping, lighting models, scene management, and GPU-side rendering with OpenGL.

If you spot something broken, have ideas for improvement, or just want to dig into the code — contributions and feedback are very welcome. Open an issue, send a PR, or fork it and make it your own.

Contact : borayalcintr@gmail.com

---

## Demo

| Scene | Shadow Debug | ImGui Editor |
|---|---|---|
| <img alt="scene_img" src="https://github.com/user-attachments/assets/915001bb-0f07-477b-967b-a1bddfef1acd" /> | <img alt="shadow_img" src="https://github.com/user-attachments/assets/67adb3d3-b858-43d6-95eb-47b27cd1fc23" /> | <img alt="editor_img" src="https://github.com/user-attachments/assets/df96c1db-b63a-44b8-ae25-0818e163692b" /> |
▶️ **[Full project showcase on YouTube](https://youtube.com/your-link-here)** (showcase video is under production !)

---

## Features

### Lighting
- Directional light with orthographic shadow mapping
- Point lights with cubemap shadow mapping (geometry shader single-pass)
- Spot lights with perspective shadow mapping
- Phong shading — ambient, diffuse, specular per material

### Shadow Mapping
- Soft shadows for directional and spot lights
- Adjustable shadow bias at runtime

### Scene
- OBJ model loading via Assimp (with MTL and texture support)
- Skybox (cubemap)
- Procedural mesh generation — floor, cube, triangle/pyramid
- Object Transform (position, rotation, scale)

### ImGui Editor
- Add / remove / transform models at runtime
- Add / remove / edit all light types (directional, point, spot)
- Floor builder with custom texture path
- Debug tools: shadow map overlay, light-camera view, shadow debug visualization, bias slider
- Create primitive shapes (For now only cubes and triangles!) and adjust material shininess and specular intensity
---

## Tech Stack

| Library | Version | Purpose |
|---|---|---|
| [LWJGL](https://www.lwjgl.org/) | 3.x | OpenGL, GLFW, OpenAL bindings |
| [Assimp](https://github.com/assimp/assimp) | via LWJGL | OBJ / model loading |
| [JOML](https://github.com/JOML-CI/JOML) | 1.x | Vector and matrix math |
| [imgui-java](https://github.com/SpaiR/imgui-java) | 1.86.11 | Runtime editor UI |
| [STB](https://github.com/nothings/stb) | via LWJGL | Image loading |
| Maven | 3.9.9 | Build system |

---

## Project Structure

```
CezveRender/
├── src/main/
│   ├── java/com/bora/renderer/
│   │   ├── Main.java               
│   │   ├── Window.java             
│   │   ├── Input.java              
│   │   ├── Camera.java             
│   │   ├── Scene.java              
│   │   ├── SceneEditor.java        
│   │   ├── Shader.java             
│   │   ├── Mesh.java               
│   │   ├── MeshFactory.java        
│   │   ├── Model.java              
│   │   ├── Material.java
│   │   ├── Texture.java
│   │   ├── Transform.java
│   │   ├── Skybox.java
│   │   ├── Light.java
│   │   ├── DirectionalLight.java
│   │   ├── PointLight.java
│   │   ├── SpotLight.java
│   │   ├── ShadowMap.java          
│   │   └── CubeShadowMap.java      
│   └── resources/
│       ├── shaders/
│       │   ├── shader.vert / shader.frag         
│       │   ├── shadow.vert / shadow.frag
│       │   ├── debug_depth.vert / debug_depth.frag      
│       │   ├── omni_shadow.vert / .geom / .frag  
│       │   └── skybox.vert / skybox.frag
│       ├── models/                 
│       └── textures/              
├── .mvn/wrapper/
│   └── maven-wrapper.properties
├── mvnw        
├── mvnw.cmd    
└── pom.xml
```

---

## Getting Started

### Prerequisites

- **Java 17+** — [Download here](https://adoptium.net/)
- **Git**
- No Maven installation needed — the project includes the Maven Wrapper

### Clone

```bash
git clone https://github.com/yourusername/CezveRender.git
cd CezveRender
```

### Run

**Windows:**
```cmd
mvnw.cmd compile exec:java
```

**Linux / macOS:**
```bash
chmod +x mvnw
./mvnw compile exec:java
```

The Maven Wrapper will automatically download Maven 3.9.9 on first run — no manual install required.

### Build a JAR

**Windows:**
```cmd
mvnw.cmd package
```

**Linux / macOS:**
```bash
./mvnw package
```

---

## Controls

| Input | Action |
|---|---|
| `Tab` | Toggle editor / camera mode |
| `W A S D` | Move camera |
| `Mouse` | Look around (camera mode) |
| `Escape` | Exit |
| `F` | Toggle Fullscreen |

---

## Known Issues & Limitations

- Spot light shadow maps currently render incorrect (bug under investigation)
- no scene save/load
- Shadow coverage limited to the directional light ortho frustum size
---

## Contributing

This is an open learning project. If you want to fix a bug, add a feature, or just experiment — go for it.

1. Fork the repo
2. Create a branch: `git checkout -b feature/my-feature`
3. Commit your changes
4. Open a Pull Request

For larger changes, opening an issue first to discuss is appreciated.

---
 
## Disclaimer
 
The ImGui editor UI was not written entirely by hand. Due to my limited familiarity with the imgui-java library at the time, modern programming tools were used to help build and iterate on the editor quickly and cleanly. All rendering, shadow mapping, lighting, and scene architecture code is my own work.
 
---
 
## References
 
- [LearnOpenGL](https://learnopengl.com/) — the go-to open reference for modern OpenGL concepts, used throughout the project
- [Essential Mathematics for Games and Interactive Applications](https://www.essentialmath.com/) — foundational math reference for vectors, matrices, and transforms
 
---
 
## Special Thanks
 
- **Daniel Camus** — for the pull request and contribution, much appreciated
- **Benny Bobaganoosh (Ben Cook)** — his [Modern OpenGL](https://www.udemy.com/course/opengl-with-modern-c/) course on Udemy laid the groundwork for this entire project. The core architecture, rendering pipeline, and shadow mapping approach all trace back to what I learned there.
 
---

## License

[MIT](LICENSE) 
