package br.fonttracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import br.fonttracker.ui.camera.GraphicOverlay;

public class CropActivity extends AppCompatActivity {

    private static final String TAG = "Crop Actitivity";
    private CropImageView cropImageView;
    private TextView txtDetect;
    private ImageButton info;
    private FloatingActionButton cropButton;
    private Classificador classificador;
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "Placeholder";
    private static final String OUTPUT_NAME = "final_result";
    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";
    private Handler handler;
    private HandlerThread handlerThread;
    private ProgressBar loading;
    Bitmap bitmap = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crop);

        cropImageView = findViewById(R.id.cropImageView);
        txtDetect = findViewById(R.id.txtDetect);
        cropButton = findViewById(R.id.cropButton);
        loading = findViewById(R.id.loading);
        info = findViewById(R.id.button_info);

        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(CropActivity.this);
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

        if (getIntent().hasExtra(Intent.EXTRA_STREAM)) {
            String caminhoFoto = getIntent().getExtras().getString(Intent.EXTRA_STREAM);
            bitmap = BitmapFactory.decodeFile(caminhoFoto);
            cropImageView.setImageBitmap(bitmap);
        }

        Context context = getApplicationContext();

        final TextRecognizer textRecognizer = new TextRecognizer.Builder(context).build();

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

        if (bitmap != null) {
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<TextBlock> itens = textRecognizer.detect(frame);
            StringBuilder stringBuilder = new StringBuilder();
            Rect rect = null;
            TextBlock itemSelecionado = null;
            for (int i = 0; i < itens.size(); i++) {
                TextBlock item = itens.valueAt(i);
                stringBuilder.append(item.getValue());
                stringBuilder.append("\n");
                if (rect == null) {
                    rect = item.getBoundingBox();
                    itemSelecionado = item;
                } else if (rect.height() * rect.width() < item.getBoundingBox().height() * item.getBoundingBox().width()) {
                    rect = item.getBoundingBox();
                    itemSelecionado = item;
                }
            }
            if (rect != null) {
                cropImageView.setAutoZoomEnabled(true);
                cropImageView.setShowProgressBar(true);
                cropImageView.setScaleType(CropImageView.ScaleType.CENTER);
                cropImageView.setCropRect(rect);
                txtDetect.setText(itemSelecionado.getValue());
            }

            if (!stringBuilder.toString().isEmpty()) {
                Log.w(TAG, stringBuilder.toString());
            } else {
                Log.w(TAG, "Nenhum caracter encontrado");
            }
        }

        cropImageView.setOnSetCropOverlayMovedListener(new CropImageView.OnSetCropOverlayMovedListener() {
            @Override
            public void onCropOverlayMoved(Rect rect) {
                Bitmap bitmap = cropImageView.getCroppedImage();
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> itens = textRecognizer.detect(frame);
                for (int i = 0; i < itens.size(); i++) {
                    TextBlock item = itens.valueAt(i);
                    txtDetect.setText(item.getValue());
                }
                itens = null;
                frame = null;
            }
        });
/*
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Log.w("onProgressChanget", "" + i);
                cropImageView.rotateImage(-cropImageView.getRotatedDegrees());
                cropImageView.rotateImage(seekBar.getProgress() - 25);
                Log.w("onProgressChanget", "degrees" + cropImageView.getRotatedDegrees());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Bitmap bitmap = cropImageView.getCroppedImage();
                Frame frame = new Frame.Builder().setBitmap(bitmap).build();
                SparseArray<TextBlock> itens = textRecognizer.detect(frame);
                for (int i = 0; i < itens.size(); i++) {
                    TextBlock item = itens.valueAt(i);
                    txtDetect.setText(item.getValue());
                }
            }
        });*/

        configureClassificador();

        Log.w("cropButton", "clicked");

        cropButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.w("cropButton", "clicked");
                String texto = "";
                loading.setVisibility(View.VISIBLE);
                final Bitmap bmp = cropImageView.getCroppedImage();
                Frame frame = new Frame.Builder().setBitmap(bmp).build();
                SparseArray<TextBlock> itens = textRecognizer.detect(frame);
                final ArrayList<Bitmap> chars = new ArrayList<>();
                for (int i = 0; i < itens.size(); i++) {
                    TextBlock item = itens.valueAt(i);
                    texto = item.getValue();
                    //txtDetect.setText(item.getValue());
                    chars.add(Bitmap.createBitmap(bitmap,
                            item.getBoundingBox().centerX(),
                            item.getBoundingBox().centerY(),
                            INPUT_SIZE,
                            INPUT_SIZE));
                }

                /*
                for (Bitmap character:chars){
                    String caminhoFoto= getExternalFilesDir(null) + "/foto"+character.getGenerationId()+".jpg";
                    try {
                        OutputStream outputStream=new FileOutputStream(caminhoFoto);
                        character.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
                    } catch (FileNotFoundException fe) {
                        fe.printStackTrace();
                    }catch(IOException ie){
                        ie.printStackTrace();
                    }

                }*/

                if (!txtDetect.getText().toString().isEmpty()) {
                    startInferenceThread();

                    runInBackgroud(new Runnable() {
                        @Override
                        public void run() {
                            Log.w("cropButton", "running");
                            if (!chars.isEmpty()) {
                                for (Bitmap ch : chars) {
                                    final List<Classificador.Reconhecimento> results = classificador.recognizeImage(ch);
                                    Intent intent = new Intent(CropActivity.this, ResultActivity.class);
                                    intent.putExtra("results", (Serializable) results);
                                    intent.putExtra("txtDetect", txtDetect.getText());
                                    startActivity(intent);
                                    for (final Classificador.Reconhecimento recog : results) {
                                        Log.i("run", "Classificação...:" +
                                                recog.getTitulo() + " : " + recog.getConfianca());
                                    /*runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (recog.getConfianca() > 0.1f)
                                                txtDetect.setText(recog.getTitulo() + " : " + recog.getConfianca());
                                        }
                                    });*/
                                    }
                                }
                            } else {
                                final List<Classificador.Reconhecimento> results = classificador.recognizeImage(bmp);
                                Intent intent = new Intent(CropActivity.this, ResultActivity.class);
                                intent.putExtra("results", (Serializable) results);
                                intent.putExtra("txtDetect", txtDetect.getText());
                                startActivity(intent);
                                for (final Classificador.Reconhecimento recog : results) {
                                    Log.i("run", "Classificação...:" + recog.getTitulo() + " : " + recog.getConfianca());
                                /*runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (recog.getConfianca() > 0.1f)
                                            txtDetect.setText(recog.getTitulo() + " : " + recog.getConfianca());
                                    }
                                });*/
                                }
                            }

                        }
                    });
                } else loading.setVisibility(View.GONE);
            }
        });


    }

    private void configureClassificador() {
        classificador =
                TensorFlowImageClassificador.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);

    }

    protected synchronized void runInBackgroud(final Runnable r) {
        if (handler != null)
            handler.post(r);
    }


    private void startInferenceThread() {
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
    }


    @Override
    protected void onPause() {
        stopInferenceThread();
        super.onPause();
    }

    private void stopInferenceThread() {
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loading.setVisibility(View.GONE);
        startInferenceThread();
    }
}
