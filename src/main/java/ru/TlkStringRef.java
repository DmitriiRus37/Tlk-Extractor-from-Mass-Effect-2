package ru;

import java.io.IOException;
import java.io.InputStream;

public class TlkStringRef {
    public int stringId;
    public int bitOffset;

    public String Data;
    public int position;

    public TlkStringRef(InputStream r) throws IOException {
        this.stringId = TlkFile.readInt32(r);
        this.bitOffset = TlkFile.readInt32(r);
    }
}