cmake_minimum_required(VERSION 3.10)
project(TextGraphProject)

set(CMAKE_CXX_STANDARD 17)

# 查找必要的库
find_package(OpenGL REQUIRED)
find_package(GLFW REQUIRED)

# 包含Dear ImGui
add_subdirectory(include/imgui)

# 添加可执行文件
add_executable(TextGraphProject 
    src/main.cpp 
    src/GUI.cpp 
    src/Graph.cpp 
    src/TextProcessor.cpp
    include/imgui/imgui.cpp
    include/imgui/imgui_demo.cpp
    include/imgui/imgui_draw.cpp
    include/imgui/imgui_tables.cpp
    include/imgui/imgui_widgets.cpp
    include/imgui/backends/imgui_impl_glfw.cpp
    include/imgui/backends/imgui_impl_opengl3.cpp
)

# 链接库
target_link_libraries(TextGraphProject 
    OpenGL::GL 
    glfw 
    imgui
)