import java.io.FileInputStream;
import java.io.IOException;

public class HuffmanNode {
    public int leftNodeId;
    public int rightNodeId;

    public HuffmanNode(FileInputStream r) throws IOException {
        this.leftNodeId = r.read();
        this.rightNodeId = r.read();
    }
}