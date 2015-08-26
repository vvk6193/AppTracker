package com.apptracker.vivek.apptracker;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloService extends Service {
    private Looper mServiceLooper;
    private ServiceHandler mServiceHandler;
    Map<String,Integer> packageList;
    String name = "", name2="";

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(Intent.ACTION_SCREEN_ON)){
                if(packageList.get(name2) != null && packageList.get(name2) != 1 && !name2.equalsIgnoreCase("com.apptracker.vivek.apptracker")) {
                    Log.d("vvk","on");
//                            PackageManager pm = getPackageManager();
//                            Intent intent=pm.getLaunchIntentForPackage(name2);
                    Intent intent1 = new Intent(getApplicationContext(),AppLockPassActivity.class);
                    intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent1.putExtra("pkg",name2 );
                    startActivity(intent1);
                    Log.d("vvk",name2);
                    packageList.put(name2,1);
                }
            }
            else if(action.equals(Intent.ACTION_SCREEN_OFF)){
                if(packageList.get(name) != null && packageList.get(name) == 1) {
                    packageList.put(name, 0);
                    Log.d("vvk","off");
                }
            }
        }
    };



    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            // Normally we would do some work here, like download a file.
            // For our sample, we just sleep for 5 seconds.
            long endTime = System.currentTimeMillis() + 5*1000;
            while (System.currentTimeMillis() < endTime) {
                synchronized (this) {
                    try {
                        wait(endTime - System.currentTimeMillis());
                    } catch (Exception e) {
                    }
                }
            }
            // Stop the service using the startId, so that we don't stop
            // the service in the middle of handling another job
            stopSelf(msg.arg1);
        }
    }

    @Override
    public void onCreate() {
        // Start up the thread running the service.  Note that we create a
        // separate thread because the service normally runs in the process's
        // main thread, which we don't want to block.  We also make it
        // background priority so CPU-intensive work will not disrupt our UI.
//        Thread thread = new Thread();
//        thread.start();
//
//        // Get the HandlerThread's Looper and use it for our Handler
//        mServiceLooper = thread.getLooper();
//        mServiceHandler = new ServiceHandler(mServiceLooper);
        IntentFilter screenStateFilter = new IntentFilter();
        screenStateFilter.addAction(Intent.ACTION_SCREEN_ON);
        screenStateFilter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(receiver, screenStateFilter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();

        // For each start request, send a message to start a job and deliver the
        // start ID so we know which request we're stopping when we finish the job
//        Message msg = mServiceHandler.obtainMessage();
//        msg.arg1 = startId;
//        mServiceHandler.sendMessage(msg);

        // If we get killed, after returning from here, restart
        start();
        return START_STICKY;
    }

    public void start(){
        Thread t = new Thread() {
            public void run(){
                 packageList = InstalledApps();
                long time = 0;
                while(true) {
                    ActivityManager manager = (ActivityManager) getApplicationContext().getSystemService(ACTIVITY_SERVICE);
                    List< ActivityManager.RunningTaskInfo > runningTaskInfo = manager.getRunningTasks(1);

                    ComponentName componentInfo = runningTaskInfo.get(0).topActivity;
                    name2 = componentInfo.getPackageName();
                    if(!name2.equalsIgnoreCase("com.apptracker.vivek.apptracker")) {

                        if(packageList.get(name2) != null && packageList.get(name2) != 1 && !name2.equalsIgnoreCase("com.apptracker.vivek.apptracker")) {
//                            PackageManager pm = getPackageManager();
//                            Intent intent=pm.getLaunchIntentForPackage(name2);
                            Intent intent = new Intent(getApplicationContext(),AppLockPassActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra("pkg",name2 );
                            startActivity(intent);
                            Log.d("vvk",name2);
                            packageList.put(name2,1);
                        }
                        if(name2.equalsIgnoreCase(name)) {
                            time += 1;
                        } else {
                            if(packageList.get(name) != null) {
                                Log.d("vvk", "old" + name + " "+ time);
                            }
                            if(packageList.get(name) != null && packageList.get(name) == 1) {
                                packageList.put(name,0);
                            }
                            name = name2;
                            time = 0;

                        }
                    }

                    try{
                        Thread.sleep(1000);
                    } catch(Exception e){

                    }
                }
            }
        };
        t.start();
    }

    public  Map<String,Integer> InstalledApps()
    {

        Map<String,Integer> packageList =  new HashMap<String,Integer>();
        PackageManager pm = getApplicationContext().getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        for(ResolveInfo info : resolveInfos) {
            ApplicationInfo applicationInfo = info.activityInfo.applicationInfo;
            String AppName = applicationInfo.packageName;
            Log.e("vvk", AppName);
            packageList.put(AppName,0);
        }
/*

        List<PackageInfo> PackList =  getApplicationContext().getPackageManager().getInstalledPackages(0);
        for (int i=0; i < PackList.size(); i++)
        {
            PackageInfo PackInfo = PackList.get(i);
            if (  (PackInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
            {
                String AppName = PackInfo.applicationInfo.packageName;
                Log.e("App № " + Integer.toString(i), AppName);
                packageList.add(AppName);
            }
        }
*/
        return packageList;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(receiver);
        Toast.makeText(this, "service done", Toast.LENGTH_SHORT).show();
    }
}