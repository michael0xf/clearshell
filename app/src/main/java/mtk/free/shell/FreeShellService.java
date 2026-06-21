package mtk.free.shell;

import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class FreeShellService extends android.app.Service{

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
