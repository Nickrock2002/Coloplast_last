package com.ninecmed.tablet;

class CRC {
    static int crc16(byte[] msg, int n) {
        int crc = 0xffff;

        for (int i = 0; i < n; i++) {
            byte element = msg[i];
            crc ^= (element << 8);
            for (int j = 0; j < 8; j++) {
                boolean carry = ((crc & 0x8000) != 0);
                crc <<= 1;
                if (carry) {
                    crc ^= 0x1021;
                }
            }
        }

        return crc & 0xffff;
    }
}
