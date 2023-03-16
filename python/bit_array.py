bits_per_unit = 8


def position_big_endian(idx):
    return 1 << (bits_per_unit - 1 - (idx % bits_per_unit))


def subscript(idx):
    return idx // bits_per_unit


def get_bit(byte_impl, index, endian):
    return byte_impl[index % bits_per_unit] if endian == 'big_endian' else byte_impl[8 - 1 - index % bits_per_unit]


class BitArray:
    def __init__(self, **kwargs):
        self.length = None
        self.repn = None
        if 'a' in kwargs:
            a = kwargs['a']
            self.length = len(a) * bits_per_unit
            rep_length = (self.length + bits_per_unit - 1) // bits_per_unit
            unused_bits = rep_length * bits_per_unit - self.length
            bit_mask = 0xFF << unused_bits
            self.repn = a[0:rep_length]
            if rep_length > 0:
                self.repn[rep_length - 1] &= bit_mask
        elif 'length' in kwargs:
            if length < 0:
                raise Exception('Negative length for BitArray')
            self.length = length
            self.repn = []
        elif 'bits' in kwargs:
            self.length = len(bits)
            self.repn = []
            for i in range(self.length):
                self.set_bit(i, bits[i])
        elif 'ba' in kwargs:
            self.length = len(ba)
            self.repn = ba.repn.clone()

    def get_bit(self, index):
        if index < 0 or index >= self.length:
            raise Exception('index < 0 or index >= self.length')
        bit_array = []
        for i in range(8):
            bit_array[8 - 1 - i] = self.repn[subscript(index)] >> i & 0x1 != 0x0
        return get_bit(bit_array, index, 'little_endian')

    def get_rev(self, index):
        if index < 0 or index >= self.length:
            raise Exception('index < 0 or index >= self.length')
        bit_array = [None] * 8
        for i in range(8):
            bit_array[8 - 1 - i] = (self.repn[subscript(index)] >> i & 0x1) != 0x0
        return get_bit(bit_array, index, 'little_endian')

    def set_bit(self, index, value):
        if index < 0 or index >= self.length:
            raise Exception('index < 0 or index >= self.length')
        idx = subscript(index)
        bit = position_big_endian(index)
        self.repn[idx] = self.repn[idx] | bit if value else self.repn[idx] & ~bit
