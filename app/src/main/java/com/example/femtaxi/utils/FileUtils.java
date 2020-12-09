package com.example.femtaxi.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;

import androidx.core.content.FileProvider;

import com.example.femtaxi.BuildConfig;
import com.example.femtaxi.helpers.Constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class FileUtils {

    private static final int EOF = -1;
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    private FileUtils() {

    }

    public static File from(Context context, Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        String fileName = getFileName(context, uri);
        String[] splitName = splitFileName(fileName);
        File tempFile = File.createTempFile(splitName[0], splitName[1]);
        tempFile = rename(tempFile, fileName);
        tempFile.deleteOnExit();
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(tempFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (inputStream != null) {
            copy(inputStream, out);
            inputStream.close();
        }

        if (out != null) {
            out.close();
        }
        return tempFile;
    }

    private static String[] splitFileName(String fileName) {
        String name = fileName;
        String extension = "";
        int i = fileName.lastIndexOf(".");
        if (i != -1) {
            name = fileName.substring(0, i);
            extension = fileName.substring(i);
        }

        return new String[]{name, extension};
    }

    private static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf(File.separator);
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    private static File rename(File file, String newName) {
        File newFile = new File(file.getParent(), newName);
        if (!newFile.equals(file)) {
            if (newFile.exists() && newFile.delete()) {
                Log.d("FileUtil", "Delete old " + newName + " file");
            }
            if (file.renameTo(newFile)) {
                Log.d("FileUtil", "Rename file to " + newName);
            }
        }
        return newFile;
    }

    private static long copy(InputStream input, OutputStream output) throws IOException {
        long count = 0;
        int n;
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        while (EOF != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static void pickImageGallery(Activity activity) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        activity.startActivityForResult(Intent.createChooser(intent, "Select Imagen"), Constants.PERMISSION.PICK_IMAGE_REQUEST);
    }

    public static Uri pickImageCamera(Activity activity, int requestCode) {
        Intent intent = new Intent();
        intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        Uri uriTemp = getMediaTempUri(activity, "IMG", "jpg");
        try {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriTemp);
            intent.putExtra("return-data", true);
            try {
                if (intent.resolveActivity(activity.getPackageManager()) != null)
                    activity.startActivityForResult(intent, requestCode);
            } catch (ActivityNotFoundException e) {
                if (intent.resolveActivity(activity.getPackageManager()) != null)
                    activity.startActivityForResult(Intent.createChooser(intent, null),
                            requestCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uriTemp;
    }

    public static Uri getMediaTempUri(Activity activity, String prefix, String extension) {
        String timeStamp = new SimpleDateFormat("HHmmssdMMyyyy")
                .format(Calendar.getInstance(Locale.getDefault()).getTime());
        String name = String.format("%s_%s.%s",
                prefix, timeStamp.replace(" ", "-"), extension);
        File fileTemp = new File(getFile(), name);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(activity,
                    BuildConfig.APPLICATION_ID, fileTemp);
        } else {
            return Uri.fromFile(fileTemp);
        }
    }

    public static File getFile() {
        String PATH_FOLDER_APP = "/" + BuildConfig.PATH_NAME_APP;
        String PATH_FOLDER_APP_MEDIA = "/Media";
        String PATH_FOLDER_APP_MEDIA_IMAGES = PATH_FOLDER_APP_MEDIA + "" + PATH_FOLDER_APP + " Images";

        File folder = new File(Environment.getExternalStorageDirectory() + File.separator
                + BuildConfig.PATH_NAME_APP + PATH_FOLDER_APP_MEDIA_IMAGES);
        folder.mkdirs();
        return folder;
    }
}
