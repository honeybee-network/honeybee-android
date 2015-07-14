package in.ureport.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import br.com.ilhasoft.support.tool.EditTextValidator;
import br.com.ilhasoft.support.widget.DatePickerFragment;
import in.ureport.R;
import in.ureport.loader.CountryListLoader;
import in.ureport.managers.ToolbarDesigner;
import in.ureport.models.User;
import in.ureport.models.holders.UserGender;
import in.ureport.models.holders.UserLocale;
import in.ureport.tasks.SaveUserTask;

/**
 * Created by johncordeiro on 7/9/15.
 */
public class SignUpFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<UserLocale>>, DatePickerDialog.OnDateSetListener {

    private static final String TAG = "SignUpFragment";

    private static final String EXTRA_USER = "user";

    private static final int FIELDS_MINIMUM_SIZE = 5;

    public static SignUpFragment newInstance(User user) {
        SignUpFragment signUpFragment = new SignUpFragment();

        Bundle args = new Bundle();
        args.putParcelable(EXTRA_USER, user);

        signUpFragment.setArguments(args);
        return signUpFragment;
    }

    private EditText username;
    private EditText email;
    private EditText password;
    private EditText birthday;
    private Spinner country;
    private Spinner gender;

    private EditTextValidator validator;
    private DateFormat birthdayFormatter;
    private User user;
    private User.Type userType = User.Type.Ureport;

    private LoginFragment.LoginListener loginListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        validator = new EditTextValidator();
        birthdayFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
        getUserFromArguments();

        return inflater.inflate(R.layout.fragment_sign_up, container, false);
    }

    private void getUserFromArguments() {
        Bundle args = getArguments();
        if(args != null && args.containsKey(EXTRA_USER)) {
            user = args.getParcelable(EXTRA_USER);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupView(view);
        setupUserIfExists();
    }

    private void setupUserIfExists() {
        if(user != null) {
            userType = user.getType();

            setEditTextValue(username, user.getUsername());
            setEditTextValue(email, user.getEmail());
            setEditTextDate(birthday, user.getBirthday());
            setUserGenderValue();
        }
    }

    private void setUserGenderValue() {
        UserGender userGender = new UserGender(getActivity(), user.getGender());
        ArrayAdapter<UserGender> adapter = ((ArrayAdapter<UserGender>) gender.getAdapter());
        int position = adapter.getPosition(userGender);

        gender.setSelection(position);
    }

    private void setEditTextValue(EditText editText, String value) {
        if(value != null && value.length() > 0) {
            editText.setText(value);
        }
    }

    private void setEditTextDate(EditText editText, Date date) {
        if(date != null) {
            editText.setText(birthdayFormatter.format(date));
        }
    }

    private void setupView(View view) {
        username = (EditText) view.findViewById(R.id.username);
        email = (EditText) view.findViewById(R.id.email);
        password = (EditText) view.findViewById(R.id.password);

        birthday = (EditText) view.findViewById(R.id.birthday);
        birthday.setOnClickListener(onBirthdayClickListener);

        gender = (Spinner) view.findViewById(R.id.gender);
        loadUserGenders();

        country = (Spinner) view.findViewById(R.id.country);
        loadCountryList();

        Button confirm = (Button) view.findViewById(R.id.confirm);
        confirm.setOnClickListener(onConfirmClickListener);

        Toolbar toolbar = (Toolbar)view.findViewById(R.id.toolbar);

        ToolbarDesigner toolbarDesigner = new ToolbarDesigner();
        toolbarDesigner.setupFragmentDefaultToolbar(toolbar, this);
    }

    private void loadUserGenders() {
        List<UserGender> userGenders = getUserGenders();

        ArrayAdapter<UserGender> genderAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, userGenders);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        gender.setAdapter(genderAdapter);
    }

    @NonNull
    private List<UserGender> getUserGenders() {
        List<UserGender> userGenders = new ArrayList<>();
        User.Gender[] genders = User.Gender.values();
        for (User.Gender gender : genders) {
            UserGender userGender = new UserGender(getActivity(), gender);
            userGenders.add(userGender);
        }
        return userGenders;
    }

    private void loadCountryList() {
        getLoaderManager().initLoader(0, null, this).forceLoad();
    }

    private void selectCurrentUserLocale(List<UserLocale> userLocales) {
        Locale locale = Locale.getDefault();
        UserLocale userLocale = new UserLocale(locale);
        int userLocalePosition = userLocales.indexOf(userLocale);

        if(userLocalePosition >= 0) {
            country.setSelection(userLocalePosition);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean isFieldsValid() {
        String messageNameValidation = getString(R.string.error_minimum_size
                , FIELDS_MINIMUM_SIZE);

        boolean validTextFields = validator.validateSizeMulti(FIELDS_MINIMUM_SIZE
                , messageNameValidation, username, password);
        boolean validEmail = validator.validateEmail(email, getString(R.string.error_valid_email));
        boolean validBirthday = validator.validateEmpty(birthday, getString(R.string.error_required_field));

        return validTextFields && validEmail && validBirthday &&
                isSpinnerValid(country) && isSpinnerValid(gender);
    }

    private boolean isSpinnerValid(Spinner spinner) {
        return spinner.getSelectedItemPosition() != Spinner.INVALID_POSITION;
    }

    public void setLoginListener(LoginFragment.LoginListener loginListener) {
        this.loginListener = loginListener;
    }

    @NonNull
    private User createUser() {
        User user = new User();
        user.setType(userType);
        user.setUsername(username.getText().toString());
        user.setEmail(email.getText().toString());
        user.setPassword(password.getText().toString());
        user.setBirthday(getBirthdayDate());

        UserLocale userLocale = (UserLocale) country.getAdapter().getItem(country.getSelectedItemPosition());
        String displayCountry = userLocale.getLocale().getDisplayCountry();
        user.setCountry(displayCountry);

        UserGender userGender = (UserGender)gender.getAdapter().getItem(gender.getSelectedItemPosition());
        user.setGender(userGender.getGender());
        return user;
    }

    @Nullable
    private Date getBirthdayDate() {
        String birthdayAsText = birthday.getText().toString();
        Date birthdayAsDate = null;
        try {
            birthdayAsDate = birthdayFormatter.parse(birthdayAsText);
        } catch (ParseException exception) {
            Log.e(TAG, "getBirthdayDate ", exception);
        }
        return birthdayAsDate;
    }

    @Override
    public Loader<List<UserLocale>> onCreateLoader(int id, Bundle args) {
        return new CountryListLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<UserLocale>> loader, List<UserLocale> data) {
        updateUserLocale(data);
    }

    private void updateUserLocale(List<UserLocale> data) {
        ArrayAdapter<UserLocale> localeAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, data);
        localeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        country.setAdapter(localeAdapter);
        selectCurrentUserLocale(data);
    }

    @Override
    public void onLoaderReset(Loader<List<UserLocale>> loader) {}

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);

        birthday.setText(birthdayFormatter.format(calendar.getTime()));
    }

    private View.OnClickListener onBirthdayClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            DatePickerFragment datePickerFragment = new DatePickerFragment();
            datePickerFragment.setOnDateSetListener(SignUpFragment.this);
            datePickerFragment.show(getFragmentManager(), "datePickerFragment");
        }
    };

    private View.OnClickListener onConfirmClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            if(isFieldsValid()) {
                User user = createUser();

                SaveUserTask saveUserTask = new SaveUserTask(getActivity());
                saveUserTask.execute(user);

                if(loginListener != null) {
                    loginListener.userReady(user);
                }
            }
        }
    };
}