package ua.pp.scandroid.util;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Properties;

import ua.pp.scandroid.BuildConfig;

public final class ConfigReader {
    public static Context context = null;
    public final static String APP_NAME  = "ScanDroid";
    private static String CONF_FILE_NAME  = APP_NAME.concat(".conf");

    private static ConfigReader instance   = null;
    public String       SERVER_ADDRESS_1;
    public String       SERVER_ADDRESS_2;
    public String       SERVER_PORT;
    public String       SERVER_DB;
    public String       USER_NAME;
    public String       USER_PASS;
    public boolean      PERMISSION_WRITE_EXTERNAL_STORAGE = false;
    public boolean      PERMISSION_CAMERA = false;
    public int          RESET_CONFIG = 1001;
    public int			TIME_WAIT = 10;//in second
    public String       good_article = "";
    public String       good_name = "";

    private int getIntegerValue(Properties props, String paramName){
        int result = 0;
        try {
            String str = props.getProperty(paramName);
            if (!str.equals("")) {
                result = Integer.parseInt(str);
            }
        } catch (NumberFormatException e) {
            MyUtil.errorToLog(this.getClass().getName(), e);
        }
        return result;
    }
    private double getDoubleValue(Properties props, String paramName){
        double result = 0;
        try {
            String str = props.getProperty(paramName);
            if (str != null) {
                result = new Double(str);
            }
        } catch (NumberFormatException e) {
            MyUtil.errorToLog(this.getClass().getName(), e);
        }
        return result;
    }
    private void addInfoIntoConfig(String typeInfo){
        PrintWriter zzz = null;
        try {
//            File fileConf = new File(CONF_FILE_NAME);
//            zzz = new PrintWriter(new FileOutputStream(fileConf, true), true);
            FileOutputStream fos = context.openFileOutput(CONF_FILE_NAME, Context.MODE_WORLD_WRITEABLE);
            zzz = new PrintWriter(fos, true);
            if(typeInfo.equals("SERVERS")){
                //zzz.println("");
                zzz.println(";servers");
                zzz.println("SERVER_ADDRESS_1 = db.scandroid.pp.ua");
                zzz.println("SERVER_ADDRESS_2 = db.scandroid.pp.ua");
            }
            zzz.close();
        } catch (FileNotFoundException e) {
            MyUtil.errorToLog(this.getClass().getName(), e);
        }
    }
    public boolean createConfFile(){
        PrintWriter zzz = null;
        try {
//            FileOutputStream fos = new FileOutputStream(fileConf, true);
            FileOutputStream fos = context.openFileOutput(CONF_FILE_NAME, Context.MODE_PRIVATE);
            zzz = new PrintWriter(fos, true);
            zzz.println(";SETTING "+APP_NAME);
            zzz.println("FORM_TITLE      = Scan-Droid");
            zzz.println(";server");
//            zzz.println("SERVER_ADDRESS_1 = 127.0.0.1");
//            zzz.println("SERVER_ADDRESS_2 = 127.0.0.1");
//            zzz.println("SERVER_PORT     = 3306");
            zzz.println("SERVER_ADDRESS_1 = db.scandroid.pp.ua");
            zzz.println("SERVER_ADDRESS_2 = db.scandroid.pp.ua");
            zzz.println("SERVER_PORT     = 43306");
            zzz.println("SERVER_DB       = scandroid");
            zzz.println("");
            zzz.println("USER_NAME       = ");
            zzz.println("USER_PASS       = ");
//            zzz.println("MARKET_ID       = 1000");
//            zzz.println("USER_NAME       = 1000.001");
//            zzz.println("TERMINAL_ID     = 8");
            zzz.close();
            Log.v("file","create file is OK");
            return true;
        } catch (FileNotFoundException e) {
            //MyUtil.errorToLog(this.getClass().getName(), e);
            Log.v("file","error create file");
        }
        return false;
    }
    private ConfigReader(Context context) throws FileNotFoundException, UnsupportedEncodingException, IOException, URISyntaxException
    {
        this.context = context;
        //APP_VERSION = BuildConfig.VERSION_NAME;

        //createConfFile();

        //System.out.println("file: "+f.toString()+" exist: "+f.exists());
        FileInputStream file = null;
//        file = context.openFileInput(CONF_FILE_NAME);
//        Log.i("file",file.toString());


        File f = new File("/data/data/"+BuildConfig.APPLICATION_ID+"/files/scandroid.conf");
        //System.out.println(f.toString());
        if(f.exists()) context.deleteFile(CONF_FILE_NAME);

        if(f.exists()){
            file = context.openFileInput(CONF_FILE_NAME);
        }else{
            //System.out.println("Создаем файл конфигурации:"+CONF_FILE_NAME);
            //проверка создан ли файл
            if(!createConfFile()){
                //System.out.println("Не найден файл конфигурации:"+CONF_FILE_NAME);
                MyUtil.errorToLog(this.getClass().getName(),"Не найден файл конфигурации:"+CONF_FILE_NAME);
                return;
            }
            file = context.openFileInput(CONF_FILE_NAME);
        }

        //reset_SERVER_ADDRESS();

        String str = "";
        Properties props = new Properties();
        InputStreamReader inChars = new InputStreamReader(file,"UTF-8");
        props.load(inChars);

        str = props.getProperty("SERVER_ADDRESS_1");//проверим есть ли настройки для ценников
        if(str==null) {
            addInfoIntoConfig("SERVERS");
            file = context.openFileInput(CONF_FILE_NAME);
            inChars = new InputStreamReader(file, "UTF-8");
            props.load(inChars);
        }

        //FORM_TITLE      = props.getProperty("FORM_TITLE");
        SERVER_ADDRESS_1= props.getProperty("SERVER_ADDRESS_1");
        SERVER_ADDRESS_2= props.getProperty("SERVER_ADDRESS_2");
        SERVER_PORT     = props.getProperty("SERVER_PORT");
        SERVER_DB       = props.getProperty("SERVER_DB");
//        MARKET_ID		= getIntegerValue(props,"MARKET_ID");
        USER_NAME       = props.getProperty("USER_NAME");
//        TERMINAL_ID		= getIntegerValue(props,"TERMINAL_ID");

        //FORM_TITLE = FORM_TITLE + " v." + APP_VERSION;

//        System.out.println("FORM_TITLE:"+FORM_TITLE);
//        System.out.println("SERVER_ADDRESS_1:"+SERVER_ADDRESS_1);
//        System.out.println("SERVER_ADDRESS_2:"+SERVER_ADDRESS_2);
//        System.out.println("SERVER_PORT:"+SERVER_PORT);
//        System.out.println("SERVER_DB:"+SERVER_DB);
//        System.out.println("MARKET_ID:"+MARKET_ID);
//        System.out.println("USER_NAME:"+USER_NAME);
//        System.out.println("TERMINAL_ID:"+TERMINAL_ID);
    }

    private void reset_SERVER_ADDRESS() throws FileNotFoundException, UnsupportedEncodingException, IOException {
        if (RESET_CONFIG > 1000) return;
        StringBuilder builder = new StringBuilder();
        File fileConf = new File(CONF_FILE_NAME);
        if (!fileConf.exists()) {
            //DialogBoxs.viewMessage("Не найден файл конфигурации:\n" + CONF_FILE_NAME);
            return;
        }
        BufferedReader reader = new BufferedReader(new FileReader(fileConf));
        String line;
        while ((line = reader.readLine()) != null) {
            if (!line.startsWith("SERVER_ADDRESS") && !line.startsWith(";") && !line.equals(""))
                builder.append(line.concat("\r\n"));
            if (line.startsWith("SERVER_DB") || line.startsWith("TERMINAL_ID") || line.startsWith("PLANK_PADDING_TOP") || line.startsWith("EKKA_HOST") || line.startsWith("POS_MerchantIdx"))
                builder.append("\r\n");
        }
        FileWriter writer = new FileWriter(fileConf);
        writer.write(builder.toString());
        writer.close();
        reader.close();
    }

    public static ConfigReader getInstance(Context context) {
        if (instance == null) {
            try {
                instance = new ConfigReader(context);
            }catch (Exception e) {
                MyUtil.errorToLog(ConfigReader.class.getName(), e);
                instance = null;
                //DialogBoxs.viewError(e);
            }
        }
        return instance;
    }
}
