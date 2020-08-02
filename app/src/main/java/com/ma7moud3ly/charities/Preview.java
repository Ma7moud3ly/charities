package com.ma7moud3ly.charities;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;


public class Preview extends AppCompatActivity {

    private TextView name, age, children, children_education, address, phone, date,
            gov_salary, charity_salary, salary_places, illness, gender;
    private ImageView user_pic, id_pic, birth_pic;
    private ProgressBar user_pic_progress, id_pic_progress, birth_pic_progress;
    private String user_id;
    private HashMap<String, String> user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview);

        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        date = findViewById(R.id.date);
        gender = findViewById(R.id.gender);
        children = findViewById(R.id.children);
        children_education = findViewById(R.id.children_education);
        address = findViewById(R.id.address);
        phone = findViewById(R.id.phone);
        phone.setPaintFlags(phone.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        gov_salary = findViewById(R.id.gov_salary);
        charity_salary = findViewById(R.id.charity_salary);
        salary_places = findViewById(R.id.salary_places);
        illness = findViewById(R.id.illness);

        user_pic = findViewById(R.id.user_pic);
        user_pic_progress = findViewById(R.id.user_pic_progress);
        id_pic = findViewById(R.id.id_pic);
        id_pic_progress = findViewById(R.id.id_pic_progress);
        birth_pic = findViewById(R.id.birth_pic);
        birth_pic_progress = findViewById(R.id.birth_pic_progress);

        Intent intent = getIntent();
        if (!intent.hasExtra("user")) finish();
        user = (HashMap<String, String>) intent.getSerializableExtra("user");

        /*name.setText(user.get("name"));
        age.setText(user.get("age"));
        date.setText(user.get("date"));
        gender.setText(user.get("gender").equals("1") ? "ذكر" : "أنثى");
        children.setText(user.get("children"));
        children_education.setText(user.get("children_education"));
        address.setText(user.get("address"));
        phone.setText(user.get("phone"));
        gov_salary.setText(user.get("gov_salary"));
        charity_salary.setText(user.get("charity_salary"));
        salary_places.setText(user.get("salary_places"));
        illness.setText(user.get("illness"));*/

        user_id = user.get("id");

        gender.setText(user.get("gender").equals("1") ? "ذكر" : "أنثى");
        setVal(name, user.get("name"));
        setVal(age, user.get("age"));
        setVal(date, user.get("date"));
        setVal(children, user.get("children"));
        setVal(children_education, user.get("children_education"));
        setVal(address, user.get("address"));
        setVal(phone, user.get("phone"));
        setVal(gov_salary, user.get("gov_salary"));
        setVal(charity_salary, user.get("charity_salary"));
        setVal(salary_places, user.get("salary_places"));
        setVal(illness, user.get("illness"));

        if (user.containsKey("profile") && user.get("profile").equals("1"))
            load_picture("user_pic", user_pic, user_pic_progress);
        else {
            user_pic.setVisibility(View.GONE);
            user_pic_progress.setVisibility(View.GONE);
        }

    }

    private void setVal(TextView tv, String val) {
        if (val.equals("")) ((LinearLayout) tv.getParent()).setVisibility(View.GONE);
        else tv.setText(val);
    }

    private void load_picture(final String pic_name, final ImageView imageView, final ProgressBar progress) {
        StorageReference storeRef = FirebaseStorage.getInstance().getReference().child("users").
                child(MainActivity.admin_id).child(user_id).child(pic_name + ".jpg");
        File dir = new File(this.getApplicationInfo().dataDir + "/com");
        if (!dir.exists()) dir.mkdir();
        File localFile = new File(dir,  pic_name + user_id + ".jpg");
        final String path = localFile.getAbsolutePath();
        if (localFile.exists()) {
            Bitmap bm = BitmapFactory.decodeFile(path);
            imageView.setImageBitmap(bm);
            imageView.setTag(path);
            progress.setVisibility(View.GONE);
            return;
        }
        storeRef.getFile(localFile).addOnSuccessListener(taskSnapshot -> {
            Bitmap bm = BitmapFactory.decodeFile(path);
            imageView.setImageBitmap(bm);
            imageView.setTag(path);
            progress.setVisibility(View.GONE);
        }).addOnFailureListener(exception -> {
            imageView.setVisibility(View.GONE);
            progress.setVisibility(View.GONE);
            exception.printStackTrace();
        });
    }

    public void pback(View v) {
        finish();
    }

    public void delete(View v) {
        FirebaseDatabase.getInstance().getReference().child("users").child(MainActivity.admin_id).child(user_id).removeValue();
        FirebaseStorage.getInstance().getReference().child("users").child(MainActivity.admin_id).child(user_id).child("user.jpg").delete();
        FirebaseStorage.getInstance().getReference().child("users").child(MainActivity.admin_id).child(user_id).child("id.jpg").delete();
        FirebaseStorage.getInstance().getReference().child("users").child(MainActivity.admin_id).child(user_id).child("birth.jpg").delete();
        finish();
    }

    public void edit(View v) {
        if (user == null) return;
        Intent intent = new Intent(this, Form.class);
        intent.putExtra("user", user);
        startActivity(intent);
        finish();
    }

    public void show(View v) {
        ImageView image = (ImageView) v;
        if (image.getTag() != null) {
            Intent intent = new Intent(this, Show.class);
            intent.putExtra("path", image.getTag().toString());
            startActivity(intent);
        }
    }

    public void load_id(View v) {
        id_pic_progress.setVisibility(View.VISIBLE);
        id_pic.setVisibility(View.VISIBLE);
        load_picture("id_pic", id_pic, id_pic_progress);
    }

    public void load_birth(View v) {
        birth_pic_progress.setVisibility(View.VISIBLE);
        birth_pic.setVisibility(View.VISIBLE);
        load_picture("birth_pic", birth_pic, birth_pic_progress);
    }

    public void call(View v) {
        String x = phone.getText().toString();
        if (x.trim().isEmpty()) return;
        startActivity(new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + x)));
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            View v = phone;
            call(v);
        } else
            Toast.makeText(getApplicationContext(), "You must enable the phone call permission", Toast.LENGTH_LONG).show();

    }

}
