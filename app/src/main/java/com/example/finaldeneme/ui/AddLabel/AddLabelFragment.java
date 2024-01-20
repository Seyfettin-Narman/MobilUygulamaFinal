package com.example.finaldeneme.ui.AddLabel;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.finaldeneme.R;
import com.example.finaldeneme.databinding.FragmentAddLabelBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class AddLabelFragment extends Fragment {

    private FragmentAddLabelBinding binding;
    private TableLayout tableLayout;
    private EditText labelInput,labeLDescription;
    TextView textView;
    private String updatedText="";
    private Button btnAddLabel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_label, container, false);

        tableLayout = view.findViewById(R.id.tableLayout);
        labelInput = view.findViewById(R.id.labelInput);
        btnAddLabel = view.findViewById(R.id.btnAddLabel);
        labeLDescription = view.findViewById(R.id.descriptionInput);

        FireStoreVeriCek();

        btnAddLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputText = labelInput.getText().toString();
                if (!TextUtils.isEmpty(inputText)) {
                    SatirEkle(inputText);
                    FirebaseFirestore firestore = FirebaseFirestore.getInstance();

                    // Yeni bir etiket oluşturun
                    Map<String, Object> label = new HashMap<>();
                    label.put("labelContent", labelInput.getText().toString());
                    label.put("description", labeLDescription.getText().toString());

                    firestore.collection("label").add(label)
                            .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                    Toast.makeText(requireContext(), "Etiket eklendi", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(requireContext(), "Etiket eklenirken hata oluştu", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        binding = FragmentAddLabelBinding.inflate(inflater, container, false);

        return view;
    }
    private void FireStoreVeriCek() {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("label")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                SatirEkle(document.getString("labelContent"));
                            }
                        } else {
                            Toast.makeText(requireContext(), "Veri çekme hatası", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
    private void SatirEkle(String text) {
        // Yeni bir TableRow oluştur
        TableRow row = new TableRow(requireContext());
        ImageView imageView = new ImageView(requireContext());
        imageView.setImageResource(R.drawable.heart);

        // TextView oluştur ve inputText'i ayarla
         textView = new TextView(requireContext());
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        layoutParams.setMargins(20, 0, 0, 0); // sol, üst, sağ, alt

        // Oluşturulan ImageView ve TextView'ı TableRow'a ekle
        textView.setText(text);
        updatedText=text;
        row.addView(imageView, layoutParams);
        row.addView(textView);

        // Düzenleme ve Silme Butonları
        Button editButton = ButonOlustur("Düzenle", row);
        Button deleteButton = ButonOlustur("Sil", row);

        // Buttonları TableRow'a ekle
        row.addView(editButton);
        row.addView(deleteButton);

        row.setGravity(Gravity.CENTER);

        // TableRow'ı TableLayout'a ekle
        tableLayout.addView(row);
    }
    private void FirestoreVeriSil(String labelContent) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("label")
                .whereEqualTo("labelContent", labelContent)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                            // Belgeyi sil
                            firestore.collection("label").document(documentSnapshot.getId()).delete()
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(requireContext(), "Etiket silindi", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(requireContext(), "Etiket silinirken hata oluştu", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Etiket bulunamadı", Toast.LENGTH_SHORT).show();
                    }
                });
    }
    private Button ButonOlustur(String buttonText, TableRow row) {
        Button button = new Button(requireContext());
        button.setText(buttonText);
        String label = textView.getText().toString();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (buttonText.equals("Düzenle")) {
                    DuzenleGoster(label, row);
                } else if (buttonText.equals("Sil")) {
                    FirestoreVeriSil(label);
                    tableLayout.removeView(row);
                }
            }
        });

        return button;
    }
    private void DuzenleGoster(String label, TableRow row) {
        LayoutInflater inflater = LayoutInflater.from(requireContext());
        View popupView = inflater.inflate(R.layout.popup_layout, null);

        // PopupWindow'u oluşturun ve genişlik, yükseklik ayarlayın
        PopupWindow popupWindow = new PopupWindow(
                popupView,
                600, // Genişlik
                600  // Yükseklik
        );

        View backgroundView = requireActivity().findViewById(android.R.id.content);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.GRAY));
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.showAtLocation(backgroundView, Gravity.CENTER, 0, 0);

        EditText editText = popupView.findViewById(R.id.labelDuzenleInput);
        Button updateButton = popupView.findViewById(R.id.btnDuzenle);
        editText.setText(label);
        label = updatedText;
        String finalLabel = label;

        updateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 updatedText = editText.getText().toString();
                FirestoreVeriGuncelle(finalLabel,updatedText);

                TextView textView = (TextView) row.getChildAt(1); // TextView row'un ikinci child'ıdır
                textView.setText(updatedText);

                popupWindow.dismiss();
            }
        });
    }
    private void FirestoreVeriGuncelle(String oldLabelContent, String newLabelContent) {
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();

        firestore.collection("label")
                .whereEqualTo("labelContent", oldLabelContent)
                .limit(1)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (!queryDocumentSnapshots.isEmpty()) {
                            QueryDocumentSnapshot documentSnapshot = (QueryDocumentSnapshot) queryDocumentSnapshots.getDocuments().get(0);

                            // Belgeyi güncelle
                            Map<String, Object> updates = new HashMap<>();
                            updates.put("labelContent", newLabelContent);

                            firestore.collection("label").document(documentSnapshot.getId()).update(updates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void unused) {
                                            Toast.makeText(requireContext(), "Etiket güncellendi", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(requireContext(), "Etiket güncellenirken hata oluştu", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        } else {
                            Toast.makeText(requireContext(), "Etiket bulunamadı", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(requireContext(), "Etiket bulunamadı", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}