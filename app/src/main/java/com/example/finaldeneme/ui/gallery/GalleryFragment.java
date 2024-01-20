package com.example.finaldeneme.ui.gallery;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.finaldeneme.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GalleryFragment extends Fragment {

    private Set<String> labelSet = new HashSet<>();
    private Set<String> fetchedImages = new HashSet<>();
    String name,surname;
    Map<String, Integer> likeCounts = new HashMap<>();
    Map<String, Integer> dislikeCounts = new HashMap<>();
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery, container, false);
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        editor = preferences.edit();
        if (currentUser != null) {
            String uid = currentUser.getUid();
            KullaniciDetay(uid);
        } else {

        }
        Spinner spinner = view.findViewById(R.id.spinner);
        LinearLayout containerLayout = view.findViewById(R.id.containerLayout1);
        // Firestore'dan etiket değerlerini çek
        FirestoreVeriCek(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String labelValue = document.getString("label_value");
                    if (labelValue != null && !labelSet.contains(labelValue)) {
                        labelSet.add(labelValue);
                    }
                }

                // Etiket değerlerini alarak Spinner'ı güncelle
                SpinnerGuncelle(spinner);

            }
        });

        // Spinner'dan seçilen etiketi dinle
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String selectedLabel = (String) parentView.getItemAtPosition(position);
                // Firestore'dan seçilen etiket ile eşleşen kayıtları al ve göster
                LinearLayoutTemizle(containerLayout);
                ResimCekveGoruntule(selectedLabel, containerLayout);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Bir şey seçilmediğinde yapılacak işlemler
            }
        });

        return view;
    }
    private void KullaniciDetay(String uid) {
        FirebaseFirestore.getInstance()
                .collection("Kullanici")
                .document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                         name = documentSnapshot.getString("name");
                         surname = documentSnapshot.getString("surname");

                        // Kullanıcı bilgilerini kullanabilirsiniz
                        if (name != null && surname != null) {
                        } else {
                            System.out.println("Name or surname is null.");
                        }
                    } else {
                        System.out.println("Belge bulunamadı.");
                    }
                })
                .addOnFailureListener(e -> {
                    System.out.println("Hata oluştu: " + e.getMessage());
                });
    }
    private void FirestoreVeriCek(OnSuccessListener<QuerySnapshot> successListener) {
        FirebaseFirestore.getInstance()
                .collection("images")
                .get()
                .addOnSuccessListener(successListener);
    }

    private void SpinnerGuncelle(Spinner spinner) {
        String[] labels = labelSet.toArray(new String[0]);

        // ArrayAdapter ile Spinner'ı güncelle
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels);
        spinner.setAdapter(adapter);
    }

    private void LinearLayoutTemizle(LinearLayout containerLayout) {
        int childCount = containerLayout.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = containerLayout.getChildAt(i);
            if (view instanceof LinearLayout) {
                containerLayout.removeViewAt(i);
                i--;
            }
        }
    }
    private void ResimCekveGoruntule(String selectedLabel, LinearLayout containerLayout) {
        LinearLayoutTemizle(containerLayout);

        Glide.get(requireContext()).clearMemory();
        new Thread(() -> Glide.get(requireContext()).clearDiskCache()).start();
        fetchedImages.clear();
        FirebaseFirestore.getInstance()
                .collection("images")
                .whereEqualTo("label_value", selectedLabel)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String imageName = document.getString("image_name");

                        // Eğer bu dosya daha önce eklenmediyse işleme devam et
                        if (!fetchedImages.contains(imageName)) {
                            fetchedImages.add(imageName);

                            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    30 // 30dp'lik boşluk
                            );

                            // Resmi eklemek için
                            StorageResimCek(imageName, containerLayout,selectedLabel);
                        }
                    }
                });
    }
    private void StorageResimCek(String imageName, LinearLayout containerLayout, String selectedLabel) {
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + imageName);

        LinearLayout linearLayout = new LinearLayout(requireContext());
        linearLayout.setOrientation(LinearLayout.VERTICAL);

        ImageView imageView = new ImageView(requireContext());

        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(
                400,
                400
        );
        imageLayoutParams.gravity = Gravity.START;

        imageView.setLayoutParams(imageLayoutParams);

        Glide.with(requireContext())
                .load(storageReference)
                .apply(new RequestOptions().override(300, 300))
                .into(imageView);

        TextView textView = new TextView(requireContext());
        TextView txtName = new TextView(requireContext());
        TextView txtSurname = new TextView(requireContext());
        textView.setText(selectedLabel);
        txtName.setText("User: " + name + " "+surname);

        LinearLayout.LayoutParams textLayoutParams = new LinearLayout.LayoutParams(
                120,
                30
        );
        textView.setLayoutParams(textLayoutParams);

        TextView txtLikeCount = new TextView(requireContext());
        TextView txtDislikeCount = new TextView(requireContext());

        likeCounts.putIfAbsent(imageName, 0);
        dislikeCounts.putIfAbsent(imageName, 0);

        ImageView likeImageView = new ImageView(requireContext());
        likeImageView.setImageResource(R.drawable.like);

        likeImageView.setLayoutParams(textLayoutParams);
        likeImageView.setOnClickListener(v -> {
            likeCounts.put(imageName, likeCounts.get(imageName) + 1);
            BegeniSayisiGuncelle(txtLikeCount, likeCounts.get(imageName));
        });
        ImageView dislikeImageView = new ImageView(requireContext());
        dislikeImageView.setImageResource(R.drawable.dislike);// Drawable dosyanızın adını kullanmalısınız
        dislikeImageView.setLayoutParams(textLayoutParams);
        dislikeImageView.setOnClickListener(v -> {
            dislikeCounts.put(imageName, dislikeCounts.get(imageName) + 1);
            BegeniSayisiGuncelle(txtDislikeCount, dislikeCounts.get(imageName));
        });
        RelativeLayout.LayoutParams likeLayoutParams = new RelativeLayout.LayoutParams(100, 100);
        likeLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.margin_top); // Adjust based on your resources
        likeLayoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.margin_bottom); // Adjust based on your resources

        likeImageView.setLayoutParams(likeLayoutParams);
        txtLikeCount.setText("Like: " + likeCounts.get(imageName));

        RelativeLayout.LayoutParams dislikeLayoutParams = new RelativeLayout.LayoutParams(100, 100);
        dislikeLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.margin_top); // Adjust based on your resources
        dislikeLayoutParams.bottomMargin = (int) getResources().getDimension(R.dimen.margin_bottom); // Adjust based on your resources

        dislikeLayoutParams.addRule(RelativeLayout.RIGHT_OF, likeImageView.getId());
        dislikeLayoutParams.leftMargin = (int) getResources().getDimension(R.dimen.gap_between_images); // Adjust based on your resources

        dislikeImageView.setLayoutParams(dislikeLayoutParams);// Adjust based on your resources

        dislikeImageView.setLayoutParams(dislikeLayoutParams);

        dislikeImageView.setLayoutParams(dislikeLayoutParams);
        txtLikeCount.setLayoutParams(textLayoutParams);
        txtDislikeCount.setLayoutParams(textLayoutParams);
        txtDislikeCount.setText("Dislike: " + dislikeCounts.get(imageName));
        linearLayout.addView(imageView);
        linearLayout.addView(textView);
        linearLayout.addView(txtName);
        linearLayout.addView(txtSurname);

        LinearLayout newLineLayout = new LinearLayout(requireContext());
        newLineLayout.setOrientation(LinearLayout.HORIZONTAL);
        newLineLayout.addView(likeImageView);
        newLineLayout.addView(txtLikeCount);
        newLineLayout.addView(dislikeImageView);
        newLineLayout.addView(txtDislikeCount);

        containerLayout.addView(linearLayout);
        containerLayout.addView(newLineLayout);
    }
    private void BegeniSayisiGuncelle(TextView textView, int count) {
        textView.setText("" + count);
    }
}