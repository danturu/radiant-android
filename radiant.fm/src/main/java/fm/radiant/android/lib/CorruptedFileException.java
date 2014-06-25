package fm.radiant.android.lib;

import java.io.IOException;

public class CorruptedFileException extends IOException {
    public CorruptedFileException() {
        super();
    }

    public CorruptedFileException(String message) {
        super(message);
    }
}