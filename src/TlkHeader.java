import java.io.FileInputStream;
import java.io.IOException;

public class TlkHeader {
    public int magic;
    public int ver;
    public int min_ver;
    public int entry1Count;
    public int entry2Count;
    public int treeNodeCount;
    public int dataLen;

    public TlkHeader(FileInputStream r) throws IOException {
        this.magic = r.read();
        this.ver = r.read();
        this.min_ver = r.read();
        this.entry1Count = r.read();
        this.entry2Count = r.read();
        this.treeNodeCount = r.read();
        this.dataLen = r.read();
    }
}