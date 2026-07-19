package com.example.smarthome.activities;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smarthome.R;
import com.example.smarthome.adapters.ChatAdapter;
import com.example.smarthome.models.ChatMessage;
import com.example.smarthome.services.SinchService;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatActivity extends AppCompatActivity implements android.content.ServiceConnection {
    private static final String TAG = "ChatActivity";

    private RecyclerView rvMessages;
    private EditText etMessage;
    private ImageButton btnSend;
    private ChatAdapter adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();
    
    private String currentUserId;
    private String otherUserId;
    private String otherUserName;
    private String houseId;
    private String chatId;
    
    private FirebaseFirestore firestore;
    private ListenerRegistration chatListener;
    private SinchService.SinchServiceInterface mSinchServiceInterface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firestore = FirebaseFirestore.getInstance();
        SharedPrefManager sharedPrefManager = SharedPrefManager.getInstance(this);
        currentUserId = sharedPrefManager.getUserId();
        
        otherUserId = getIntent().getStringExtra("other_user_id");
        otherUserName = getIntent().getStringExtra("other_user_name");
        houseId = getIntent().getStringExtra("house_id");

        if (otherUserId == null || houseId == null) {
            Toast.makeText(this, "Error: Missing chat information", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            Toast.makeText(this, "Please login to use chat", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate consistent chatId: propertyId_tenantId_ownerId
        // To keep it simple, we assume the one who starts the chat is the tenant if they are not the owner.
        // But the intent should already have these details.
        chatId = String.format("%s_%s_%s", 
            houseId, 
            sharedPrefManager.isOwner() ? otherUserId : currentUserId,
            sharedPrefManager.isOwner() ? currentUserId : otherUserId
        );

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(otherUserName != null ? otherUserName : "Chat");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        rvMessages = findViewById(R.id.rv_chat_messages);
        etMessage = findViewById(R.id.et_chat_message);
        btnSend = findViewById(R.id.btn_send_message);

        adapter = new ChatAdapter(this, messageList, currentUserId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());

        listenForMessages();
        getApplicationContext().bindService(new android.content.Intent(this, SinchService.class), this, BIND_AUTO_CREATE);
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        if (item.getItemId() == R.id.action_call) {
            startCall();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void startCall() {
        if (mSinchServiceInterface != null && mSinchServiceInterface.isStarted()) {
            com.sinch.android.rtc.calling.Call call = mSinchServiceInterface.callUser(otherUserId);
            if (call != null) {
                android.content.Intent intent = new android.content.Intent(this, CallActivity.class);
                intent.putExtra("CALL_ID", call.getCallId());
                intent.putExtra("IS_INCOMING", false);
                startActivity(intent);
            }
        } else {
            Toast.makeText(this, "Call service not ready", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onServiceConnected(android.content.ComponentName name, android.os.IBinder service) {
        mSinchServiceInterface = (SinchService.SinchServiceInterface) service;
    }

    @Override
    public void onServiceDisconnected(android.content.ComponentName name) {
        mSinchServiceInterface = null;
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        String messageId = UUID.randomUUID().toString();
        String timestamp = String.valueOf(System.currentTimeMillis());
        
        ChatMessage message = new ChatMessage(messageId, currentUserId, otherUserId, text, timestamp, houseId);
        
        etMessage.setText("");

        // Store messages in chats/{chatId}/messages/{messageId}
        firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .document(messageId)
                .set(message)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to send: " + e.getMessage());
                    Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                });
                
        // Also update latest message info in the main chat document for a chat list feature later
        firestore.collection("chats").document(chatId).set(new java.util.HashMap<String, Object>() {{
            put("lastMessage", text);
            put("lastTimestamp", timestamp);
            put("propertyId", houseId);
            put("participants", java.util.Arrays.asList(currentUserId, otherUserId));
        }}, com.google.firebase.firestore.SetOptions.merge());
    }

    private void listenForMessages() {
        if (chatListener != null) chatListener.remove();

        chatListener = firestore.collection("chats")
                .document(chatId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed", error);
                        Toast.makeText(ChatActivity.this, "Connection lost. Reopen the chat to reconnect.", Toast.LENGTH_LONG).show();
                        return;
                    }
                    
                    if (value != null) {
                        for (DocumentChange dc : value.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                ChatMessage msg = dc.getDocument().toObject(ChatMessage.class);
                                messageList.add(msg);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        if (!messageList.isEmpty()) {
                            rvMessages.smoothScrollToPosition(messageList.size() - 1);
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatListener != null) chatListener.remove();
        if (mSinchServiceInterface != null) {
            getApplicationContext().unbindService(this);
        }
    }
}
