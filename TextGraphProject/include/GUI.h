#ifndef GUI_H
#define GUI_H

#include <string>
#include <vector>
#include "Graph.h"

class GUI {
public:
    void run();
    
private:
    void showMainWindow();
    void showFileSelector();
    void showGraphVisualization();
    void drawGraph();
    
    std::string currentFilePath;
    bool fileLoaded = false;
    Graph wordGraph;
    std::vector<std::string> wordList;
};

#endif // GUI_H