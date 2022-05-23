package ru;

import java.io.IOException;
import java.io.InputStream;

public class TlkHeader {
    public int magic;
    public int ver;
    public int min_ver;
    public int entry1Count;
    public int entry2Count;
    public int treeNodeCount;
    public int dataLen;

    public TlkHeader(InputStream r) throws IOException {
        this.magic = TlkFile.readInt32(r);
        this.ver = TlkFile.readInt32(r);
        this.min_ver = TlkFile.readInt32(r);
        this.entry1Count = TlkFile.readInt32(r);
        this.entry2Count = TlkFile.readInt32(r);
        this.treeNodeCount = TlkFile.readInt32(r);
        this.dataLen = TlkFile.readInt32(r);
    }

}