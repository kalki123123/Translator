package com.kalki.psbhat.cameraapplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;

import java.util.ArrayList;
import java.util.List;

public class TextTranslation extends AppCompatActivity {
    Spinner spinner;
    String[] lanuageCodes;
    String[] lanuages;
    public static final GoogleClientRequestInitializer KEY_INITIALIZER = new TranslateRequestInitializer("your-api-key");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_translation);
        final Button buttonAnylanguage = (Button) findViewById(R.id.anyLanguage);
        final Button buttonEnglish = (Button) findViewById(R.id.english);
        final EditText inputText = (EditText) findViewById(R.id.textInput);
        buttonAnylanguage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable img = getResources().getDrawable( R.drawable.ic_selected_language );
                buttonAnylanguage.setCompoundDrawablesWithIntrinsicBounds(null, null, img,null );
                buttonEnglish.setCompoundDrawables(null, null, null, null);
                SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.languageSettings), Context.MODE_PRIVATE);
                String languageCode = lanuageCodes[sharedPreferences.getInt(getString(R.string.lanuagePreferrence), 1)];
                GetTranslations getTranslations = new GetTranslations(inputText.getText().toString(), languageCode);
                getTranslations.execute();            }
        });
        buttonEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Drawable img = getResources().getDrawable( R.drawable.ic_selected_language );
                buttonEnglish.setCompoundDrawablesWithIntrinsicBounds(null, null, img,null );
                buttonAnylanguage.setCompoundDrawables(null, null, null, null);
                GetTranslations getTranslations = new GetTranslations(inputText.getText().toString(), "en");
                getTranslations.execute();
            }
        });

        //lanuage setting
        lanuageCodes = this.getResources().getStringArray(R.array.lanuage_code_array);
        lanuages = this.getResources().getStringArray(R.array.lanugae_arrray);
        //language preferences
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.languageSettings), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        spinner = findViewById(R.id.spinner);
        int selectedInt = sharedPreferences.getInt(getString(R.string.lanuagePreferrence), 1);
        spinner.setSelection(selectedInt);
        buttonAnylanguage.setText(lanuages[selectedInt]);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt(getString(R.string.lanuagePreferrence), i);
                editor.apply();
                buttonAnylanguage.setText(lanuages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }



    private class GetTranslations extends AsyncTask<Void, Void, TranslationsListResponse > {
        String translateThisString, languageCode;

        GetTranslations(String toBeTranslated, String langCode){
            translateThisString = toBeTranslated;
            languageCode = langCode;

        }


        @Override
        protected TranslationsListResponse  doInBackground(Void... xxx) {
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = AndroidJsonFactory.getDefaultInstance();
            Translate.Builder builder = new Translate.Builder(httpTransport, jsonFactory, null);
            builder.setGoogleClientRequestInitializer(KEY_INITIALIZER);
            List<String> qList = new ArrayList<>();
            qList.add(translateThisString);

            Translate.Translations.List list = null;
            TranslationsListResponse response = null;
            try
            {
                list = builder.build().translations().list(qList, languageCode); //your language code here~
                response = list.execute();

            } catch(Exception e)

            {
                e.printStackTrace();
            }
            return response;
        }

        @Override
        protected void onPostExecute(TranslationsListResponse result) {
            try{
                Log.d("response tranlsation :", ""+result.getTranslations().get(0).getTranslatedText());
                TextView output = findViewById(R.id.textOutput);
                output.setText(result.getTranslations().get(0).getTranslatedText());
                output.setFocusable(false);
                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(output.getWindowToken(),
                            InputMethodManager.RESULT_UNCHANGED_SHOWN);
                }
            }
            catch (Exception e){
                e.getStackTrace();
            }

        }

    }


}
