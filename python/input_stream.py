class InputStream:
    def __init__(self, *args):
        if len(args) == 1:
            with open(args[0], "rb") as f:
                bytes_read = f.read()
                self.bytes = tuple(bytes_read)
            self.pos = 0
        elif len(args) == 2:
            with open(args[1], "rb") as f:
                bytes_read = f.read()
                self.bytes = tuple(bytes_read)
            self.pos = args[0]

    def read_to_array(self, b, off, length):
        if b == None:
            raise Exception('b == None')
        elif off < 0 or length < 0 or length > len(b) - off:
            raise Exception('off < 0 or len < 0 or len > len(b) - off')
        elif length == 0:
            return 0

        c = read_byte(self)
        if c == -1:
            return -1
        b[off] = c

        counter = 1
        try:
            for i in range(1, length):
                c = read_byte(self)
                if c == -1:
                    break
                b[off + i] = c
                counter += 1
        except:
            Exception('')
        return counter


def read_int_32(input_stream):
    ch1 = read_byte(input_stream)
    ch2 = read_byte(input_stream)
    ch3 = read_byte(input_stream)
    ch4 = read_byte(input_stream)
    if (ch1 | ch2 | ch3 | ch4) < 0:
        raise Exception('(ch1 | ch2 | ch3 | ch4) < 0')
    return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1


def read_byte(input_stream):
    ch = input_stream.bytes[input_stream.pos]
    input_stream.pos = input_stream.pos + 1
    return ch
