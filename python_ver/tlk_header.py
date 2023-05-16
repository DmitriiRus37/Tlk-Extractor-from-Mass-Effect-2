import input_stream
import tlk_file


class TlkHeader:
    def __init__(self, in_stream):
        self.magic = in_stream.read_int_32()  # TODO ???
        self.ver = in_stream.read_int_32()  # TODO ???
        self.min_ver = in_stream.read_int_32()  # TODO ???
        self.entry_1_count = in_stream.read_int_32()  # TODO ???
        self.entry_2_count = in_stream.read_int_32()  # TODO ???
        self.tree_nodes_count = in_stream.read_int_32()  # count of unique symbols
        self.data_len = in_stream.read_int_32()  # count of bytes of encoded sequence
