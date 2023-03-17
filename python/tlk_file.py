import os

import bit_array
import bit_convertor
import huffman_node
import tlk_header
import input_stream
import tlk_string_ref
import wrap
from xml.etree import ElementTree as ET


class TlkFile:
    def __init__(self):
        self.header = None
        self.string_refs = []
        self.character_tree = []
        self.bits = []

    def get_string(self, bit_offset_wrap):
        root = self.character_tree[0]
        cur_node = root
        cur_string = ''

        i = bit_offset_wrap.value
        while i < self.bits.length:
            next_node_id = cur_node.right_node_id if self.bits.get_rev(i) else cur_node.left_node_id

            if next_node_id >= 0:
                cur_node = self.character_tree[next_node_id]
            else:
                c = chr(0)
                try:
                    c = bit_convertor.to_char_rev(bit_convertor.get_bytes_by_value(0xffff - next_node_id), 0)
                except:
                    raise Exception
                if c != '\0':
                    cur_string += c
                    cur_node = root
                else:
                    i += 1
                    bit_offset_wrap.value = i
                    return cur_string
            i +=1
        i += 1
        bit_offset_wrap.value = i
        return None

    def load_tlk_data(self, source_path):
        input_s = input_stream.InputStream(source_path)
        self.header = tlk_header.TlkHeader(input_s)

        if self.header.magic == 1416391424:
            raise Exception('header.magic == 1416391424')
        if self.header.magic != 7040084:
            raise Exception('header.magic != 7040084')

        pos = input_s.pos
        r = input_stream.InputStream(pos, source_path)
        r.pos = pos + (self.header.entry_1_count + self.header.entry_2_count) * 8

        for i in range(self.header.tree_node_count):
            h_node = huffman_node.HuffmanNode(r)
            self.character_tree += [h_node]

        data_length = self.header.data_len
        data = [None] * data_length
        r.read_to_array(data, 0, data_length)
        self.bits = bit_array.BitArray(a=data)

        r = input_stream.InputStream(pos, source_path)

        raw_strings = {}
        offset = 0
        offset_wrap = wrap.Wrap(offset)

        while offset_wrap.value < self.bits.length:
            key = offset_wrap.value
            s = self.get_string(offset_wrap)
            raw_strings[key] = s

        for i in range(self.header.entry_1_count + self.header.entry_2_count):
            s_ref = tlk_string_ref.TlkStringRef(r)
            s_ref.position = i
            if s_ref.bit_offset >= 0:
                s_ref.data = raw_strings[s_ref.bit_offset] \
                    if s_ref.bit_offset in raw_strings.keys() \
                    else self.get_string(wrap.Wrap(s_ref.bit_offset))
            self.string_refs.append(s_ref)

    def store_to_file(self, dest_file, file_format):
        if os.path.isfile(dest_file):
            os.remove(dest_file)
        if file_format == 'xml':
            self.save_to_xml_file(dest_file)
        #             pretty_xml(dest_file)
        else:
            # save_to_text_file(dest_file)
            pass

    def save_to_xml_file(self, abs_path):
        # doc = ET.fromstring("<test>test öäü</test>")

        root = ET.Element('tlkFile')
        root.set("TLKToolVersion", '1.0.4')
        comment = ET.Comment(
            'Male entries section begin (ends at position {0})'.format((str(self.header.entry_1_count - 1))))
        root.append(comment)

        for i in range(len(self.string_refs)):
            sr = self.string_refs[i]

            if sr.position == self.header.entry_1_count:
                comment1 = ET.Comment('Male entries section end')
                comment2 = ET.Comment('Female entries section begin (ends at position {0})'.format((str(self.header.entry_1_count + self.header.entry_2_count - 1))))
                root.append(comment1)
                root.append(comment2)

            s = ET.SubElement(root, 'string')

            s1 = ET.SubElement(s, 'id')
            s1.text = str(sr.string_id)

            s2 = ET.SubElement(s, 'position')
            s2.text = str(sr.position)

            s3 = ET.SubElement(s, 'data')
            s3.text = '-1' if sr.bit_offset < 0 else sr.data
        comment = ET.Comment("Female entries section end")
        root.append(comment)

        tree = ET.ElementTree(root)
        ET.indent(tree, space="\t", level=0)
        tree.write(abs_path, encoding="utf-8")
