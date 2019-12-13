package com.example.rewards;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class RewardAdapter extends RecyclerView.Adapter<RewardAdapter.MyViewHolder> {
    private static final String TAG = "RewardAdapter";
    List<Rewards> rewardsList;

    public RewardAdapter(List<Rewards> rewardsList) {
        this.rewardsList = rewardsList;
    }

    @NonNull
    @Override
    public RewardAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.reward_recycler, viewGroup, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull RewardAdapter.MyViewHolder myViewHolder, int i) {
        Rewards rewards = rewardsList.get(i);
        setRewardContent(myViewHolder, rewards);
    }

    private void setRewardContent(MyViewHolder holder, Rewards rewards) {
        holder.first_last_names.setText(rewards.getName());
        holder.date.setText(rewards.getDate());
        holder.points.setText(Integer.toString(rewards.getValue()));
        holder.comments.setText(rewards.getNotes());
    }

    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount: " + rewardsList.size());
        return rewardsList.size();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView date;
        public TextView first_last_names;
        public TextView points;
        public TextView comments;
        public TextView rewardsNumber;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.reward_date);
            first_last_names = itemView.findViewById(R.id.reward_name);
            points = itemView.findViewById(R.id.reward_points);
            comments = itemView.findViewById(R.id.reward_comment);
            rewardsNumber = itemView.findViewById(R.id.rewardHistory);
        }
    }
}