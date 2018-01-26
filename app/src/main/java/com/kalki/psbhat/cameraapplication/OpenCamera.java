package com.kalki.psbhat.cameraapplication;
import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.translate.Translate;
import com.google.api.services.translate.TranslateRequestInitializer;
import com.google.api.services.translate.model.TranslationsListResponse;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionRequestInitializer;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;



import org.apache.commons.io.output.ByteArrayOutputStream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class OpenCamera extends AppCompatActivity {
    Activity activity;
    static final int PIC_COUNT = 1;
    ImageView mImageView;
    private AdView mAdView;
    Spinner spinner;
    String[] lanuageCodes;
    String[] lanuages;
    public static final GoogleClientRequestInitializer KEY_INITIALIZER = new TranslateRequestInitializer("your-api-key");
    private Translate.Builder builder;
    List<EntityAnnotation> EnglishResult ;
    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_camera);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        mImageView = findViewById(R.id.capturedImage);
        activity = this;
        final TextView setSelectedLanguage = findViewById(R.id.selectedLanguage);

        //toast
        Toast toast = getCustomToast(this);
        toast.show();
        //start camera
        dispatchTakePictureIntent();
        //retake picture
        Button retakePicture = findViewById(R.id.retakePicture);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        retakePicture.startAnimation(animation);
        //launch camera activity
        retakePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });


        //ads
        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        //lanuage codes
        lanuageCodes = this.getResources().getStringArray(R.array.lanuage_code_array);
        lanuages = this.getResources().getStringArray(R.array.lanugae_arrray);
        //language preferences
        final SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.languageSettings), Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        spinner = findViewById(R.id.spinner);
        int selectedInt = sharedPreferences.getInt(getString(R.string.lanuagePreferrence), 1);
        spinner.setSelection(selectedInt);
        setSelectedLanguage.setText(lanuages[selectedInt]);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                editor.putInt(getString(R.string.lanuagePreferrence), i);
                editor.apply();
                setSelectedLanguage.setText(lanuages[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });



        // History
        ImageButton historyButton = findViewById(R.id.history);
        historyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);//
                startActivityForResult(Intent.createChooser(intent, "Select Picture"),REQUEST_IMAGE_CAPTURE);
            }
        });

        //request permission
        ActivityCompat.requestPermissions(OpenCamera.this,
                new String[]{Manifest.permission.CAMERA},
                1);
        ActivityCompat.requestPermissions(OpenCamera.this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            ActivityCompat.requestPermissions(OpenCamera.this,
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

    private Toast getCustomToast(Context context){
        //custom toast
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_open_camera,
                (ViewGroup) findViewById(R.id.custom_toast_container));
        TextView text = layout.findViewById(R.id.customToasttext);
        text.setText(getString(R.string.OpenCameraToast));
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        return toast;
    }



    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = null;
            //vision api
            Vision.Builder visionBuilder = new Vision.Builder(
                    new NetHttpTransport(),
                    new AndroidJsonFactory(),
                    null);

            visionBuilder.setVisionRequestInitializer(
                    new VisionRequestInitializer(getString(R.string.API_KEY)));

            if (extras != null) {
                imageBitmap = (Bitmap) extras.get("data");
                GetVisionResults getResponse = new GetVisionResults(imageBitmap, activity);
                getResponse.execute();
                mImageView.setImageBitmap(imageBitmap);
            }
            else {
                try {

                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data.getData());
                    bitmap = getResizedBitmap(bitmap, 500);
                    GetVisionResults getResponse = new GetVisionResults(bitmap, activity);
                    getResponse.execute();
                    mImageView.setImageBitmap(bitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
    }


    private class GetVisionResults extends AsyncTask<Void, Void, BatchAnnotateImagesResponse> {

        private Bitmap bitmap;
        private ProgressDialog progressDialog;
        private Context context;

        GetVisionResults(Bitmap bit, Context con){
            bitmap = bit ;
            context = con ;
        }

        @Override
        protected void onPreExecute(){
            progressDialog = new ProgressDialog(context);
            progressDialog.setTitle("Searching Results");
            progressDialog.setMessage("It may take a while..");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected BatchAnnotateImagesResponse doInBackground(Void... xxx) {
            com.google.api.services.vision.v1.model.Image newTakenImage = getImageEncodeImage(bitmap);
            Vision.Builder visionBuilder = new Vision.Builder(
                    new NetHttpTransport(),
                    new AndroidJsonFactory(),
                    null);

            visionBuilder.setVisionRequestInitializer(
                    new VisionRequestInitializer("your-api-key"));

            final Vision vision = visionBuilder.build();
            Feature desiredFeature = new Feature();
            desiredFeature.setType("LABEL_DETECTION");
            AnnotateImageRequest request = new AnnotateImageRequest();
            request.setImage(newTakenImage);
            request.setFeatures(Arrays.asList(desiredFeature));
            BatchAnnotateImagesRequest batchRequest =
                    new BatchAnnotateImagesRequest();
            batchRequest.setRequests(Arrays.asList(request));
            BatchAnnotateImagesResponse batchResponse =
                    null;
            try {
                batchResponse = vision.images().annotate(batchRequest).execute();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return batchResponse;
        }

        @Override
        protected void onPostExecute(BatchAnnotateImagesResponse result) {
            try{
                List<EntityAnnotation> searchResults = result.getResponses().get(0).getLabelAnnotations();
                EnglishResult = searchResults;
                progressDialog.cancel();
                GetTranslations getTranslations = new GetTranslations( 0,searchResults.get(0).getDescription(), R.id.englishResult1, R.id.germanResult1);
                getTranslations.execute();
                getTranslations = new GetTranslations( 1 , searchResults.get(1).getDescription(),R.id.englishResult2, R.id.germanResult2 );
                getTranslations.execute();
                getTranslations = new GetTranslations(2, searchResults.get(2).getDescription(), R.id.englishResult3, R.id.germanResult3);
                getTranslations.execute();
                getTranslations = new GetTranslations( 3, searchResults.get(3).getDescription(), R.id.englishResult4, R.id.germanResult4);
                getTranslations.execute();


            }
            catch (Exception e){
                Log.e("Error",""+e);
                progressDialog.setTitle("Error Occured !");
                progressDialog.setMessage("Please check your internet connection.");
                progressDialog.setCancelable(true);
                progressDialog.show();
            }

        }

        private com.google.api.services.vision.v1.model.Image getImageEncodeImage(Bitmap bitmap) {
            com.google.api.services.vision.v1.model.Image base64EncodedImage = new com.google.api.services.vision.v1.model.Image();
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] imageBytes = byteArrayOutputStream.toByteArray();
            // Base64 encode the JPEG
            base64EncodedImage.encodeContent(imageBytes);
            return base64EncodedImage;
        }
    }

    private class GetTranslations extends AsyncTask<Void, Void, TranslationsListResponse > {
        String translateThisString;
        private int englishID, germanID;
        int position;

        GetTranslations(int pos, String toBeTranslated, int engID, int gerID){
            position = pos ;
            translateThisString = toBeTranslated;
            englishID = engID;
            germanID = gerID;
        }


        @Override
        protected TranslationsListResponse  doInBackground(Void... xxx) {
            HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = AndroidJsonFactory.getDefaultInstance();
            builder = new Translate.Builder(httpTransport, jsonFactory, null);
            builder.setGoogleClientRequestInitializer(KEY_INITIALIZER);
            List<String> qList = new ArrayList<>();
            qList.add(translateThisString);
            SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.languageSettings), Context.MODE_PRIVATE);
            String languageCode = lanuageCodes[sharedPreferences.getInt(getString(R.string.lanuagePreferrence), 1)];
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
                TextView english1 = findViewById(englishID);
                english1.setText(EnglishResult.get(position).getDescription());
                TextView german1 = findViewById(germanID);
                Log.d("translation size - ", ""+result.getTranslations().size());
                german1.setText(result.getTranslations().get(0).getTranslatedText());

            }
            catch (Exception e){
             e.getStackTrace();
            }

        }

    }

    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }


}
