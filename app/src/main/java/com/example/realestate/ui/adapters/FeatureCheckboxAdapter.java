package com.example.realestate.ui.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.realestate.data.model.Feature;
import com.example.realestate.databinding.ItemFeatureCheckboxBinding;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureCheckboxAdapter extends RecyclerView.Adapter<FeatureCheckboxAdapter.ViewHolder> {
    private final List<Feature> features;
    private final Set<Integer> selectedFeatureIds = new HashSet<>();

    public FeatureCheckboxAdapter(List<Feature> features) {
        this.features = features;
    }

    public void setSelectedFeatureIds(List<Integer> ids) {
        this.selectedFeatureIds.clear();
        if (ids != null) {
            this.selectedFeatureIds.addAll(ids);
        }
        notifyDataSetChanged();
    }

    public Set<Integer> getSelectedFeatureIds() {
        return selectedFeatureIds;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeatureCheckboxBinding binding = ItemFeatureCheckboxBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Feature feature = features.get(position);
        holder.binding.checkboxFeature.setText(feature.getName());
        
        // Disable listener temporarily to prevent infinite loop on change
        holder.binding.checkboxFeature.setOnCheckedChangeListener(null);
        holder.binding.checkboxFeature.setChecked(selectedFeatureIds.contains(feature.getFeatureId()));

        holder.binding.checkboxFeature.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedFeatureIds.add(feature.getFeatureId());
            } else {
                selectedFeatureIds.remove(feature.getFeatureId());
            }
        });
    }

    @Override
    public int getItemCount() {
        return features.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final ItemFeatureCheckboxBinding binding;

        public ViewHolder(ItemFeatureCheckboxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
