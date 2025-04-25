package ru;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

class HuffmanCompression {
    private String inputFileVersion = "1.0.0.0";
    private final List<TlkEntry> inputData = new LinkedList<>();
    private final Map<Character, Integer> frequencyCount = new HashMap<>();
    private final List<HuffmanNode> huffmanTree = new LinkedList<>();
    private final Map<Character, BitArray> huffmanCodes = new HashMap<>();

    private static class TlkEntry implements Comparable {
        public int stringID;
        public int position;
        public String data;

        public TlkEntry(int stringID, int position, String data) {
            this.stringID = stringID;
            this.position = position;
            this.data = data;
        }

        @Override
        public int compareTo(Object o) {
            return Integer.compare(this.position, ((TlkEntry) o).position);
        }

    }

    private static class HuffmanNode {
        public char data;
        public int frequencyCount;
        public HuffmanNode left;
        public HuffmanNode right;
        public int id;

        public HuffmanNode(char d, int freq) {
            data = d;
            frequencyCount = freq;
        }

        public HuffmanNode(HuffmanNode left, HuffmanNode right) {
            frequencyCount = left.frequencyCount + right.frequencyCount;
            this.left = left;
            this.right = right;
        }
    }

    /** <summary>
     *      Loads a file into memory and prepares for compressing it to TLK
     * </summary>
     * <param name="fileName"></param>
     * <param name="ff"></param>
     * <param name="debugVersion"></param>
     */
    public void loadInputData(String fileName, FileFormat ff, boolean debugVersion) throws IOException, ParserConfigurationException, SAXException {
        inputData.clear();
        LoadXmlInputData(fileName, debugVersion);
        Collections.sort(inputData);
        PrepareHuffmanCoding();
    }

    /** <summary>
     *       Dumps data from memory to TLK compressed file format.
     *       <remarks>
     *           Compressed data should be read into memory first, by LoadInputData method.
     *       </remarks>
     *  </summary>
     *  <param name="fileName"></param>
     *  <param name="isPC"></param>
     */
    public void saveToTlkFile(String fileName, boolean isPC) throws IOException {
        Files.deleteIfExists(Paths.get(fileName));

        /* converts Huffmann Tree to binary form */
        List<Integer> treeBuffer = ConvertHuffmanTreeToBuffer();

        /* preparing data and entries for writing to file
         * entries list consists of pairs <String ID, Offset> */
        List<BitArray> binaryData = new LinkedList<>();
        Map<Integer, Integer> entries1 = new HashMap<>();
        Map<Integer, Integer> entries2 = new HashMap<>();
        int offset = 0;

        for (TlkEntry entry : inputData) {
            if (entry.stringID < 0) {
                if (!entries1.containsKey(entry.stringID)) {
                    entries1.put(entry.stringID, Integer.parseInt(entry.data));
                } else {
                    entries2.put(entry.stringID, Integer.parseInt(entry.data));
                }
                continue;
            }

            if (!entries1.containsKey(entry.stringID)) {
                entries1.put(entry.stringID, offset);
            } else {
                entries2.put(entry.stringID, offset);
            }

            /* for every character in a string, put it's binary code into data array */
            for (char c : entry.data.toCharArray()) {
                binaryData.add(huffmanCodes.get(c));
                offset += huffmanCodes.get(c).length();
            }
        }

        /* preparing TLK Header */
        int magic = 7040084;
        int ver = 3;
        int min_ver = 2;
        int entry1Count = entries1.size();
        int entry2Count = entries2.size();
        int treeNodeCount = treeBuffer.size() / 2;
        int dataLength = offset / 8;
        if (offset % 8 > 0) {
            ++dataLength;
        }

        /* using LittleEndian for PC architecture and BigEndian for Xbox360 */
//        MiscUtil.Conversion.EndianBitConverter bitConverter;
//        if (isPC)
//            bitConverter = new MiscUtil.Conversion.LittleEndianBitConverter();
//        else
//            bitConverter = new MiscUtil.Conversion.BigEndianBitConverter();

        OutputStream bw = new FileOutputStream(fileName);

//        var bw = new MiscUtil.IO.EndianBinaryWriter(bitConverter, File.OpenWrite(fileName));

        /* writing TLK Header */
        bw.write(magic);
        bw.write(ver);
        bw.write(min_ver);
        bw.write(entry1Count);
        bw.write(entry2Count);
        bw.write(treeNodeCount);
        bw.write(dataLength);

        /* writing entries */
        for (Map.Entry<Integer, Integer> entry : entries1.entrySet()) {
            bw.write(entry.getKey());
            bw.write(entry.getValue());
        }
        for (Map.Entry<Integer, Integer> entry : entries2.entrySet()) {
            bw.write(entry.getKey());
            bw.write(entry.getValue());
        }

        /* writing HuffmanTree */
        for (int element : treeBuffer) {
            bw.write(element);
        }
        /* writing data */
        byte[] data = BitArrayListToByteArray(binaryData, offset);
        bw.write(data);
        bw.close();
    }

    /** <summary>
     *        Loads data from XML file into memory
     *  </summary>
     *  <param name="fileName"></param>
     *  <param name="debugVersion"></param>
     */
    private void LoadXmlInputData(String fileName, boolean debugVersion) throws IOException, SAXException, ParserConfigurationException {
        // Получение фабрики, чтобы после получить билдер документов.
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        // Получили из фабрики билдер, который парсит XML, создает структуру Document в виде иерархического дерева.
        DocumentBuilder builder = factory.newDocumentBuilder();
        // Запарсили XML, создав структуру Document. Теперь у нас есть доступ ко всем элементам, каким нам нужно.
        Document document = builder.parse(new File(fileName));
        // Получение списка всех элементов tlkFile внутри корневого элемента (getDocumentElement возвращает ROOT элемент XML файла).
        NodeList tlkFileElements = document.getDocumentElement().getElementsByTagName("tlkFile");
        /* read and store TLK Tool version, which was used to create the XML file */
        String toolVersion = String.valueOf(document.getDocumentElement().getAttributes().getNamedItem("TLKToolVersion").getNodeValue());
        if (toolVersion != null) {
            inputFileVersion = toolVersion;
        }

        // Получение списка всех элементов string внутри корневого элемента.
        NodeList stringElements = document.getDocumentElement().getElementsByTagName("string");
        for (int i=0; i< stringElements.getLength(); i++) {
            int id = 0, position = 0;
            String data = "";
            NodeList childNodes = stringElements.item(i).getChildNodes();

            for (int j=0; j<childNodes.getLength(); j++) {
                if (childNodes.item(j).getNodeName().equals("id")) {
                    id = Integer.parseInt(childNodes.item(j).getTextContent());
                } else if (childNodes.item(j).getNodeName().equals("position")) {
                    position = Integer.parseInt(childNodes.item(j).getTextContent());
                } else if (childNodes.item(j).getNodeName().equals("data")) {
                    data = childNodes.item(j).getTextContent();
                }
            }
            data = data.replace("\r\n", "\n");
            /* every String should be NULL-terminated */
            if (id >= 0) {
                data += '\0';
            }
            /* only add debug info if we are in debug mode and StringID is positive AND it's localizable */
            inputData.add(id >= 0 && debugVersion && (id & 0x8000000) != 0x8000000 ?
                    new TlkEntry(id, position, "(#" + id + ") " + data) :
                    new TlkEntry(id, position, data)
            );
        }

        /* code for XML files created BEFORE v. 1.0.3 */
        String lastEntryFixVersion = "1.0.3";

        /* check if someone isn't loading the bugged version < 1.0.3 */
        if (compareVersionStrings(inputFileVersion, lastEntryFixVersion, "<")) {
            throw new RuntimeException();
        }
    }

    private static boolean compareVersionStrings(String ver1, String ver2, String sign) {
        String[] arr1 = ver1.split("\\.");
        String[] arr2 = ver2.split("\\.");
        int len = Math.max(arr1.length, arr2.length);
        int[] arr1New = new int[len];
        int[] arr2New = new int[len];

        for (int i=0; i<len; i++) {
            arr1New[i] = arr1.length > i ? Integer.parseInt(arr1[i]) : 0;
            arr2New[i] = arr2.length > i ? Integer.parseInt(arr2[i]) : 0;
        }

        if ("<".equals(sign)) {
            for (int i = 0; i < len; i++) {
                if (arr1New[i] > arr2New[i]) {
                    return false;
                } else if (arr1New[i] < arr2New[i]) {
                    return true;
                }
            }
            return false;
        } else if (">".equals(sign)) {
            for (int i = 0; i < len; i++) {
                if (arr1New[i] > arr2New[i]) {
                    return true;
                } else if (arr1New[i] < arr2New[i]) {
                    return false;
                }
            }
            return false;
        } else if ("=".equals(sign)) {
            for (int i = 0; i < len; i++) {
                if (arr1New[i] > arr2New[i]) {
                    return false;
                } else if (arr1New[i] < arr2New[i]) {
                    return false;
                }
            }
            return true;
        }
        throw new RuntimeException("Set valid sign( '<' '>' '=' )");
    }

    /* maybe will be finished in the future */
//    private void LoadTxtInputData(String fileName) throws FileNotFoundException, UnsupportedEncodingException {
//
//        BufferedReader streamReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
//        String line;
//        int i = 1;
//
//        while (streamReader.Peek() != -1) {
//            line = streamReader.readLine();
//            System.out.println(i++);
//            System.out.println(line);
//            String[] words = line.split(":");
//            System.out.println(words.length+" words in text:");
//            for (String s : words) {
//                System.out.println(s + " | ");
//            }
//            System.out.println();
//        }
//        streamReader.close();
//    }

    /** <summary>
     *       Creates Huffman Tree based on data from memory.
     *       For every character in text data, a corresponding Huffman Code is prepared.
     *       Source: http://en.wikipedia.org/wiki/Huffman_coding
     *   </summary>
     */
    private void PrepareHuffmanCoding() {
        frequencyCount.clear();
        for (TlkEntry entry : inputData) {
            if (entry.stringID < 0) {
                continue;
            }
            for (char c : entry.data.toCharArray()) {
                if (!frequencyCount.containsKey(c)) {
                    frequencyCount.put(c, 0);
                }
                frequencyCount.put(c, frequencyCount.get(c) + 1);
            }
        }
        frequencyCount.forEach((key, value) -> huffmanTree.add(new HuffmanNode(key, value)));
        BuildHuffmanTree();
        BuildCodingArray();
        // DebugTools.LoadHuffmanTree(_huffmanCodes);
        // DebugTools.PrintLookupTable();
    }

    /** <summary>
     *       Standard implementation of builidng a Huffman Tree
     * </summary>
     */
    private void BuildHuffmanTree() {
        while (huffmanTree.size() > 1) {
            /* sort Huffman Nodes by frequency */
            huffmanTree.sort(HuffmanCompression::CompareNodes);
            HuffmanNode parent = new HuffmanNode(huffmanTree.get(0), huffmanTree.get(1));
            huffmanTree.remove(0);
            huffmanTree.remove(0);
            huffmanTree.add(parent);
        }
    }

    /** <summary>
     *       Using Huffman Tree (created with BuildHuffmanTree method), generates a binary code for every character.
     *   </summary>
     */
    private void BuildCodingArray() {
        /* stores a binary code */
        List<Boolean> currentCode = new LinkedList<>();
        HuffmanNode currenNode = huffmanTree.get(0);
        TraverseHuffmanTree(currenNode, currentCode);
    }

    /** <summary>
     *       Recursively traverses Huffman Tree and generates codes
     *   </summary>
     *   <param name="node"></param>
     *   <param name="code"></param>
     */
    private void TraverseHuffmanTree(HuffmanNode node, List<Boolean> code) {
        /* check if both sons are null */
        if (node.left == node.right) {
            boolean[] arr = new boolean[code.size()];
            for (int i = 0; i < code.size(); i++) {
                arr[i] = code.get(i);
            }
            BitArray ba = new BitArray(arr);
            huffmanCodes.put(node.data, ba);
        } else {
            /* adds 0 to the code - process left son*/
            code.add(false);
            TraverseHuffmanTree(node.left, code);
            code.remove(code.size() - 1);

            /* adds 1 to the code - process right son*/
            code.add(true);
            TraverseHuffmanTree(node.right, code);
            code.remove(code.size() - 1);
        }
    }

    /** <summary>
     *       Converts a Huffman Tree to it's binary representation used by TLK format of Mass Effect 2.
     *   </summary>
     *   <returns></returns>
     */
    private List<Integer> ConvertHuffmanTreeToBuffer() {
        Queue<HuffmanNode> q = new ArrayDeque<>();
        Map<Integer, HuffmanNode> indices = new HashMap<>();

        int index = 0;
        q.add(huffmanTree.get(0));

        while (q.size() > 0) {
            HuffmanNode node = q.remove();
            /* if it's a leaf - set it's ID to reflect char data the node contains */
            if (node.left == node.right) {
                /* store the char data */
                node.id = -1 - node.data;

                /* that's how it's going to be decoded when parsing TLK file:
                 * char c = ru.BitConverter.ToChar(ru.BitConverter.GetBytes(0xffff - node.ID), 0); */
            } else {
                node.id = index++;
                indices.put(node.id, node);
            }
            if (node.right != null)
                q.add(node.right);
            if (node.left != null)
                q.add(node.left);
        }

        List<Integer> output = new LinkedList<>();

        indices.values().forEach(node -> {
            output.add(node.left.id);
            output.add(node.right.id);
        });
        return output;
    }

    /** <summary>
     *       Converts bits in a main.java.ru.BitArray to an array with bytes.
     *       Such array is ready to be written to a file.
     *   </summary>
     *   <param name="bitsList"></param>
     *   <param name="bitsCount"></param>
     *   <returns></returns>
     */
    private static byte[] BitArrayListToByteArray(List<BitArray> bitsList, int bitsCount) {
        final int BITSPERBYTE = 8;

        int bytesize = bitsCount / BITSPERBYTE;
        if (bitsCount % BITSPERBYTE > 0)
            bytesize++;

        byte[] bytes = new byte[bytesize];
        int bytepos = 0;
        int bitsRead = 0;
        byte value = 0;
        byte significance = 1;

        for (BitArray bits : bitsList) {
            int bitpos = 0;

            while (bitpos < bits.length()) {
                if (bits.get(bitpos)) {
                    value += significance;
                }
                ++bitpos;
                ++bitsRead;
                if (bitsRead % BITSPERBYTE == 0) {
                    bytes[bytepos] = value;
                    ++bytepos;
                    value = 0;
                    significance = 1;
                    bitsRead = 0;
                }
                else {
                    significance <<= 1;
                }
            }
        }
        if (bitsRead % BITSPERBYTE != 0) {
            bytes[bytepos] = value;
        }
        return bytes;
    }

    /** <summary>
     *      For sorting Huffman Nodes
     *  </summary>
     *  <param name="l1"></param>
     *  <param name="l2"></param>
     *  <returns></returns>
     */
    private static int CompareNodes(HuffmanNode l1, HuffmanNode l2) {
        return Integer.compare(l1.frequencyCount, l2.frequencyCount);
    }
}