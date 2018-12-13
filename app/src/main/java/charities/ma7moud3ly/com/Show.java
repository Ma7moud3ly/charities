package charities.ma7moud3ly.com;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

public class Show extends AppCompatActivity {

    private ImageView image;
    private String path;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.show);
        image = findViewById(R.id.image);
        Intent intent = getIntent();
        if (!intent.hasExtra("path")) finish();
        path = intent.getStringExtra("path");
        try {
            Bitmap bm = BitmapFactory.decodeFile(path);
            image.setImageBitmap(bm);
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }


}
