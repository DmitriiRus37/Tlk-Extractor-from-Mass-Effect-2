package ru;

import java.io.IOException;
import java.io.InputStream;

public class HuffmanNode {
    public int leftNodeId;
    public int rightNodeId;

    public HuffmanNode(InputStream r) throws IOException {
        this.leftNodeId = TlkFile.readInt32(r);
        this.rightNodeId = TlkFile.readInt32(r);
    }
}