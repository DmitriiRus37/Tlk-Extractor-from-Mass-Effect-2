class HuffmanNode:
    def __init__(self, **kwargs):
        if 'd' in kwargs and 'freq' in kwargs:
            self.letter = kwargs['d']
            self.frequency_count = kwargs['freq']
            self.left = None
            self.right = None
        elif 'left' in kwargs and 'right' in kwargs:
            self.left = kwargs['left']
            self.right = kwargs['right']
            self.frequency_count = self.left.frequency_count + self.right.frequency_count
        elif 'stream' in kwargs:
            left_val = kwargs['stream'].read_int_32()
            right_val = kwargs['stream'].read_int_32()
            if left_val >= 2 ** 31:
                left_val -= 2 ** 32
            if right_val >= 2 ** 31:
                right_val -= 2 ** 32
            self.left_node_id = left_val
            self.right_node_id = right_val
