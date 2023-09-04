package com.example.textdetection;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.face.Face;

import java.util.List;

public class rostrosDetector implements OnSuccessListener<List<Face>>, OnFailureListener {
    @Override
    public void onFailure(@NonNull Exception e) {
        
    }



    @Override
    public void onSuccess(List<Face> faces) {
       /* if (faces.size() == 0) {
                        txtResults.setText("No Hay rostros");
                    }else{
                        txtResults.setText("Hay " + faces.size() + " rostro(s)");
                    }*/
    }
}
