import input_stream


class HuffmanNode:
    def __init__(self, stream):
        left_val = stream.read_int_32()
        right_val = stream.read_int_32()
        if left_val >= 2 ** 31:
            left_val -= 2 ** 32
        if right_val >= 2 ** 31:
            right_val -= 2 ** 32
        self.left_node_id = left_val
        self.right_node_id = right_val
