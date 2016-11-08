package pl.pg.gda.eti.kio.esc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Wojciech Stanisławski
 * @since 08.11.2016
 */
class ChunkFileReader {
    private File file;
    private InputStream stream;
    private long chunkStart;
    private long chunkSize;
    private long readed;
    private boolean startsWithEndline;
    private String lastLinePart;

    public ChunkFileReader(File file, long chunkStart, long chunkSize) {
        this.file = file;
        this.chunkStart = chunkStart;
        this.chunkSize = chunkSize;
        lastLinePart = "";
        readed = 0;
    }

    public void open() throws IOException {
        stream = new FileInputStream(file);
        if(chunkStart > 0) {
            stream.skip(chunkStart - 1);
            startsWithEndline = stream.read() == '\n';
        }
        else {
            startsWithEndline = true;
        }
    }

    public String readLine() throws IOException {
        if (readed > chunkSize) {
            return null;
        }
        byte[] buffer = new byte[128];
        String bufferString = lastLinePart;
        do {
            readed += stream.read(buffer);
            bufferString += new String(buffer, "UTF-8");
        } while(!bufferString.contains("\n"));
        bufferString.replace("\r", "");
        lastLinePart = bufferString.substring(bufferString.indexOf("\n") + 1);
        bufferString = bufferString.substring(0, bufferString.indexOf("\n"));
        if(startsWithEndline) {
            return bufferString;
        }
        else {
            startsWithEndline = true;
            return readLine();
        }
    }


    public void close() throws IOException {
        stream.close();
    }
}