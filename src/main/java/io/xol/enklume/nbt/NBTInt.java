package io.xol.enklume.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTInt extends NBTNamed {
    public int data;

    @Override
    void feed(DataInputStream is) throws IOException {
        super.feed(is);
        data = is.read() << 24;
        data += is.read() << 16;
        data += is.read() << 8;
        data += is.read();
    }

    public int getData() {
        return data;
    }
}
