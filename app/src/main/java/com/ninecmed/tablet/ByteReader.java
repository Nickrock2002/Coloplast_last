package com.ninecmed.tablet;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.List;

import me.aflak.bluetooth.reader.SocketReader;

// The Bluetooth communications library is provided by Omar Aflak,
// https://github.com/OmarAflak/Bluetooth-Library.  The default behavior of the library will
// read the input stream until a new line character is received. However, in our case we
// would like to continue reading the input stream until a custom delimiter is recieved.
// The library can be altered to do this by creating our own reader class that extends
// SocketReader. As explained in Omar's git hub description, the ByteReader should override
// the byte[] read() throws IOException method. This method must block. It should not return
// if no values were received.  The class below shows an implementation with a custom
// delimiter = 0.  Once this class is defined, the reader can be used by calling the following
// method, mBluetooth.setReader(ByteReader.class);

public class ByteReader extends SocketReader {
    // Use PushbackInputStream instead of InputStream so that the read() can
    // perform a read and unread. This will force an IOException when the socket
    // is closed so that the library can detect that the BT connection is lost.  Otherwise
    // there's no easy way to tell if the socket is closed.
    private final PushbackInputStream reader;
    private final byte delimiter;

    public ByteReader(InputStream inputStream) {
        super(inputStream);
        reader = new PushbackInputStream(inputStream);
        delimiter = 0;
    }

    @Override
    public byte[] read() throws IOException {
        List<Byte> byteList = new ArrayList<>();
        byte[] tmp = new byte[1];

        while (true) {
            int n = reader.read();
            reader.unread(n);

            int count = reader.read(tmp);
            if (count > 0) {
                if (tmp[0] == delimiter) {

                    // Add the delimiter to the returned byte array
                    byteList.add(delimiter);

                    byte[] returnBytes = new byte[byteList.size()];
                    for (int i = 0; i < byteList.size(); i++) {
                        returnBytes[i] = byteList.get(i);
                    }
                    return returnBytes;
                } else {
                    byteList.add(tmp[0]);
                }
            }
        }
    }
}