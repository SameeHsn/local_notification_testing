package com.dexterous.flutterlocalnotifications;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Keep;
import androidx.core.app.NotificationManagerCompat;

import com.dexterous.flutterlocalnotifications.models.NotificationDetails;
import com.dexterous.flutterlocalnotifications.utils.StringUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import android.os.Bundle;
import java.lang.Exception;

import io.sentry.SentryLevel;
import io.sentry.android.core.SentryAndroid;
import android.app.Application;
import io.sentry.Sentry;

import java.text.ParseException;
import java.text.*;
import android.app.NotificationManager;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;

import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import java.util.HashMap;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.Duration;

import java.io.IOException;
import java.util.List;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import java.util.HashSet;
import java.util.Set;
/** Created by michaelbui on 24/3/18. */
@Keep
public class ScheduledNotificationReceiver extends BroadcastReceiver {

  private static final String TAG = "ScheduledNotifReceiver";
  private static final String SHARED_PREFERENCES_NAME = "FlutterSharedPreferences";
  private static final String FLUTTER_DELAYED_NNOTIFICATION_KEY = "flutter.FLUTTER_DELAYED_NOTIFICATION_KEY";
  private static final String FLUTTER_DEBUGGING_NOTIFICATION_KEY = "flutter.FLUTTER_DEBUGGING_NOTIFICATION_KEY";
  private static final String FLUTTER_IS_DEBUG_MODE_KEY = "flutter.NOTIFICATION_DEBUGGING_SETTINGS";
  private static SharedPreferences preferences;

  //api calling
  private static Retrofit retrofit;
  private static final String BASE_URL = "https://google";
  @Override
  @SuppressWarnings("deprecation")
  public void onReceive(final Context context, Intent intent) {
    //makin Obj for sharedpreferennce
    preferences=context.getSharedPreferences(SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
    
    String notificationDetailsJson =
        intent.getStringExtra(FlutterLocalNotificationsPlugin.NOTIFICATION_DETAILS);
    if (StringUtils.isNullOrEmpty(notificationDetailsJson)) {
      // This logic is needed for apps that used the plugin prior to 0.3.4

      Notification notification;
      int notificationId = intent.getIntExtra("notification_id", 0);

      if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
        notification = intent.getParcelableExtra("notification", Notification.class);
      } else {
        notification = intent.getParcelableExtra("notification");
      }

      if (notification == null) {
        // This means the notification is corrupt
        FlutterLocalNotificationsPlugin.removeNotificationFromCache(context, notificationId);
        Log.e(TAG, "Failed to parse a notification from  Intent. ID: " + notificationId);
        return;
      }

      notification.when = System.currentTimeMillis();
      NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
      notificationManager.notify(notificationId, notification);
      boolean repeat = intent.getBooleanExtra("repeat", false);
      if (!repeat) {
        FlutterLocalNotificationsPlugin.removeNotificationFromCache(context, notificationId);
      }
    } else {
      Gson gson = FlutterLocalNotificationsPlugin.buildGson();
      Type type = new TypeToken<NotificationDetails>() {}.getType();
      NotificationDetails notificationDetails = gson.fromJson(notificationDetailsJson, type);

      FlutterLocalNotificationsPlugin.showNotification(context, notificationDetails);
      FlutterLocalNotificationsPlugin.scheduleNextNotification(context, notificationDetails);

       boolean isDebugModeEnable = getBoolValue(FLUTTER_IS_DEBUG_MODE_KEY);

      // ApiService apiService = getRetrofitInstance().create(ApiService.class);

//      Response<YourModelClass> getTheCall = apiService.getSomeData();
//      if (getTheCall.isSuccessful()){
//        getTheCall.body(); // TODO In this you will get all your data
//      }else {
//
//      }
//
//      HashMap<String,String> thisisMyHash = new HashMap<>();
//      thisisMyHash.put("key","value");
//
//      YourModelClass alpha = new YourModelClass(1,2,"demo title","demo body");
//
//      Response<YourModelClass> postTheCall = apiService.postSomeData(thisisMyHash);
//      if (postTheCall.isSuccessful()){
//
//      }else {
//
//      }


        String isPowerSavingModeOn="";
        String isDoNotDisturbOn="";
        String isBatteryOptimizationEnabled="";

        Date date = new Date();
        SimpleDateFormat dashDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        String formattedCurrentDateTime = dashDateTimeFormat.format(date);

        long epochMilli = ZonedDateTime.of(
                LocalDateTime.parse(notificationDetails.scheduledDateTime),
                ZoneId.of(notificationDetails.timeZoneName)).toInstant().toEpochMilli();
        Instant instant = Instant.ofEpochMilli(epochMilli);
        ZoneId zoneId = ZoneId.systemDefault();

        LocalDateTime localDateTimeOfSchedualNotification = instant.atZone(zoneId).toLocalDateTime();

        String schedualTime=localDateTimeOfSchedualNotification.toString();
        String formatedSchedualDateTime=schedualTime.split("T")[0]+" "+ schedualTime.split("T")[1]+":00";
        Date cTime = new Date();
        Date sTime = new Date();


        try{
          cTime=dashDateTimeFormat.parse(formattedCurrentDateTime);
          sTime=dashDateTimeFormat.parse(formatedSchedualDateTime);
        }
        catch (Exception e) {
          Log.e("ParseException",e.toString());
        }

        Instant instant1 = cTime.toInstant();
        Instant instant2 = sTime.toInstant();

        // Calculate the difference in milliseconds
        long millisecondsDifference = Duration.between(instant2, instant1).toMillis();
        long inSeconds=millisecondsDifference/1000;

//      Log.d("----millisecondsDifference:",String.valueOf(inSeconds));
//      int result = cTime.compareTo(sTime);
//      Log.d("----current date time:",String.valueOf(date));
//      Log.d("----dashDateTimeFormat:",String.valueOf(dashDateTimeFormat));
//      Log.d("----formattedCurrentDateTime current date time:",String.valueOf(formattedCurrentDateTime));
//      Log.d("----localDateTimeOfSchedualNotification:",String.valueOf(localDateTimeOfSchedualNotification));
//      Log.d("----localDateTimeOfSchedualNotification as  String:",String.valueOf(schedualTime));
//      Log.d("----formatedSchedualDateTime:",String.valueOf(formatedSchedualDateTime));
//      Log.d("----cTime as parse:",String.valueOf(cTime));
//      Log.d("----sTime as parse:",String.valueOf(sTime));
//      Log.d("----sTimeWith20SecondAdded added 20 Seconds:",String.valueOf(sTimeWith20SecondAdded));
//      Log.d("----result:",String.valueOf(result));

        if (isPowerSavingModeOn(context)) {
          Log.d("isPowerSavingModeOn?:", "True");
          isPowerSavingModeOn="True";
        } else {
          Log.d("isPowerSavingModeOn?:", "False");
          isPowerSavingModeOn="False";
        }
        if (isDoNotDisturbOn(context)) {
          Log.d("isDoNotDisturbOn?:", "True");
          isDoNotDisturbOn="True";
        } else {
          Log.d("isDoNotDisturbOn?:", "False");
          isDoNotDisturbOn="False";
        }
        if (isBatteryOptimizationEnabled(context)) {
          Log.d("isBatteryOptimizationEnabled?:", "True");
          isBatteryOptimizationEnabled="True";

        } else {
          Log.d("isBatteryOptimizationEnabled?:", "False");
          isBatteryOptimizationEnabled="False";
        }


        String baseString=  "currentDateTime: " + formattedCurrentDateTime.toString() +" ,scheduledDateTime: " + formatedSchedualDateTime + " ,isPowerSavingModeOn: " +isPowerSavingModeOn.toString() + " ,isDoNotDisturbOn: " +isDoNotDisturbOn.toString() +" ,isBatteryOptimizationEnabled: " + isBatteryOptimizationEnabled.toString() +" ,noitification_title: " + notificationDetails.title.toString();
        if (inSeconds>20) {
          Log.d("---------------result:","Delayed Notification");
          try {
            Log.d("baseString:",baseString);
            HashMap<String, String> saveValue = new HashMap<String, String>();
            saveValue.put("currentDateTime",formattedCurrentDateTime.toString());
            saveValue.put("scheduledDateTime",formatedSchedualDateTime);
            saveValue.put("isPowerSavingModeOn",isPowerSavingModeOn.toString());
            saveValue.put("isDoNotDisturbOn",isDoNotDisturbOn.toString());
            saveValue.put("isBatteryOptimizationEnabled",isBatteryOptimizationEnabled.toString());

            String hashMapString = gson.toJson(saveValue);
            storePref(context,FLUTTER_DELAYED_NNOTIFICATION_KEY,hashMapString);
            throw new Exception(baseString);
          } catch (Exception e) {
            Sentry.captureException(e);
          }
          if(isDebugModeEnable) {
            Set<String> getStringSetToEdit = new HashSet<>();

            Log.d("isDebugModeEnable:", String.valueOf(isDebugModeEnable));
            HashMap<String, String> debuggingValue = new HashMap<String, String>();

            debuggingValue.put("id",notificationDetails.id.toString());
            debuggingValue.put("title",notificationDetails.title.toString());
            debuggingValue.put("body",notificationDetails.body);
            debuggingValue.put("playSound",notificationDetails.playSound.toString());
            debuggingValue.put("currentDateTime",formattedCurrentDateTime.toString());
            debuggingValue.put("scheduledDateTime",formatedSchedualDateTime);
            debuggingValue.put("isPowerSavingModeOn",isPowerSavingModeOn.toString());
            debuggingValue.put("isDoNotDisturbOn",isDoNotDisturbOn.toString());
            debuggingValue.put("isBatteryOptimizationEnabled",isBatteryOptimizationEnabled.toString());

            String hashMapString = gson.toJson(debuggingValue);

            getStringSetToEdit.add(hashMapString);
            Log.d("hashMapString",String.valueOf(hashMapString));
            Log.d("tempGetDataSet",String.valueOf(getStringSetToEdit));
            storePrefList(context,FLUTTER_DEBUGGING_NOTIFICATION_KEY,getStringSetToEdit);
            getPrefList(FLUTTER_DEBUGGING_NOTIFICATION_KEY);
          }
        }
        else{
          Log.d("---------------result:","Not Delayed Notification");
        }
    }
  }
  public boolean isPowerSavingModeOn(Context context) {
    PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    return powerManager != null && powerManager.isPowerSaveMode();
  }
  public static boolean isDoNotDisturbOn(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
      if (notificationManager != null) {
        int currentInterruptionFilter = notificationManager.getCurrentInterruptionFilter();
        return currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_ALARMS ||
                currentInterruptionFilter == NotificationManager.INTERRUPTION_FILTER_PRIORITY;
      }
    }
    return false;
  }

  public static boolean isBatteryOptimizationEnabled(Context context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      String packageName = context.getPackageName();
      PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);

      if (powerManager != null) {
        return !powerManager.isIgnoringBatteryOptimizations(packageName);
      }
    }
    return false;
  }

  //sharedpreference method
  public static void storePref(Context context,String key, String value) {
    preferences.edit().putString(key, value).commit();
  }

  public static void storePrefList(Context context,String key, Set<String> value) {
    Gson gson = FlutterLocalNotificationsPlugin.buildGson();
    preferences.edit().putString(key, gson.toJson(value)).commit();
  }

  public static void getPrefList(String key) {
    Gson gson = FlutterLocalNotificationsPlugin.buildGson();
    Type type = new TypeToken<HashSet>() {}.getType();
    Set<String> getStringSet = new HashSet<>();

    String stringSet = preferences.getString(key,"");
    Log.d("stringSet as String",String.valueOf(stringSet));

    getStringSet=gson.fromJson(stringSet,type);

    Log.d("getStringSet as list",String.valueOf(getStringSet));
  }
  public static void getPref(String key) {
    String result=preferences.getString(key,"");
     Log.d("result is:", result);
  }
  public static boolean getBoolValue(String key) {
    boolean result=preferences.getBoolean(key,false);
//    Log.d("result is:", String.valueOf(result));
    return result;
  }

  public static void getKeys(Context context) {
    Log.d("-----getKeys:",preferences.getAll().toString());
  }

  //api calling
  public static Retrofit getRetrofitInstance() {
    if (retrofit == null) {
      retrofit = new Retrofit.Builder()
              .baseUrl(BASE_URL)
              .addConverterFactory(GsonConverterFactory.create())
              .client(provideOkHttpClient())
              .build();
    }
    return retrofit;
  }

  public static OkHttpClient provideOkHttpClient() {
    Interceptor interceptor = new Interceptor() {
      @Override
      public okhttp3.Response intercept(Chain chain) throws IOException {
        Request originalRequest = chain.request();
        Request modifiedRequest = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("session", "") //put your header like this
                .build();
        return chain.proceed(modifiedRequest);
      }
    };

    return new OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build();
  }
}

interface ApiService {
  @GET("end-points")
    // Replace "endpoint" with the actual endpoint of your API
  Response<YourModelClass> getSomeData();

  @POST("end-points")
    // Replace "endpoint" with the actual endpoint of your API
  Response<YourModelClass> postSomeData(@Body HashMap<String,String> myHashMap);
}


class YourModelClass {
  private int userId;
  private int id;
  private String title;
  private String body;


  public YourModelClass(int userId, int id, String title, String body) {
    this.userId = userId;
    this.id = id;
    this.title = title;
    this.body = body;
  }

  public int getUserId() {
    return userId;
  }

  public void setUserId(int userId) {
    this.userId = userId;
  }

  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}
