package in.ureport.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import in.ureport.R;
import in.ureport.fragments.ChallengeInfoFragment;
import in.ureport.fragments.PollAllResultsFragment;
import in.ureport.fragments.PollRegionResultsFragment;
import in.ureport.fragments.PollsFragment;
import in.ureport.managers.CountryProgramManager;
import in.ureport.models.Poll;
import in.ureport.models.PollResult;
import in.ureport.models.holders.NavigationItem;
import in.ureport.views.adapters.NavigationAdapter;
import in.ureport.views.adapters.PollResultsAdapter;

/**
 * Created by johncordeiro on 18/07/15.
 */
public class PollResultsActivity extends AppCompatActivity implements PollResultsAdapter.PollResultsListener {

    public static final String EXTRA_POLL = "poll";

    private Poll poll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CountryProgramManager.setThemeIfNeeded(this);
        setContentView(R.layout.activity_poll_results);

        getObjectFromArgments(savedInstanceState);
        setupView();
    }

    private void getObjectFromArgments(Bundle savedInstanceState) {
        if(savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras != null && extras.containsKey(EXTRA_POLL)) {
                poll = extras.getParcelable(EXTRA_POLL);
            } else {
                finish();
            }
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if(getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            finish();
        }
        return true;
    }

    private void setupView() {
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(R.string.label_poll_results);

        NavigationAdapter adapter = new NavigationAdapter(getSupportFragmentManager(), getNavigationItems());

        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(adapter);
        pager.setOffscreenPageLimit(adapter.getCount());

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);
    }

    @NonNull
    private NavigationItem[] getNavigationItems() {
        NavigationItem callengeInfo = new NavigationItem(ChallengeInfoFragment.newInstance(poll),  getString(R.string.label_challenge));
        NavigationItem pollResults = new NavigationItem(PollAllResultsFragment.newInstance(poll), getString(R.string.label_challenge_results));

        return new NavigationItem[]{callengeInfo, pollResults};
    }

    @Override
    public void onViewResultByRegion(PollResult pollResult) {
        PollRegionResultsFragment pollRegionResultsFragment = PollRegionResultsFragment.newInstance(pollResult);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.content, pollRegionResultsFragment)
                .addToBackStack(null)
                .commit();
    }
}
