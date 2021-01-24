package menion.android.whereyougo.maps.mapsforge.filefilter;

/** copied from mapsforge library 0.3.0 - since it is used in WHereYouGo also outside of pure "maps" usage */
public class FileOpenResult {
    /**
     * Singleton for a FileOpenResult instance with {@code success=true}.
     */
    public static final FileOpenResult SUCCESS = new FileOpenResult();

    private final String errorMessage;
    private final boolean success;

    /**
     * @param errorMessage
     *            a textual message describing the error, must not be null.
     */
    public FileOpenResult(String errorMessage) {
        if (errorMessage == null) {
            throw new IllegalArgumentException("error message must not be null");
        }

        this.success = false;
        this.errorMessage = errorMessage;
    }

    private FileOpenResult() {
        this.success = true;
        this.errorMessage = null;
    }

    /**
     * @return a textual error description (might be null).
     */
    public String getErrorMessage() {
        return this.errorMessage;
    }

    /**
     * @return true if the file could be opened successfully, false otherwise.
     */
    public boolean isSuccess() {
        return this.success;
    }
}
