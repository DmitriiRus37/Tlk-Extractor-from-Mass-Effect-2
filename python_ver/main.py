import os
import sys
import tlk_file
import huffman_compression


def main():
    method = sys.argv[1]
    source_path = os.path.abspath(sys.argv[2])
    dest_path = os.path.abspath(sys.argv[3])

    if method.lower() == 'to_tlk':
        hc = huffman_compression.HuffmanCompression()
        hc.load_input_data(source_path, 'xml')
        hc.save_to_tlk_file(dest_path)
    else:
        tlk = tlk_file.TlkFile()
        tlk_file.TlkFile.load_tlk_data(tlk, source_path)
        tlk_file.TlkFile.store_to_file(tlk, dest_path, method)


main()
