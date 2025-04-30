#ifndef GRAPH_H
#define GRAPH_H

#include <string>
#include <vector>
#include <map>
#include <unordered_map>
#include <set>

class Graph {
public:
    // 添加边
    void addEdge(const std::string& from, const std::string& to);
    
    // 获取所有节点
    std::set<std::string> getNodes() const;
    
    // 获取从某节点出发的边
    const std::map<std::string, int>& getEdges(const std::string& node) const;
    
    // 获取图的邻接表表示
    const std::unordered_map<std::string, std::map<std::string, int>>& getAdjacencyList() const;
    
    // 可视化图
    void visualize(const std::string& outputPath) const;

private:
    // 邻接表表示: 源节点 -> (目标节点 -> 权重)
    std::unordered_map<std::string, std::map<std::string, int>> adjacencyList;
};

#endif // GRAPH_H