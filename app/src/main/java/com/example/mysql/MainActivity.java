package com.example.mysql;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class MainActivity extends AppCompatActivity {

    private EditText usernameField, passwordField;
    private ProgressBar progressBar;
    private Button loginBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initLayouts();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String strUsername = usernameField.getText().toString();
                String strPassword = passwordField.getText().toString();

                LoginTask loginTask = new LoginTask(MainActivity.this);
                loginTask.execute(strUsername, strPassword);
            }
        });
    }

    private void initLayouts() {
        usernameField = findViewById(R.id.usernameField);
        passwordField = findViewById(R.id.passwordField);
        progressBar = findViewById(R.id.progressBar);
        loginBtn = findViewById(R.id.loginBtn);

        progressBar.setVisibility(View.GONE);
    }

    private class LoginTask extends AsyncTask<String, Void, String> {
        private Context context;
        private AlertDialog alertDialog;

        public LoginTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            alertDialog = new AlertDialog.Builder(context).create();
            alertDialog.setTitle("Etat de connexion");

            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... strings) {
            String result = "";
            String user = strings[0], pass = strings[1];
            String dbURL = "http://192.168.1.3/ExpertMaintenance/login.php";

            try {
                URL url = new URL(dbURL);
                HttpURLConnection http = (HttpURLConnection) url.openConnection();
                http.setRequestMethod("POST");
                http.setDoInput(true);
                http.setDoOutput(true);
                OutputStream ops = http.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(ops, "UTF-8"));
                String data = URLEncoder.encode("user", "UTF-8") + "=" +
                        URLEncoder.encode(user, "UTF-8") + "&&" +
                        URLEncoder.encode("pass", "UTF-8") + "=" +
                        URLEncoder.encode(pass, "UTF-8");
                // user=admin&&pass=123456

                writer.write(data);
                writer.flush();
                writer.close();
                InputStream ips = http.getInputStream();
                BufferedReader reader = new BufferedReader(new
                        InputStreamReader(ips, "UTF-8"));
                String ligne = "";
                while ((ligne = reader.readLine()) != null) {
                    result += ligne;
                }
                reader.close();
                ips.close();
                http.disconnect();
                return result;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            String message = "";
            progressBar.setVisibility(View.GONE);

            if (s.contains("server_fail")) {
                message = "Cannot connect to server !";
                displayAlertDialog(message);
            } else if (s.contains("login_fail")) {
                message = "Wrong username or password !";
                displayAlertDialog(message);
            }
            else if (s.contains("login_success")) {
                message = "Login successful !";
                displayAlertDialog(message);

                Intent i = new Intent();
                i.setClass(context.getApplicationContext(), WelcomeActivity.class);
                context.startActivity(i);
            }
        }

        private void displayAlertDialog(String string) {
            alertDialog.setMessage(string);
            alertDialog.show();
        }
    }

}