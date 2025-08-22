package com.s23010344.parkzone;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FavoriteParksAdapter extends RecyclerView.Adapter<FavoriteParksAdapter.ViewHolder> {

    private List<Park> favoriteParks;
    private Context context;
    private OnFavoriteRemoveListener onFavoriteRemoveListener;

    public interface OnFavoriteRemoveListener {
        void onRemoveFavorite(String parkId, int position);
    }

    public FavoriteParksAdapter(Context context, List<Park> favoriteParks) {
        this.context = context;
        this.favoriteParks = favoriteParks;
    }

    public void setOnFavoriteRemoveListener(OnFavoriteRemoveListener listener) {
        this.onFavoriteRemoveListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_favorite_park, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Park park = favoriteParks.get(position);

        holder.tvParkName.setText(park.name != null ? park.name : "Unnamed Park");
        holder.tvParkLocation.setText("Lat: " + park.latitude + ", Lng: " + park.longitude);
        holder.tvParkType.setText(park.paid ? "Paid Parking" : "Free Parking");

        // Set click listener for the entire card to open park details
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ParkDetailActivity.class);
            intent.putExtra("park_id", park.id);
            context.startActivity(intent);
        });

        // Set click listener for remove favorite button
        holder.btnRemoveFavorite.setOnClickListener(v -> {
            if (onFavoriteRemoveListener != null) {
                onFavoriteRemoveListener.onRemoveFavorite(park.id, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteParks.size();
    }

    public void removePark(int position) {
        favoriteParks.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, favoriteParks.size());
    }

    public void updateParks(List<Park> newFavoriteParks) {
        this.favoriteParks = newFavoriteParks;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvParkName, tvParkLocation, tvParkType;
        ImageView imgPark, btnRemoveFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvParkName = itemView.findViewById(R.id.tvParkName);
            tvParkLocation = itemView.findViewById(R.id.tvParkLocation);
            tvParkType = itemView.findViewById(R.id.tvParkType);
            imgPark = itemView.findViewById(R.id.imgPark);
            btnRemoveFavorite = itemView.findViewById(R.id.btnRemoveFavorite);
        }
    }
}
