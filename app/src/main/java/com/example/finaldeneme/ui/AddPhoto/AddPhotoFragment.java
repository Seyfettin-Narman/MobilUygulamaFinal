package com.example.finaldeneme.ui.AddPhoto;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.finaldeneme.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

public class AddPhotoFragment extends Fragment {
    private Button camera;
    private ImageView image;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private FirebaseStorage storage;
    private FirebaseFirestore firestore;
    private int childCount=0;
    private TableLayout tableLayout;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_photo, container, false);

        camera = view.findViewById(R.id.camera);
        image = view.findViewById(R.id.imageView);
        tableLayout = view.findViewById(R.id.tableLayout);
        storage = FirebaseStorage.getInstance();
        firestore = FirebaseFirestore.getInstance();

        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Kamera izni kontrolü
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    // Kullanıcıdan kamera izni iste
                    ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
                } else {
                    // Kullanıcı izin verdiyse, kamerayı aç
                    dispatchTakePictureIntent();
                }
            }
        });
        EtiketCekveGoruntule();
        return view;
    }
    private void FotografveSecilenLabelKaydet(ImageView image) {

        int childCount = tableLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                View checkBoxView = row.getChildAt(0); // CheckBox
                View textViewView = row.getChildAt(2); // TextView

                if (checkBoxView instanceof CheckBox &&
                        textViewView instanceof TextView) {

                    CheckBox checkBox = (CheckBox) checkBoxView;
                    TextView textView = (TextView) textViewView;

                    if (checkBox.isChecked()) {
                        FirestoreFotografveLabelGuncelle(image, textView.getText().toString());
                    }
                }
            }
        }
    }
    private void FotografIsimGuncelle(String documentId, String imageName) {

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("image_name", imageName);

        firestore.collection("images")
                .document(documentId)
                .update(updateMap)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });
    }
    private void FirestoreFotografveLabelGuncelle(ImageView imageView, String labelValue) {

        Bitmap imageBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        // Image dosya adını sabit bir isim ile belirle
        String imageName = "image_" + System.currentTimeMillis() + ".jpg"; // Farklı bir isim kullanmak için zaman damgası ekledik

        // Firestore'a belge eklemek için bir harita oluştur
        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("image_name", imageName);
        imageMap.put("label_value", labelValue);
        // Firestore koleksiyonu ve belge oluştur
        firestore.collection("images")
                .add(imageMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        String documentId = documentReference.getId(); // Firestore'dan dönen belgenin ID'sini al
                        FotografIsimGuncelle(documentId, imageName);

                        // Resmi Firestore Storage'a yükle
                        StorageFotografEkle(imageName, data);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Firestore'a belge ekleme hatası
                    }
                });
    }
    // Kamera intent'ini başlatan metot
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Kullanıcı izin verdiyse, kamerayı aç
                dispatchTakePictureIntent();
            } else {
                // Kullanıcı izin vermediyse, hata mesajı gösterilebilir
                Toast.makeText(requireContext(), "Kamera izni reddedildi", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            // Kamera başarıyla kullanıldı, alınan resmi göster
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            image.setImageBitmap(imageBitmap);

            // Fotoğrafı Firestore ve Storage'a yükle
            FotografveSecilenLabelKaydet(image);
        }
    }
    private void EtiketCekveGoruntule() {
        firestore.collection("label")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String labelContent = document.getString("labelContent");
                            String description = document.getString("description");

                            // Her bir label için satır ekle
                            SatirEkle(labelContent);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Veri çekerken hata oluştu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private void SatirEkle(String text) {
        TableRow row = new TableRow(requireContext());
        CheckBox checkBox = new CheckBox(requireContext());
        ImageView imageView = new ImageView(requireContext());
        imageView.setImageResource(R.drawable.heart);
        TextView textView = new TextView(requireContext());


        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(20, 0, 0, 0); // sol, üst, sağ, alt

        row.addView(checkBox);
        row.addView(imageView, layoutParams);
        row.addView(textView);

        row.setGravity(Gravity.CENTER);

        tableLayout.addView(row);

        // CheckBox işaretli olduğunda işlem yap
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // CheckBox işaretliyse ve ImageView içinde bir fotoğraf varsa işlem yap
                    if (imageView.getDrawable() != null) {
                        EtiketveResimMap(textView.getText().toString());
                    }
                }
            }
        });

        textView.setText(text);
    }
    private void EtiketveResimMap(String labelContent) {
        int childCount = tableLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = tableLayout.getChildAt(i);
            if (view instanceof TableRow) {
                TableRow row = (TableRow) view;
                View checkBoxView = row.getChildAt(0); // CheckBox
                View imageViewView = row.getChildAt(1); // ImageView
                View textViewView = row.getChildAt(2); // TextView

                if (checkBoxView instanceof CheckBox &&
                        imageViewView instanceof ImageView &&
                        textViewView instanceof TextView) {

                    CheckBox checkBox = (CheckBox) checkBoxView;
                    ImageView imageView = (ImageView) imageViewView;
                    TextView textView = (TextView) textViewView;

                    if (checkBox.isChecked()) {
                        // CheckBox işaretliyse ve ImageView içinde bir fotoğraf varsa işlem yap
                        if (imageView.getDrawable() != null) {

                        }
                    }
                }
            }
        }
    }
    private void StorageFotografEkle(String imageName, byte[] data) {
        StorageReference storageReference = storage.getReference().child("images/" + imageName);

        // Resmi Firestore Storage'a yükle
        storageReference.putBytes(data)
                .addOnSuccessListener(new OnSuccessListener() {
                    @Override
                    public void onSuccess(Object o) {
                        // Resim başarıyla Firestore Storage'a yüklendi
                        Toast.makeText(requireContext(), "Fotoğraf başarıyla yüklendi", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Resmi Firestore Storage'a yükleme hatası
                        Toast.makeText(requireContext(), "Fotoğraf yüklenirken bir hata oluştu", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
