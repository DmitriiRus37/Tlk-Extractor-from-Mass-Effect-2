import input_stream


class TlkStringRef:
    def __init__(self, in_stream):
        self.string_id = input_stream.read_int_32(in_stream)
        self.bit_offset = input_stream.read_int_32(in_stream)
        self.data = None
        self.position = None
