package com.asus.amax.qrcodereader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button mOpenQRCodeReaderBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mOpenQRCodeReaderBtn = findViewById(R.id.open_qr_code_reader_btn);
        mOpenQRCodeReaderBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, BarcodeReaderActivity.class));
            }
        });
    }

    @Override
    protected void onDestroy() {
        mOpenQRCodeReaderBtn.setOnClickListener(null);
        super.onDestroy();
    }
}
