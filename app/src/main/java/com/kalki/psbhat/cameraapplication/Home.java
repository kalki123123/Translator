package com.kalki.psbhat.cameraapplication;
import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class Home extends AppCompatActivity  {
    private AdView mAdView;
    Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        //set animations
        TextView infoTextHead = findViewById(R.id.appInfo);
        Animation animation;
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        infoTextHead.startAnimation(animation);
        TextView infoTextTail = findViewById(R.id.appDescription);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.bounce);
        infoTextTail.startAnimation(animation);
        Button translateNow =  findViewById(R.id.translateNow);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        translateNow.startAnimation(animation);

        //launch camera activity
        translateNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentOpencamera = new Intent(getApplication(), OpenCamera.class);
                startActivity(intentOpencamera);
            }
        });

        //text translation
        Button textTranslate =  findViewById(R.id.textTranslate);
        animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        textTranslate.startAnimation(animation);
        textTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent textTranslate = new Intent(getApplication(), TextTranslation.class);
                startActivity(textTranslate);
            }
        });

       // Youtube Player
        //playerFragment = (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_player_fragment);
        //playerFragment.initialize(YouTubeKey, this);


        //ads
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //language preferences
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.languageSettings), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        spinner = findViewById(R.id.spinner);
        spinner.setSelection(sharedPreferences.getInt(getString(R.string.lanuagePreferrence), 1));
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt(getString(R.string.lanuagePreferrence), i);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //request permission
        ActivityCompat.requestPermissions(Home.this,
                new String[]{Manifest.permission.CAMERA},
                1);
        ActivityCompat.requestPermissions(Home.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(Home.this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }


    }



    @Override
    public void onResume() {
        super.onResume();
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.languageSettings), Context.MODE_PRIVATE);
        spinner = findViewById(R.id.spinner);
        spinner.setSelection(sharedPreferences.getInt(getString(R.string.lanuagePreferrence), 1));
    }


}
