import input_stream


class TlkStringRef:
    def __init__(self, in_stream):
        self.string_id = in_stream.read_int_32()
        self.bit_offset = in_stream.read_int_32()
        self.data = None
        self.position = None
