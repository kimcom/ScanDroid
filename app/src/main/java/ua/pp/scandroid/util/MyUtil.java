package ua.pp.scandroid.util;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class MyUtil {
    private static final String PATH = "sdcard/";
    private static final String PATH_LOG = "ScanDroidLogs";
    public static int DIALOG_ACTIVITY = 999;

    public static void dialogActivity(final Activity activity, String title, String message, String btn_ok, String btn_cancel){
        Intent i = new Intent(activity.getApplicationContext(), FrmDialogActivity.class);
        // passing array index
        i.putExtra("title", title);
        i.putExtra("message", message);
        i.putExtra("btn_ok", btn_ok);
        i.putExtra("btn_cancel", btn_cancel);
        activity.startActivityForResult(i,DIALOG_ACTIVITY);
    }

    public static void errorToLog(String className, Exception err) {
        File dir = new File(PATH + PATH_LOG);
        if(!dir.exists()) dir.mkdir();
        Logger logger = Logger.getLogger(className);
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            File file = new File(PATH + dir.getName()+"/"+className+".log");
            if(!file.exists()) file.createNewFile();
            fh = new FileHandler(PATH + dir.getName()+"/"+className+".log",true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            // the following statement is used to log any messages
            //logger.info(err.getMessage());
            logger.log(Level.SEVERE, "Exception:",err);
            fh.close();
        } catch (SecurityException | IOException e) {
            //MyUtil.errorToLog(MyUtil.class.getName(), e);
            Log.e("MyUtil",e.getMessage());
        }
    }
    public static void errorToLog(String className, String error_info) {
        File dir = new File(PATH + PATH_LOG);
        if(!dir.exists()) dir.mkdir();
        Logger logger = Logger.getLogger(className);
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            File file = new File(PATH + dir.getName()+"/"+className+".log");
            if(!file.exists()) file.createNewFile();
            fh = new FileHandler(PATH + dir.getName()+"/"+className+".log",true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            // the following statement is used to log any messages
            //logger.info(error_info);
            logger.log(Level.SEVERE, "Exception:"+error_info);
            fh.close();
        } catch (SecurityException | IOException e) {
            //MyUtil.errorToLog(MyUtil.class.getName(), e);
            Log.e("MyUtil",e.getMessage());
        }
    }
    public static void messageToLog(String className, String error_info) {
        File dir = new File(PATH + PATH_LOG);
        if(!dir.exists()) dir.mkdir();
        Logger logger = Logger.getLogger(className);
        FileHandler fh;
        try {
            // This block configure the logger with handler and formatter
            File file = new File(PATH + dir.getName()+"/"+className+".log");
            if(!file.exists()) file.createNewFile();
            fh = new FileHandler(PATH + dir.getName()+"/"+className+".log",true);
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            // the following statement is used to log any messages
            logger.info(error_info);
            //logger.log(Level.SEVERE, "Exception:" + error_info);
            fh.close();
        } catch (SecurityException | IOException e) {
            Log.e("MyUtil",e.getMessage());
        }
    }
    public static void deleteLogDir(){
        File dir = new File(PATH + PATH_LOG);
        if(dir.exists()) deleteRecursive(dir);
    }
    private static void deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        fileOrDirectory.delete();
    }

    private static String getClassName(Class<?> clazz) {
        if (clazz != null) {
            if (!TextUtils.isEmpty(clazz.getSimpleName())) {
                return clazz.getSimpleName();
            }

            return getClassName(clazz.getEnclosingClass());
        }

        return "";
    }

    public static void errorDialog(final Activity activity, String title, int message, int positiveBtn, int negativeBtn){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        if (positiveBtn!=0) {
            builder.setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) { activity.finish(); }
            });
        }
        if (negativeBtn!=0) {
            builder.setNegativeButton(negativeBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
        builder.show();
    }
    public static void errorDialog(final Activity activity, String title, String message, int positiveBtn, int negativeBtn){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(false);
        if (positiveBtn!=0) {
            builder.setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) { activity.finish(); }
            });
        }
        if (negativeBtn!=0) {
            builder.setNegativeButton(negativeBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
        builder.show();
    }
    public static void alertDialog(final Activity activity, String title, int message, int positiveBtn, int negativeBtn){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        if (positiveBtn!=0) {
            builder.setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
        if (negativeBtn!=0) {
            builder.setNegativeButton(negativeBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
        builder.show();
    }
    public static void alertDialog(final Activity activity, String title, String message, int positiveBtn, int negativeBtn){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        if (positiveBtn!=0) {
            builder.setPositiveButton(positiveBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
        if (negativeBtn!=0) {
            builder.setNegativeButton(negativeBtn, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
        }
        builder.show();
    }
    public static void alertDialog2(final Activity activity, String title, String message, int positiveBtn, int negativeBtn){
        AlertDialog alertDialog = new AlertDialog.Builder(activity).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setCancelable(false);
        if (positiveBtn!=0) {
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, activity.getResources().getString(positiveBtn),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
        if (negativeBtn!=0) {
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, activity.getResources().getString(negativeBtn),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
        }
        alertDialog.show();
    }

    public static void createLogDir() {
        File dir = new File(PATH + PATH_LOG);
        if(!dir.exists()) dir.mkdir();
    }
}
