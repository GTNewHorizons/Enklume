package io.xol.enklume.nbt;

import java.io.DataInputStream;
import java.io.IOException;

class NBTEnd extends NBTag {

    @Override
    void feed(DataInputStream is) throws IOException {
        // Read nothing
    }
}
