package com.xretia.sonyiem.volumeshutter;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam;

/** Sony IEM volume shutter for modern LibXposed/LSPatch. */
public final class VolumeShutterModern extends XposedModule {
    private static final String TARGET = "com.sony.playmemories.mobile";
    private static final String REMOTE = TARGET + ".remotecontrol.RemoteControlActivity";
    private static final String DEBUG_FILE = "/sdcard/vsdebug.xretia";
    private static final String CHANNEL = "sonyiem_volume_shutter_debug";
    private static int notificationId = 7800;
    private static boolean debugEnabled;

    @Override public void onPackageReady(PackageReadyParam param) {
        if (!TARGET.equals(param.getPackageName())) return;
        try {
            ClassLoader loader = param.getClassLoader();
            debugEnabled = new File(DEBUG_FILE).exists();
            Class<?> appClass = Class.forName("android.app.Application", false, loader);
            Method appCreate = appClass.getDeclaredMethod("onCreate");
            hook(appCreate).intercept(chain -> {
                Object result = chain.proceed();
                if (chain.getThisObject() instanceof Context && debugEnabled) debug((Context) chain.getThisObject(), "SONY IEM 音量键快门：模块已加载");
                return result;
            });
            Class<?> remote = Class.forName(REMOTE, false, loader);
            Method dispatch = remote.getMethod("dispatchKeyEvent", KeyEvent.class);
            hook(dispatch).intercept(chain -> {
                Activity activity = (Activity) chain.getThisObject();
                KeyEvent event = (KeyEvent) chain.getArg(0);
                if (event == null || !isVolume(event.getKeyCode())) return chain.proceed();
                if (debugEnabled) debug(activity, "收到音量键 " + event.getKeyCode() + " / " + event.getAction());
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (event.getRepeatCount() == 0) dispatchShutterTouch(activity, MotionEvent.ACTION_DOWN);
                } else if (event.getAction() == KeyEvent.ACTION_UP) {
                    dispatchShutterTouch(activity, MotionEvent.ACTION_UP);
                }
                return true;
            });
        } catch (Throwable t) {
            if (debugEnabled) {
                try {
                    Class<?> thread = Class.forName("android.app.ActivityThread");
                    Method current = thread.getDeclaredMethod("currentApplication");
                    Context context = (Context) current.invoke(null);
                    if (context != null) debug(context, "模块初始化失败：" + t.getClass().getSimpleName());
                } catch (Throwable ignored) {}
            }
        }
    }

    private static boolean isVolume(int code) { return code == KeyEvent.KEYCODE_VOLUME_UP || code == KeyEvent.KEYCODE_VOLUME_DOWN; }

    private static void dispatchShutterTouch(Activity activity, int action) {
        try {
            int id = activity.getResources().getIdentifier("act_button", "id", TARGET);
            View button = id == 0 ? null : activity.findViewById(id);
            if (button == null || !button.isEnabled()) return;
            long now = android.os.SystemClock.uptimeMillis();
            float x = button.getWidth() > 0 ? button.getWidth() / 2.0f : 1.0f;
            float y = button.getHeight() > 0 ? button.getHeight() / 2.0f : 1.0f;
            MotionEvent event = MotionEvent.obtain(now, now, action, x, y, 0);
            try { button.dispatchTouchEvent(event); } finally { event.recycle(); }
        } catch (Throwable ignored) {}
    }

    private static void debug(Context context, String text) {
        try { Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_LONG).show(); } catch (Throwable ignored) {}
        try {
            Context app = context.getApplicationContext();
            NotificationManager manager = (NotificationManager) app.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;
            if (Build.VERSION.SDK_INT >= 26) {
                manager.createNotificationChannel(new NotificationChannel(CHANNEL, "SONY IEM 音量键快门", NotificationManager.IMPORTANCE_HIGH));
                manager.notify(notificationId++, new Notification.Builder(app, CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("SONY IEM 音量键快门").setContentText(text).setAutoCancel(false).build());
            } else {
                manager.notify(notificationId++, new Notification.Builder(app).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("SONY IEM 音量键快门").setContentText(text).setAutoCancel(false).build());
            }
        } catch (Throwable ignored) {}
    }
}
