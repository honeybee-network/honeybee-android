package in.ureport.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import in.ureport.R;
import in.ureport.models.Poll;

/**
 * Created by john-mac on 5/16/16.
 */
public class ChallengeInfoFragment extends Fragment {

    public static final String EXTRA_POLL = "poll";

    private Poll poll;

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

        setupView(view);
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
}
