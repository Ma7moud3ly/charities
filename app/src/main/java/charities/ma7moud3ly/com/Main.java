package charities.ma7moud3ly.com;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Main extends AppCompatActivity {
    public static String email = "";
    public static String admin_id = "";
    public static long count = -1;
    private boolean loading;
    private FirebaseAuth mAuth;
    private RecyclerView usersRecyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;
    private SwipeRefreshLayout refresh;
    private SearchView search;
    private CheckBox show_pic;
    public static boolean showPic = false;
    private ArrayList<HashMap<String, String>> users_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mAuth = FirebaseAuth.getInstance();

        init_users();

        show_pic = findViewById(R.id.show_pic);
        show_pic.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                showPic = b;
                read("");
            }
        });


        search = findViewById(R.id.search);
        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                read(s.trim());
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                if (s.trim().equals("")) read("");
                return false;
            }
        });
    }

    private void init_users() {
        refresh = findViewById(R.id.refresh);
        refresh.setEnabled(false);

        usersRecyclerView = findViewById(R.id.users);
        usersRecyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        usersRecyclerView.setLayoutManager(layoutManager);
        usersRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        usersRecyclerView.setItemAnimator(new DefaultItemAnimator());

        recyclerViewAdapter = new RecyclerAdapter(users_list, this);

        usersRecyclerView.setAdapter(recyclerViewAdapter);
        final Intent previewIntent = new Intent(this, Preview.class);
        usersRecyclerView.addOnItemTouchListener(new RecyclerTouchListener(this,
                usersRecyclerView, new RecyclerTouchListener.ClickListener() {
            @Override
            public void onClick(View view, int position) {
                HashMap<String, String> user = users_list.get(position);
                previewIntent.putExtra("user", user);
                startActivity(previewIntent);
            }

            @Override
            public void onLongClick(View view, int position) {

            }
        }));
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) finish();
        else {
            email = currentUser.getEmail();
            admin_id = currentUser.getUid();
        }
        refresh.setRefreshing(true);
        loading = true;
        show_pic.setChecked(showPic);
        read("");
    }

    public void add(View v) {
        if (loading) return;
        if (admin_id.equals("") || email.equals("") || count == -1) {
            Toast.makeText(getApplicationContext(), "Database Error", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!isConnected(this)) {
            Toast.makeText(getApplicationContext(), getResources().getString(R.string.action_no_internet_connection),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, Form.class);
        startActivity(intent);
        finish();
    }

    public void logout(View v) {
        AlertDialog.Builder ad = new AlertDialog.Builder(this);
        ad.setMessage(getResources().getString(R.string.action_sign_out_message) + "\n" + email);
        ad.setNegativeButton(getResources().getString(R.string.action_sign_out), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();
                finish();
            }
        });
        ad.show();
    }

    public void about(View v) {
        startActivity(new Intent(this, About.class));
    }

    public boolean isConnected(Context context) {
        try {
            ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            boolean b = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            return b;
        } catch (Exception e) {
            return false;
        }
    }

    private void read(final String s) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("users").child(admin_id);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot usersSnapshot) {
                try {
                    users_list.clear();
                    count = usersSnapshot.getChildrenCount();
                    for (DataSnapshot userSnapshot : usersSnapshot.getChildren()) {
                        HashMap<String, String> user = new HashMap<>();
                        String id = userSnapshot.getKey();
                        user.put("id", id);
                        //search
                        if (!search.equals("")) if (!userSnapshot.toString().contains(s)) continue;

                        for (DataSnapshot snapshot : userSnapshot.getChildren())
                            user.put(snapshot.getKey(), snapshot.getValue().toString());
                        users_list.add(user);
                    }
                    recyclerViewAdapter.notifyDataSetChanged();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                loading = false;
                refresh.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                refresh.setRefreshing(false);
            }
        });
    }

}
