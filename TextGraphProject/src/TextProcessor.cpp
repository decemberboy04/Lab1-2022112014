#include "TextProcessor.h"
#include <fstream>
#include <sstream>
#include <algorithm>
#include <cctype>

std::vector<std::string> TextProcessor::processFile(const std::string& filePath) {
    std::ifstream file(filePath);
    if (!file.is_open()) {
        throw std::runtime_error("无法打开文件: " + filePath);
    }
    
    std::stringstream buffer;
    buffer << file.rdbuf();
    return processText(buffer.str());
}

std::vector<std::string> TextProcessor::processText(const std::string& text) {
    std::vector<std::string> words;
    std::string currentWord;
    
    for (char c : text) {
        // 只保留字母字符
        if (isalpha(c)) {
            currentWord += tolower(c);
        }
        // 非字母字符作为分隔符
        else if (!currentWord.empty()) {
            words.push_back(currentWord);
            currentWord.clear();
        }
    }
    
    // 添加最后一个单词（如果有）
    if (!currentWord.empty()) {
        words.push_back(currentWord);
    }
    
    return words;
}

std::string TextProcessor::cleanWord(const std::string& word) {
    std::string cleaned;
    for (char c : word) {
        if (isalpha(c)) {
            cleaned += tolower(c);
        }
    }
    return cleaned;
}