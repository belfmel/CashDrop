package com.example.cashdrop;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;



import java.util.ArrayList;

import static android.support.v4.app.ActivityCompat.startActivityForResult;


public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder>  {

    private static final String TAG = "RecyclerViewAdapter";

    String mUser;
    ArrayList<String> mCloseBy;
    private Context mContext;

    public RecyclerViewAdapter(Context context, String currentUser, ArrayList<String> peopleCloseBy) {
        Log.d(TAG, "Constructor of recycler view adapter");
        mContext = context;
        mUser = currentUser;
        mCloseBy = peopleCloseBy;
    }

    /**
     * Each view holder is in charge of displaying a single item with a view.
     * Complex data items may need more than one view per item, and
     * you provide access to all the views for a data item in a view holder
     */
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item
        TextView otherUser;
        RelativeLayout parentLayout;

        public MyViewHolder(View v) {
            super(v);
            otherUser = itemView.findViewById(R.id.userName);
            parentLayout = itemView.findViewById(R.id.parent_layout);
        }

    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View v = (View) LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_bubbles, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        Log.d(TAG, "onBindViewHolder: called");

        String temp = mCloseBy.get(position);
        temp = temp.replace('-', '.');
        holder.otherUser.setText(temp);

        holder.parentLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String temp = mCloseBy.get(position);
                temp = temp.replace('-', '.');
                Log.d(TAG, "onClick: clicked on: " + temp);
                Toast.makeText(mContext, temp, Toast.LENGTH_SHORT).show();

                Intent userIntent = new Intent(mContext, PayToUsers.class);
                userIntent.putExtra("Username", temp);
                mContext.startActivity(userIntent);

            }
        });
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mCloseBy.size();
    }


}
