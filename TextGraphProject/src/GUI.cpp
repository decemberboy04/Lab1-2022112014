#include "GUI.h"
#include "TextProcessor.h"
#include "imgui.h"
#include "imgui_impl_glfw.h"
#include "imgui_impl_opengl3.h"
#include <GLFW/glfw3.h>
#include <iostream>
#include <filesystem>

namespace fs = std::filesystem;

void GUI::run() {
    // 初始化GLFW
    if (!glfwInit()) {
        std::cerr << "无法初始化GLFW" << std::endl;
        return;
    }
    
    // 创建窗口
    GLFWwindow* window = glfwCreateWindow(1280, 720, "文本图生成器", NULL, NULL);
    if (!window) {
        std::cerr << "无法创建窗口" << std::endl;
        glfwTerminate();
        return;
    }
    
    glfwMakeContextCurrent(window);
    glfwSwapInterval(1); // 启用垂直同步
    
    // 初始化Dear ImGui
    IMGUI_CHECKVERSION();
    ImGui::CreateContext();
    ImGuiIO& io = ImGui::GetIO(); (void)io;
    
    ImGui::StyleColorsDark();
    
    ImGui_ImplGlfw_InitForOpenGL(window, true);
    ImGui_ImplOpenGL3_Init("#version 130");
    
    // 主循环
    while (!glfwWindowShouldClose(window)) {
        glfwPollEvents();
        
        // 开始新帧
        ImGui_ImplOpenGL3_NewFrame();
        ImGui_ImplGlfw_NewFrame();
        ImGui::NewFrame();
        
        showMainWindow();
        
        // 渲染
        ImGui::Render();
        int display_w, display_h;
        glfwGetFramebufferSize(window, &display_w, &display_h);
        glViewport(0, 0, display_w, display_h);
        glClearColor(0.45f, 0.55f, 0.60f, 1.00f);
        glClear(GL_COLOR_BUFFER_BIT);
        ImGui_ImplOpenGL3_RenderDrawData(ImGui::GetDrawData());
        
        glfwSwapBuffers(window);
    }
    
    // 清理
    ImGui_ImplOpenGL3_Shutdown();
    ImGui_ImplGlfw_Shutdown();
    ImGui::DestroyContext();
    
    glfwDestroyWindow(window);
    glfwTerminate();
}

void GUI::showMainWindow() {
    ImGui::Begin("文本图生成器");
    
    if (ImGui::Button("选择文本文件")) {
        showFileSelector();
    }
    
    if (!currentFilePath.empty()) {
        ImGui::SameLine();
        ImGui::Text("当前文件: %s", currentFilePath.c_str());
    }
    
    if (fileLoaded) {
        if (ImGui::Button("显示图")) {
            showGraphVisualization();
        }
    }
    
    ImGui::End();
}

void GUI::showFileSelector() {
    // 这里简化了文件选择器，实际项目中应该使用原生文件对话框
    static char filePath[256] = "";
    
    ImGui::Begin("选择文件", NULL, ImGuiWindowFlags_AlwaysAutoResize);
    ImGui::InputText("文件路径", filePath, IM_ARRAYSIZE(filePath));
    
    if (ImGui::Button("确定")) {
        currentFilePath = filePath;
        
        // 检查文件扩展名
        if (fs::path(currentFilePath).extension() != ".txt") {
            ImGui::OpenPopup("错误");
        } else {
            try {
                wordList = TextProcessor::processFile(currentFilePath);
                
                // 构建图
                wordGraph = Graph();
                for (size_t i = 0; i < wordList.size() - 1; ++i) {
                    wordGraph.addEdge(wordList[i], wordList[i+1]);
                }
                
                fileLoaded = true;
                ImGui::CloseCurrentPopup();
            } catch (const std::exception& e) {
                std::cerr << "错误: " << e.what() << std::endl;
                ImGui::OpenPopup("错误");
            }
        }
    }
    
    // 错误弹窗
    if (ImGui::BeginPopupModal("错误", NULL, ImGuiWindowFlags_AlwaysAutoResize)) {
        ImGui::Text("无效的文件路径或不是.txt文件");
        if (ImGui::Button("确定")) {
            ImGui::CloseCurrentPopup();
        }
        ImGui::EndPopup();
    }
    
    ImGui::End();
}

void GUI::showGraphVisualization() {
    try {
        wordGraph.visualize("word_graph");
        ImGui::OpenPopup("图可视化");
    } catch (const std::exception& e) {
        std::cerr << "可视化错误: " << e.what() << std::endl;
    }
    
    if (ImGui::BeginPopupModal("图可视化", NULL, ImGuiWindowFlags_AlwaysAutoResize)) {
        ImGui::Text("图已生成并保存为word_graph.png");
        
        // 这里可以添加显示图片的代码，需要额外的库支持
        
        if (ImGui::Button("关闭")) {
            ImGui::CloseCurrentPopup();
        }
        ImGui::EndPopup();
    }
}