package com.example.captureimageapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class Login extends AppCompatActivity{
    public static final String LOGIN_URL= "http://192.168.43.73/final.test/api/checklogin";
    public static final String KEY_USERNAME= "username";
    public static final String KEY_PASSWORD= "password";
    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private String username;
    private String password;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        usernameEditText = findViewById(R.id.activity_main_usernameEditText);
        passwordEditText = findViewById(R.id.activity_main_passwordEditText);
        loginButton = findViewById(R.id.activity_main_loginButton);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userLogin();
            }

            private void userLogin()
            {
                username = usernameEditText.getText().toString().trim();
                password = passwordEditText.getText().toString().trim();

                StringRequest stringRequest = new StringRequest(Request.Method.POST, LOGIN_URL,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                Log.d("res", response);
                                if (response.trim().contains("successfully logged in")) {
                                    openProfile();
                                } else {
                                    Toast.makeText(Login.this, response, Toast.LENGTH_LONG).show();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Toast.makeText(Login.this, error.toString(), Toast.LENGTH_LONG).show();
                            }
                        }) {
                    @Override
                    public Map<String, String> getParams() throws AuthFailureError {
                        Map<String, String> map = new HashMap<>();
                        map.put(KEY_USERNAME, username);
                        map.put(KEY_PASSWORD, password);
                        return map;
                    }
                };
                RequestQueue requestQueue = Volley.newRequestQueue(Login.this);
                requestQueue.add(stringRequest);
            }

            private void openProfile() {
                Intent intent = new Intent(Login.this, MainActivity.class);
                //intent.putExtra(KEY_USERNAME, username);
                startActivity(intent);
                finish();
            }


        });


    }


}