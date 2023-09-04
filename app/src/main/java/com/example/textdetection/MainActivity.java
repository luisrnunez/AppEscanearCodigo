package com.example.textdetection;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements OnSuccessListener<Text>,
        OnFailureListener {
    public static int REQUEST_CAMERA = 111;
    public static int REQUEST_GALLERY = 222;
    Bitmap mSelectedImage;
    ImageView mImageView;
    ArrayList<String> permisosNoAprobados;
    Button btnCamara;
    Button btnGaleria;
    TextView txtResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtResults = findViewById(R.id.txtresults);
        mImageView = findViewById(R.id.image_view);

        btnCamara = findViewById(R.id.btCamera);
        btnGaleria = findViewById(R.id.btGallery);

        ArrayList<String> permisos_requeridos = new ArrayList<String>();
        permisos_requeridos.add(Manifest.permission.CAMERA);
        permisos_requeridos.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
        permisos_requeridos.add(Manifest.permission.READ_EXTERNAL_STORAGE);

        permisosNoAprobados  = getPermisosNoAprobados(permisos_requeridos);
        requestPermissions(permisosNoAprobados.toArray(new String[permisosNoAprobados.size()]),
                100);
    }
    public void abrirGaleria (View view){
    Intent i = new Intent(Intent.ACTION_PICK,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
    startActivityForResult(i, REQUEST_GALLERY);
}
    public void abrirCamera (View view){
    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
    startActivityForResult(intent, REQUEST_CAMERA);

}
    @Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_GALLERY) {
                // Seleccionar imagen de la galería
                Uri imageUri = data.getData();
                try {
                    Bitmap selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    mSelectedImage = selectedImage; // Asigna la imagen seleccionada a mSelectedImage
                    procesarImagen(mSelectedImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }} else if (requestCode == REQUEST_CAMERA) {
                // Capturar imagen desde la cámara
                Bundle extras = data.getExtras();
                if (extras != null && extras.containsKey("data")) {
                    Bitmap capturedImage = (Bitmap) extras.get("data");
                    mSelectedImage = capturedImage; // Asigna la imagen capturada a mSelectedImage
                    procesarImagen(mSelectedImage);
                }
            }
        }
}

    private void procesarImagen(Bitmap image) {
        // Realizar el escaneo de códigos de barras y códigos QR en la imagen
        BarcodeScanner scanner = BarcodeScanning.getClient();
        InputImage inputImage = InputImage.fromBitmap(image, 0);

        scanner.process(inputImage)
                .addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        // Procesar los resultados del escaneo
                        for (Barcode barcode : barcodes) {
                            String codeValue = barcode.getRawValue(); // Valor del código leído
                            // Muestra el resultado en un TextView o realiza la acción que desees
                            txtResults.setText("Código: " + codeValue);

                            // Muestra la imagen escaneada en la ImageView
                            ImageView scannedImageView = findViewById(R.id.image_view);
                            scannedImageView.setImageBitmap(image);
                            scannedImageView.setVisibility(View.VISIBLE); // Muestra la ImageView
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Manejo de errores en caso de fallo en el escaneo
                        txtResults.setText("Error al escanear: " + e.getMessage());
                    }
                });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        for(int i=0; i<permissions.length; i++){
            if(permissions[i].equals(Manifest.permission.CAMERA)){
                btnCamara.setEnabled(grantResults[i] == PackageManager.PERMISSION_GRANTED);
            } else if(permissions[i].equals(Manifest.permission.MANAGE_EXTERNAL_STORAGE) ||
                    permissions[i].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
            ) {
                btnGaleria.setEnabled(grantResults[i] == PackageManager.PERMISSION_GRANTED);
            }
        }
    }

    public ArrayList<String> getPermisosNoAprobados(ArrayList<String>  listaPermisos) {
        ArrayList<String> list = new ArrayList<String>();

        if (Build.VERSION.SDK_INT >= 23)
            for(String permiso: listaPermisos) {
                if (checkSelfPermission(permiso) != PackageManager.PERMISSION_GRANTED) {
                    list.add(permiso);
                }
            }


        return list;
    }
    public void OCRfx(View v) {
    InputImage image = InputImage.fromBitmap(mSelectedImage, 0);

    TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

    recognizer.process(image)
            .addOnSuccessListener(this)
            .addOnFailureListener(this);
}


    @Override
    public void onFailure(@NonNull Exception e) {
        txtResults.setText("Error al procesar imagen");
    }

    @Override
    public void onSuccess(Text text) {
        List<Text.TextBlock> blocks = text.getTextBlocks();
    String resultados="";
    if (blocks.size() == 0) {
        resultados = "No hay Texto";
    }else{
        for (int i = 0; i < blocks.size(); i++) {
           List<Text.Line> lines = blocks.get(i).getLines();
            for (int j = 0; j < lines.size(); j++) {
                 List<Text.Element> elements = lines.get(j).getElements();
                 for (int k = 0; k < elements.size(); k++) {
                     resultados = resultados + elements.get(k).getText() + " ";
                 }
            }
        }
        resultados=resultados + "\n";
    }
    txtResults.setText(resultados);
    }


    public void Rostrosfx(View  v) {
    InputImage image = InputImage.fromBitmap(mSelectedImage, 0);
    FaceDetectorOptions options =
            new FaceDetectorOptions.Builder()
                    .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                    .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                    .build();

    FaceDetector detector = FaceDetection.getClient(options);

        BitmapDrawable drawable = (BitmapDrawable)  mImageView.getDrawable();
        Bitmap bitmap= drawable.getBitmap().copy(Bitmap.Config.ARGB_8888,true);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStrokeWidth(15);
        paint.setStyle(Paint.Style.STROKE);
        paint.setTextSize(40);
        detector.process(image)
            .addOnSuccessListener(new OnSuccessListener<List<Face>>() {
                @Override
                public void onSuccess(List<Face> faces) {
                    if (faces.size() == 0) {
                        txtResults.setText("No Hay rostros");
                    }else{
                        txtResults.setText("Hay " + faces.size() + " rostro(s)");
                    }
                    for (Face face: faces){
                        canvas.drawRect(face.getBoundingBox(),paint);

                    }
                }
            })
            .addOnFailureListener(this);
    mImageView.setImageBitmap(bitmap);
}
}


