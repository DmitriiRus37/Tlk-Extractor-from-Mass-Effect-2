import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TlkFile  {

    TlkHeader header;
    public List<TlkStringRef> stringRefs;
    List<HuffmanNode> characterTree;
    BitArray bits;

    private enum FileFormat {
        txt,
        csv,
        xml
    }

    public void ProgressChangedEventHandler(int percentProgress);
    public Event progressChanged;

    private void OnProgressChanged(int percentProgress)
    {
        ProgressChangedEventHandler handler = ProgressChanged;
        if (handler != null)
            handler(percentProgress);
    }

    /// <summary>
    /// Loads a TLK file into memory.
    /// </summary>
    /// <param name="fileName"></param>
    public void LoadTlkData(String fileName, boolean isPC) throws IOException {
        /* **************** STEP ONE ****************
         *          -- load TLK file header --
         *
         * reading first 28 (4 * 7) bytes
         */

        /* using LittleEndian for PC architecture and BigEndian for Xbox360 */
        MiscUtil.Conversion.EndianBitConverter bitConverter;
        if (isPC)
            bitConverter = new MiscUtil.Conversion.LittleEndianBitConverter();
        else
            bitConverter = new MiscUtil.Conversion.BigEndianBitConverter();
        FileInputStream r = new MiscUtil.IO.EndianBinaryReader(bitConverter, File.OpenRead(fileName));

        header = new TlkHeader(r);
        /* read possibly correct ME2 TLK file, but from another platfrom */
        if (header.magic == 1416391424)
            throw new RuntimeException();
        /* read definately NOT a ME2 TLK ile */
        if (header.magic != 7040084)
            throw new RuntimeException();

        //DebugTools.PrintHeader(Header);

        /* **************** STEP TWO ****************
         *  -- read and store Huffman Tree nodes --
         */
        /* jumping to the beginning of Huffmann Tree stored in TLK file */
        long pos = r.BaseStream.Position;
        r.BaseStream.Seek(pos + (header.entry1Count + header.entry2Count) * 8, SeekOrigin.Begin);

        characterTree = new LinkedList<>();
        for (int i = 0; i < header.treeNodeCount; i++)
            characterTree.add(new HuffmanNode(r));

        /* **************** STEP THREE ****************
         *  -- read all of coded data into memory --
         */
        byte[] data = new byte[header.dataLen];
        r.BaseStream.Read(data, 0, data.length);
        /* and store it as raw bits for further processing */
        bits = new BitArray(data.length * BitArray.BITS_PER_UNIT, data);

        /* rewind BinaryReader just after the Header
         * at the beginning of TLK Entries data */
        r.BaseStream.Seek(pos, SeekOrigin.Begin);

        /* **************** STEP FOUR ****************
         * -- decode (basing on Huffman Tree) raw bits data into actual strings --
         * and store them in a Dictionary<int, string> where:
         *   int: bit offset of the beginning of data (offset starting at 0 and counted for Bits array)
         *        so offset == 0 means the first bit in Bits array
         *   string: actual decoded String */
        Map<Integer, String> rawStrings = new HashMap<>();
        int offset = 0;
        while (offset < bits.length())
        {
            int key = offset;
            /* read the String and update 'offset' variable to store NEXT String offset */
            String s = GetString(offset);
            rawStrings.put(key, s);
        }

        /* **************** STEP FIVE ****************
         *         -- bind data to String IDs --
         * go through Entries in TLK file and read it's String ID and offset
         * then check if offset is a key in rawStrings and if it is, then bind data.
         * Sometimes there's no such key, in that case, our String ID is probably a substring
         * of another String present in rawStrings.
         */
        stringRefs = new LinkedList<>();
        for (int i = 0; i < header.entry1Count + header.entry2Count; i++) {
            TlkStringRef sref = new TlkStringRef(r);
            sref.position = i;
            if (sref.BitOffset >= 0)
            {
                if (!rawStrings.containsKey(sref.BitOffset))
                {
                    int tmpOffset = sref.BitOffset;
                    String partString = GetString(tmpOffset);

                    /* actually, it should store the fullString and subStringOffset,
                     * but as we don't have to use this compression feature,
                     * we will store only the part of String we need */

                    /* int key = rawStrings.Keys.Last(c => c < sref.BitOffset);
                     * String fullString = rawStrings[key];
                     * int subStringOffset = fullString.LastIndexOf(partString);
                     * sref.StartOfString = subStringOffset;
                     * sref.Data = fullString;
                     */
                    sref.Data = partString;
                }
                else
                {
                    sref.Data = rawStrings.get(sref.BitOffset);
                }
            }
            stringRefs.add(sref);
        }
        r.close();
    }

    /** <summary>
     *      Writes data stored in memory to an appriopriate text format.
     *  </summary>
     *  <param name="fileName"></param>
     *  <param name="ff"></param>
     */
    public void DumpToFile(String fileName, FileFormat ff)
    {
        File.Delete(fileName);
        /* for now, it's better not to sort, to preserve original order */
        // StringRefs.Sort(CompareTlkStringRef);

        if (ff.equals(FileFormat.xml)) {
            SaveToXmlFile(fileName);
        } else {
            SaveToTextFile(fileName);
        }
    }

    /** <summary>
     *      Starts reading 'Bits' array at position 'bitOffset'. Read data is
     *      used on a Huffman Tree to decode read bits into real strings.
     *      'bitOffset' variable is updated with last read bit PLUS ONE (first unread bit).
     *  </summary>
     *  <param name="bitOffset"></param>
     *  <returns>
     *      decoded String or null if there's an error (last string's bit code is incomplete)
     *  </returns>
     *  <remarks>
     *      Global variables used:
     *      List(of HuffmanNodes) CharacterTree
     *      BitArray Bits
     *  </remarks>
     **/
    private String GetString(Integer bitOffset)
    {
        HuffmanNode root = characterTree.get(0);
        HuffmanNode curNode = root;

        String curString = "";
        int i;
        for (i = bitOffset; i < bits.length(); i++)
        {
            /* reading bits' sequence and decoding it to Strings while traversing Huffman Tree */
            int nextNodeID;
            if (bits.get(i)) {
                nextNodeID = curNode.RightNodeID;
            } else {
                nextNodeID = curNode.LeftNodeID;
            }

            if (nextNodeID >= 0) {
                /* it's an internal node - keep looking for a leaf */
                curNode = characterTree.get(nextNodeID);
            } else {
                /* it's a leaf! */
                char c = BitConverter.ToChar(BitConverter.GetBytes(0xffff - nextNodeID), 0);
                if (c != '\0') {
                    /* it's not NULL */
                    curString += c;
                    curNode = root;
                } else {
                    /* it's a NULL terminating processed string, we're done */
                    bitOffset = i + 1;
                    return curString;
                }
            }
        }
        bitOffset = i + 1;
        return null;
    }

   /** <summary>
    *       Writing data in an XML format.
    *  </summary>
    * <param name="fileName"></param>
    * private void SaveToXmlFile(String fileName)
    */
    {
        int totalCount = stringRefs.size();
        int count = 0;
        int lastProgress = -1;
        XmlTextWriter xr = new XmlTextWriter(fileName, Encoding.UTF8);
        xr.Formatting = Formatting.Indented;
        xr.Indentation = 4;

        xr.WriteStartDocument();
        xr.WriteStartElement("tlkFile");
        xr.WriteAttributeString("TLKToolVersion", App.GetVersion());

        xr.WriteComment("Male entries section begin (ends at position " + (Header.entry1Count - 1) + ")");

        for (TlkStringRef s : stringRefs) {
            if (s.position == Header.entry1Count)
            {
                xr.WriteComment("Male entries section end");
                xr.WriteComment("Female entries section begin (ends at position " + (Header.entry1Count + Header.entry2Count - 1) + ")");
            }

            xr.WriteStartElement("string");

            xr.WriteStartElement("id");
            xr.WriteValue(s.StringID);
            xr.WriteEndElement(); // </id>

            xr.WriteStartElement("position");
            xr.WriteValue(s.position);
            xr.WriteEndElement(); // </position>

            if (s.BitOffset < 0)
                xr.WriteElementString("data", "-1");
            else
                xr.WriteElementString("data", s.Data);

            xr.WriteEndElement(); // </string>

            int progress = (++count * 100) / totalCount;
            if (progress > lastProgress)
            {
                lastProgress = progress;
                OnProgressChanged(lastProgress);
            }
        }
        xr.WriteComment("Female entries section end");
        xr.WriteEndElement(); // </tlkFile>
        xr.Flush();
        xr.Close();
    }

    /** <summary>
     *      Writing data in a normal text format.
     * </summary>
     * <remarks>
     *      Currently not used by main application, but it works ok.
     * </remarks>
     * <param name="fileName"></param>
     */
    private void SaveToTextFile(String fileName)
    {
        int totalCount = stringRefs.size();
        int count = 0;
        int lastProgress = -1;

        for (TlkStringRef s : stringRefs) {
            String line = s.stringId + ": " + s.Data + "\r\n";

            try (FileWriter fw = new FileWriter(fileName, true)) {
                fw.write(line);
            } catch (IOException e) {
                e.printStackTrace();
            }

            int progress = (++count * 100) / totalCount;
            if (progress > lastProgress) {
                lastProgress = progress;
                OnProgressChanged(lastProgress);
            }
        }
    }

//        /* for sorting */
//        private static int CompareTlkStringRef(TlkHeader.TlkStringRef strRef1, TlkHeader.TlkStringRef strRef2) {
//            int result = strRef1.stringId.compareTo(strRef2.stringId);
//            return result;
//        }
}
}
