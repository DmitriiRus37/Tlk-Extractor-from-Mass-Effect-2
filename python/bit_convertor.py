def to_int_32(bytes, index):
    if len(bytes) != 4:
        raise Exception('The length of the byte array must be at least 4 bytes long.')
    return (0xff & bytes[index]) << 24 | \
        (0xff & bytes[index + 1]) << 16 | \
        (0xff & bytes[index + 2]) << 8 | \
        (0xff & bytes[index + 3])


def to_int_64(bytes, index):
    if len(bytes) != 4:
        raise Exception('The length of the byte array must be at least 8 bytes long.')
    return (0xff & bytes[index]) << 56 | \
        (0xff & bytes[index + 1]) << 48 | \
        (0xff & bytes[index + 2]) << 40 | \
        (0xff & bytes[index + 3]) << 32 | \
        (0xff & bytes[index + 4]) << 24 | \
        (0xff & bytes[index + 5]) << 16 | \
        (0xff & bytes[index + 6]) << 8 | \
        (0xff & bytes[index + 7])


def get_bytes_by_value(value):
    b = format(value, '32b').replace(' ', '0')
    byte_1 = int(b[:8], 2)
    byte_2 = int(b[8:16], 2)
    byte_3 = int(b[16:24], 2)
    byte_4 = int(b[24:], 2)
    return [byte_4, byte_3, byte_2, byte_1]


def to_char_rev(bytes, index):
    if len(bytes) < 2:
        raise Exception('The length of the byte array must be at least 2 bytes long.')
    buffer = [None] * (len(bytes) // 2)
    for i in range(len(bytes) // 2):
        bytes[i], bytes[len(bytes) - 1 - i] = bytes[len(bytes) - 1 - i], bytes[i]
    for i in range(len(buffer)):
        bpos = i << 1
        c = (((bytes[bpos] & 0x00FF) << 8) + (bytes[bpos + 1] & 0x00FF))
        buffer[i] = chr(c)
    count_of_chars = len(buffer)
    return buffer[count_of_chars - 1 - index]


def get_bytes(x):
    if isinstance(x, int):
        return [x >> 24, x >> 16, x >> 8, x]
    elif isinstance(x, long):
        return [x >> 56, x >> 48, x >> 40, x >> 32, x >> 24, x >> 16, x >> 8, x]
