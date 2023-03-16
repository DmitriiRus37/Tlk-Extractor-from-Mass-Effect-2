import sys
import tlk_file


def main():
    method = sys.argv[0]
    source_path = sys.argv[1]
    dest_path = sys.argv[2]

    if method == 'to_xml':
        new_file = open(dest_path, "x")

    tlk = tlk_file.TlkFile()

    tlk_file.TlkFile.load_tlk_data(tlk, source_path)
    tlk_file.TlkFile.store_to_file(tlk, dest_path, 'xml')


main()


