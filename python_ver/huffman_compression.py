from xml.etree import ElementTree as ET


class HuffmanCompression:
    def __init__(self):
        self.input_file_version = '1.0.0.0'
        self.input_data = []
        self.frequency_count = {}
        self.huffman_tree = []
        self.huffman_codes = {}

    # Loads a file into memory and prepares for compressing it to TLK
    def load_input_data(self, file_name: str, ff: str):
        self.input_data = []
        self.load_xml_input_data(file_name)
        sorted(self.input_data, key=lambda tlk_entry: tlk_entry.position)
        self.prepare_huffman_coding()

    def prepare_huffman_coding(self):
        self.frequency_count = {}
        for entry in self.input_data:
            if entry.string_id < 0:
                continue
            if entry.data is not None:
                for ch in list(entry.data):
                    if ch not in self.frequency_count.keys():
                        self.frequency_count[ch] = 0
                    self.frequency_count[ch] = self.frequency_count[ch] + 1

        for k, v in self.frequency_count.items():
            hn = huffman_node(d=k, freq=v)
            self.huffman_tree.append(hn)
        # todo
        # build_huffman_tree()
        # build_coding_array()


    # Loads data from XML file into memory
    def load_xml_input_data(self, file_name: str):
        tree = ET.parse(file_name)
        root = tree.getroot()

        # read and store TLK Tool version, which was used to create the XML file
        self.input_file_version = root.attrib['TLKToolVersion']

        # get list of all <string> elements inside <tlkFile> tag
        for child in root:
            id = 0
            position = 0
            data = ''
            for child_node in child:
                if child_node.tag == 'id':
                    id = int(child_node.text)
                elif child_node.tag == 'position':
                    position = int(child_node.text)
                elif child_node.tag == 'data':
                    data = child_node.text
            # todo check
            if data is not None:
                data.replace('\r\n', '\n')

                # every string should be NONE-terminated
                if id >= 0:
                    data += '\0'

            # only add debug info if we are in debug mode and StringID is positive AND it's localizable
            tlk_e = tlk_entry(id, position, data)
            self.input_data.append(tlk_e)
            # if id >= 0 && debug_version && (id & 0x8000000) != 0x8000000:
            #     tlk_e = tlk_entry(id, position, "(#" + id + ") " + data)
            #     self.input_data.append(tlk_e)
            # else:
            #     tlk_e = tlk_entry(id, position, data)
            #     self.input_data.append(tlk_e)

        # code for XML files created BEFORE v. 1.0.3
        last_entry_fix_version = '1.0.3'

        # check if someone isn't loading the bugged version < 1.0.3
        if compare_version_strings(self.input_file_version, last_entry_fix_version, '<'):
            raise Exception('version < 1.0.3')


def compare_version_strings(ver1: str, ver2: str, sign):
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


class tlk_entry:
    def __init__(self, id, pos, data):
        self.string_id = id
        self.position = pos
        self.data = data


class huffman_node:
    def __init__(self, **kwargs):
        left = None
        right = None
        if 'd' in kwargs and 'freq' in kwargs:
            d = kwargs['d']
            freq = kwargs['freq']
            self.data = d
            self.frequency_count = freq
        elif 'left' in kwargs and 'right' in kwargs:
            left = kwargs['left']
            right = kwargs['right']
            self.frequency_count = left.frequency_count + right.frequency_count
            self.data = kwargs['d']
