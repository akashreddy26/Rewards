package com.example.rewards;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class LAdapter extends RecyclerView.Adapter<LAdapter.MyViewHolder> {
    LeaderBoardActivity leaderBoardActivity;
    List<UserProfiles> profData;
    String user;

    private static final String TAG = "LAdapter";

    public LAdapter(LeaderBoardActivity leaderBoardActivity, List<UserProfiles> profilesContent, String thisUser) {
        this.leaderBoardActivity = leaderBoardActivity;
        this.profData = profilesContent;
        this.user = thisUser;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public LAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.leaderboard_recycler, viewGroup, false);
        v.setOnClickListener(leaderBoardActivity);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull LAdapter.MyViewHolder holder, int i) {
        //TODO get lboard data, put data
        UserProfiles data = profData.get(i);
        setData(holder, data, user);
    }

    private void setData(MyViewHolder holder, UserProfiles data, String thisUser) {
        if (data.getUsername().equals(thisUser)) {
            holder.name.setTextColor(Color.rgb(0,133,119));
            holder.rewardPts.setTextColor(Color.rgb(0,133,119));
            holder.position.setTextColor(Color.rgb(0,133,119));
        }
        String s = data.getLastName() + ", " + data.getFirstName();
        holder.name.setText(s);
        holder.position.setText(data.getPosition());
        holder.profImg.setImageBitmap(getImgBit(data.getImageByteEncoded()));
        holder.rewardPts.setText(Integer.toString(getPts(data.rewards)));
    }

    private int getPts(List<Rewards> rewards) {
        int sum = 0;
        for (Rewards content : rewards) {
            sum += content.getValue();
        }
        return sum;
    }

    private Bitmap getImgBit(String imageByteEncoded) {
        byte[] decodedString = Base64.decode(imageByteEncoded, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + profData.size());
        return profData.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView position;
        public TextView name;
        public TextView rewardPts;
        public ImageView profImg;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            position = itemView.findViewById(R.id.leaderboard_position);
            name = itemView.findViewById(R.id.leaderboard_names);
            rewardPts = itemView.findViewById(R.id.leaderboard_rewards_point);
            profImg = itemView.findViewById(R.id.leaderboard_profile_image);


        }
    }
}