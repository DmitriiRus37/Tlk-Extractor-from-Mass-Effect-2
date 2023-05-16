import os
from xml.etree import ElementTree as ET

from collections import deque
from typing import Any

from helpers import bit_array_list_to_byte_array, compare_version_strings
from huffman_node import HuffmanNode


class HuffmanCompression:
    def __init__(self):
        self.input_file_version = '1.0.0.0'
        self.input_data = []
        self.frequency_count = {}
        self.huffman_tree = []
        self.huffman_codes = {}

    # Converts a Huffman Tree to it's binary representation used by TLK format of Mass Effect 2.
    def convert_huffman_tree_to_buffer(self) -> list[Any]:
        q = deque()
        indices = {}

        index = 0
        q.append(self.huffman_tree[0])

        while len(q) > 0:
            node = q.popleft()
            # if it's a leaf - set it's ID to reflect char data the node contains
            if node.left == node.right:
                # store the char data
                node.id = -1 - node.letter
                # that's how it's going to be decoded when parsing TLK file:
                # char c = ru.BitConverter.ToChar(ru.BitConverter.GetBytes(0xffff - node.ID), 0);
            else:
                node.id = index
                index += 1
                indices[node.id] = node
            if node.right is not None:
                q.append(node.right)
            if node.left is not None:
                q.append(node.left)
        output = []
        for node in indices.values():
            output.append(node.left.id)
            output.append(node.right.id)
        return output

    # Dumps data from memory to TLK compressed file format.
    # Compressed data should be read into memory first, by LoadInputData method.
    def save_to_tlk_file(self, dest_file: str) -> None:
        if os.path.isfile(dest_file):
            os.remove(dest_file)
        # converts Huffmann Tree to binary form
        tree_buffer = self.convert_huffman_tree_to_buffer()

        # preparing data and entries for writing to file;
        # entries list consists of pairs <string_id, offset>
        binary_data = []
        entries1 = {}
        entries2 = {}
        offset = 0

        for entry in self.input_data:
            str_id = entry.string_id
            if str_id < 0:
                if str_id not in entries1:
                    entries1[str_id] = int(entry.data)
                else:
                    entries2[str_id] = int(entry.data)
                continue

            if str_id not in entries1:
                entries1[str_id] = offset
            else:
                entries2[str_id] = offset

            # for every character in a string, put it's binary code into data array
            for ch in list(entry.data):
                binary_data.append(self.huffman_codes[ch])
                offset += len(self.huffman_codes[ch])

        # preparing TLK Header
        magic = 7040084
        ver = 3
        min_ver = 2
        entry_1_count = len(entries1)
        entry_2_count = len(entries2)
        tree_node_count = len(tree_buffer) // 2
        data_length = offset // 8
        if offset % 8 > 0:
            data_length += 1

        # writing TLK Header
        with open(dest_file, "w+") as f:
            f.write(str(magic))
            f.write(str(ver))
            f.write(str(min_ver))
            f.write(str(entry_1_count))
            f.write(str(entry_2_count))
            f.write(str(tree_node_count))
            f.write(str(data_length))

            # writing entries
            for k, v in entries1.items():
                f.write(k)
                f.write(v)
            for k, v in entries2.items():
                f.write(k)
                f.write(v)

            # writing HuffmanTree
            for el in tree_buffer:
                f.write(el)

            # writing data
            data = bit_array_list_to_byte_array(binary_data, offset)
            f.write(data)

    # Loads a file into memory and prepares for compressing it to TLK
    def load_input_data(self, file_name: str, ff: str):
        self.input_data = []
        self.load_xml_input_data(file_name)
        # sorted(self.input_data, key=lambda tlk_entry: tlk_entry.position)
        self.input_data.sort(key=lambda x: x.position)
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

        # here we have frequency of each char at TLK file
        for k, v in self.frequency_count.items():
            self.huffman_tree.append(HuffmanNode(d=k, freq=v))
        self.build_huffman_tree()
        self.build_coding_array()

    # Using Huffman Tree (created with build_huffman_tree method), generates a binary code for every character.
    def build_coding_array(self):
        # stores a binary code
        currend_code = []
        current_node = self.huffman_tree[0]
        self.traverse_huffman_tree(current_node, currend_code)

    # Recursively traverses Huffman Tree and generates codes
    def traverse_huffman_tree(self, node, code):
        # check if both sons are None
        if node.left == node.right:
            # arr = []
            # for i in range(len(code)):
            #     arr.append(code[i])
            # ba = bit_array.BitArray(bits=arr)
            self.huffman_codes[node.letter] = ''.join(code)
        else:
            # adds 0 to the code - process left son
            code.append('0')
            self.traverse_huffman_tree(node.left, code)
            del code[len(code) - 1]

            # adds 1 to the code - process right son
            code.append('1')
            self.traverse_huffman_tree(node.right, code)
            del code[len(code) - 1]

    def build_huffman_tree(self):
        while len(self.huffman_tree) > 1:
            # sort Huffman Nodes by frequency
            self.huffman_tree.sort(key=lambda x: x.frequency_count)

            parent = HuffmanNode(left=self.huffman_tree[0], right=self.huffman_tree[1])
            del self.huffman_tree[0]
            del self.huffman_tree[0]
            self.huffman_tree.append(parent)

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
            if data is not None:
                data.replace('\r\n', '\n')

                # every string should be NONE-terminated
                if id >= 0:
                    data += '\0'

            # only add debug info if we are in debug mode and StringID is positive AND it's localizable
            tlk_e = TlkEntry(id, position, data)
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


class TlkEntry:
    def __init__(self, id: int, pos: int, data: str):
        self.string_id = id
        self.position = pos
        self.data = data
