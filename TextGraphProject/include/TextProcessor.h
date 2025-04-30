#ifndef TEXT_PROCESSOR_H
#define TEXT_PROCESSOR_H

#include <string>
#include <vector>

class TextProcessor {
public:
    // 处理文本文件
    static std::vector<std::string> processFile(const std::string& filePath);
    
    // 处理单个字符串
    static std::vector<std::string> processText(const std::string& text);
    
private:
    // 清理单词
    static std::string cleanWord(const std::string& word);
};

#endif // TEXT_PROCESSOR_H