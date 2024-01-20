package com.example.finaldeneme.ui.Logout;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.example.finaldeneme.Login;
import com.example.finaldeneme.R;
import com.example.finaldeneme.databinding.FragmentLogoutBinding;
import com.google.firebase.auth.FirebaseAuth;

public class LogoutFragment extends Fragment {
    private FragmentLogoutBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_logout, container, false);

        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Firebase'den çıkış yap
                FirebaseAuth.getInstance().signOut();

                // LoginActivity'e geri dön
                Intent intent = new Intent(requireContext(), Login.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                requireActivity().finish();
            }
        });

        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}