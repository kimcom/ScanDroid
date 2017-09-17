package ua.pp.scandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import ua.pp.scandroid.forms.FrmLoginActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //изменение языка приложения
//        Locale locale = new Locale("en");
//        Locale.setDefault(locale);
//        Configuration configuration = new Configuration();
//        configuration.locale = locale;
//        getBaseContext().getResources().updateConfiguration(configuration, null);
        //System.out.println("MyTask");
//        try {
//            Thread.sleep(500);
//        } catch (InterruptedException e) {
//        }

        startFrmLogin();
    }

    private void startFrmLogin(){
        Intent intent = new Intent(this, FrmLoginActivity.class);
        startActivity(intent);
        finish();
    }
}
