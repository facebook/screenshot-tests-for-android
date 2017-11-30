// Copyright 2004-present Facebook. All Rights Reserved.

package com.example.screenshots;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class DetailActivity extends AppCompatActivity {

  public static final String TYPE = "type";
  public static final String TEXT = "text";

  enum Type {
    WARNING,
    OK,
    ERROR
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_detail);

    TextView textView = (TextView) findViewById(R.id.text_view);

    final Intent intent = getIntent();
    final Type textType = (Type) intent.getSerializableExtra(TYPE);
    final String text = intent.getStringExtra(TEXT);

    textView.setText(text);

    switch (textType) {
      case WARNING:
        textView.setTextColor(ContextCompat.getColor(this, R.color.warning));
        break;
      case OK:
        textView.setTextColor(ContextCompat.getColor(this, R.color.ok));
        break;
      case ERROR:
        textView.setTextColor(ContextCompat.getColor(this, R.color.error));
        break;
    }
  }
}
