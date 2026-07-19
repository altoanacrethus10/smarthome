package com.example.smarthome.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.smarthome.R;
import com.example.smarthome.activities.ChatActivity;
import com.example.smarthome.adapters.ConversationAdapter;
import com.example.smarthome.models.Conversation;
import com.example.smarthome.models.House;
import com.example.smarthome.models.User;
import com.example.smarthome.repository.HouseRepository;
import com.example.smarthome.repository.UserRepository;
import com.example.smarthome.utils.SharedPrefManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment {

    private RecyclerView rvConversations;
    private View emptyState;
    private SwipeRefreshLayout swipeRefresh;
    private ConversationAdapter adapter;
    private final List<Conversation> conversations = new ArrayList<>();

    private final Map<String, User> userCache = new HashMap<>();
    private final Map<String, House> houseCache = new HashMap<>();

    private FirebaseFirestore firestore;
    private ListenerRegistration chatsListener;
    private SharedPrefManager sharedPrefManager;
    private UserRepository userRepository;
    private HouseRepository houseRepository;
    private String currentUserId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                              @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_messages, container, false);

        firestore = FirebaseFirestore.getInstance();
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        userRepository = UserRepository.getInstance(requireContext());
        houseRepository = HouseRepository.getInstance(requireContext());
        currentUserId = sharedPrefManager.getUserId();

        rvConversations = view.findViewById(R.id.rv_conversations);
        emptyState = view.findViewById(R.id.empty_state);
        swipeRefresh = view.findViewById(R.id.swipe_refresh);
        swipeRefresh.setColorSchemeResources(R.color.primary);
        swipeRefresh.setOnRefreshListener(this::listenForConversations);

        adapter = new ConversationAdapter(requireContext(), conversations, this::openChat);
        rvConversations.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvConversations.setAdapter(adapter);

        listenForConversations();

        return view;
    }

    private void openChat(Conversation conversation) {
        if (conversation.getOtherUserId() == null || conversation.getPropertyId() == null) return;
        Intent intent = new Intent(getActivity(), ChatActivity.class);
        intent.putExtra("other_user_id", conversation.getOtherUserId());
        intent.putExtra("other_user_name", conversation.getOtherUserName());
        intent.putExtra("house_id", conversation.getPropertyId());
        startActivity(intent);
    }

    private void listenForConversations() {
        if (currentUserId == null || currentUserId.trim().isEmpty()) {
            updateEmptyState();
            if (swipeRefresh != null) swipeRefresh.setRefreshing(false);
            return;
        }

        if (chatsListener != null) chatsListener.remove();

        chatsListener = firestore.collection("chats")
                .whereArrayContains("participants", currentUserId)
                .addSnapshotListener((snapshot, error) -> {
                    if (!isAdded()) return;
                    if (swipeRefresh != null) swipeRefresh.setRefreshing(false);

                    if (error != null) {
                        Toast.makeText(getContext(), "Failed to load messages", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snapshot == null) return;

                    List<Conversation> result = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Conversation conversation = doc.toObject(Conversation.class);
                        conversation.setChatId(doc.getId());

                        String otherUserId = null;
                        if (conversation.getParticipants() != null) {
                            for (String participantId : conversation.getParticipants()) {
                                if (participantId != null && !participantId.equals(currentUserId)) {
                                    otherUserId = participantId;
                                    break;
                                }
                            }
                        }
                        conversation.setOtherUserId(otherUserId);
                        result.add(conversation);
                    }

                    result.sort((a, b) -> {
                        long ta = safeParseLong(a.getLastTimestamp());
                        long tb = safeParseLong(b.getLastTimestamp());
                        return Long.compare(tb, ta);
                    });

                    conversations.clear();
                    conversations.addAll(result);
                    adapter.notifyDataSetChanged();
                    updateEmptyState();
                    resolveConversationDetails();
                });
    }

    private void resolveConversationDetails() {
        for (Conversation conversation : new ArrayList<>(conversations)) {
            String otherUserId = conversation.getOtherUserId();
            if (otherUserId == null) continue;

            User cachedUser = userCache.get(otherUserId);
            if (cachedUser != null) {
                applyUser(conversation, cachedUser);
            } else {
                userRepository.getUser(otherUserId, new UserRepository.RepositoryCallback<User>() {
                    @Override
                    public void onSuccess(User user) {
                        if (user == null || !isAdded()) return;
                        userCache.put(otherUserId, user);
                        applyUser(conversation, user);
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String error) { }
                });
            }

            String propertyId = conversation.getPropertyId();
            if (propertyId == null) continue;
            House cachedHouse = houseCache.get(propertyId);
            if (cachedHouse != null) {
                conversation.setPropertyTitle(cachedHouse.getTitle());
            } else {
                houseRepository.getHouse(propertyId, new HouseRepository.RepositoryCallback<House>() {
                    @Override
                    public void onSuccess(House house) {
                        if (house == null || !isAdded()) return;
                        houseCache.put(propertyId, house);
                        conversation.setPropertyTitle(house.getTitle());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(String error) { }
                });
            }
        }
    }

    private void applyUser(Conversation conversation, User user) {
        conversation.setOtherUserName(user.getName());
        conversation.setOtherAvatarUrl(user.getAvatarUrl());
    }

    private long safeParseLong(String value) {
        if (value == null) return 0L;
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    private void updateEmptyState() {
        if (emptyState == null || rvConversations == null) return;
        boolean empty = conversations.isEmpty();
        emptyState.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvConversations.setVisibility(empty ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (chatsListener != null) {
            chatsListener.remove();
            chatsListener = null;
        }
    }
}
