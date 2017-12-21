package box.gift.shakelight;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

public class TransparentActivity extends Activity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        if (!isRunning(ShakeService.class))
        {
            Intent startShake = new Intent(this, ShakeService.class);

            startService(startShake);

            Toast.makeText(this, "ShakeLight starting...", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Intent startShake = new Intent(this, ShakeService.class);

            stopService(startShake);

            Toast.makeText(this, "ShakeLight stopping...", Toast.LENGTH_SHORT).show();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            finishAndRemoveTask();
        }
        else
        {
            finish();
        }
    }

    private boolean isRunning(Class<?> serviceClass)
    {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE))
        {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
