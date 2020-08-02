package com.ma7moud3ly.charities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

    private ArrayList<HashMap<String, String>> myList;
    private Context c;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public FrameLayout layout;
        public TextView name, phone;
        public ImageView image;
        public ProgressBar refresh;

        public MyViewHolder(View view) {
            super(view);
            layout = view.findViewById(R.id.item_pic_layout);
            name = view.findViewById(R.id.item_name);
            phone = view.findViewById(R.id.item_phone);
            image = view.findViewById(R.id.item_image);
            refresh = view.findViewById(R.id.item_refresh);
        }
    }


    public RecyclerAdapter(ArrayList<HashMap<String, String>> myList, Context c) {
        this.myList = myList;
        this.c = c;
    }

    @Override
    public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item, parent, false);
        return new RecyclerAdapter.MyViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(RecyclerAdapter.MyViewHolder holder, int position) {
        HashMap<String, String> user = myList.get(position);
        holder.name.setText(user.get("name"));
        holder.phone.setText(user.get("phone"));
        if (/*Main.showPic*/true) {
            holder.layout.setVisibility(View.VISIBLE);
            load_picture("user_pic", user.get("id"), holder.image, holder.refresh);
        } else {
            holder.layout.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return myList.size();
    }

    private void load_picture(final String pic_name, final String user_id, final ImageView imageView, final ProgressBar progress) {
        StorageReference storeRef = FirebaseStorage.getInstance().getReference().child("users").
                child(MainActivity.admin_id).child(user_id).child(pic_name + ".jpg");
        if (storeRef == null) return;
        File dir = new File(c.getApplicationInfo().dataDir + "/com");
        if (!dir.exists()) dir.mkdir();
        File localFile = new File(dir, pic_name + user_id + ".jpg");
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
            progress.setVisibility(View.GONE);
            exception.printStackTrace();
        });
    }

}


