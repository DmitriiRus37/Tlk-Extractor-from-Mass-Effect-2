import sys
import tlk_file


def main():
    source_path = sys.argv[1]
    dest_path = sys.argv[2]

    tlk = tlk_file.TlkFile()
    tlk_file.TlkFile.load_tlk_data(tlk, source_path)
    tlk_file.TlkFile.store_to_file(tlk, dest_path, 'xml')


main()
