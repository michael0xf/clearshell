package mtk.free.shell;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AutoStartFreeShell extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Intent serviceIntent = new Intent(context, FreeShellService.class);
            Intent serviceIntent = new Intent(context, FreeShell.class);
            serviceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(serviceIntent);
            //context.startService(serviceIntent);
        }
    }

}
