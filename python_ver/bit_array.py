# A packed array of booleans.
# @author Joshua Bloch
# @author Douglas Hoover
bits_per_unit = 8


def position_big_endian(idx):
    return 1 << (bits_per_unit - 1 - (idx % bits_per_unit))


def subscript(idx) -> int:
    return idx // bits_per_unit


def get_bit(byte_impl, index, endian) -> bool:
    return byte_impl[index % bits_per_unit] if endian == 'big_endian' else byte_impl[8 - 1 - index % bits_per_unit]


class BitArray:
    def __init__(self, **kwargs):
        self.length = None
        self.repn = None
        if 'a' in kwargs:
            # Creates a bit_array of the specified size, initialized from the
            # specified byte array. The most significant bit of a[0] gets
            # index zero in the bit_array. The array 'a' must be large enough
            # to specify a value for every bit in the bit_array. In other words,
            # 8*a.length <= length.
            a = kwargs['a']
            self.length = len(a) * bits_per_unit
            rep_length = (self.length + bits_per_unit - 1) // bits_per_unit
            unused_bits = rep_length * bits_per_unit - self.length
            bit_mask = 0xFF << unused_bits

            # normalize the representation:
            # 1. discard extra bytes
            # 2. zero out extra bits in the last byte
            self.repn = a[0:rep_length]
            if rep_length > 0:
                self.repn[rep_length - 1] &= bit_mask
        elif 'length' in kwargs:
            # Creates a bit_array of the specified size, initialized to zeros.
            length = kwargs['length']
            if length < 0:
                raise Exception('Negative length for BitArray')
            self.length = length
            self.repn = []
        elif 'bits' in kwargs:
            # Create a bit_array whose bits are those of the given array of Booleans.
            bits = kwargs['bits']
            self.length = len(bits)
            self.repn = [0] * ((self.length + 7) // 8)
            [self.set_bit(i, bits[i]) for i in range(self.length)]
        elif 'ba' in kwargs:
            # Copy constructor (for cloning).
            ba = kwargs['ba']
            self.length = len(ba)
            self.repn = ba.repn.clone()

    def get_bit(self, index) -> bool:
        if index < 0 or index >= self.length:
            raise Exception('index < 0 or index >= self.length')
        bit_array = []
        for i in range(8):
            bit_array[8 - 1 - i] = self.repn[subscript(index)] >> i & 0x1 != 0x0
        return get_bit(bit_array, index, 'little_endian')

    # Returns the reversed indexed bit in this bit_array.
    def get_rev(self, index) -> bool:
        if index < 0 or index >= self.length:
            raise Exception('index < 0 or index >= self.length')
        bit_array = [False] * 8
        for i in range(8):
            bit_array[8 - 1 - i] = (self.repn[subscript(index)] >> i & 0x1) != 0x0
        return get_bit(bit_array, index, 'little_endian')

    # Sets the indexed bit in this bit_array.
    def set_bit(self, index: int, value) -> None:
        if index < 0 or index >= self.length:
            raise Exception('index < 0 or index >= self.length')
        idx = subscript(index)
        bit = position_big_endian(index)
        self.repn[idx] = self.repn[idx] | bit if value else self.repn[idx] & ~bit
