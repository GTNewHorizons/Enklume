package io.xol.enklume;

import gnu.trove.list.array.TByteArrayList;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class MinecraftRegion {

    int[] locations = new int[1024];
    int[] sizes = new int[1024];
    Inflater inflater = new Inflater();

    RandomAccessFile is;
    private final MinecraftChunk[][] chunks = new MinecraftChunk[32][32];

    public MinecraftRegion(File regionFile) throws IOException, DataFormatException {
        is = new RandomAccessFile(regionFile, "r");
        // First read the 1024 chunks offsets
        // int n = 0;
        byte[] buffer = new byte[1024 * 4];
        is.readFully(buffer);
        for (int i = 0; i < 1024; i++) {
            // & 0xFF to avoid negative numbers (read as unsigned byte)
            locations[i] = (buffer[i * 4] & 0xFF) << 16;
            locations[i] += (buffer[i * 4 + 1] & 0xFF) << 8;
            locations[i] += buffer[i * 4 + 2] & 0xFF;

            sizes[i] = buffer[i * 4 + 3] & 0xFF;
        }
        // Discard the timestamp bytes, we don't care.
        is.seek(is.getFilePointer() + 1024 * 4);

        for (int x = 0; x < 32; x++) for (int z = 0; z < 32; z++) chunks[x][z] = getChunkInternal(x, z);
    }

    final int offset(int x, int z) {
        return ((x & 31) + (z & 31) * 32);
    }

    public MinecraftChunk getChunk(int x, int z) {
        return chunks[x][z];
    }

    private MinecraftChunk getChunkInternal(int x, int z) throws DataFormatException, IOException {
        int l = offset(x, z);
        if (sizes[l] > 0) {
            // Chunk non-void, load it
            is.seek(locations[l] * 4096L);
            // Read 4-bytes of data length
            int compressedLength = 0;
            compressedLength += is.read() << 24;
            compressedLength += is.read() << 16;
            compressedLength += is.read() << 8;
            compressedLength += is.read();
            // Read compression mode
            int compression = is.read();
            if (compression != 2) {
                throw new DataFormatException(
                        "\"Fatal error : compression scheme not Zlib. (\" + compression + \") at \" + is.getFilePointer() + \" l = \" + l + \" s= \" + sizes[l]");
            } else {
                byte[] compressedData = new byte[compressedLength];
                is.read(compressedData);

                TByteArrayList allBytes = new TByteArrayList();

                // Unzip the ordeal
                inflater.reset();
                inflater.setInput(compressedData);

                byte[] buffer = new byte[1024 * 1024];
                while (!inflater.finished()) {
                    int c = inflater.inflate(buffer);
                    allBytes.add(buffer, 0, c);
                }

                ByteBuffer byteBuffer = ByteBuffer.wrap(allBytes.toArray());
                byteBuffer.order(ByteOrder.BIG_ENDIAN);
                return new MinecraftChunk(x, z, byteBuffer);
            }
        }
        return new MinecraftChunk(x, z);
    }

    public void close() throws IOException {
        is.close();
        inflater.end();
    }
}
