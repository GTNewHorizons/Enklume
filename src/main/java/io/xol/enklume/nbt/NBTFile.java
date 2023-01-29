package io.xol.enklume.nbt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.io.IOUtils;

public class NBTFile {

    private final NBTCompound root;

    public enum CompressionScheme {
        NONE,
        GZIPPED,
    }

    public NBTFile(File file, CompressionScheme scheme) throws IOException {
        if (!file.exists()) throw new FileNotFoundException(file.getAbsolutePath());

        try (FileInputStream fis = new FileInputStream(file)) {
            if (scheme == CompressionScheme.GZIPPED) {
                final ByteBuffer buf;
                try (GZIPInputStream zis = new GZIPInputStream(fis)) {
                    buf = ByteBuffer.wrap(IOUtils.toByteArray(zis));
                }
                root = (NBTCompound) NBTag.parseByteBuffer(buf);
            } else if (scheme == CompressionScheme.NONE) {
                root = (NBTCompound) NBTag.parseByteBuffer(ByteBuffer.wrap(IOUtils.toByteArray(fis)));
            } else {
                fis.close();
                throw new RuntimeException("Unknown CompressionScheme: " + scheme);
            }
        }
    }

    public NBTCompound getRoot() {
        return root;
    }
}
