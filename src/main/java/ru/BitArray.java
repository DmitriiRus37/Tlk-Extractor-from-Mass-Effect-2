package ru;/*
 * Copyright (c) 1997, 2006, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * A packed array of booleans.
 *
 * @author Joshua Bloch
 * @author Douglas Hoover
 */

public class BitArray {

    private final byte[] repn;
    private final int length;

    public static final byte BITS_PER_UNIT = 8;

    private static int subscript(int idx) {
        return idx / BITS_PER_UNIT;
    }

    private static int positionBigEndian(int idx) { // bits big-endian in each unit
        return 1 << (BITS_PER_UNIT - 1 - (idx % BITS_PER_UNIT));
    }

    /**
     * Creates a main.java.ru.BitArray of the specified size, initialized to zeros.
     */
    public BitArray(int length) throws IllegalArgumentException {
        if (length < 0) {
            throw new IllegalArgumentException("Negative length for BitArray");
        }
        this.length = length;
        repn = new byte[(length + BITS_PER_UNIT - 1)/BITS_PER_UNIT];
    }


    /**
     * Creates a BitArray of the specified size, initialized from the
     * specified byte array.  The most significant bit of a[0] gets
     * index zero in the BitArray.  The array a must be large enough
     * to specify a value for every bit in the BitArray.  In other words,
     * 8*a.length <= length.
     */
    public BitArray(byte[] a) throws IllegalArgumentException {
        length = a.length * BITS_PER_UNIT;
        int repLength = (length + BITS_PER_UNIT - 1) / BITS_PER_UNIT;
        int unusedBits = repLength * BITS_PER_UNIT - length;
        byte bitMask = (byte) (0xFF << unusedBits);

        /*
         normalize the representation:
          1. discard extra bytes
          2. zero out extra bits in the last byte
         */
        repn = new byte[repLength];
        System.arraycopy(a, 0, repn, 0, repLength);
        if (repLength > 0) {
            repn[repLength - 1] &= bitMask;
        }
    }

    /**
     * Create a BitArray whose bits are those of the given array
     * of Booleans.
     */
    public BitArray(boolean[] bits) {
        length = bits.length;
        repn = new byte[(length + 7)/8];

        for (int i=0; i < length; i++) {
            set(i, bits[i]);
        }
    }


    /**
     *  Copy constructor (for cloning).
     */
    private BitArray(BitArray ba) {
        length = ba.length;
        repn = ba.repn.clone();
    }

    /**
     *  Returns the indexed bit in this BitArray.
     */
    public boolean get(int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(String.valueOf(index));
        }
        return (repn[subscript(index)] & positionBigEndian(index)) != 0;
    }

    /**
     *  Returns the reversed indexed bit in this main.java.ru.BitArray.
     */
    public boolean getRev(int index) throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(String.valueOf(index));
        }
        boolean[] bitArray = new boolean[Byte.SIZE];
        for (int i = 0; i<Byte.SIZE; i++) {
            bitArray[Byte.SIZE-1-i] = (repn[subscript(index)] >> i & 0x1) != 0x0;
        }
        return getBit(bitArray, index, Endian.LittleEndian);
    }

    private boolean getBit(boolean[] byteImpl, int index, Endian e) {
        return byteImpl[e.equals(Endian.BigEndian) ?
                index % BITS_PER_UNIT :
                Byte.SIZE - 1 - index % BITS_PER_UNIT];
    }

    /**
     *  Sets the indexed bit in this main.java.ru.BitArray.
     */
    public void set(int index, boolean value)
            throws ArrayIndexOutOfBoundsException {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException(String.valueOf(index));
        }
        int idx = subscript(index);
        int bit = positionBigEndian(index);
        repn[idx] = value ? (byte) (repn[idx] | bit) : (byte) (repn[idx] & ~bit);
    }

    /**
     * Returns the length of this BitArray.
     */
    public int length() {
        return length;
    }

    /**
     * Returns a Byte array containing the contents of this BitArray.
     * The bit stored at index zero in this BitArray will be copied
     * into the most significant bit of the zeroth element of the
     * returned byte array.  The last byte of the returned byte array
     * will be contain zeros in any bits that do not have corresponding
     * bits in the BitArray.  (This matters only if the BitArray's size
     * is not a multiple of 8.)
     */
    public byte[] toByteArray() {
        return repn.clone();
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof BitArray)) {
            return false;
        }
        BitArray ba = (BitArray) obj;
        return (ba.length == length) && IntStream.range(0, repn.length).noneMatch(i -> repn[i] != ba.repn[i]);
    }

    /**
     * Return a boolean array with the same bit values as this BitArray.
     */
    public boolean[] toBooleanArray() {
        boolean[] bits = new boolean[length];

        for (int i=0; i < length; i++) {
            bits[i] = get(i);
        }
        return bits;
    }

    /**
     * Returns a hash code value for this bit array.
     *
     * @return  a hash code value for this bit array.
     */
    public int hashCode() {
        int hashCode = 0;

        for (byte b : repn) {
            hashCode = 31 * hashCode + b;
        }
        return hashCode ^ length;
    }


    @Override
    public Object clone() {
        return new BitArray(this);
    }

    private static final byte[][] NYBBLE = {
            { (byte)'0',(byte)'0',(byte)'0',(byte)'0'},
            { (byte)'0',(byte)'0',(byte)'0',(byte)'1'},
            { (byte)'0',(byte)'0',(byte)'1',(byte)'0'},
            { (byte)'0',(byte)'0',(byte)'1',(byte)'1'},
            { (byte)'0',(byte)'1',(byte)'0',(byte)'0'},
            { (byte)'0',(byte)'1',(byte)'0',(byte)'1'},
            { (byte)'0',(byte)'1',(byte)'1',(byte)'0'},
            { (byte)'0',(byte)'1',(byte)'1',(byte)'1'},
            { (byte)'1',(byte)'0',(byte)'0',(byte)'0'},
            { (byte)'1',(byte)'0',(byte)'0',(byte)'1'},
            { (byte)'1',(byte)'0',(byte)'1',(byte)'0'},
            { (byte)'1',(byte)'0',(byte)'1',(byte)'1'},
            { (byte)'1',(byte)'1',(byte)'0',(byte)'0'},
            { (byte)'1',(byte)'1',(byte)'0',(byte)'1'},
            { (byte)'1',(byte)'1',(byte)'1',(byte)'0'},
            { (byte)'1',(byte)'1',(byte)'1',(byte)'1'}
    };

    private static final int BYTES_PER_LINE = 8;

    /**
     *  Returns a String representation of this BitArray.
     */
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (int i = 0; i < repn.length - 1; i++) {
            out.write(NYBBLE[(repn[i] >> 4) & 0x0F], 0, 4);
            out.write(NYBBLE[repn[i] & 0x0F], 0, 4);
            out.write(i % BYTES_PER_LINE == BYTES_PER_LINE - 1 ? '\n' : ' ');
        }

        // in last byte of repn, use only the valid bits
        for (int i = BITS_PER_UNIT * (repn.length - 1); i < length; i++) {
            out.write(get(i) ? '1' : '0');
        }
        return out.toString();
    }

    public BitArray truncate() {
        for (int i=length-1; i>=0; i--) {
            if (get(i)) {
                return new BitArray(Arrays.copyOf(repn, (i + BITS_PER_UNIT)/BITS_PER_UNIT));
            }
        }
        return new BitArray(1);
    }

}