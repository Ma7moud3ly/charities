package charities.ma7moud3ly.com;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.FileNotFoundException;
import java.util.HashMap;


public class Form extends AppCompatActivity {
    private EditText name, age, children, children_education, address, date,
            phone, gov_salary, charity_salary, salary_places, illness;
    private RadioButton male, female;
    private ImageView user_pic, id_pic, birth_pic;
    private Uri userPic, idPic, birthPic;
    private TextView id;
    private ProgressBar progress;
    private LinearLayout layout;
    private HashMap<String, String> form;
    private static String user_id = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.form);

        layout = findViewById(R.id.input_layout);
        name = findViewById(R.id.name);
        age = findViewById(R.id.age);
        date = findViewById(R.id.date);
        children = findViewById(R.id.children);
        children_education = findViewById(R.id.children_education);
        address = findViewById(R.id.address);
        phone = findViewById(R.id.phone);
        gov_salary = findViewById(R.id.gov_salary);
        charity_salary = findViewById(R.id.charity_salary);
        salary_places = findViewById(R.id.salary_places);
        illness = findViewById(R.id.illness);
        male = findViewById(R.id.male);
        male.setChecked(true);
        female = findViewById(R.id.female);

        user_pic = findViewById(R.id.user_pic);
        user_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose(USER_CODE);
            }
        });
        birth_pic = findViewById(R.id.birth_pic);
        birth_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose(BIRTH_CODE);
            }
        });
        id_pic = findViewById(R.id.id_pic);
        id_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                choose(ID_CODE);
            }
        });

        id = findViewById(R.id.id);
        id.setText(String.valueOf(Main.count + 1));
        progress = findViewById(R.id.input_progress);
        name.requestFocus();
        user_id = "";

        Intent intent = getIntent();
        if (intent.hasExtra("user")) {
            HashMap<String, String> user = (HashMap<String, String>) intent.getSerializableExtra("user");
            user_id = user.get("id");
            name.setText(user.get("name"));
            age.setText(user.get("age"));
            date.setText(user.get("date"));
            if (user.get("gender").equals("1")) male.setChecked(true);
            else female.setChecked(true);
            children.setText(user.get("children"));
            children_education.setText(user.get("children_education"));
            address.setText(user.get("address"));
            phone.setText(user.get("phone"));
            gov_salary.setText(user.get("gov_salary"));
            charity_salary.setText(user.get("charity_salary"));
            salary_places.setText(user.get("salary_places"));
            illness.setText(user.get("illness"));
            id.setText(String.valueOf("-"));
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            layout.setVisibility(show ? View.GONE : View.VISIBLE);
            layout.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    layout.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            progress.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progress.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            progress.setVisibility(show ? View.VISIBLE : View.GONE);
            layout.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        View v = layout;
        back(v);
    }

    public void save(View v) {
        if (!new Main().isConnected(this)) {
            Snackbar.make(layout, getResources().getString(R.string.action_no_internet_connection),
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }

        if (name.getText().toString().trim().equals("")) {
            Snackbar.make(layout, getResources().getString(R.string.action_no_inputs),
                    Snackbar.LENGTH_LONG).setAction("Action", null).show();
            return;
        }

        form = new HashMap<>();
        form.put("name", name.getText().toString());
        form.put("age", age.getText().toString());
        form.put("date", date.getText().toString());
        form.put("children", children.getText().toString());
        form.put("children_education", children_education.getText().toString());
        form.put("address", address.getText().toString());
        form.put("phone", phone.getText().toString());
        form.put("gov_salary", gov_salary.getText().toString());
        form.put("charity_salary", charity_salary.getText().toString());
        form.put("salary_places", salary_places.getText().toString());
        form.put("illness", illness.getText().toString());
        form.put("gender", male.isChecked() ? "1" : "2");


        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("users").child(Main.admin_id);

        if (user_id.equals("")) user_id = dbRef.push().getKey();

        for (String key : form.keySet()) {
            dbRef.child(user_id).child(key).setValue(form.get(key).toString());
        }

        StorageReference storeRef = FirebaseStorage.getInstance().getReference().child("users").child(Main.admin_id).child(user_id);

        showProgress(true);
        upload_user_pic(storeRef);
    }

    private void upload_user_pic(final StorageReference storeRef) {
        if (userPic == null) {
            upload_id_pic(storeRef);
            return;
        }
        storeRef.child("user_pic.jpg").putFile(userPic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.upload_user_pic), Toast.LENGTH_LONG).show();
                upload_id_pic(storeRef);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    private void upload_id_pic(final StorageReference storeRef) {
        if (idPic == null) {
            upload_birth_pic(storeRef);
            return;
        }
        storeRef.child("id_pic.jpg").putFile(idPic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.upload_id_pic), Toast.LENGTH_LONG).show();
                upload_birth_pic(storeRef);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    private void upload_birth_pic(final StorageReference storeRef) {
        if (birthPic == null) {
            finish();
            return;
        }
        storeRef.child("birth_pic.jpg").putFile(birthPic).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(getApplicationContext(), getResources().getString(R.string.upload_birth_pic), Toast.LENGTH_LONG).show();
                finish();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public void back(View v) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setMessage(getResources().getString(R.string.action_back_message));
        ad.setNegativeButton(getResources().getString(R.string.action_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showProgress(false);
            }
        });
        ad.setPositiveButton(getResources().getString(R.string.action_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
        ad.show();
    }

    final int USER_CODE = 1, BIRTH_CODE = 2, ID_CODE = 3;

    private void choose(int code) {
        if (!isStoragePermissionGranted()) return;
        Intent getIntent = new Intent(Intent.ACTION_GET_CONTENT);
        getIntent.setType("image/*");
        Intent pickIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickIntent.setType("image/*");
        Intent chooserIntent = Intent.createChooser(getIntent, "Select Image");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{pickIntent});
        startActivityForResult(chooserIntent, code);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK || data == null) return;
        Uri uri = data.getData();
        Bitmap bm = null;
        try {
            bm = BitmapFactory.decodeStream(getContentResolver().openInputStream(uri));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (bm == null) return;
        if (requestCode == USER_CODE) {
            userPic = uri;
            user_pic.setImageBitmap(bm);
            user_pic.setBackgroundResource(android.R.color.transparent);
        } else if (requestCode == BIRTH_CODE) {
            birthPic = uri;
            birth_pic.setImageBitmap(bm);
            birth_pic.setBackgroundResource(android.R.color.transparent);
        } else if (requestCode == ID_CODE) {
            idPic = uri;
            id_pic.setImageBitmap(bm);
            id_pic.setBackgroundResource(android.R.color.transparent);
        }
    }

    private boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            ;//choose();
        } else
            Toast.makeText(getApplicationContext(), "You must enable the external storage permission", Toast.LENGTH_LONG).show();

    }

}
