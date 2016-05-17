package in.ureport.views.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import in.ureport.R;
import in.ureport.models.Poll;

/**
 * Created by johncordeiro on 7/16/15.
 */
public class PollAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_CURRENT_POLL = 0;
    private static final int TYPE_PAST_POLL = 1;

    private List<Poll> polls;

    private PollParticipationListener pollParticipationListener;

    private boolean currentPollEnabled = false;

    public PollAdapter(List<Poll> polls) {
        this.polls = polls;
        setHasStableIds(true);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if(viewType == TYPE_CURRENT_POLL) {
            return new CurrentPollViewHolder(inflater.inflate(R.layout.item_current_poll, parent, false));
        } else {
            return new PastPollViewHolder(inflater.inflate(R.layout.item_past_poll, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        switch(getItemViewType(position)) {
            case TYPE_PAST_POLL:
                ((PastPollViewHolder)holder).bindView(polls.get(getPollPosition(position)));
        }
    }

    @Override
    public long getItemId(int position) {
        if(getItemViewType(position) == TYPE_CURRENT_POLL) {
            return R.layout.item_current_poll;
        }
        return polls.get(getPollPosition(position)).hashCode();
    }

    private int getPollPosition(int position) {
        return isCurrentPollEnabled() ? position-1 : position;
    }

    private boolean isCurrentPollEnabled() {
        return currentPollEnabled;
    }

    public void setCurrentPollEnabled(boolean currentPollEnabled) {
        this.currentPollEnabled = currentPollEnabled;
        notifyDataSetChanged();
    }

    public void removePoll(Poll poll) {
        this.polls.remove(poll);
    }

    @Override
    public int getItemCount() {
        return isCurrentPollEnabled() ? polls.size() + 1 : polls.size();
    }

    @Override
    public int getItemViewType(int position) {
        if(isCurrentPollEnabled() && position == 0) return TYPE_CURRENT_POLL;
        return TYPE_PAST_POLL;
    }

    private class PastPollViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;

        public PastPollViewHolder(View itemView) {
            super(itemView);

            title = (TextView) itemView.findViewById(R.id.description);

            Button results = (Button) itemView.findViewById(R.id.results);
            results.setOnClickListener(onSeeResultsClickListener);
        }

        private void bindView(Poll poll) {
            title.setText(poll.getTitle());
        }

        private View.OnClickListener onSeeResultsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (pollParticipationListener != null) {
                    Poll poll = polls.get(isCurrentPollEnabled() ? getLayoutPosition() - 1 : getLayoutPosition());
                    pollParticipationListener.onSeeResults(poll);
                }
            }
        };
    }

    private class CurrentPollViewHolder extends RecyclerView.ViewHolder {
        public CurrentPollViewHolder(View itemView) {
            super(itemView);
        }
    }

    public void setPollParticipationListener(PollParticipationListener pollParticipationListener) {
        this.pollParticipationListener = pollParticipationListener;
    }

    public interface PollParticipationListener {
        void onSeeResults(Poll poll);
    }
}
