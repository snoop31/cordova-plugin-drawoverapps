/*
Copyright (C) 2017 by Ahmed Wahba

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in
all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.
*/
package org.apache.cordova.overApps;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PermissionHelper;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Build;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.util.Log;
import android.net.Uri;

import org.apache.cordova.overApps.Services.OverAppsService;
import org.apache.cordova.overApps.Services.ServiceParameters;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.content.res.Resources;

import android.os.PowerManager;
import android.content.Context;


public class DrawOverApps extends CordovaPlugin {
		private static final String TAG = "DrawOverApps";
	  private static final String ACTION_OPEN_OVER_APP_VIEW = "open";
		private static final String ACTION_CLOSE_OVER_APP_VIEW = "close";

		private static final String ACTION_CHECK_OVER_APP_PERMISSION = "checkOverAppPermission";
		private static final String ACTION_REQUEST_OVER_APP_PERMISSION = "requestOverAppPermission";
		private static final String ACTION_OPEN_OVER_APP_PERMISSION = "openOverAppPermission";

		private static final String ACTION_CHECK_BATTERY_OPTIMIZATION = "checkBatteryOptimization";
		private static final String ACTION_REQUEST_BATTERY_OPTIMIZATION = "requestBatteryOptimization";

		private static final String ACTION_OPEN_NOTIFICATION_SETTINGS = "openNotificationSettings";
	  private static final String FOLDER_PATH = "file:///android_asset/www/";
		public Activity activity;
		public ServiceParameters serviceParameters;
		CallbackContext callback;
		public DrawOverApps() {
			super();
		}

		@Override
		public boolean execute(String action, JSONArray options,
				final CallbackContext callbackContext) throws JSONException {
					activity = this.cordova.getActivity();
					serviceParameters = new ServiceParameters(activity);

					if (action.equals(ACTION_CHECK_OVER_APP_PERMISSION)) {
						if (Build.VERSION.SDK_INT >= 23) {
								if (!checkDrawOverAppsPermission(activity)) {
										callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "ERROR"));
								}
								else
								{
										callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "OK"));
								}

						}
									return true;
					} else
					if (action.equals(ACTION_REQUEST_OVER_APP_PERMISSION)) {
						if (Build.VERSION.SDK_INT >= 23) {
								if (!checkDrawOverAppsPermission(activity)) {
										Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + activity.getPackageName()));
										activity.startActivityForResult(intent, 0);
										callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Granting Draw Over Apps Permission "));
								}
								else
								{
										callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Permission already Granted "));
								}

						}
            			return true;
      		} else
					if (action.equals(ACTION_OPEN_OVER_APP_PERMISSION)) {
						if (Build.VERSION.SDK_INT >= 23) {
								Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:" + activity.getPackageName()));
								activity.startActivityForResult(intent, 0);
								callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "OK"));
						}
									return true;
					} else

					if (action.equals(ACTION_CHECK_BATTERY_OPTIMIZATION)) {
						PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
						if (Build.VERSION.SDK_INT >= 23) {
								if (!pm.isIgnoringBatteryOptimizations(activity.getPackageName())) {
										callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "ERROR"));
								}
								else
								{
										callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "OK"));
								}

						}
									return true;
					} else
					if (action.equals(ACTION_REQUEST_BATTERY_OPTIMIZATION)) {
						if (Build.VERSION.SDK_INT >= 23) {
							PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
							if (!pm.isIgnoringBatteryOptimizations(activity.getPackageName())) {
								Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + activity.getPackageName()));
								activity.startActivityForResult(intent, 0);
								callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Granting Ignore Battery Optimization Permission "));
							} else {
								callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Ignore Battery Optimization Permission already Granted "));
							}
						}
						return true;
					} else

					if (action.equals(ACTION_OPEN_NOTIFICATION_SETTINGS)){
						Intent intent = new Intent();
				    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				        intent.setAction(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
				        intent.putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
				    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
				        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
				        intent.putExtra("app_package", activity.getPackageName());
				        intent.putExtra("app_uid", activity.getApplicationInfo().uid);
				    } else {
				        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
				        intent.addCategory(Intent.CATEGORY_DEFAULT);
				        intent.setData(Uri.parse("package:" + activity.getPackageName()));
				    }
				    activity.startActivity(intent);
						callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "OK"));
						return true;
					} else
					if (action.equals(ACTION_OPEN_OVER_APP_VIEW)) {
								if (checkDrawOverAppsPermission(activity)) {
									if (options.getJSONObject(0).has("path")) {
										openServiceWithHTMLfile(callbackContext,options.getJSONObject(0) );
										callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "Draw Over Apps Should Start Successfully "));
									}

								}
								else
								{
										callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, "No Permission Granted for Draw Over Apps"));
								}

								return true;
					} else
					if (action.equals(ACTION_CLOSE_OVER_APP_VIEW)){
						 activity.stopService(new Intent(activity, OverAppsService.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
					}

        	return false;

		}

		public void openServiceWithHTMLfile(CallbackContext callbackContext,JSONObject option) throws JSONException {
				serviceParameters.setString(FOLDER_PATH + option.getString("path"),"file_path");
				if (option.has("hasHead")) {
						serviceParameters.setBoolean(option.getBoolean("hasHead"),"has_head");
				}
				if (option.has("layoutIcon")) {
						serviceParameters.setString(option.getString("layoutIcon"),"layout_icon");
				}
				if (option.has("dragToSide")) {
						serviceParameters.setBoolean(option.getBoolean("dragToSide"),"drag_to_side");
				}
				if (option.has("enableBackBtn")) {
						serviceParameters.setBoolean(option.getBoolean("enableBackBtn"),"enable_hardware_back");
				}
				if (option.has("enableCloseBtn")) {
						serviceParameters.setBoolean(option.getBoolean("enableCloseBtn"),"enable_close_btn");
				}
				if(option.has("verticalPosition")) {
					  serviceParameters.setString(option.getString("verticalPosition"),"vertical_position");
				 }
				if(option.has("horizontalPosition")) {
					  serviceParameters.setString(option.getString("horizontalPosition"),"horizontal_position");
				 }
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			     activity.startForegroundService(new Intent(activity, OverAppsService.class));
				} else {
			     activity.startService(new Intent(activity,OverAppsService.class));
				}

    }

		public static boolean checkDrawOverAppsPermission(Activity currentActivity) {
        Log.d("checkDrawPermission", "Called");
        if (Build.VERSION.SDK_INT >= 23) {
            if (!Settings.canDrawOverlays(currentActivity)) {
                Log.d("checkDrawPermission", "false");
                return false;
            } else {
                Log.d("checkDrawPermission", "true");
                return true;
            }
        } else {
            Log.d("checkDrawPermission", "true");
            return true;
        }

    }

		@Override
		public void onDestroy() {
			Log.i("TAG","DESTROY MAIN");
			activity.stopService(new Intent(activity, OverAppsService.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
			super.onDestroy();
		}

}
