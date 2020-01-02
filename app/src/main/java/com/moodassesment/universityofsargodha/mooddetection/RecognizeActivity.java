//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Emotion-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package com.moodassesment.universityofsargodha.mooddetection;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.face.FirebaseVisionFace;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector;
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions;
/*import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;
import com.microsoft.projectoxford.face.FaceServiceRestClient;
import com.microsoft.projectoxford.face.contract.Face;*/

import java.util.List;

public class RecognizeActivity extends AppCompatActivity {

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private Button mButtonSelectImage, mButtonContinue;

    // The URI of the image selected to detect.
    private Uri mImageUri;

    // The image selected to detect.
    private Bitmap mBitmap;
    private ProgressBar progressBar;

    // The edit to show status and result.
    private TextView mEditText;

    //  private EmotionServiceClient client;
    FirebaseVisionFaceDetectorOptions options;
    FirebaseVisionFaceDetector detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognize);
        // FirebaseApp.initializeApp(this);

        options = new FirebaseVisionFaceDetectorOptions.Builder()
                .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
                .enableTracking()
                .build();


        detector = FirebaseVision.getInstance().getVisionFaceDetector(options);

    /*    if (client == null) {
            client = new EmotionServiceRestClient(getString(R.string.subscription_key));
        }
    */
        mButtonSelectImage = (Button) findViewById(R.id.buttonSelectImage);
        mButtonContinue = (Button) findViewById(R.id.buttonContinue);
        mEditText = (TextView) findViewById(R.id.editTextResult);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar_detect);
        mButtonContinue.setEnabled(false);
    }

    // Called when the "Select Image" button is clicked.
    public void selectImage(View view) {
        mEditText.setText("");
        mButtonContinue.setEnabled(false);
        Intent intent;
        intent = new Intent(RecognizeActivity.this, SelectImageActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_IMAGE);
    }


    Task<List<FirebaseVisionFace>> result;
    String mood="";
    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("RecognizeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                if (resultCode == RESULT_OK) {
                    // If image is selected successfully, set the image URI and bitmap.
                    mImageUri = data.getData();
                    progressBar.setVisibility(View.VISIBLE);
                    mButtonContinue.setEnabled(false);
                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                        imageView.setImageBitmap(mBitmap);
                        // Add detection log.
                        Log.d("RecognizeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth() + "x" + mBitmap.getHeight());


                        FirebaseVisionImage firebaseImage = FirebaseVisionImage.fromBitmap(mBitmap);
                        result = detector.detectInImage(firebaseImage).addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionFace>>() {
                            @Override
                            public void onSuccess(List<FirebaseVisionFace> faces) {
                                String final_result = "Number of faces is " + faces.size() + "\n";
                                int i = 1;
                                for (FirebaseVisionFace face : faces) {
                                    final_result = final_result.concat("Face No: " + i + " = ");
                                    float smiling = face.getSmilingProbability();
                                    if (smiling > 0.8) {
                                        final_result = final_result.concat("Surprised \n");    //surprise
                                        mood="surprise";
                                    } else if (smiling > 0.6) {
                                        final_result = final_result.concat("Happy \n");   //smiling
                                        mood="happy";
                                    } else if (smiling > 0.5) {
                                        final_result = final_result.concat("Neutral \n");//neutral
                                        mood="neutral";
                                    } else if (smiling > 0.45) {
                                        final_result = final_result.concat("Disgust \n");//disgust
                                        mood="disgust";
                                    } else if (smiling > 0.35) {
                                        final_result = final_result.concat("Contempt \n");//contempt
                                        mood="contempt";
                                    } else if (smiling > 0.25) {
                                        final_result = final_result.concat("Angry \n");//angry
                                        mood="angry";
                                    } else if (smiling <= 0.25) {
                                        final_result = final_result.concat("Sad \n");//sad
                                        mood="sad";
                                    }
                                    Log.d(TAG, "****************************");
                                    Log.d(TAG, "face [" + face + "]");
                                    Log.d(TAG, "Smiling Prob [" + face.getSmilingProbability() + "]");
                                    Log.d(TAG, "Left eye open [" + face.getLeftEyeOpenProbability() + "]");
                                    Log.d(TAG, "Right eye open [" + face.getRightEyeOpenProbability() + "]");
                                    checkFaceExpression(face);
                                    i++;
                                }
                                mEditText.setText(final_result);
                                progressBar.setVisibility(View.INVISIBLE);
                                mButtonSelectImage.setEnabled(true);
                                mButtonContinue.setEnabled(true);
                                if(mood.equals(""))
                                    mood="sad";

                                mButtonContinue.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        Intent intent = new Intent(RecognizeActivity.this, ChangeMoodActivity.class);
                                        intent.putExtra("mood", mood);
                                        startActivity(intent);
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG, "Exception [" + e.getLocalizedMessage() + "]");
                                Log.d(TAG, "Exception [" + e + "]");
                            }
                        });
                    }


                }
                break;
            default:
                break;
        }
    }

    public static String TAG = "RIZWAN";

    private void checkFaceExpression(FirebaseVisionFace face) {
        if (face.getSmilingProbability() > 0.5) {
            Log.d(TAG, "**** Smiling ***");
            // listener.onSuccess(FACE_STATUS.SMILING);
        }
        if (face.getLeftEyeOpenProbability() < 0.2 && face.getLeftEyeOpenProbability() != -1 && face.getRightEyeOpenProbability() > 0.5) {
            Log.d(TAG, "Right Open..");
            // listener.onSuccess(FACE_STATUS.RIGHT_EYE_OPEN_LEFT_CLOSE);
        }
        if (face.getRightEyeOpenProbability() < 0.2 && face.getRightEyeOpenProbability() != -1 && face.getLeftEyeOpenProbability() > 0.5) {
            Log.d(TAG, "Left Open..");
            //  listener.onSuccess(FACE_STATUS.LEFT_EYE_OPEN_RIGHT_CLOSE);
        }
        // listener.onSuccess(FACE_STATUS.LEFT_OPEN_RIGHT_OPEN);
    }


/*    private List<RecognizeResult> processWithAutoFaceDetection() throws EmotionServiceException, IOException {
        Log.d("emotion", "Start emotion detection with auto-face detection");

        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long startTime = System.currentTimeMillis();
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE STARTS HERE
        // -----------------------------------------------------------------------

        List<RecognizeResult> result = null;
        //
        // Detect emotion by auto-detecting faces in the image.
        //
        result = this.client.recognizeImage(inputStream);

        String json = gson.toJson(result);
        Log.d("result", json);

        Log.d("emotion", String.format("Detection done. Elapsed time: %d ms", (System.currentTimeMillis() - startTime)));
        // -----------------------------------------------------------------------
        // KEY SAMPLE CODE ENDS HERE
        // -----------------------------------------------------------------------
        return result;
    }

    public void doRecognize() {
        mButtonSelectImage.setEnabled(false);
        // Do emotion detection using auto-detected faces.
        try {
            new doRequest(false).execute();
        } catch (Exception e) {
            mEditText.append("Error encountered. Exception is: " + e.toString());
        }

        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        if (faceSubscriptionKey.equalsIgnoreCase("9baf86ab1e9c41c5b480b2a180a16448")) {
            //mEditText.append("\n\nThere is no face subscription key in res/values/strings.xml. Skip the sample for detecting emotions using face rectangles\n");
        } else {
            // Do emotion detection using face rectangles provided by Face API.
            try {
                new doRequest(true).execute();
            } catch (Exception e) {
                mEditText.append("Error encountered. Exception is: " + e.toString());
            }
        }
    }
    private List<RecognizeResult> processWithFaceRectangles() throws EmotionServiceException, com.microsoft.projectoxford.face.rest.ClientException, IOException {
        Log.d("emotion", "Do emotion detection with known face rectangles");
        Gson gson = new Gson();

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        long timeMark = System.currentTimeMillis();
        Log.d("emotion", "Start face detection using Face API");
        FaceRectangle[] faceRectangles = null;
        String faceSubscriptionKey = getString(R.string.faceSubscription_key);
        FaceServiceRestClient faceClient = new FaceServiceRestClient(faceSubscriptionKey);
        Face faces[] = faceClient.detect(inputStream, false, false, null);
        Log.d("emotion", String.format("Face detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));

        if (faces != null) {
            faceRectangles = new FaceRectangle[faces.length];

            for (int i = 0; i < faceRectangles.length; i++) {
                // Face API and Emotion API have different FaceRectangle definition. Do the conversion.
                com.microsoft.projectoxford.face.contract.FaceRectangle rect = faces[i].faceRectangle;
                faceRectangles[i] = new com.microsoft.projectoxford.emotion.contract.FaceRectangle(rect.left, rect.top, rect.width, rect.height);
            }
        }

        List<RecognizeResult> result = null;
        if (faceRectangles != null) {
            inputStream.reset();

            timeMark = System.currentTimeMillis();
            Log.d("emotion", "Start emotion detection using Emotion API");
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE STARTS HERE
            // -----------------------------------------------------------------------
            result = this.client.recognizeImage(inputStream, faceRectangles);

            String json = gson.toJson(result);
            Log.d("result", json);
            // -----------------------------------------------------------------------
            // KEY SAMPLE CODE ENDS HERE
            // -----------------------------------------------------------------------
            Log.d("emotion", String.format("Emotion detection is done. Elapsed time: %d ms", (System.currentTimeMillis() - timeMark)));
        }
        return result;
    }

    private class doRequest extends AsyncTask<String, String, List<RecognizeResult>> {
        // Store error message
        private Exception e = null;
        private boolean useFaceRectangles = false;

        public doRequest(boolean useFaceRectangles) {
            this.useFaceRectangles = useFaceRectangles;
        }

        @Override
        protected List<RecognizeResult> doInBackground(String... args) {
            if (this.useFaceRectangles == false) {
                try {
                    return processWithAutoFaceDetection();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            } else {
                try {
                    return processWithFaceRectangles();
                } catch (Exception e) {
                    this.e = e;    // Store error
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<RecognizeResult> result) {
            super.onPostExecute(result);
            // Display based on error existence

            if (this.useFaceRectangles == false) {
                mEditText.setText("\n\nRecognizing emotions with auto-detected face rectangles...\n");
            } else {
                mEditText.setText("\n\nRecognizing emotions with existing face rectangles from Face API...\n");
            }
            if (e != null) {
                mEditText.setText("Error: " + e.getMessage());
                this.e = null;
            } else {
                if (result.size() == 0) {
                    mEditText.setText("No emotion detected :(");
                } else {
                    Integer count = 0;
                    // Covert bitmap to a mutable bitmap by copying it
                    Bitmap bitmapCopy = mBitmap.copy(Bitmap.Config.ARGB_8888, true);
                    Canvas faceCanvas = new Canvas(bitmapCopy);
                    faceCanvas.drawBitmap(mBitmap, 0, 0, null);
                    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeWidth(5);
                    paint.setColor(Color.RED);

                    double max=-1;
                    for (RecognizeResult r : result) {
                        if(max<r.scores.anger){ max=r.scores.anger; mEditText.setText("Angry"); }
                        if(max<r.scores.contempt){ max=r.scores.contempt; mEditText.setText("Contempt"); }
                        if(max<r.scores.disgust){ max=r.scores.disgust; mEditText.setText("Disgust"); }
                        if(max<r.scores.fear){ max=r.scores.fear; mEditText.setText("Fear"); }
                        if(max<r.scores.happiness){ max=r.scores.happiness; mEditText.setText("Happy"); }
                        if(max<r.scores.neutral){ max=r.scores.neutral; mEditText.setText("Neutral"); }
                        if(max<r.scores.sadness){ max=r.scores.sadness; mEditText.setText("Sad"); }
                        if(max<r.scores.surprise){ max=r.scores.surprise; mEditText.setText("Surprise"); }
                        faceCanvas.drawRect(r.faceRectangle.left,
                                r.faceRectangle.top,
                                r.faceRectangle.left + r.faceRectangle.width,
                                r.faceRectangle.top + r.faceRectangle.height,
                                paint);
                        count++;
                    }
                    ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                    imageView.setImageDrawable(new BitmapDrawable(getResources(), mBitmap));
                }
            }
            progressBar.setVisibility(View.INVISIBLE);
            mButtonSelectImage.setEnabled(true);
            mButtonContinue.setEnabled(true);
            mButtonContinue.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(RecognizeActivity.this, ChangeMoodActivity.class);
                    intent.putExtra("mood",mEditText.getText().toString().toLowerCase());
                    startActivity(intent);
                }
            });
        }
    }*/
}
