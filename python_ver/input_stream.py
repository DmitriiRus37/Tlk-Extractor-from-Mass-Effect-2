class InputStream:
    def __init__(self, *args):
        self.pos = None  # pos in bytes
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

    def read_to_array(self, arr: list, offset: int, length: int):
        if length > len(arr) - offset:
            raise Exception('length > len(b) - offset')
        elif length == 0:
            return 0

        c = self.read_byte()
        if c == -1:
            return -1
        arr[offset] = c

        counter = 1
        try:
            for i in range(1, length):
                c = self.read_byte()
                if c == -1:
                    break
                arr[offset + i] = c
                counter += 1
        except:
            Exception('exception')
        return counter


    def read_int_32(self):
        ch1 = self.read_byte()
        ch2 = self.read_byte()
        ch3 = self.read_byte()
        ch4 = self.read_byte()
        if (ch1 | ch2 | ch3 | ch4) < 0:
            raise Exception('(ch1 | ch2 | ch3 | ch4) < 0')
        return (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + ch1


    def read_byte(self):
        ch = self.bytes[self.pos]
        self.pos = self.pos + 1
        return ch
