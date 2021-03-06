package com.example.team24p;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editTextEmail, editTextPassword, editTextValidatePassword, editTextId,editTextFullName,editTextAddress,editTextPhoneNumber,editTextAge;
    private Button buttonRegister;
    private Button buttonLogin;
    private ProgressDialog progressDialog;
    private FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
    private DatabaseReference m_up = mDatabase.getReference().child("UsersAndPasswords");
    private DatabaseReference m_users = mDatabase.getReference().child("Users"); //CONNECT TO DB WITH RELEVANT TABLE

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        progressDialog = new ProgressDialog(this);
        findViewById(R.id.SignInButton).setOnClickListener(this);
        editTextValidatePassword = (EditText) findViewById(R.id.ValidatePasswordEditText);
        buttonRegister = (Button)findViewById(R.id.RegisterButton);
        buttonLogin = (Button)findViewById(R.id.SignInButton);
        editTextEmail = (EditText) findViewById(R.id.EmailTextView);
        editTextPassword = (EditText) findViewById(R.id.PasswordTextView);
        editTextId = (EditText) findViewById(R.id.IdTextView);
        editTextFullName = (EditText) findViewById(R.id.NameTextView);
        editTextAddress = (EditText) findViewById(R.id.AddressTextView);
        editTextPhoneNumber = (EditText) findViewById(R.id.PhoneTextView);
        editTextAge = (EditText) findViewById(R.id.AgeTextView);
        buttonRegister.setOnClickListener(this);
    }

    private void registerUser(){
        boolean flag = true;
        final String email = editTextEmail.getText().toString().trim();
        final String password = editTextPassword.getText().toString().trim();
        String validate = editTextValidatePassword.getText().toString().trim();
        final String id = editTextId.getText().toString().trim();
        final String fullName = editTextFullName.getText().toString().trim();
        final String adress = editTextAddress.getText().toString().trim();
        final String phone = editTextPhoneNumber.getText().toString().trim();
        final String age = editTextAge.getText().toString().trim();
//-----------regex to form
        if (!fullName.matches("^[a-zA-Z]+ [ a-zA-Z]+$")){
            editTextFullName.setError("Please enter valid name");
            editTextFullName.requestFocus();
            flag = false;
        }
        if (adress.isEmpty()){
            editTextAddress.setError("Address cannot be empty");
            editTextAddress.requestFocus();
            flag = false;
        }
        if (!id.matches("^[0-9]{9}$")){
            editTextId.setError("Please enter valid id");
            editTextId.requestFocus();
            flag = false;
        }
        if (!phone.matches("^05[0-9]{8}$")){
            editTextPhoneNumber.setError("Please enter valid phone number");
            editTextPhoneNumber.requestFocus();
            flag = false;
        }
        if (!age.matches("^[1-9][0-9]{1,2}$")){
            editTextAge.setError("Please enter valid age");
            editTextAge.requestFocus();
            flag = false;
        }
        if (email.isEmpty()){
            editTextEmail.setError("Email is required");
            editTextEmail.requestFocus();
            flag = false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            editTextEmail.setError("Please enter a valid email");
            editTextEmail.requestFocus();
            flag = false;
        }
        if (password.isEmpty()){
            editTextPassword.setError("Password is required");
            editTextPassword.requestFocus();
            flag = false;
        }
        if (password.length() < 6){
            editTextPassword.setError("Minimum length of password should be 6");
            editTextPassword.requestFocus();
            flag = false;
        }
        if (!validate.equals(password)){
            editTextValidatePassword.setError("Password and validate not the same");
            editTextValidatePassword.requestFocus();
            flag = false;
        }
        if (flag) { //-- check if all forms valid and user not registered
            m_up.orderByChild("UserName").equalTo(email).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (dataSnapshot.exists()) {
                        editTextEmail.setError("Email is already signed up");
                        editTextEmail.requestFocus();
                    } else {
                        progressDialog.setMessage("Registering User...");
                        progressDialog.show();
                        Map<String, String> userData = new HashMap<String, String>();
                        userData.put("Password", password);
                        userData.put("UserName", email);
                        userData.put("isAdmin", "False");
                        userData.put("enabled", "True");
                        m_up.push().setValue(userData); //upload to db
                        userData.clear(); //clear to use same obj again
                        userData.put("Name", fullName);
                        userData.put("address", adress);
                        userData.put("age", age);
                        userData.put("id", id);
                        userData.put("phone", phone);
                        userData.put("username", email);
                        m_users.push().setValue(userData);
                        Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
                        //send vars to other activity
                        intent.putExtra("userNameLoggedIn", email);
                        intent.putExtra("isAdmin", "False");
                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "User registered successfully", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(intent);

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }


    }


    @Override
    public void onClick(View view) {

        if (view == buttonRegister){
            registerUser();

        }
        if (view == buttonLogin)
        {
            finish();
            startActivity(new Intent(this,LoginActivity.class));
        }
    }
}
