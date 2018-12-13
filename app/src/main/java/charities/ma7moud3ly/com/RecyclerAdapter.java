package charities.ma7moud3ly.com;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

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
        if (Main.showPic) {
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
                child(Main.admin_id).child(user_id).child(pic_name + ".jpg");
        if (storeRef == null) return;
        File localFile = null;
        try {
            localFile = File.createTempFile(user_id, "jpg");
        } catch (IOException e) {

            e.printStackTrace();
            progress.setVisibility(View.GONE);
            return;
        }
        final String path = localFile.getAbsolutePath();
        storeRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Bitmap bm = BitmapFactory.decodeFile(path);
                imageView.setImageBitmap(bm);
                imageView.setTag(path);
                progress.setVisibility(View.GONE);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progress.setVisibility(View.GONE);
                exception.printStackTrace();
            }
        });
    }

}


