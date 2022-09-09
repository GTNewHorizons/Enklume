package io.xol.enklume.nbt;

import java.io.DataInputStream;
import java.io.IOException;

public class NBTShort extends NBTNamed {
    public short data;

    @Override
    void feed(DataInputStream is) throws IOException {
        super.feed(is);
        int i = is.read() << 8;
        i += is.read();
        data = (short) i;
    }
}
