package org.apache.cordova.overApps.Services;

import android.app.Service;
import android.content.Intent;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.webkit.JavascriptInterface;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import android.view.animation.Animation;
import android.view.animation.AlphaAnimation;
import android.view.animation.LinearInterpolator;
import android.os.Build;

import android.widget.TextView;

import org.apache.cordova.overApps.Services.ServiceParameters;
import org.apache.cordova.overApps.GeneralUtils.KeyDispatchLayout;


import java.util.Date;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import android.content.res.Resources;

/**
 * Created by Mohamed Sayed .
 */

 public class OverAppsService extends Service {

     String TAG = getClass().getSimpleName();

     private WindowManager windowManager;
     WindowManager.LayoutParams params_head_float,params_head_view,params_key_dispature;
     LayoutInflater inflater;

     private View overAppsHead,overAppsView;
     ImageView imgClose;
     WebView webView;
     ImageView imageHead;
     ServiceParameters serviceParameters;
     private GestureDetector gestureDetector;
     public static final String CHANNEL_ID = "overappservice";


     @Override
     public IBinder onBind(Intent intent) {
         // Not used
         return null;
     }

     @Override
     public int onStartCommand(Intent intent, int flags, int startId) {
       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
         createNotificationChannel();
         PendingIntent pendingIntent = PendingIntent.getActivity(this,
                 0, intent, 0);

         Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                 .setContentTitle("Инфо-кнопка")
                 .setContentText("Кнопка уведомлений на экране устройства")
                 .setSmallIcon(this.getResources().getIdentifier("btn_icon", "drawable", this.getPackageName()))
                 .setContentIntent(pendingIntent)
                 .build();

         startForeground(-574543955, notification);

         //do heavy work on a background thread


         //stopSelf();

       }
       return START_NOT_STICKY;
     }

     private void createNotificationChannel() {
         NotificationChannel serviceChannel = new NotificationChannel(
                 CHANNEL_ID,
                 "Foreground Service Channel",
                 NotificationManager.IMPORTANCE_DEFAULT
         );

         NotificationManager manager = getSystemService(NotificationManager.class);
         manager.createNotificationChannel(serviceChannel);
     }

     @Override
     public void onCreate() {
         super.onCreate();
         Log.i(TAG,"onCreate");

         LocalBroadcastManager lbm = LocalBroadcastManager.getInstance(this);

         gestureDetector = new GestureDetector(this, new SingleTapConfirm());

         serviceParameters = new ServiceParameters(this);
         animator = new MoveAnimator();
         windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

         inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);

         overAppsHead = inflater.inflate(getResources().getIdentifier("service_over_apps_head", "layout", getPackageName()), null, false);
         overAppsView = inflater.inflate(getResources().getIdentifier("service_over_apps_view", "layout", getPackageName()), null, false);
         webView = (WebView) overAppsView.findViewById(getResources().getIdentifier("webView", "id", getPackageName()));
         imageHead = (ImageView) overAppsHead.findViewById(getResources().getIdentifier("imageHead", "id", getPackageName()));
         imgClose = (ImageView) overAppsView.findViewById(getResources().getIdentifier("imgClose", "id", getPackageName()));

         // webViewSettings();

         if (Build.VERSION.SDK_INT >= 26) {
           params_head_float = new WindowManager.LayoutParams(
                   WindowManager.LayoutParams.WRAP_CONTENT,
                   WindowManager.LayoutParams.WRAP_CONTENT,
                   WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                   WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                   PixelFormat.TRANSLUCENT);

           params_head_float.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
           params_head_view = new WindowManager.LayoutParams();
           params_head_view.width = WindowManager.LayoutParams.WRAP_CONTENT;
           params_head_view.height = WindowManager.LayoutParams.WRAP_CONTENT;
           params_head_view.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
           params_head_view.format = PixelFormat.TRANSLUCENT;
         } else {
           params_head_float = new WindowManager.LayoutParams(
                   WindowManager.LayoutParams.WRAP_CONTENT,
                   WindowManager.LayoutParams.WRAP_CONTENT,
                   WindowManager.LayoutParams.TYPE_PHONE,
                   WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                   PixelFormat.TRANSLUCENT);

           params_head_float.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
           params_head_view = new WindowManager.LayoutParams();
           params_head_view.width = WindowManager.LayoutParams.WRAP_CONTENT;
           params_head_view.height = WindowManager.LayoutParams.WRAP_CONTENT;
           params_head_view.type = WindowManager.LayoutParams.TYPE_PHONE;
           params_head_view.format = PixelFormat.TRANSLUCENT;
         }

         Boolean has_head = serviceParameters.getBoolean("has_head",true);
         String layout_icon = serviceParameters.getString("layout_icon");
         int id = this.getResources().getIdentifier(layout_icon, "drawable", this.getPackageName());
         imageHead.setImageResource(id);
         final Boolean enable_hardware_back = serviceParameters.getBoolean("enable_hardware_back",true);

         final BroadcastReceiver receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
              switch(intent.getExtras().getString("type")) {
                case "start":
                  Log.i(TAG,"onCreate - START OK");
                  if (overAppsHead != null) {
                    webViewSettings();
                    adjustWebViewGravity();
                    if (has_head) {
                      windowManager.addView(overAppsHead, params_head_float);
                      showKeyDispatureVisibilty(false);
                    } else {
                      windowManager.addView(overAppsView, params_head_view);

                      showKeyDispatureVisibilty(enable_hardware_back);
                    }
                  }

                  overAppsHead.setOnTouchListener(new View.OnTouchListener() {
                      private int initialX;
                      private int initialY;
                      private float initialTouchX;
                      private float initialTouchY;

                      @Override
                      public boolean onTouch(View v,MotionEvent event) {
                          Log.i(TAG,"onTouch ... Click");
                          if (event != null) {
                              if (gestureDetector.onTouchEvent(event)) {
                                  // ....  click on the whole over app head event
                                  Log.i(TAG,"Click");

                                  v.animate().cancel();
                                  v.animate().scaleX(1f).setDuration(100).start();
                                  v.animate().scaleY(1f).setDuration(100).start();

                                   final Intent intent = new Intent("layoutChannel");
                                   Bundle b = new Bundle();
                                   b.putString( "action", "open_app" );
                                   intent.putExtras( b);
                                   lbm.sendBroadcastSync(intent);

                                  Log.i(TAG,"Click");
                              }else {
                                  switch (event.getAction()) {
                                      case MotionEvent.ACTION_DOWN:
                                          Log.i(TAG,"ACTION_DOWN");
                                          initialX = params_head_float.x;
                                          initialY = params_head_float.y;
                                          initialTouchX = event.getRawX();
                                          initialTouchY = event.getRawY();
                                          updateSize();
                                          animator.stop();

                                          v.animate().scaleXBy(-0.1f).setDuration(100).start();
                                          v.animate().scaleYBy(-0.1f).setDuration(100).start();
                                          break;
                                      case MotionEvent.ACTION_MOVE:
                                          Log.i(TAG,"ACTION_MOVE");
                                          int x = initialX - (int) (event.getRawX() - initialTouchX);
                                          int y = initialY + (int) (event.getRawY() - initialTouchY);
                                          params_head_float.x = x;
                                          params_head_float.y = y;
                                          windowManager.updateViewLayout(overAppsHead, params_head_float);
                                          break;
                                      case MotionEvent.ACTION_UP:
                                          Boolean drag_to_side = serviceParameters.getBoolean("drag_to_side",true);
                                          if (drag_to_side) {
                                             goToWall();
                                          }
                                          Log.i(TAG,"ACTION_UP");
                                          v.animate().cancel();
                                          v.animate().scaleX(1f).setDuration(100).start();
                                          v.animate().scaleY(1f).setDuration(100).start();
                                          break;
                                  }
                              }
                          }
                          return false;
                      }
                  });
                  Log.i(TAG,"onCreate - START DONE");
                break;
                case "open":
                  Log.i(TAG,"SHOW LAYOUT");
                  try {
                    if (overAppsHead != null) {
                      int icon = context.getResources().getIdentifier(intent.getExtras().getString("icon"), "drawable", context.getPackageName());
                      imageHead.setImageResource(icon);
                      imageHead.setVisibility(View.VISIBLE);
                    }
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                break;
                case "close":
                  Log.i(TAG,"HIDE LAYOUT");
                  try {
                    if (overAppsHead != null) {
                      imageHead.clearAnimation();
                      imageHead.setVisibility(View.INVISIBLE);
                    };
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                break;
                case "action":
                  try {
                    int icon = context.getResources().getIdentifier(intent.getExtras().getString("icon"), "drawable", context.getPackageName());
                    imageHead.setImageResource(icon);

                    Animation animation = new AlphaAnimation(1f, 0.5f); //to change visibility from visible to invisible
                              animation.setDuration(500); //1 second duration for each animation cycle
                              animation.setInterpolator(new LinearInterpolator());
                              animation.setRepeatCount(Animation.INFINITE); //repeating indefinitely
                              animation.setRepeatMode(Animation.REVERSE); //animation will start from end point once ended.
                    imageHead.startAnimation(animation); //to start animation
                  } catch (Exception e) {
                    e.printStackTrace();
                  }
                break;
              }
            }
         };
         lbm.registerReceiver(receiver, new IntentFilter("app.event"));

         Log.i(TAG,"onCreate - STARTING...");

         final Intent intent = new Intent("layoutChannel");
         Bundle b = new Bundle();
         b.putString( "action", "start_layout" );
         intent.putExtras(b);
         lbm.sendBroadcastSync(intent);
     }


     @Override
     public void onStart(Intent intent, int startId) {
         super.onStart(intent, startId);
         Log.i(TAG,"START");
         String file_path = serviceParameters.getString("file_path");
       //  String file_path = intent.getExtras().getString("file_path");
         webView.loadUrl(file_path);
     }

     @Override
     public void onDestroy() {
       Log.i(TAG,"[DESTROY LAYOUT]");
         try {
           if (overAppsHead != null) {
             windowManager.removeView(overAppsHead);
           }
         } catch (Exception e) {
           e.printStackTrace();
         }
         try {
             overAppsHead = null;
             if (overAppsView != null) {
               windowManager.removeView(overAppsView);
             }
             overAppsView = null;
             showKeyDispatureVisibilty(false);
         }catch (Exception e){
             e.printStackTrace();
         }
         super.onDestroy();

     }

     private KeyDispatchLayout rlKeyDispature;
     private View keyDispatureView;
     public void showKeyDispatureVisibilty(boolean visible) {

         if (keyDispatureView ==null)
         {
             keyDispatureView = inflater.inflate(getResources().getIdentifier("key_dispature", "layout", getPackageName()), null, false);
         }
         rlKeyDispature = (KeyDispatchLayout) keyDispatureView.findViewById(getResources().getIdentifier("tab_left", "id", getPackageName()));

         if (visible)
         {
           if (Build.VERSION.SDK_INT >= 26) {
             params_key_dispature = new WindowManager.LayoutParams(
                     WindowManager.LayoutParams.WRAP_CONTENT,
                     WindowManager.LayoutParams.WRAP_CONTENT,
                     WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                     WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                     PixelFormat.TRANSLUCENT);
             params_key_dispature.gravity = Gravity.CENTER;

             //This one is necessary.
             params_key_dispature.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
             // Play around with these two.
             params_key_dispature.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
             //ll_lp.flags = ll_lp.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
           } else {
             params_key_dispature = new WindowManager.LayoutParams(
                     WindowManager.LayoutParams.WRAP_CONTENT,
                     WindowManager.LayoutParams.WRAP_CONTENT,
                     WindowManager.LayoutParams.TYPE_PHONE,
                     WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                     PixelFormat.TRANSLUCENT);
             params_key_dispature.gravity = Gravity.CENTER;

             //This one is necessary.
             params_key_dispature.type = WindowManager.LayoutParams.TYPE_PHONE;
             // Play around with these two.
             params_key_dispature.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
             //ll_lp.flags = ll_lp.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
           }

             try
             {
                 windowManager.removeView(rlKeyDispature);
             }
             catch (Exception e){}

             windowManager.addView(rlKeyDispature, params_key_dispature);
             Log.i(TAG, "Key DISPATURE -- ADDED");
         }
         else
         {
             try
             {
                 windowManager.removeView(rlKeyDispature);
                 Log.i(TAG, "Key DISPATURE -- REMOVED");
             }
             catch (Exception e){}
         }

     }

     public void webViewSettings() {

               webView.setBackgroundColor(Color.TRANSPARENT);
			         webView.addJavascriptInterface(new WebAppInterface(this), "OverApps");
               WebSettings webSettings = webView.getSettings();
               webSettings.setJavaScriptEnabled(true);
               webSettings.setAppCacheMaxSize(10 * 1024 * 1024); // 10MB
               webSettings.setAppCachePath(getApplicationContext().getCacheDir().getAbsolutePath());
               webSettings.setAllowFileAccess(true);
               webSettings.setDomStorageEnabled(true);
               webSettings.setAppCacheEnabled(true);
               try {
                   Log.i(TAG, "Enabling HTML5-Features");
                   Method m1 = WebSettings.class.getMethod("setDomStorageEnabled", new Class[]{Boolean.TYPE});
                   m1.invoke(webView.getSettings(), Boolean.TRUE);

                   Method m2 = WebSettings.class.getMethod("setDatabaseEnabled", new Class[]{Boolean.TYPE});
                   m2.invoke(webView.getSettings(), Boolean.TRUE);

                   Method m3 = WebSettings.class.getMethod("setDatabasePath", new Class[]{String.class});
                   m3.invoke(webView.getSettings(), "/data/data/" + getPackageName() + "/databases/");

                   Method m4 = WebSettings.class.getMethod("setAppCacheMaxSize", new Class[]{Long.TYPE});
                   m4.invoke(webView.getSettings(), 1024 * 1024 * 8);

                   Method m5 = WebSettings.class.getMethod("setAppCachePath", new Class[]{String.class});
                   m5.invoke(webView.getSettings(), "/data/data/" + getPackageName() + "/cache/");

                   Method m6 = WebSettings.class.getMethod("setAppCacheEnabled", new Class[]{Boolean.TYPE});
                   m6.invoke(webView.getSettings(), Boolean.TRUE);

                   Log.i(TAG, "Enabled HTML5-Features");
               } catch (NoSuchMethodException e) {
                   Log.e(TAG, "Reflection fail", e);
               } catch (InvocationTargetException e) {
                   Log.e(TAG, "Reflection fail", e);
               } catch (IllegalAccessException e) {
                   Log.e(TAG, "Reflection fail", e);
               }

              Boolean enable_close_btn = serviceParameters.getBoolean("enable_close_btn",true);
              if (enable_close_btn) {
                  imgClose.setVisibility(View.VISIBLE);
              }else {
                  imgClose.setVisibility(View.GONE);
              }

     }


     public void goToWall() {

            int middle = width / 2;
            float nearestXWall = params_head_float.x >= middle ? width : 0;
            animator.start(nearestXWall, params_head_float.y);

    }

     public void adjustWebViewGravity(){
       String vertical_position = serviceParameters.getString("vertical_position");
       String horizontal_position = serviceParameters.getString("horizontal_position");
       String position = vertical_position + "_" + horizontal_position;

       if(position.equals("top_right")) {
            params_head_view.gravity = Gravity.TOP | Gravity.RIGHT;
        }else if (position.equals("top_center")) {
            params_head_view.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        }else if (position.equals("top_left")) {
            params_head_view.gravity = Gravity.TOP | Gravity.LEFT;
        }else if (position.equals("center_right")) {
            params_head_view.gravity = Gravity.CENTER_VERTICAL | Gravity.RIGHT;
        }else if (position.equals("center_center")) {
            params_head_view.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        }else if (position.equals("center_left")) {
            params_head_view.gravity = Gravity.CENTER_VERTICAL | Gravity.LEFT;
        }else if (position.equals("bottom_right")) {
            params_head_view.gravity = Gravity.BOTTOM | Gravity.RIGHT;
        }else if (position.equals("bottom_center")) {
            params_head_view.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        }else if (position.equals("bottom_left")) {
            params_head_view.gravity = Gravity.BOTTOM | Gravity.LEFT;
        }else {
            params_head_view.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
        }

     }

     MoveAnimator animator;
     int width;
     private void updateSize() {
         DisplayMetrics metrics = new DisplayMetrics();
         windowManager.getDefaultDisplay().getMetrics(metrics);
         Display display = windowManager.getDefaultDisplay();
         Point size = new Point();
         display.getSize(size);
         width = (size.x - overAppsHead.getWidth());

     }

     private void move(float deltaX, float deltaY) {
         params_head_float.x += deltaX;
         params_head_float.y += deltaY;
         windowManager.updateViewLayout(overAppsHead, params_head_float);
     }

     private class SingleTapConfirm extends GestureDetector.SimpleOnGestureListener {

         @Override
         public boolean onSingleTapUp(MotionEvent event) {
             return true;
         }
     }

     private class MoveAnimator implements Runnable {
         private Handler handler = new Handler(Looper.getMainLooper());
         private float destinationX;
         private float destinationY;
         private long startingTime;

         private void start(float x, float y) {
             this.destinationX = x;
             this.destinationY = y;
             startingTime = System.currentTimeMillis();
             handler.post(this);
         }

         @Override
         public void run() {
             if (overAppsHead.getRootView() != null && overAppsHead.getRootView().getParent() != null) {
                 float progress = Math.min(1, (System.currentTimeMillis() - startingTime) / 400f);
                 float deltaX = (destinationX -  params_head_float.x) * progress;
                 float deltaY = (destinationY -  params_head_float.y) * progress;
                 move(deltaX, deltaY);
                 if (progress < 1) {
                     handler.post(this);
                 }
             }
         }

         private void stop() {
             handler.removeCallbacks(this);
         }
     }

     public class WebAppInterface {
  		Context mContext;

  		/** Instantiate the interface and set the context */
  		public WebAppInterface(Context c) {
  			mContext = c;
  		}

  		/** Close from inside web view  */
  		@JavascriptInterface
  		public void closeWebView() {
  		    Log.i(TAG,"Click");
          stopSelf();
          try {
              if (overAppsView != null) windowManager.removeView(overAppsView);
              if (overAppsHead != null) windowManager.removeView(overAppsHead);
          }catch (Exception e){
              e.printStackTrace();
          }
  		}

      @JavascriptInterface
      public void openApp(){
        // mContext.startActivity(new Intent(mContext,com.ionicframework.overapp809848.MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
      }
  	}
 }
