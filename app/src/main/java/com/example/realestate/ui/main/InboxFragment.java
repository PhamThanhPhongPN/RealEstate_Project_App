package com.example.realestate.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.realestate.data.model.AuthResponse;
import com.example.realestate.data.model.Conversation;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.ChatSocketManager;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.FragmentInboxBinding;
import com.example.realestate.ui.adapters.ConversationsAdapter;
import com.example.realestate.ui.chat.ChatActivity;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InboxFragment extends Fragment {
    private FragmentInboxBinding binding;
    private ApiService apiService;
    private ChatSocketManager socketManager;
    private ConversationsAdapter adapter;
    private List<Conversation> conversationsList = new ArrayList<>();
    private int currentUserId = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentInboxBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        apiService = RetrofitClient.getApiService(requireContext());
        socketManager = new ChatSocketManager(requireContext());

        // Fetch current user id first to differentiate incoming/outgoing messages
        fetchCurrentUser();
    }

    private void fetchCurrentUser() {
        apiService.getMe().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    currentUserId = response.body().getUser().getUserId();
                    setupRecycler();
                    loadInbox();
                    setupSocket();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {}
        });
    }

    private void setupRecycler() {
        adapter = new ConversationsAdapter(conversationsList, currentUserId, (conversation, otherUserId, otherUserName, otherUserAvatar) -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra("conversation_id", conversation.getConversationId());
            intent.putExtra("receiver_id", otherUserId);
            intent.putExtra("receiver_name", otherUserName);
            intent.putExtra("receiver_avatar", otherUserAvatar);
            intent.putExtra("property_title", conversation.getPropertyTitle());
            intent.putExtra("property_id", conversation.getPropertyId());
            startActivity(intent);
        });
        binding.rvInbox.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvInbox.setAdapter(adapter);
    }

    private void loadInbox() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvEmptyState.setVisibility(View.GONE);

        apiService.getConversations().enqueue(new Callback<List<Conversation>>() {
            @Override
            public void onResponse(Call<List<Conversation>> call, Response<List<Conversation>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    conversationsList.clear();
                    conversationsList.addAll(response.body());
                    adapter.notifyDataSetChanged();

                    if (conversationsList.isEmpty()) {
                        binding.tvEmptyState.setVisibility(View.VISIBLE);
                    }
                } else {
                    binding.tvEmptyState.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<Conversation>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.tvEmptyState.setVisibility(View.VISIBLE);
            }
        });
    }

    private void setupSocket() {
        // Connect to Socket.io and trigger reload on events
        socketManager.connect(() -> {
            if (socketManager.getSocket() != null) {
                socketManager.getSocket().on("message", args -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(InboxFragment.this::loadInbox);
                    }
                });
                socketManager.getSocket().on("inbox_update", args -> {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(InboxFragment.this::loadInbox);
                    }
                });
            }
        }, null);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (currentUserId != -1) {
            loadInbox();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        socketManager.disconnect();
        binding = null;
    }
}
