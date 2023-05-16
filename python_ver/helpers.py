def compare_version_strings(ver1: str, ver2: str, sign: str):
    arr1 = [int(x) for x in ver1.split('.')]
    arr2 = [int(x) for x in ver2.split('.')]
    length = max(len(arr1), len(arr2))

    arr1_new = []
    arr2_new = []

    for i in range(length):
        arr1_new.append(int(arr1[i]) if len(arr1) > i else 0)
        arr2_new.append(int(arr2[i]) if len(arr2) > i else 0)

    if sign == '<':
        for i in range(length):
            if arr1_new[i] > arr2_new[i]:
                return False
            elif arr1_new[i] < arr2_new[i]:
                return True
    elif sign == '>':
        for i in range(length):
            if arr1_new[i] > arr2_new[i]:
                return True
            elif arr1_new[i] < arr2_new[i]:
                return False
    elif sign == '=':
        for i in range(length):
            if arr1_new[i] > arr2_new[i] or arr1_new[i] < arr2_new[i]:
                return False
            return True
    raise Exception("Set valid sign( '<' '>' '=' )")


def bit_array_list_to_byte_array(bits_list: list, bits_count: int) -> str:
    bits_per_byte = 8
    byte_size = bits_count // bits_per_byte

    if bits_count % bits_per_byte > 0:
        byte_size += 1

    bytes_list = [0] * byte_size
    byte_pos = 0
    bits_read = 0
    value = 0
    significance = 1

    for bits in bits_list:
        bit_pos = 0

        while bit_pos < len(bits):
            if bits[bit_pos]:
                value += significance
            bit_pos += 1
            bits_read += 1

            if bits_read % bits_per_byte == 0:
                bytes_list[byte_pos] = value
                byte_pos += 1
                value = 0
                significance = 0
                bits_read = 0
            else:
                significance = significance << 1
    if bits_read % bits_per_byte != 0:
        bytes_list[byte_pos] = value
    return bytes_list
