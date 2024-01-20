package com.example.finaldeneme.ui.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.finaldeneme.R;
import com.example.finaldeneme.databinding.FragmentAboutBinding;
import com.github.barteksc.pdfviewer.PDFView;

import java.io.IOException;
import java.io.InputStream;

public class AboutFragment extends Fragment {
    private FragmentAboutBinding binding;
    private PDFView pdfView;
    private ImageView imageViewLinkedIn;
    private ImageView imageViewInstagram;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentAboutBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        pdfView = root.findViewById(R.id.pdfView);
        imageViewLinkedIn = root.findViewById(R.id.imageViewLinkedIn);
        imageViewInstagram = root.findViewById(R.id.imageViewInstagram);

        // PDF dosyasını assets klasöründen yükleyip görüntülemek için
        displayPDFFromAssets("Seyfettin_NarmanCV.pdf");

        // LinkedIn ImageView'a tıklama olayı ekleme
        imageViewLinkedIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage("https://www.linkedin.com/in/seyfettin-narman/");
            }
        });

        // Instagram ImageView'a tıklama olayı ekleme
        imageViewInstagram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openWebPage("https://www.instagram.com/seyf_nrmn/");
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void displayPDFFromAssets(String pdfFileName) {
        try {
            InputStream inputStream = requireActivity().getAssets().open(pdfFileName);
            pdfView.fromStream(inputStream).load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void openWebPage(String url) {
        Uri webpage = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, webpage);

        // Intenti güncelle, kullanıcıya tarayıcı seçme ekranı göster
        Intent chooser = Intent.createChooser(intent, "Tarayıcı Seç");

        if (chooser.resolveActivity(requireActivity().getPackageManager()) != null) {
            startActivity(chooser);
        } else {
        }
    }
}
