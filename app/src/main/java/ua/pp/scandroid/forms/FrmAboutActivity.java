package ua.pp.scandroid.forms;

import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.ContextThemeWrapper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import ua.pp.scandroid.R;

public class FrmAboutActivity extends AppCompatActivity {
/*
    @Override
    public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
        Context themedContext = new ContextThemeWrapper(context, android.R.style.Theme_Material_Dialog_Alert);

// in LayoutInflater
        View view = LayoutInflater.from(themedContext)
                .inflate(R.layout.activity_about, null);
        return super.onCreateView(parent, name, context, attrs);
    }
*/
    /*    @Override
        public Resources.Theme getTheme() {
            Resources.Theme theme = super.getTheme();
            theme.applyStyle(android.R.style.Theme_DeviceDefault_Dialog_Alert, true);
            // you could also use a switch if you have many themes that could apply
            return theme;
            //return super.getTheme();
        }
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setTheme(android.R.style.Theme_DeviceDefault_Dialog_Alert);
        setContentView(R.layout.activity_about);
        //setTheme(android.R.style.Theme_Light_Panel);
        //setTheme(android.R.style.Theme_Holo_Light_Dialog);
    }
}
