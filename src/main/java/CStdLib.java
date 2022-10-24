import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Platform;

public interface CStdLib extends Library {
    CStdLib INSTANCE = Native.loadLibrary(Platform.isWindows() ? "msvcrt" : "c", CStdLib.class);

    int syscall(int number, Object... args);
}
