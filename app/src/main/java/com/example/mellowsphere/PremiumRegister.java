package com.example.mellowsphere;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PremiumRegister extends AppCompatActivity {
    private long lastBackPressTime = 0;
    int emptySpacePosIncrement = 0;
    EditText nameOnCard, cardNumber, expiryDate, securityCode, zipCode;
    Button getPremium, back;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_premium_register);

        nameOnCard = findViewById(R.id.nameoncard);
        cardNumber = findViewById(R.id.cardnumber);
        expiryDate = findViewById(R.id.expiry);
        securityCode = findViewById(R.id.security);
        zipCode = findViewById(R.id.zipcode);

        getPremium = findViewById(R.id.getpremium);
        back = findViewById(R.id.exitpremium);

        // Define the regex pattern for MM/YY format
        String regex = "^(0[1-9]|1[0-2])/(\\d{2})$";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(regex);

        cardNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String cardnumber = cardNumber.getText().toString().replaceAll("\\s", "");

                    Long.parseLong(cardnumber);
                }
                catch (NumberFormatException  e) {
                    if (!cardNumber.getText().toString().equals("")){
                        cardNumber.setError("Invalid card number");
                    }
                }

                // Remove the existing spaces
                String cardnumber = s.toString().replace(" ", "");

                // Add spaces every fourth digit
                StringBuilder formattedCardNumber = new StringBuilder();
                for (int i = 0; i < cardnumber.length(); i++) {
                    formattedCardNumber.append(cardnumber.charAt(i));
                    if ((i + 1) % 4 == 0 && i + 1 < cardnumber.length()) {
                        formattedCardNumber.append(" ");
                    }
                }

                // Set the formatted text back to the EditText
                cardNumber.removeTextChangedListener(this); // Avoid infinite loop
                cardNumber.setText(formattedCardNumber.toString());
                cardNumber.setSelection(formattedCardNumber.length());
                cardNumber.addTextChangedListener(this);
            }
        });

        getPremium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!(nameOnCard.getText().toString().length() >= 3 && nameOnCard.getText().toString().length() <= 26)){
                    nameOnCard.setError("Name should be within 3-26 characters");
                    return;
                }

                if (!(cardNumber.getText().toString().length() == 19)){
                    cardNumber.setError("Card number must be 16 digit");
                    return;
                }

                // Create a Matcher object
                Matcher matcher = pattern.matcher(expiryDate.getText().toString());

                // Check if the input matches the pattern
                if (!matcher.matches()) {
                    expiryDate.setError("Date should be in the format MM/YY");
                    return;
                }

                if (!(securityCode.getText().toString().length() == 3)) {
                    securityCode.setError("Security code must be 3 digits");
                    return;
                }

                if (zipCode.getText().toString().length() != 5) {
                    zipCode.setError("Zip code must be 5 digits");
                    return;
                }

                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                FirebaseFirestore db = FirebaseFirestore.getInstance();

                // Specify the field and its new value in a Map
                Map<String, Object> updates = new HashMap<>();
                updates.put("isPremium", "t");

                if (user != null) {
                    db.collection("users").document(user.getUid()).update(updates).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            Toast.makeText(PremiumRegister.this, "Enjoy our premium services!", Toast.LENGTH_SHORT).show();

                            Intent intent = getBaseContext().getPackageManager()
                                    .getLaunchIntentForPackage(getBaseContext().getPackageName());
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                    });
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Set touch listener on the root view
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboardAndCursor();
                return false;
            }
        });

    }

    private void hideKeyboardAndCursor() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            getCurrentFocus().clearFocus();
        }
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastBackPressTime < 2000) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            lastBackPressTime = currentTime;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}