package in.ureport.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Collections;
import java.util.Date;

import in.ureport.R;
import in.ureport.activities.ChatRoomActivity;
import in.ureport.helpers.ValueEventListenerAdapter;
import in.ureport.managers.UserManager;
import in.ureport.models.GroupChatRoom;
import in.ureport.models.Poll;
import in.ureport.models.User;
import in.ureport.network.ChatRoomServices;
import in.ureport.network.PollServices;
import in.ureport.network.UserServices;

/**
 * Created by john-mac on 5/16/16.
 */
public class ChallengeInfoFragment extends Fragment {

    private static final String TAG = "ChallengeInfoFragment";

    public static final String EXTRA_POLL = "poll";

    private Poll poll;

    private UserServices userServices;
    private ChatRoomServices chatRoomServices;
    private PollServices pollServices;

    public static ChallengeInfoFragment newInstance(Poll poll) {
        Bundle args = new Bundle();
        args.putParcelable(EXTRA_POLL, poll);

        ChallengeInfoFragment fragment = new ChallengeInfoFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_challenge_info, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        poll = getArguments().getParcelable(EXTRA_POLL);

        setupObjects();
        setupView(view);
    }

    private void setupObjects() {
        userServices = new UserServices();
        chatRoomServices = new ChatRoomServices();
        pollServices = new PollServices();
    }

    private void setupView(View view) {
        TextView title = (TextView) view.findViewById(R.id.title);
        title.setText(poll.getTitle());

        TextView category = (TextView) view.findViewById(R.id.category);
        category.setText(getString(R.string.label_challenge_area, poll.getCategory().getName()));

        View issue = view.findViewById(R.id.issue);
        setupChallengeDetailRow(issue, getString(R.string.label_issue), poll.getIssue(), true);

        View need = view.findViewById(R.id.need);
        setupChallengeDetailRow(need, getString(R.string.label_need), poll.getNeed(), false);

        View outcome = view.findViewById(R.id.outcome);
        setupChallengeDetailRow(outcome, getString(R.string.label_outcome), poll.getExpectedOutcome(), false);

        Button enterChat = (Button) view.findViewById(R.id.enterChat);
        enterChat.setOnClickListener(onEnterChatClickListener);
    }

    private void setupChallengeDetailRow(View issue, String title, String content, boolean enabled) {
        final TextView rowContent = (TextView) issue.findViewById(R.id.content);
        rowContent.setText(content);

        final TextView rowTitle = (TextView) issue.findViewById(R.id.title);
        rowTitle.setText(title);
        rowTitle.setOnClickListener(view1 -> toggleRowContent(rowContent, rowTitle));

        if(enabled) {
            rowTitle.performClick();
        }
    }

    private void toggleRowContent(TextView rowContent, TextView rowTitle) {
        if (rowContent.getVisibility() == View.GONE) {
            rowContent.setVisibility(View.VISIBLE);
            rowTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_down, 0);
        } else {
            rowContent.setVisibility(View.GONE);
            rowTitle.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_chevron_right, 0);
        }
    }

    private View.OnClickListener onEnterChatClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if (poll.getChatRoom() != null) {
                getUserAddingAsMember();
            } else {
                createGroupChatRoom();
            }
        }
    };

    private void createGroupChatRoom() {
        GroupChatRoom groupChatRoom = new GroupChatRoom();
        groupChatRoom.setCreatedDate(new Date());
        groupChatRoom.setTitle(poll.getTitle());
        groupChatRoom.setSubject(poll.getIssue());
        groupChatRoom.setPrivateAccess(false);
        groupChatRoom.setMediaAllowed(true);

        userServices.getUser(UserManager.getUserId(), new ValueEventListenerAdapter() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                super.onDataChange(dataSnapshot);
                User me = dataSnapshot.getValue(User.class);
                chatRoomServices.saveGroupChatRoom(getActivity(), null, groupChatRoom, Collections.singletonList(me)
                        , (chatRoom, chatMembers) -> pollServices.updatePollChatRoom(poll, chatRoom.getKey(), onCompletionListener));
            }
        });
    }

    private void getUserAddingAsMember() {
        userServices.getUser(UserManager.getUserId(), new ValueEventListenerAdapter() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                super.onDataChange(dataSnapshot);

                User me = dataSnapshot.getValue(User.class);
                chatRoomServices.addChatMember(getContext(), me, poll.getChatRoom(), onCompletionListener);
            }
        });
    }

    private Firebase.CompletionListener onCompletionListener = new Firebase.CompletionListener() {
        @Override
        public void onComplete(FirebaseError firebaseError, Firebase firebase) {
            Intent chatRoomIntent = new Intent(getActivity(), ChatRoomActivity.class);
            chatRoomIntent.putExtra(ChatRoomActivity.EXTRA_CHAT_ROOM_KEY, poll.getChatRoom());
            startActivity(chatRoomIntent);
        }
    };
}
