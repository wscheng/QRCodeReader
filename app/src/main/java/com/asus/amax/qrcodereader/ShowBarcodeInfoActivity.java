package com.asus.amax.qrcodereader;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ShowBarcodeInfoActivity extends AppCompatActivity {

    public static final String BARCODE_STR = "BARCODE_STR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_barcode_info);
        Intent intent = getIntent();
        String barcode = "";
        if(intent!=null) {
            barcode = intent.getStringExtra(BARCODE_STR);
        }

        TextView barcodeText = findViewById(R.id.barcode);
        barcodeText.setText(barcode);

        Button okButton = findViewById(R.id.ok);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
