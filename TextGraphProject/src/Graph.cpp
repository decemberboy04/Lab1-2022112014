#include "Graph.h"
#include <fstream>
#include <iostream>

void Graph::addEdge(const std::string& from, const std::string& to) {
    // 自动初始化不存在的节点
    adjacencyList[from][to]++;
}

std::set<std::string> Graph::getNodes() const {
    std::set<std::string> nodes;
    for (const auto& pair : adjacencyList) {
        nodes.insert(pair.first);
        for (const auto& edge : pair.second) {
            nodes.insert(edge.first);
        }
    }
    return nodes;
}

const std::map<std::string, int>& Graph::getEdges(const std::string& node) const {
    static const std::map<std::string, int> empty;
    auto it = adjacencyList.find(node);
    return it != adjacencyList.end() ? it->second : empty;
}

const std::unordered_map<std::string, std::map<std::string, int>>& Graph::getAdjacencyList() const {
    return adjacencyList;
}

void Graph::visualize(const std::string& outputPath) const {
    std::ofstream dotFile(outputPath + ".dot");
    if (!dotFile.is_open()) {
        throw std::runtime_error("无法创建DOT文件");
    }
    
    dotFile << "digraph WordGraph {\n";
    dotFile << "    rankdir=LR;\n";
    dotFile << "    node [shape=circle];\n";
    
    // 添加所有节点
    for (const auto& node : getNodes()) {
        dotFile << "    \"" << node << "\";\n";
    }
    
    // 添加所有边
    for (const auto& fromPair : adjacencyList) {
        for (const auto& toPair : fromPair.second) {
            dotFile << "    \"" << fromPair.first << "\" -> \"" << toPair.first 
                   << "\" [label=\"" << toPair.second << "\"];\n";
        }
    }
    
    dotFile << "}\n";
    dotFile.close();
    
    // 调用Graphviz生成图片
    std::string command = "dot -Tpng " + outputPath + ".dot -o " + outputPath + ".png";
    system(command.c_str());
}