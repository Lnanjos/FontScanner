package br.fonttracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import br.fonttracker.ui.camera.CameraSource;
import br.fonttracker.ui.camera.CameraSourcePreview;
import br.fonttracker.ui.camera.GraphicOverlay;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    //String caminhofoto;
    //static final int CODIGO_CAMERA = 567;
    //Bitmap bitmapReduzido;
    @BindView(R.id.button_camera)
    ImageButton botaocamera;
    @BindView(R.id.preview)
    CameraSourcePreview preview;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.button_info)
    ImageButton info;
    @BindView(R.id.button_imgPicker)
    ImageButton imgPicker;
    public static final int RESULT_LOAD_IMG = 1;

    private CameraSource cameraSource;
    // Intent request code to handle updating play services if needed.
    private static final int RC_HANDLE_GMS = 9001;
    // Permission request codes need to be < 256
    private static final int RC_HANDLE_CAMERA_PERM = 2;

    // Constants used to pass extra data in the intent
    public static final String AutoFocus = "AutoFocus";
    public static final String UseFlash = "UseFlash";

    private GraphicOverlay<OcrGraphic> graphicOverlay;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        graphicOverlay = (GraphicOverlay<OcrGraphic>) findViewById(R.id.graphicOverlay);


        // Set good defaults for capturing text.
        boolean autoFocus = true;
        boolean useFlash = true;

        // Check for the camera permission before accessing the camera.  If the
        // permission is not granted yet, request permission.
        int rc = ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        if (rc == PackageManager.PERMISSION_GRANTED) {
            createCameraSource(autoFocus, useFlash);
        } else {
            requestCameraPermission();
        }

        final CameraSource.PictureCallback pictureCallback = new CameraSource.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data) {
                if (data != null) {
                    int maxSize = 1024;

                    preview.stop();
                    String caminhoFoto = getExternalFilesDir(null) + "/foto.jpg";
                    BitmapFactory.Options opt = new BitmapFactory.Options();

                    /*before making an actual bitmap, check size
                    if the bitmap's size is too large,out of memory occurs.
                    */
                    opt.inJustDecodeBounds = true;
                    BitmapFactory.decodeByteArray(data, 0, data.length, opt);
                    int srcSize = Math.max(opt.outWidth, opt.outHeight);
                    System.out.println("out w:" + opt.outWidth + " h:" + opt.outHeight);

                    opt.inSampleSize = maxSize < srcSize ? (srcSize / maxSize) : 1;

                    System.out.println("sample size " + opt.inSampleSize);
                    opt.inJustDecodeBounds = false;
                    Bitmap tmp = BitmapFactory.decodeByteArray(data, 0, data.length, opt);
                    try {
                        OutputStream outputStream = new FileOutputStream(caminhoFoto);
                        tmp.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
                    } catch (FileNotFoundException fe) {
                        fe.printStackTrace();
                    } catch (IOException ie) {
                        ie.printStackTrace();
                    }

                    Intent intent = new Intent(MainActivity.this, CropActivity.class);
                    intent.putExtra(Intent.EXTRA_STREAM, caminhoFoto);
                    startActivity(intent);
                } else {
                    System.out.println("no data");
                }
            }
        };

        botaocamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loading.setVisibility(View.VISIBLE);
                cameraSource.takePicture(null, pictureCallback);

            }
        });


        imgPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, RESULT_LOAD_IMG);
            }
        });

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                builder.setView(inflater.inflate(R.layout.info_dialog, null))
                        .setTitle("Para bons resultados:")
                        .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

    }

    @Override
    protected void onActivityResult(int reqCode, int resultCode, Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            try {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                //image_view.setImageBitmap(selectedImage);
                String caminhoFoto = getExternalFilesDir(null) + "/foto.jpg";
                try {
                    OutputStream outputStream = new FileOutputStream(caminhoFoto);
                    selectedImage.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();
                } catch (FileNotFoundException fe) {
                    fe.printStackTrace();
                } catch (IOException ie) {
                    ie.printStackTrace();
                }

                Intent intent = new Intent(MainActivity.this, CropActivity.class);
                intent.putExtra(Intent.EXTRA_STREAM, caminhoFoto);
                startActivity(intent);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Toast.makeText(this, "Algo de errado não está certo!", Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(this, "Nenhuma imagem foi escolhida!", Toast.LENGTH_LONG).show();
        }
    }


    private void createCameraSource(boolean autoFocus, boolean useFlash) {
        Context context = getApplicationContext();

        TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

        textRecognizer.setProcessor(new OcrDetectorProcessor(graphicOverlay));

        if (!textRecognizer.isOperational()) {
            Log.w(TAG, "Detector dependencies are not yet avaliable.");
            // Check for low storage.  If there is low storage, the native library will not be
            // downloaded, so detection will not become operational.
            IntentFilter lowstorageFilter = new IntentFilter(Intent.ACTION_DEVICE_STORAGE_LOW);
            boolean hasLowStorage = registerReceiver(null, lowstorageFilter) != null;

            if (hasLowStorage) {
                Toast.makeText(this, "Pouco Armanezamento", Toast.LENGTH_LONG).show();
                Log.w(TAG, "Pouco Armanezamento");
            }
        }

        cameraSource =
                new CameraSource.Builder(getApplicationContext(), textRecognizer)
                        .setFacing(CameraSource.CAMERA_FACING_BACK)
                        .setRequestedPreviewSize(1280, 1024)
                        .setRequestedFps(30.0f)
                        .setFlashMode(useFlash ? Camera.Parameters.FLASH_MODE_AUTO : null)
                        .setFocusMode(autoFocus ? Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE: null)
                        .build();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loading.setVisibility(View.GONE);
        startCameraSource();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (preview != null) {
            preview.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (preview != null) {
            preview.release();
        }
    }

    private void startCameraSource() throws SecurityException {
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());
        if (code != ConnectionResult.SUCCESS) {
            Dialog dlg =
                    GoogleApiAvailability.getInstance().getErrorDialog(this, code, RC_HANDLE_GMS);
            dlg.show();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource, graphicOverlay);
            } catch (IOException e) {
                Log.e(TAG, "Unable to start camera source.", e);
                cameraSource.release();
                cameraSource = null;
            }
        }
    }

    private void requestCameraPermission() {
        Log.w(TAG, "Camera permission is not granted. Requesting permission");

        final String[] permissions = new String[]{Manifest.permission.CAMERA};

        if (!ActivityCompat.shouldShowRequestPermissionRationale(this,
                Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, permissions, RC_HANDLE_CAMERA_PERM);
            return;
        }

        final Activity thisActivity = this;

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityCompat.requestPermissions(thisActivity, permissions,
                        RC_HANDLE_CAMERA_PERM);
            }
        };

        Snackbar.make(preview, "Necessária permissão de acesso a camera",
                Snackbar.LENGTH_INDEFINITE)
                .setAction("ok", listener)
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != RC_HANDLE_CAMERA_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission granted - initialize the camera source");
            // We have permission, so create the camerasource
            boolean autoFocus = getIntent().getBooleanExtra(AutoFocus, false);
            boolean useFlash = getIntent().getBooleanExtra(UseFlash, false);
            createCameraSource(autoFocus, useFlash);
            return;
        }

        Log.e(TAG, "Permission not granted: results len = " + grantResults.length +
                " Result code = " + (grantResults.length > 0 ? grantResults[0] : "(empty)"));

        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("FontScanner")
                .setMessage("Necessária permissão de acesso a camera")
                .setPositiveButton("ok", listener)
                .show();
    }

}
