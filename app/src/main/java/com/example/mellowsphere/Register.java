package com.example.mellowsphere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.text.method.SingleLineTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class Register extends AppCompatActivity {
    private long lastBackPressTime = 0;
    private FirebaseAuth mAuth;

    //for switching between normal and error display of edittext
    private boolean switchEditTextdisp;

    //for setting the constraint of linear layout containing option to switch to sign up or login
    ConstraintLayout mainLayout;
    LinearLayout linearLayout;
    ConstraintLayout.LayoutParams layoutParams;
    TextView title;

    //components of sign up display
    EditText usernameET;
    EditText emailET;
    RelativeLayout relativeLayout;
    EditText passwordET;
    ImageView togglePasswordVisibility;
    RelativeLayout relativeLayout2;
    EditText confPassET;
    ImageView togglePasswordVisibility2;

    //components of sign up display
    EditText usernameLogin;
    RelativeLayout relativeLayout3;
    EditText passwordLogin;
    ImageView togglePasswordVisibility3;
    TextView forgotPass;
    private boolean isPasswordVisible = false;

    //components for switching display
    TextView switchTV;
    TextView switchDisplayTV;
    Button submit;
    private boolean switchDisplay = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        mainLayout = findViewById(R.id.constrlayout);
        linearLayout = findViewById(R.id.linearLayout);
        layoutParams = (ConstraintLayout.LayoutParams) linearLayout.getLayoutParams();

        title = findViewById(R.id.signuplogin);

        //signup
        usernameET = findViewById(R.id.usernameET);
        emailET = findViewById(R.id.emailET);
        relativeLayout = findViewById(R.id.relativelayout);
        passwordET = findViewById(R.id.passwordET);
        togglePasswordVisibility = findViewById(R.id.togglePasswordVisibility);
        relativeLayout2 = findViewById(R.id.relativelayout2);
        confPassET = findViewById(R.id.confpassET);
        togglePasswordVisibility2 = findViewById(R.id.togglePasswordVisibility2);

        //login
        usernameLogin = findViewById(R.id.usernamelogin);
        relativeLayout3 = findViewById(R.id.relativelayout3);
        passwordLogin = findViewById(R.id.passwordlogin);
        togglePasswordVisibility3 = findViewById(R.id.togglePasswordVisibility3);
        forgotPass = findViewById(R.id.forgotpass);

        //switch display
        switchTV = findViewById(R.id.switchTV);
        switchDisplayTV = findViewById(R.id.switchdisplayTV);
        submit = findViewById(R.id.submit);

        // Set touch listener on the root view
        ViewGroup rootView = findViewById(android.R.id.content);
        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                hideKeyboardAndCursor();
                return false;
            }
        });

        //onclick function for submitting
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (switchDisplay) {
                    login();
                }
                else {
                    register();
                }
            }
        });

        handlePasswordVisibility(togglePasswordVisibility, passwordET);
        handlePasswordVisibility(togglePasswordVisibility2, confPassET);
        handlePasswordVisibility(togglePasswordVisibility3, passwordLogin);

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPasswordDialog();
            }
        });
    }

    private void showForgotPasswordDialog() {
        ForgotPassword forgotPasswordDialog = new ForgotPassword();
        forgotPasswordDialog.show(getSupportFragmentManager(), "ForgotPasswordDialog");
    }
    public void switchDisp(View view) {
        switchDisplay = !switchDisplay;

        //hide keyboard if displayed
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (getCurrentFocus() != null) {
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            getCurrentFocus().clearFocus();
        }
        if (switchDisplay){
            logInDisplay();
        }
        else{
            signUpDisplay();
        }
    }

    public void logInDisplay() {
        layoutParams.topToBottom = relativeLayout3.getId();
        linearLayout.setLayoutParams(layoutParams);
        mainLayout.requestLayout();

        //showing login components
        usernameLogin.setVisibility(View.VISIBLE);
        relativeLayout3.setVisibility(View.VISIBLE);
        forgotPass.setVisibility(View.VISIBLE);

        usernameLogin.setBackgroundResource(R.drawable.custom_edittext_1);
        passwordLogin.setBackgroundResource(R.drawable.custom_edittext_1);

        usernameLogin.setText("");
        passwordLogin.setText("");

        Drawable vectorAsset = ContextCompat.getDrawable(this, R.drawable.custom_username_icon);
        usernameLogin.setCompoundDrawablesRelativeWithIntrinsicBounds(vectorAsset, null, null, null);

        vectorAsset = ContextCompat.getDrawable(this, R.drawable.custom_password_icon);
        passwordLogin.setCompoundDrawablesRelativeWithIntrinsicBounds(vectorAsset, null, null, null);

        //hiding signup components
        usernameET.setVisibility(View.GONE);
        emailET.setVisibility(View.GONE);
        relativeLayout.setVisibility(View.GONE);
        relativeLayout2.setVisibility(View.GONE);

        title.setText("Log In");
        switchTV.setText("Don't have an account? ");
        switchDisplayTV.setText("Sign Up");

        submit.setText("Login");
    }
    public void signUpDisplay() {
        layoutParams.topToBottom = relativeLayout2.getId();
        linearLayout.setLayoutParams(layoutParams);
        mainLayout.requestLayout();

        //hiding login components
        usernameLogin.setVisibility(View.GONE);
        relativeLayout3.setVisibility(View.GONE);
        forgotPass.setVisibility(View.GONE);

        //showing signup components
        usernameET.setVisibility(View.VISIBLE);
        emailET.setVisibility(View.VISIBLE);
        relativeLayout.setVisibility(View.VISIBLE);
        relativeLayout2.setVisibility(View.VISIBLE);

        usernameET.setBackgroundResource(R.drawable.custom_edittext_1);
        emailET.setBackgroundResource(R.drawable.custom_edittext_1);
        passwordET.setBackgroundResource(R.drawable.custom_edittext_1);
        confPassET.setBackgroundResource(R.drawable.custom_edittext_1);

        usernameET.setText("");
        emailET.setText("");
        passwordET.setText("");
        confPassET.setText("");

        Drawable vectorAsset = ContextCompat.getDrawable(this, R.drawable.custom_username_icon);
        usernameET.setCompoundDrawablesRelativeWithIntrinsicBounds(vectorAsset, null, null, null);

        vectorAsset = ContextCompat.getDrawable(this, R.drawable.custom_email_icon);
        emailET.setCompoundDrawablesRelativeWithIntrinsicBounds(vectorAsset, null, null, null);

        vectorAsset = ContextCompat.getDrawable(this, R.drawable.custom_password_icon);
        passwordET.setCompoundDrawablesRelativeWithIntrinsicBounds(vectorAsset, null, null, null);

        vectorAsset = ContextCompat.getDrawable(this, R.drawable.custom_password_icon);
        confPassET.setCompoundDrawablesRelativeWithIntrinsicBounds(vectorAsset, null, null, null);

        passwordET.setTransformationMethod(new PasswordTransformationMethod());
        togglePasswordVisibility.setImageResource(R.drawable.password_invisible);

        confPassET.setTransformationMethod(new PasswordTransformationMethod());
        togglePasswordVisibility2.setImageResource(R.drawable.password_invisible);

        passwordLogin.setTransformationMethod(new PasswordTransformationMethod());
        togglePasswordVisibility3.setImageResource(R.drawable.password_invisible);

        title.setText("Sign Up");
        switchTV.setText("Already have an account? ");
        switchDisplayTV.setText("Log In");

        submit.setText("Register");
    }

    public void back(View view) {
        finish();
    }

    private void login(){
        String username, password;

        username = usernameLogin.getText().toString();
        password = passwordLogin.getText().toString();

        checkIfEmpty(username, usernameLogin, R.drawable.custom_username_icon_error, R.drawable.custom_username_icon);
        checkIfEmpty(password, passwordLogin, R.drawable.custom_password_icon_error, R.drawable.custom_password_icon);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter required credentials", Toast.LENGTH_SHORT).show();
            return;
        }

        verifyUsername(username, password);
    }

    private void verifyUsername(String username, String password) {

        // Store additional user data in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users")
            .whereEqualTo("username", username)
            .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                 @Override
                 public void onComplete(@NonNull Task<QuerySnapshot> task) {
                     if (task.isSuccessful()) {
                         if (task.getResult().isEmpty()){
                             Toast.makeText(Register.this, "Authentication failed.",
                                     Toast.LENGTH_SHORT).show();
                         }
                         else{
                             for (QueryDocumentSnapshot userDoc : task.getResult()) {
                                 // Retrieve the email from the user document
                                 String email = userDoc.getString("email");
                                 String uid = userDoc.getId();


                                 signIn(email, password, db);
                             }
                         }
                     }
                     else {
                         Toast.makeText(Register.this, "Authentication failed.",
                                 Toast.LENGTH_SHORT).show();
                     }
                 }
             }
        );
    }
    private void signIn(String email, String password, FirebaseFirestore db){
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult() == null){
                            Toast.makeText(Register.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                        else {
                            FirebaseUser user = mAuth.getCurrentUser();

                            assert user != null;
                            db.collection("expiration").document(user.getUid()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    DocumentSnapshot result = task.getResult();
                                    if (result == null) {
                                        Toast.makeText(Register.this, ".",
                                                Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        long expirationTimestamp = result.getLong("expiry");

                                        if (result.getLong("current") == null) {
                                            long currentTime = System.currentTimeMillis();

                                            if (currentTime < expirationTimestamp && user.isEmailVerified()) {
                                                db.collection("expiration").document(user.getUid()).update("current", currentTime);

                                                // Sign in success, update UI with the signed-in user's information
                                                Toast.makeText(Register.this, "Login successful",
                                                        Toast.LENGTH_SHORT).show();

//                                                Intent homepage = new Intent(getApplicationContext(), MainActivity.class);
//                                                homepage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                startActivity(homepage);
//                                                finish();

                                                Intent intent = getBaseContext().getPackageManager()
                                                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                            else if (currentTime < expirationTimestamp && !user.isEmailVerified()) {
                                                int minutes = (int) (expirationTimestamp-currentTime)/1000/60;
                                                int seconds = (int) (expirationTimestamp-currentTime)/1000 - minutes*60;
                                                Toast.makeText(Register.this, "Please verify email within "+minutes+"m "+seconds+"s",
                                                        Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                            }
                                            else {
                                                // Get a Firestore instance
                                                FirebaseFirestore  firestore = FirebaseFirestore.getInstance();

                                                String users = "users";
                                                String expiration = "expiration";
                                                String documentId = user.getUid();

                                                // Create a reference to the document
                                                firestore.collection(users).document(documentId).delete();

                                                // Create a reference to the document
                                                firestore.collection(expiration).document(documentId).delete();

                                                mAuth.signOut();
                                                user.delete();
                                            }
                                        }
                                        else {
                                            long currentTime = result.getLong("current");

                                            if (currentTime < expirationTimestamp && user.isEmailVerified()) {
                                                // Sign in success, update UI with the signed-in user's information
                                                Toast.makeText(Register.this, "Login successful",
                                                        Toast.LENGTH_SHORT).show();

//                                                Intent homepage = new Intent(getApplicationContext(), MainActivity.class);
//                                                homepage.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                                                startActivity(homepage);
//                                                finish();

                                                Intent intent = getBaseContext().getPackageManager()
                                                        .getLaunchIntentForPackage(getBaseContext().getPackageName());
                                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                startActivity(intent);
                                            }
                                            else if (currentTime < expirationTimestamp && !user.isEmailVerified()) {
                                                int minutes = (int) (expirationTimestamp-currentTime)/1000/60;
                                                int seconds = (int) (expirationTimestamp-currentTime)/1000 - minutes*60;
                                                Toast.makeText(Register.this, "Please verify email within "+minutes+"m "+seconds+"s",
                                                        Toast.LENGTH_SHORT).show();
                                                mAuth.signOut();
                                            }
                                            else {
                                                // Get a Firestore instance
                                                FirebaseFirestore  firestore = FirebaseFirestore.getInstance();

                                                String users = "users";
                                                String expiration = "expiration";
                                                String documentId = user.getUid();

                                                // Create a reference to the document
                                                firestore.collection(users).document(documentId).delete();

                                                // Create a reference to the document
                                                firestore.collection(expiration).document(documentId).delete();

                                                mAuth.signOut();
                                                user.delete();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }

                    else {
                        Toast.makeText(Register.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void register(){
        String username, email, password;

        username = usernameET.getText().toString();
        email = emailET.getText().toString();
        password = passwordET.getText().toString();

        checkIfEmpty(username, usernameET, R.drawable.custom_username_icon_error, R.drawable.custom_username_icon);
        checkIfEmpty(email, emailET, R.drawable.custom_email_icon_error, R.drawable.custom_email_icon);
        checkIfEmpty(password, passwordET, R.drawable.custom_password_icon_error, R.drawable.custom_password_icon);
        checkIfEmpty(confPassET.getText().toString(), confPassET, R.drawable.custom_password_icon_error, R.drawable.custom_password_icon);



        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please enter required credentials", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(username.length() >= 3 && username.length() <= 30)) {
            Toast.makeText(getApplicationContext(), "Username must be minimum 3 and maximum 30 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!(password.length() >= 8 && password.length() <= 16)){
            Toast.makeText(getApplicationContext(), "Password must be minimum 8 characters or maximum 16 characters", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confPassET.getText().toString())){
            Toast.makeText(getApplicationContext(), "Passwords don't match", Toast.LENGTH_SHORT).show();
            return;
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").whereEqualTo("username", username).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Username already exists",
                                Toast.LENGTH_SHORT).show();
                    }
                    else {
                        createUser(username, email, password);
                    }
                }
            }
        });
    }
    private void createUser(String username, String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();

                        // Store additional user data in Firestore
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        sendVerificationEmailAndCreateUser(user, db, email, username);
                    }
                    else {
                        // If sign in fails, display a message to the user.
                        Toast.makeText(Register.this, "Authentication failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        );
    }

    private void sendVerificationEmailAndCreateUser(FirebaseUser user, FirebaseFirestore db, String email, String username) {
        // Send verification email
        user.sendEmailVerification()
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> emailTask) {
                    if (emailTask.isSuccessful()) {
                        // Email sent successfully
                        Toast.makeText(getApplicationContext(), "Verification mail sent to "+email, Toast.LENGTH_SHORT).show();
                        Toast.makeText(getApplicationContext(), "Verify email within 3 minutes to successfully create account", Toast.LENGTH_SHORT).show();
                        // Store timestamp in Firebase Database or Cloud Firestore
                        long expirationTimestamp = System.currentTimeMillis() + (3 * 60 * 1000);
                        Map<String, Object> expirationTime = new HashMap<>();
                        expirationTime.put("expiry", expirationTimestamp);
                        expirationTime.put("current", null);

                        db.collection("expiration").document(user.getUid()).set(expirationTime);

                        Map<String, Object> userData = new HashMap<>();
                        userData.put("email", email);
                        userData.put("username", username);
                        userData.put("isPremium", "f");

                        assert user != null;
                        db.collection("users").document(user.getUid()).set(userData)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    // Registration and data storage successful
                                    switchDisp(findViewById(R.id.switchdisplayTV));
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(getApplicationContext(), "Firestore data storing failed.",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        );
                    }
                    else {
                        // Email not sent, handle the error
                        Exception exception = emailTask.getException();
                        if (exception instanceof FirebaseAuthException) {
                            FirebaseAuthException authException = (FirebaseAuthException) exception;
                            if (authException.getErrorCode().equals("ERROR_INVALID_EMAIL")) {
                                // Invalid email address
                                Toast.makeText(Register.this, "Invalid email address.",
                                        Toast.LENGTH_SHORT).show();
                                user.delete();
                            }
                            else {
                                // Handle other FirebaseAuthExceptions
                                Toast.makeText(Register.this, "Email verification failed: " + exception.getMessage(),
                                        Toast.LENGTH_SHORT).show();
                                user.delete();
                            }
                        }
                    }
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

    private void checkIfEmpty(String str, EditText editText, int errorDrawableId, int normalDrawableID){
        if (str.isEmpty()) {
            switchEditTextdisp = true;
            switchEditTextDisp(editText, errorDrawableId, normalDrawableID);
        }
        else{
            switchEditTextdisp = false;
            switchEditTextDisp(editText, errorDrawableId, normalDrawableID);
        }
    }

    private void switchEditTextDisp(EditText edittext, int errorDrawableId, int normalDrawableID) {
        if (switchEditTextdisp){
            edittext.setBackgroundResource(R.drawable.custom_edittext_2);
            Drawable vectorAsset = ContextCompat.getDrawable(this, errorDrawableId);
            edittext.setCompoundDrawablesRelativeWithIntrinsicBounds(vectorAsset, null, null, null);
        }
        else{
            edittext.setBackgroundResource(R.drawable.custom_edittext_1);
            Drawable vectorAsset = ContextCompat.getDrawable(this, normalDrawableID);
            edittext.setCompoundDrawablesRelativeWithIntrinsicBounds(vectorAsset, null, null, null);
        }
    }

    private void handlePasswordVisibility(ImageView imageView, EditText editText) {
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editText.getTransformationMethod().getClass().getSimpleName() .equals("PasswordTransformationMethod")) {
                    editText.setTransformationMethod(new SingleLineTransformationMethod());
                    imageView.setImageResource(R.drawable.password_visible);
                }
                else {
                    editText.setTransformationMethod(new PasswordTransformationMethod());
                    imageView.setImageResource(R.drawable.password_invisible);
                }

                editText.setSelection(editText.getText().length());
            }
        });
    }
}