import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.DoubleStream;


public class Main {
    private DirectedGraph graph;
    private JFrame frame;
    private JTextArea outputArea;
    private JTextField filePathField;
    private JTextField word1Field;
    private JTextField word2Field;
    private JTextField newTextField;
    private JTextField randomWalkField;
    private String graphImagePath = "graph.png";
    private boolean isRandomWalking = false;
    private Thread randomWalkThread;
    private boolean generatePageRankGraph = false;

    public void initForTest(String filePath) {
        try {
            String text = readFile(filePath);
            List<String> words = processText(text);
            this.graph = buildGraph(words);
        } catch (IOException e) {
            this.graph = new DirectedGraph();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new Main().initialize();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void initialize() {
        // 创建主窗口
        frame = new JFrame("文本图处理系统");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLayout(new BorderLayout());

        // 创建顶部面板
        JPanel topPanel = new JPanel(new BorderLayout());

        // 文件选择部分
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel fileLabel = new JLabel("文本文件路径:");
        filePathField = new JTextField(30);
        JButton browseButton = new JButton("浏览");
        browseButton.addActionListener(e -> browseFile());
        JButton loadButton = new JButton("加载");
        loadButton.addActionListener(e -> loadFile());

        filePanel.add(fileLabel);
        filePanel.add(filePathField);
        filePanel.add(browseButton);
        filePanel.add(loadButton);
        topPanel.add(filePanel, BorderLayout.NORTH);

        // 功能按钮部分
        JPanel buttonPanel = new JPanel(new GridLayout(2, 3, 5, 5));

        // 桥接词查询
        JPanel bridgePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel word1Label = new JLabel("单词1:");
        word1Field = new JTextField(10);
        JLabel word2Label = new JLabel("单词2:");
        word2Field = new JTextField(10);
        JButton bridgeButton = new JButton("查询桥接词");
        bridgeButton.addActionListener(e -> queryBridgeWords());

        bridgePanel.add(word1Label);
        bridgePanel.add(word1Field);
        bridgePanel.add(word2Label);
        bridgePanel.add(word2Field);
        bridgePanel.add(bridgeButton);
        buttonPanel.add(bridgePanel);

        // 生成新文本
        JPanel newTextPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel newTextLabel = new JLabel("输入文本:");
        newTextField = new JTextField(20);
        JButton generateButton = new JButton("生成新文本");
        generateButton.addActionListener(e -> generateNewText());

        newTextPanel.add(newTextLabel);
        newTextPanel.add(newTextField);
        newTextPanel.add(generateButton);
        buttonPanel.add(newTextPanel);

        // 最短路径
        JButton shortestPathButton = new JButton("计算最短路径");
        shortestPathButton.addActionListener(e -> calcShortestPath());
        buttonPanel.add(shortestPathButton);

        // PageRank
        JPanel pageRankPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton pageRankButton = new JButton("计算PageRank");
        pageRankButton.addActionListener(e -> showPageRank());
        // buttonPanel.add(pageRankButton);

        JToggleButton toggleButton = new JToggleButton("生成图", false);
        toggleButton.addActionListener(e -> {
            generatePageRankGraph = toggleButton.isSelected();
            showMessage("PageRank图生成: " + (generatePageRankGraph ? "开启" : "关闭"));
        });

        pageRankPanel.add(pageRankButton);
        pageRankPanel.add(toggleButton);
        buttonPanel.add(pageRankPanel);


        // 随机游走
        JPanel walkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton startWalkButton = new JButton("开始随机游走");
        startWalkButton.addActionListener(e -> startRandomWalk());
        JButton stopWalkButton = new JButton("停止");
        stopWalkButton.addActionListener(e -> stopRandomWalk());

        walkPanel.add(startWalkButton);
        walkPanel.add(stopWalkButton);
        buttonPanel.add(walkPanel);

        // 显示图
        JButton showGraphButton = new JButton("显示图");
        showGraphButton.addActionListener(e -> showDirectedGraph());
        buttonPanel.add(showGraphButton);

        topPanel.add(buttonPanel, BorderLayout.CENTER);
        frame.add(topPanel, BorderLayout.NORTH);

        // 创建输出区域
        outputArea = new JTextArea();
        outputArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(outputArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // 显示窗口
        frame.setVisible(true);
    }

    private void browseFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
            }

            @Override
            public String getDescription() {
                return "文本文件 (*.txt)";
            }
        });

        int result = fileChooser.showOpenDialog(frame);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getAbsolutePath());
        }
    }

    public void loadFile() {
        String filePath = filePathField.getText();
        if (filePath.isEmpty()) {
            showMessage("请选择或输入文件路径");
            return;
        }

        if (!filePath.toLowerCase().endsWith(".txt")) {
            showMessage("只支持.txt文件");
            return;
        }

        try {
            String text = readFile(filePath);
            List<String> words = processText(text);
            graph = buildGraph(words);
            showMessage("文件加载成功，图已构建");
            // showDirectedGraph();
        } catch (IOException e) {
            showMessage("读取文件错误: " + e.getMessage());
        }
    }

    public DirectedGraph getGraph() {
        return this.graph;
    }

    private String readFile(String filePath) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append(" ");
            }
        }
        return content.toString();
    }

    private List<String> processText(String text) {
        // 替换所有非字母字符为空格，并转换为小写
        String processed = text.replaceAll("[^a-zA-Z]", " ").toLowerCase();
        // 分割为单词列表，过滤掉空字符串
        return Arrays.stream(processed.split("\\s+"))
                .filter(word -> !word.isEmpty())
                .collect(Collectors.toList());
    }

    private DirectedGraph buildGraph(List<String> words) {
        DirectedGraph graph = new DirectedGraph();
        if (words.isEmpty())
            return graph;

        String prevWord = words.get(0);
        for (int i = 1; i < words.size(); i++) {
            String currentWord = words.get(i);
            graph.addEdge(prevWord, currentWord);
            prevWord = currentWord;
        }
        return graph;
    }

    public void showDirectedGraph() {
        if (graph == null) {
            showMessage("请先加载文件构建图");
            return;
        }

        try {
            GraphVisualizer.visualize(graph, graphImagePath);
            showMessage("有向图已生成并保存为: " + graphImagePath);

            // 尝试用默认图片查看器打开
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(new File(graphImagePath));
            }
        } catch (IOException e) {
            showMessage("生成图错误: " + e.getMessage());
        }
    }

    public String queryBridgeWords(String word1, String word2) {
        if (graph == null)
            return "图未构建";

        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if( word1.isEmpty() || word2.isEmpty() ){
            return "Input Error!";
        }

        if (!graph.containsNode(word1) || !graph.containsNode(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        List<String> bridgeWords = graph.findBridgeWords(word1, word2);

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            StringJoiner joiner = new StringJoiner(", ");
            for (int i = 0; i < bridgeWords.size(); i++) {
                if (i == bridgeWords.size() - 1 && bridgeWords.size() > 1) {
                    joiner.add("and " + bridgeWords.get(i));
                } else {
                    joiner.add(bridgeWords.get(i));
                }
            }
            return "The bridge words from " + word1 + " to " + word2 + " are: " + joiner.toString() + ".";
        }
    }

    private void queryBridgeWords() {
        if (graph == null) {
            showMessage("请先加载文件构建图");
            return;
        }

        String word1 = word1Field.getText().trim();
        String word2 = word2Field.getText().trim();

        if (word1.isEmpty() || word2.isEmpty()) {
            showMessage("请输入两个单词");
            return;
        }

        String result = queryBridgeWords(word1, word2);
        showMessage(result);
    }

    public String generateNewText(String inputText) {
        if (graph == null)
            return "图未构建";

        List<String> words = processText(inputText);
        if (words.size() < 2)
            return inputText;

        StringBuilder newText = new StringBuilder(words.get(0));
        Random random = new Random();

        for (int i = 1; i < words.size(); i++) {
            String word1 = words.get(i - 1);
            String word2 = words.get(i);

            List<String> bridges = graph.findBridgeWords(word1, word2);
            if (!bridges.isEmpty()) {
                String bridge = bridges.get(random.nextInt(bridges.size()));
                newText.append(" ").append(bridge);
            }
            newText.append(" ").append(word2);
        }

        return newText.toString();
    }

    private void generateNewText() {
        if (graph == null) {
            showMessage("请先加载文件构建图");
            return;
        }

        String inputText = newTextField.getText().trim();
        if (inputText.isEmpty()) {
            showMessage("请输入文本");
            return;
        }

        String result = generateNewText(inputText);
        showMessage("原文本: " + inputText + "\n新文本: " + result);
    }

    public String calcShortestPath(String word1, String word2) {
        if (graph == null)
            return "图未构建";

        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        if (word1.isEmpty()) {
            return "请输入至少一个单词";
        }

        if (!graph.containsNode(word1)) {
            return "No " + word1 + " in the graph!";
        }

        if (word2.isEmpty()) {
            // 计算word1到所有其他节点的最短路径
            Map<String, List<String>> allPaths = graph.calculateAllShortestPathsFrom(word1);
            if (allPaths.isEmpty()) {
                return "No paths from " + word1 + " to any other words!";
            }

            StringBuilder result = new StringBuilder();
            result.append("Shortest paths from ").append(word1).append(":\n");
            for (Map.Entry<String, List<String>> entry : allPaths.entrySet()) {
                result.append("To ").append(entry.getKey()).append(": ");
                result.append(String.join(" -> ", entry.getValue()));
                result.append("\n");
            }
            return result.toString();
        } else {
            if (!graph.containsNode(word2)) {
                return "No " + word2 + " in the graph!";
            }

            List<List<String>> paths = graph.calculateShortestPaths(word1, word2);
            if (paths.isEmpty()) {
                return "No path from " + word1 + " to " + word2 + "!";
            }

            StringBuilder result = new StringBuilder();
            result.append("Shortest path(s) from ").append(word1).append(" to ").append(word2).append(":\n");
            for (List<String> path : paths) {
                result.append(String.join(" -> ", path));
                result.append(" (length: ").append(path.size() - 1).append(")\n");
            }
            return result.toString();
        }
    }

    private void calcShortestPath() {
        if (graph == null) {
            showMessage("请先加载文件构建图");
            return;
        }

        String word1 = word1Field.getText().trim().toLowerCase();
        String word2 = word2Field.getText().trim().toLowerCase();

        if (word1.isEmpty()) {
            showMessage("请输入至少一个单词");
            return;
        }

        String result = calcShortestPath(word1, word2);
        showMessage(result);

        // 可视化最短路径
        if (!word2.isEmpty() && graph.containsNode(word1) && graph.containsNode(word2)) {
            try {
                List<List<String>> paths = graph.calculateShortestPaths(word1, word2);
                if (!paths.isEmpty()) {
                    GraphVisualizer.visualizeShortestPaths(graph, graphImagePath, paths);
                    showMessage("最短路径图已更新: " + graphImagePath);

                    // 尝试用默认图片查看器打开
                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().open(new File(graphImagePath));
                    }
                }
            } catch (IOException e) {
                showMessage("可视化最短路径错误: " + e.getMessage());
            }
        }
    }

    public Double calcPageRank(String word) {
        if (graph == null)
            return null;
        return graph.calculatePageRank().getOrDefault(word.toLowerCase(), 0.0);
    }

    private void showPageRank() {
        if (graph == null) {
            showMessage("请先加载文件构建图");
            return;
        }

        Map<String, Double> pageRanks = graph.calculatePageRank();
        List<Map.Entry<String, Double>> sorted = new ArrayList<>(pageRanks.entrySet());
        sorted.sort((e1, e2) -> e2.getValue().compareTo(e1.getValue()));

        StringBuilder result = new StringBuilder("PageRank 结果 (Top 20):\n");
        int count = 0;
        for (Map.Entry<String, Double> entry : sorted) {
            if (count++ >= 20)
                break;
            result.append(String.format("%s: %.6f\n", entry.getKey(), entry.getValue()));
        }
        showMessage(result.toString());

        // 只有当开关打开时才生成图
        if (generatePageRankGraph) {
            try {
                GraphVisualizer.visualizePageRank(graph, graphImagePath, pageRanks);
                showMessage("PageRank图已更新: " + graphImagePath);

                // 尝试用默认图片查看器打开
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(new File(graphImagePath));
                }
            } catch (IOException e) {
                showMessage("可视化PageRank错误: " + e.getMessage());
            }
        }
    }

    public String randomWalk() {
        if (graph == null || graph.isEmpty())
            return "图未构建或为空";

        Random random = new Random();
        List<String> nodes = new ArrayList<>(graph.getAllNodes());
        if (nodes.isEmpty())
            return "图中无节点";

        String startNode = nodes.get(random.nextInt(nodes.size()));
        List<String> walkPath = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        walkPath.add(startNode);

        String currentNode = startNode;
        while (true) {
            Map<String, Integer> neighbors = graph.getAdjacentNodes(currentNode);
            if (neighbors == null || neighbors.isEmpty()) {
                break; // 无出边，停止
            }

            List<String> neighborList = new ArrayList<>(neighbors.keySet());
            String nextNode = neighborList.get(random.nextInt(neighborList.size()));
            String edge = currentNode + "->" + nextNode;

            if (visitedEdges.contains(edge)) {
                walkPath.add(nextNode);
                break; // 重复边，停止
            }

            visitedEdges.add(edge);
            walkPath.add(nextNode);
            currentNode = nextNode;

            if (isRandomWalking == false) {
                break; // 用户停止
            }
        }

        // 保存到文件
        try (PrintWriter writer = new PrintWriter("random_walk.txt")) {
            writer.println(String.join(" ", walkPath));
        } catch (FileNotFoundException e) {
            return "保存随机游走结果失败: " + e.getMessage();
        }

        return "随机游走路径: " + String.join(" -> ", walkPath) + "\n已保存到 random_walk.txt";
    }

    private void startRandomWalk() {
        if (graph == null) {
            showMessage("请先加载文件构建图");
            return;
        }

        if (isRandomWalking) {
            showMessage("随机游走已在运行");
            return;
        }

        isRandomWalking = true;
        showMessage("随机游走开始... (点击停止按钮结束)");

        randomWalkThread = new Thread(() -> {
            String result = randomWalk();
            SwingUtilities.invokeLater(() -> showMessage(result));
            isRandomWalking = false;
        });
        randomWalkThread.start();
    }

    private void stopRandomWalk() {
        if (!isRandomWalking) {
            showMessage("没有正在进行的随机游走");
            return;
        }

        isRandomWalking = false;
        showMessage("正在停止随机游走...");
    }

    private void showMessage(String message) {
        outputArea.append(message + "\n\n");
        outputArea.setCaretPosition(outputArea.getDocument().getLength());
    }
}

class DirectedGraph {
    private Map<String, Map<String, Integer>> adjacencyList;
    private Map<String, Integer> wordFrequency;

    public DirectedGraph() {
        adjacencyList = new HashMap<>();
        wordFrequency = new HashMap<>();
    }

    public void addEdge(String from, String to) {
        // 更新词频
        wordFrequency.put(from, wordFrequency.getOrDefault(from, 0) + 1);
        wordFrequency.put(to, wordFrequency.getOrDefault(to, 0) + 1);

        // 添加边
        adjacencyList.putIfAbsent(from, new HashMap<>());
        Map<String, Integer> edges = adjacencyList.get(from);
        edges.put(to, edges.getOrDefault(to, 0) + 1);
    }

    public boolean containsNode(String word) {
        return adjacencyList.containsKey(word) ||
                adjacencyList.values().stream().anyMatch(edges -> edges.containsKey(word));
    }

    public List<String> findBridgeWords(String word1, String word2) {
        if (!containsNode(word1))
            return Collections.emptyList();
        if (!containsNode(word2))
            return Collections.emptyList();

        List<String> bridges = new ArrayList<>();
        Map<String, Integer> word1Neighbors = adjacencyList.getOrDefault(word1, Collections.emptyMap());

        for (String potentialBridge : word1Neighbors.keySet()) {
            Map<String, Integer> bridgeNeighbors = adjacencyList.getOrDefault(potentialBridge, Collections.emptyMap());
            if (bridgeNeighbors.containsKey(word2)) {
                bridges.add(potentialBridge);
            }
        }

        return bridges;
    }

    public List<List<String>> calculateShortestPaths(String start, String end) {
        // 使用Dijkstra算法计算最短路径
        Map<String, Integer> distances = new HashMap<>();
        Map<String, List<String>> predecessors = new HashMap<>();
        PriorityQueue<String> queue = new PriorityQueue<>(
                Comparator.comparingInt(node -> distances.getOrDefault(node, Integer.MAX_VALUE)));

        // 初始化
        for (String node : getAllNodes()) {
            distances.put(node, Integer.MAX_VALUE);
            predecessors.put(node, new ArrayList<>());
        }
        distances.put(start, 0);
        queue.add(start);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            if (current.equals(end))
                break;

            Map<String, Integer> neighbors = adjacencyList.getOrDefault(current, Collections.emptyMap());
            for (Map.Entry<String, Integer> neighbor : neighbors.entrySet()) {
                String nextNode = neighbor.getKey();
                int newDist = distances.get(current) + 1; // 这里我们假设边权重为1，简化计算

                if (newDist < distances.get(nextNode)) {
                    distances.put(nextNode, newDist);
                    predecessors.get(nextNode).clear();
                    predecessors.get(nextNode).add(current);
                    queue.add(nextNode);
                } else if (newDist == distances.get(nextNode)) {
                    predecessors.get(nextNode).add(current);
                }
            }
        }

        // 如果不可达
        if (distances.get(end) == Integer.MAX_VALUE) {
            return Collections.emptyList();
        }

        // 回溯所有最短路径
        List<List<String>> paths = new ArrayList<>();
        reconstructPaths(start, end, predecessors, new ArrayList<>(), paths);
        return paths;
    }

    private void reconstructPaths(String start, String current,
                                  Map<String, List<String>> predecessors,
                                  List<String> currentPath,
                                  List<List<String>> paths) {
        currentPath.add(current);

        if (current.equals(start)) {
            List<String> path = new ArrayList<>(currentPath);
            Collections.reverse(path);
            paths.add(path);
        } else {
            for (String predecessor : predecessors.get(current)) {
                reconstructPaths(start, predecessor, predecessors, currentPath, paths);
            }
        }

        currentPath.remove(currentPath.size() - 1);
    }

    public Map<String, List<String>> calculateAllShortestPathsFrom(String start) {
        Map<String, List<String>> allPaths = new HashMap<>();

        for (String node : getAllNodes()) {
            if (!node.equals(start)) {
                List<List<String>> paths = calculateShortestPaths(start, node);
                if (!paths.isEmpty()) {
                    allPaths.put(node, paths.get(0)); // 只取第一条路径
                }
            }
        }

        return allPaths;
    }

    public Map<String, Double> calculatePageRank() {
        // 初始化数据结构
        Set<String> nodes = getAllNodes();
        int nodeCount = nodes.size();
        if (nodeCount == 0) return Collections.emptyMap();

        // 预处理阶段 - 只执行一次
        // 1. 建立节点到索引的映射
        List<String> nodeList = new ArrayList<>(nodes);
        Map<String, Integer> nodeIndexMap = new HashMap<>();
        for (int i = 0; i < nodeList.size(); i++) {
            nodeIndexMap.put(nodeList.get(i), i);
        }

        // 2. 预先计算每个节点的入边列表和出度
        List<List<Integer>> incomingEdges = new ArrayList<>(nodeCount);
        int[] outDegree = new int[nodeCount];
        List<Integer> danglingNodes = new ArrayList<>();

        for (int i = 0; i < nodeCount; i++) {
            incomingEdges.add(new ArrayList<>());
        }

        for (int i = 0; i < nodeCount; i++) {
            String node = nodeList.get(i);
            Map<String, Integer> edges = adjacencyList.getOrDefault(node, Collections.emptyMap());
            outDegree[i] = edges.size();
            if (outDegree[i] == 0) {
                danglingNodes.add(i);
            }

            // 建立入边索引
            for (String to : edges.keySet()) {
                int toIndex = nodeIndexMap.get(to);
                incomingEdges.get(toIndex).add(i);
            }
        }

        // 3. 初始化PageRank值
        double[] pr = new double[nodeCount];
        double initialValue = 1.0 / nodeCount;
        Arrays.fill(pr, initialValue);

        // 迭代参数
        double dampingFactor = 0.85;
        double constantTerm = (1 - dampingFactor) / nodeCount;
        int maxIterations = 100;
        double convergenceThreshold = 0.0001;
        boolean hasConverged = false;

        // 迭代计算
        for (int iter = 0; iter < maxIterations && !hasConverged; iter++) {
            double[] newPr = new double[nodeCount];
            double danglingSum = 0.0;

            // 计算悬挂节点贡献
            for (int i : danglingNodes) {
                danglingSum += pr[i];
            }
            danglingSum /= nodeCount;

            // 计算每个节点的新PR值
            for (int i = 0; i < nodeCount; i++) {
                double sum = 0.0;

                // 遍历所有入边节点
                for (int j : incomingEdges.get(i)) {
                    if (outDegree[j] > 0) {
                        sum += pr[j] / outDegree[j];
                    }
                }

                newPr[i] = constantTerm + dampingFactor * (sum + danglingSum);
            }

            // 检查收敛
            hasConverged = true;
            for (int i = 0; i < nodeCount; i++) {
                if (Math.abs(pr[i] - newPr[i]) > convergenceThreshold) {
                    hasConverged = false;
                    break;
                }
            }

            // 更新PR值
            pr = newPr;
        }

        // 转换为结果Map
        Map<String, Double> result = new HashMap<>(nodeCount);
        for (int i = 0; i < nodeCount; i++) {
            result.put(nodeList.get(i), pr[i]);
        }

        return result;
    }

    public Set<String> getAllNodes() {
        Set<String> nodes = new HashSet<>();
        nodes.addAll(adjacencyList.keySet());
        for (Map<String, Integer> edges : adjacencyList.values()) {
            nodes.addAll(edges.keySet());
        }
        return nodes;
    }

    public Map<String, Integer> getAdjacentNodes(String node) {
        return adjacencyList.getOrDefault(node, Collections.emptyMap());
    }

    public boolean isEmpty() {
        return adjacencyList.isEmpty();
    }
}

class GraphVisualizer {
    public static void visualize(DirectedGraph graph, String outputPath) throws IOException {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=circle];\n");

        // 添加节点
        for (String node : graph.getAllNodes()) {
            dot.append("  \"").append(node).append("\";\n");
        }

        // 添加边
        for (String from : graph.getAllNodes()) {
            Map<String, Integer> edges = graph.getAdjacentNodes(from);
            for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                String to = edge.getKey();
                int weight = edge.getValue();
                dot.append("  \"").append(from).append("\" -> \"").append(to)
                        .append("\" [label=\"").append(weight).append("\"];\n");
            }
        }

        dot.append("}\n");

        // 调用Graphviz生成图像
        generateGraphImage(dot.toString(), outputPath);
    }

    public static void visualizeShortestPaths(DirectedGraph graph, String outputPath,
                                              List<List<String>> paths) throws IOException {
        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=circle];\n");

        // 添加所有节点
        for (String node : graph.getAllNodes()) {
            dot.append("  \"").append(node).append("\";\n");
        }

        // 添加所有边（灰色）
        for (String from : graph.getAllNodes()) {
            Map<String, Integer> edges = graph.getAdjacentNodes(from);
            for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                String to = edge.getKey();
                int weight = edge.getValue();
                dot.append("  \"").append(from).append("\" -> \"").append(to)
                        .append("\" [label=\"").append(weight).append("\", color=\"gray\"];\n");
            }
        }

        // 高亮显示最短路径
        String[] colors = { "red", "blue", "green", "orange", "purple" };
        int colorIndex = 0;

        for (List<String> path : paths) {
            if (path.size() < 2)
                continue;

            String color = colors[colorIndex % colors.length];
            colorIndex++;

            // 高亮路径上的节点
            for (String node : path) {
                dot.append("  \"").append(node).append("\" [color=\"").append(color)
                        .append("\", style=\"filled\", fillcolor=\"").append(color)
                        .append("\", fontcolor=\"white\"];\n");
            }

            // 高亮路径上的边
            for (int i = 0; i < path.size() - 1; i++) {
                String from = path.get(i);
                String to = path.get(i + 1);
                dot.append("  \"").append(from).append("\" -> \"").append(to)
                        .append("\" [color=\"").append(color).append("\", penwidth=2.0];\n");
            }
        }

        dot.append("}\n");

        generateGraphImage(dot.toString(), outputPath);
    }

    public static void visualizePageRank(DirectedGraph graph, String outputPath,
                                         Map<String, Double> pageRanks) throws IOException {
        // 归一化PageRank值用于节点大小
        double maxRank = pageRanks.values().stream().max(Double::compare).orElse(1.0);
        double minRank = pageRanks.values().stream().min(Double::compare).orElse(0.0);

        StringBuilder dot = new StringBuilder();
        dot.append("digraph G {\n");
        dot.append("  rankdir=LR;\n");
        dot.append("  node [shape=circle];\n");

        // 添加节点，大小和颜色基于PageRank
        for (String node : graph.getAllNodes()) {
            double rank = pageRanks.getOrDefault(node, 0.0);
            // 归一化到0.5-2.0之间的值用于节点大小
            double normalizedSize = 0.5 + 1.5 * (rank - minRank) / (maxRank - minRank);
            // 颜色从蓝色(低)到红色(高)
            int colorValue = (int) (255 * (rank - minRank) / (maxRank - minRank));
            String color = String.format("#%02x%02x%02x", colorValue, 0, 255 - colorValue);

            dot.append("  \"").append(node).append("\" [width=").append(normalizedSize)
                    .append(", height=").append(normalizedSize)
                    .append(", style=\"filled\", fillcolor=\"").append(color)
                    .append("\", fontsize=").append(10 + (int) (10 * normalizedSize))
                    .append("];\n");
        }

        // 添加边
        for (String from : graph.getAllNodes()) {
            Map<String, Integer> edges = graph.getAdjacentNodes(from);
            for (Map.Entry<String, Integer> edge : edges.entrySet()) {
                String to = edge.getKey();
                int weight = edge.getValue();
                dot.append("  \"").append(from).append("\" -> \"").append(to)
                        .append("\" [label=\"").append(weight).append("\"];\n");
            }
        }

        dot.append("}\n");

        generateGraphImage(dot.toString(), outputPath);
    }

    private static void generateGraphImage(String dotContent, String outputPath) throws IOException {
        // 检查Graphviz是否安装
        // 显式指定dot.exe的绝对路径
        String dotPath = "C:\\Program Files\\Graphviz\\bin\\dot.exe";

        try {
            // 检查Graphviz是否可调用
            Process process = new ProcessBuilder(dotPath, "-V").start();
            if (process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0) {
                // 生成图像
                ProcessBuilder pb = new ProcessBuilder(
                        dotPath,
                        "-Tpng",
                        "-o", outputPath);
                Process p = pb.start();

                // 写入dot内容
                try (OutputStream out = p.getOutputStream()) {
                    out.write(dotContent.getBytes());
                    out.flush();
                }

                // 等待完成
                if (!p.waitFor(10, TimeUnit.SECONDS)) {
                    throw new IOException("生成图像超时");
                }
            } else {
                throw new IOException("Graphviz调用失败");
            }
        } catch (Exception e) {
            throw new IOException("Graphviz未正确配置: " + e.getMessage());
        }

        // 创建临时.dot文件
        File dotFile = File.createTempFile("graph", ".dot");
        try (PrintWriter writer = new PrintWriter(dotFile)) {
            writer.println(dotContent);
        }

        // 调用dot生成图像
        Process process = Runtime.getRuntime().exec("dot -Tpng " + dotFile.getAbsolutePath() + " -o " + outputPath);
        try {
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                throw new IOException("生成图像失败，Graphviz返回错误代码: " + exitCode);
            }
        } catch (InterruptedException e) {
            throw new IOException("生成图像被中断");
        }

        // 删除临时文件
        dotFile.delete();
    }
}