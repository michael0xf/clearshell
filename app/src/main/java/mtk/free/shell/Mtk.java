package mtk.free.shell;

import com.mtk.map.Tag;

import java.io.PrintWriter;

public interface Mtk {
    void save(PrintWriter writer);
    boolean load(Tag tag);
    void start(Tag tag);
    void init(int index);
    String getName();
    boolean isLocked();
}
