package com.zconnect.zutto.zconnect;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class StoreRoom extends AppCompatActivity {

    public String category;
    Query queryCategory;
    private RecyclerView mProductList;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private boolean flag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_room);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Intent intent = getIntent();
        if (intent != null) {
            category = intent.getStringExtra("Category");
        }
        Toast.makeText(this, category, Toast.LENGTH_SHORT).show();
        mProductList = (RecyclerView) findViewById(R.id.productList);
        mProductList.setHasFixedSize(true);
        mProductList.setLayoutManager(new LinearLayoutManager(this));

        mAuth = FirebaseAuth.getInstance();


        mDatabase = FirebaseDatabase.getInstance().getReference().child("ZConnect/storeroom");
        queryCategory = mDatabase.orderByChild("Category").equalTo(category);
        mDatabase.keepSynced(true);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Product, ProductViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Product, ProductViewHolder>(

                Product.class,
                R.layout.products_row,
                ProductViewHolder.class,
                queryCategory
        ) {

            @Override
            protected void populateViewHolder(ProductViewHolder viewHolder, final Product model, int position) {
                viewHolder.defaultSwitch(model.getKey());
                //viewHolder.setSwitch(model.getKey());
                viewHolder.setProductName(model.getProductName());
                viewHolder.setProductDesc(model.getProductDescription());
                viewHolder.setImage(getApplicationContext(), model.getImage());
                viewHolder.setProductPrice(model.getPrice());

                viewHolder.mListener = new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                        flag = true;

                        mDatabase.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {

                                if (flag) {
                                    if (dataSnapshot.child(model.getKey()).child("UsersReserved").hasChild(mAuth.getCurrentUser().getUid())) {
                                        mDatabase.child(model.getKey()).child("UsersReserved").child(mAuth.getCurrentUser().getUid()).removeValue();
                                        flag = false;
                                    } else {

                                        mDatabase.child(model.getKey()).child("UsersReserved").child(mAuth.getCurrentUser().getUid()).setValue(mAuth.getCurrentUser().getDisplayName());
                                        flag = false;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                };
                viewHolder.mReserve.setOnCheckedChangeListener(viewHolder.mListener);

            }
        };
        mProductList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class ProductViewHolder extends RecyclerView.ViewHolder {

        public CompoundButton.OnCheckedChangeListener mListener;
        View mView;
        String[] keyList;
        String ReservedUid;
        private Switch mReserve;
        private TextView ReserveStatus;
        private DatabaseReference StoreRoom = FirebaseDatabase.getInstance().getReference().child("ZConnect/storeroom");
        private FirebaseAuth mAuth;

        public ProductViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            mReserve = (Switch) mView.findViewById(R.id.switch1);
            ReserveStatus = (TextView) mView.findViewById(R.id.switch1);
            StoreRoom.keepSynced(true);

        }

        public void defaultSwitch(final String key) {
            // Getting User ID
            mAuth = FirebaseAuth.getInstance();
            FirebaseUser user = mAuth.getCurrentUser();
            final String userId = user.getUid();

            //Getting  data from database
            StoreRoom.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    mReserve.setOnCheckedChangeListener(null);
                    if (dataSnapshot.child(key).child("UsersReserved").hasChild(userId)) {
                        mReserve.setChecked(true);
                    } else {
                        mReserve.setChecked(false);
                    }
                    mReserve.setOnCheckedChangeListener(mListener);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }

            });


        }

        public void setProductName(String productName) {

            TextView post_name = (TextView) mView.findViewById(R.id.productName);
            post_name.setText(productName);

        }

        public void setProductDesc(String productDesc) {

            TextView post_desc = (TextView) mView.findViewById(R.id.productDescription);
            post_desc.setText(productDesc);

        }

        public void setImage(Context ctx, String image) {


            ImageView post_image = (ImageView) mView.findViewById(R.id.postImg);
            Picasso.with(ctx).load(image).into(post_image);


        }

        public void setProductPrice(String productPrice) {
            TextView post_name = (TextView) mView.findViewById(R.id.price);
            post_name.setText(productPrice);
        }

    }
}