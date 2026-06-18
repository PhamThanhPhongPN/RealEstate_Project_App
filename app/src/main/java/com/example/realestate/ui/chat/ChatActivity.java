package com.example.realestate.ui.chat;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.bumptech.glide.Glide;
import com.example.realestate.data.model.AuthResponse;
import com.example.realestate.data.model.Message;
import com.example.realestate.data.remote.ApiService;
import com.example.realestate.data.remote.ChatSocketManager;
import com.example.realestate.data.remote.RetrofitClient;
import com.example.realestate.databinding.ActivityChatBinding;
import com.example.realestate.ui.adapters.MessagesAdapter;
import com.google.gson.Gson;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private static final String TAG = "ChatActivity";
    private ActivityChatBinding binding;
    private ApiService apiService;
    private ChatSocketManager chatSocketManager;
    private MessagesAdapter adapter;
    private final List<Message> messagesList = new ArrayList<>();
    
    private int conversationId;
    private int receiverId;
    private String receiverName;
    private String receiverAvatar;
    private String propertyTitle;
    private int propertyId;
    private int currentUserId = -1;

    private final Emitter.Listener onNewMessage = args -> {
        if (args.length > 0 && args[0] != null) {
            String json = args[0].toString();
            Log.d(TAG, "Socket new message: " + json);
            try {
                Message msg = new Gson().fromJson(json, Message.class);
                if (msg.getConversationId() == conversationId) {
                    runOnUiThread(() -> {
                        messagesList.add(msg);
                        adapter.notifyItemInserted(messagesList.size() - 1);
                        binding.rvMessages.smoothScrollToPosition(messagesList.size() - 1);
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing socket message", e);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = RetrofitClient.getApiService(this);
        chatSocketManager = new ChatSocketManager(this);

        conversationId = getIntent().getIntExtra("conversation_id", -1);
        receiverId = getIntent().getIntExtra("receiver_id", -1);
        receiverName = getIntent().getStringExtra("receiver_name");
        receiverAvatar = getIntent().getStringExtra("receiver_avatar");
        propertyTitle = getIntent().getStringExtra("property_title");
        propertyId = getIntent().getIntExtra("property_id", -1);

        if (conversationId == -1) {
            Toast.makeText(this, "Không tìm thấy cuộc hội thoại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        setupRecyclerView();
        setupListeners();
        
        loadCurrentUserAndMessages();
    }

    private void setupToolbar() {
        binding.tvChatName.setText(receiverName != null ? receiverName : "Chủ nhà");
        if (propertyTitle != null && !propertyTitle.trim().isEmpty()) {
            binding.tvChatPropertyContext.setText("Tin: " + propertyTitle);
            binding.tvChatPropertyContext.setVisibility(View.VISIBLE);
        } else {
            binding.tvChatPropertyContext.setVisibility(View.GONE);
        }

        String avatar = receiverAvatar;
        if (avatar == null || avatar.trim().isEmpty()) {
            avatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=150&q=80";
        }
        Glide.with(this)
                .load(avatar)
                .circleCrop()
                .placeholder(android.R.drawable.sym_def_app_icon)
                .into(binding.ivChatAvatar);
    }

    private void setupRecyclerView() {
        binding.rvMessages.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupListeners() {
        binding.btnChatBack.setOnClickListener(v -> finish());
        binding.btnSendMessage.setOnClickListener(v -> sendMessage());
    }

    private void loadCurrentUserAndMessages() {
        binding.progressBar.setVisibility(View.VISIBLE);
        apiService.getMe().enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getUser() != null) {
                    currentUserId = response.body().getUser().getUserId();
                    adapter = new MessagesAdapter(messagesList, currentUserId);
                    binding.rvMessages.setAdapter(adapter);
                    loadMessageHistory();
                } else {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(ChatActivity.this, "Vui lòng đăng nhập để trò chuyện", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this, "Lỗi mạng", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void loadMessageHistory() {
        apiService.getConversationMessages(conversationId, null, 100).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                binding.progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    messagesList.clear();
                    messagesList.addAll(response.body());
                    adapter.notifyDataSetChanged();
                    if (!messagesList.isEmpty()) {
                        binding.rvMessages.scrollToPosition(messagesList.size() - 1);
                    }
                    connectSocket();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this, "Không thể tải lịch sử tin nhắn", Toast.LENGTH_SHORT).show();
                connectSocket();
            }
        });
    }

    private void connectSocket() {
        chatSocketManager.connect(() -> {
            Socket socket = chatSocketManager.getSocket();
            if (socket != null) {
                socket.emit("join_conversation", conversationId);
                socket.on("new_message", onNewMessage);
                Log.d(TAG, "Socket connected and joined conversation: " + conversationId);
            }
        }, () -> Log.d(TAG, "Socket disconnected"));
    }

    private void sendMessage() {
        String text = binding.etMessageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        Socket socket = chatSocketManager.getSocket();
        if (socket != null && socket.connected()) {
            try {
                JSONObject json = new JSONObject();
                json.put("conversation_id", conversationId);
                json.put("receiver_id", receiverId);
                json.put("body", text);
                json.put("type", "text");
                socket.emit("send_message", json);
                binding.etMessageInput.setText("");
                Log.d(TAG, "Sent message: " + text);
            } catch (Exception e) {
                Log.e(TAG, "Error forming send_message JSON", e);
            }
        } else {
            Toast.makeText(this, "Chưa kết nối socket", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatSocketManager != null) {
            Socket socket = chatSocketManager.getSocket();
            if (socket != null) {
                socket.emit("leave_conversation", conversationId);
                socket.off("new_message", onNewMessage);
            }
            chatSocketManager.disconnect();
        }
        binding = null;
    }
}
