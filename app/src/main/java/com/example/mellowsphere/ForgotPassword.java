package com.example.mellowsphere;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class ForgotPassword extends DialogFragment {
    EditText email;
    TextInputLayout emailInputLayout;
    Button submitEmail;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_forgot_password, container, false);

        emailInputLayout = view.findViewById(R.id.TILEmail);
        email = emailInputLayout.getEditText();
        submitEmail = view.findViewById(R.id.submit_forgot_pass);

        submitEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Retrieve the email address from the TextInputEditText
                String emailAddress = email.getText().toString().trim();

                // Validate the email address if needed
                if (isValidEmail(emailAddress)) {
                    // TODO: Send "forgot password" email
                    sendForgotPasswordEmail(emailAddress);
                } else {
                    Toast.makeText(getContext(), "Invalid email address", Toast.LENGTH_SHORT).show();
                }
            }
        });



        // Set touch listener on the root view
        if (view != null) {
            view.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    hideKeyboardAndCursor();
                    return false;
                }
            });
        }

        // You can do any additional setup or UI initialization here
        return view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Set other dialog properties if needed
        return dialog;
    }

    private boolean isValidEmail(String email) {
        // You can implement your email validation logic here
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void sendForgotPasswordEmail(String emailAddress) {
        // Create a reference to the Firestore "users" collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference usersCollection = db.collection("users");

        // Query to check if the provided email address exists in the "users" collection
        Query query = usersCollection.whereEqualTo("email", emailAddress);

        query.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Check if any documents match the query
                if (!task.getResult().isEmpty()) {
                    // Email address found in Firestore "users" collection
                    // Now, send the password reset email using FirebaseAuth
                    FirebaseAuth.getInstance().sendPasswordResetEmail(emailAddress)
                            .addOnCompleteListener(passwordResetTask -> {
                                if (passwordResetTask.isSuccessful()) {
                                    // Email sent successfully
                                    Toast.makeText(getContext(), "Password reset email sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    // If there is an issue with sending the reset email
                                    Toast.makeText(getContext(), "Failed to send reset email", Toast.LENGTH_SHORT).show();
                                }
                            });
                } else {
                    // Email address not found in Firestore "users" collection
                    Toast.makeText(getContext(), "Email address not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Error occurred while querying the "users" collection
                Toast.makeText(getContext(), "Error checking email address", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void hideKeyboardAndCursor() {
        if (getView() == null || getActivity() == null) {
            return; // Fragment not attached to an activity or view is null
        }

        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        View currentFocus = getView().findFocus();
        if (currentFocus != null) {
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
            currentFocus.clearFocus();
        }
    }
}
