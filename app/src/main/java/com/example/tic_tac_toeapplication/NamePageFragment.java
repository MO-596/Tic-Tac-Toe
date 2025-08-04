package com.example.tic_tac_toeapplication;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;

public class NamePageFragment extends Fragment {

    private EditText Player1, Player2;
    private NavController navController;
    Button Submit_Button;
    String Players1Name, Players2Name;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        navController = Navigation.findNavController(view);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_name_page, container, false);

        Submit_Button = view.findViewById(R.id.submit_names);
        Player1 = view.findViewById(R.id.etplayer1Name);
        Player2 = view.findViewById(R.id.etplayer2Name);

        Submit_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Players1Name = Player1.getText().toString();
                Players2Name = Player2.getText().toString();
                if (Players1Name.isEmpty() || Players2Name.isEmpty()) {
                    showError("Names cannot be empty");// Display's an error message saying that the name can't be empty
                    return;
                }

                // Check if the names contain only letters
                if (!Players1Name.matches("^[a-zA-Z ]+$") || !Players2Name.matches("^[a-zA-Z ]+$")) {
                    // Display's an error message saying that the name must be letters
                    showError("Names must contain only letters");
                    return;
                }

                // Check if the name matches the word nobody
                if(Players1Name.matches("Nobody") || Players1Name.matches("nobody") ||
                        Players2Name.matches("Nobody") || Players2Name.matches("nobody"))
                {
                   // Display's an error message if the names are the same as nobody
                    showError("Can not use the name nobody");
                    return;
                }

                // Check if the names are the same (case-sensitive)
                if(Players1Name.equals(Players2Name)) {
                    showError("Names can not be the same as each other");
                    return;
                }

                // Check if the names exceed a character limit (e.g., 20 characters)
                int maxCharacterLimit = 25;
                if (Players1Name.length() >= maxCharacterLimit || Players2Name.length() >= maxCharacterLimit) {
                    // Display's an error message saying that the name must be less than or equal to
                    showError("Names cannot exceed " + maxCharacterLimit + " characters");
                    return;
                }

                // Create a Bundle to pass data
                Bundle bundle = new Bundle();
                bundle.putString("Players1Name", Players1Name);
                bundle.putString("Players2Name", Players2Name);

                // Navigate to the gameFragment.java with the Bundle
                navController.navigate(R.id.action_namePageFragment_to_gameFragment, bundle);
            }
        });



        // Inflate the layout for this fragment
        return view;
    }
    private void showError(String errorMessage) {
        Snackbar.make(requireView(), errorMessage, Snackbar.LENGTH_LONG).show();
    }
}