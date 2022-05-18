import java.io.FileInputStream;
import java.io.IOException;

public class TlkStringRef {
    public int stringId;
    public int BitOffset;

    public String Data;
    public int position;

    public TlkStringRef(FileInputStream r) throws IOException {
        this.stringId = r.read();
        this.BitOffset = r.read();
    }
}