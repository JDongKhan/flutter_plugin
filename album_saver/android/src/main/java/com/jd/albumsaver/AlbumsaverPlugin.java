package com.jd.albumsaver;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

import androidx.annotation.NonNull;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** AlbumsaverPlugin */
public class AlbumsaverPlugin implements FlutterPlugin, MethodCallHandler {
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private MethodChannel channel;
  private Context context;

  AlbumsaverPlugin(Context context) {
    this.context = context;
  }

  @Override
  public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
    this.context = flutterPluginBinding.getApplicationContext();
    channel = new MethodChannel(flutterPluginBinding.getFlutterEngine().getDartExecutor(), "album_saver");
    channel.setMethodCallHandler(this);
  }

  // This static function is optional and equivalent to onAttachedToEngine. It supports the old
  // pre-Flutter-1.12 Android projects. You are encouraged to continue supporting
  // plugin registration via this function while apps migrate to use the new Android APIs
  // post-flutter-1.12 via https://flutter.dev/go/android-project-migration.
  //
  // It is encouraged to share logic between onAttachedToEngine and registerWith to keep
  // them functionally equivalent. Only one of onAttachedToEngine or registerWith will be called
  // depending on the user's project. onAttachedToEngine or registerWith must both be defined
  // in the same class.
  public static void registerWith(Registrar registrar) {
    Context context = registrar.activeContext().getApplicationContext();
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "album_saver");
    channel.setMethodCallHandler(new AlbumsaverPlugin(context));
  }

  @Override
  public void onMethodCall(@NonNull MethodCall call, @NonNull Result result) {
    if (call.method.equals("saveImageToGallery")) {
      byte[] image = (byte[])call.arguments;
      result.success(saveImageToGallery(BitmapFactory.decodeByteArray(image,0,image.length)));
    } else  if (call.method.equals("saveFileToGallery")) {
      String path = (String)call.arguments;
      result.success(saveFileToGallery(path));
    } else {
      result.notImplemented();
    }
  }

  @Override
  public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
    channel.setMethodCallHandler(null);
  }

  private  String getApplicationName () {
    ApplicationInfo ai = null;
    try {
      ai = context.getPackageManager().getApplicationInfo(context.getPackageName(),0);
    } catch (PackageManager.NameNotFoundException e) {
    }
    if (ai == null) {
      CharSequence charSequence = context.getPackageManager().getApplicationLabel(ai);
      String appName = charSequence.toString();
      return  appName;
    }
    return "image_gallery_saver";
  }

  private File generateFile(String extension) {
    String storePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + getApplicationName();
    File appDir = new File(storePath);
    if (!appDir.exists()) {
      appDir.mkdir();
    }
    String fileName = String.valueOf(System.currentTimeMillis());
    if (extension != null && !extension.isEmpty()) {
      fileName  += ("." + extension);
    }
    return new File(appDir,fileName);
  }

  private String  saveImageToGallery(Bitmap bmp) {
    File file = generateFile("png");
    try {
      FileOutputStream fos = new FileOutputStream(file);
      bmp.compress(Bitmap.CompressFormat.PNG,60,fos);
      fos.flush();
      fos.close();
      Uri uri = Uri.fromFile(file);
      context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
      return uri.toString();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return  "";
  }



  private String saveFileToGallery(String filePath) {
     try {
       String extension = filePath.substring(filePath.lastIndexOf("."));
       File originalFile = new File(filePath);
       File file = generateFile(extension);

       FileInputStream in = new FileInputStream(originalFile);
       FileOutputStream out = new FileOutputStream(file);

       byte[] buffer = new byte[1024];
       int length;
       while ((length = in.read(buffer)) > 0) {
         out.write(buffer,0,length);
       }

       Uri uri = Uri.fromFile(file);
      context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
      return uri.toString();
    } catch (IOException e) {
      e.printStackTrace();
    }
     return  "";
  }

}
