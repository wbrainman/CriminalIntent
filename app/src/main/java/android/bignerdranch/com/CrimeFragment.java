package android.bignerdranch.com;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.DatabaseErrorHandler;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import static android.app.Activity.RESULT_OK;

public class CrimeFragment extends Fragment {
    private static final String TAG = "cc";
    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";
    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    List<String> permissionList = new ArrayList<>();
    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private Button mDialButton;

    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        Log.d(TAG, "newInstance: id = " + crimeId);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID)getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        Log.d(TAG, "onCreate: CrimeFragment id = " + crimeId);
        if (mCrime == null) {
            Log.d(TAG, "onCreate: CrimeFragment null, " + crimeId);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        Log.d(TAG, "onCreateView: CrimeFragment");

        mTitleField = (EditText)v.findViewById(R.id.crime_title);
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: ");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
                Log.d(TAG, "onTextChanged: "+ s);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: ");
            }
        });

        mDateButton = (Button)v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = (Button)v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSoloved(isChecked);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.send_crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));

                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.choose_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getPermission()) {
                    startActivityForResult(pickContact, REQUEST_CONTACT);
                }
                else {
                    requestPermission();
                }
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null)
            mSuspectButton.setEnabled(false);

        mDialButton = (Button) v.findViewById(R.id.dial);
        if (mCrime.getSuspect() == null) {
            mDialButton.setEnabled(false);
            mDialButton.setText(R.string.crime_dial);
        }
        else {
            mDialButton.setText(getString(R.string.crime_call_text, mCrime.getSuspect()));
        }
        mDialButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: before send dial");
                if (mCrime.getSuspectNumber() != null) {
                    Log.d(TAG, "onClick: send dial");
                    Intent intent = new Intent(Intent.ACTION_DIAL,
                            Uri.parse("tel:" + mCrime.getSuspectNumber()));
                    startActivity(intent);
                }
            }
        });

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        }

        if (requestCode == REQUEST_TIME) {
            Log.d(TAG, "onActivityResult: time");
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateTime();
        }

        if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            Log.d(TAG, "onActivityResult: " + contactUri);
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.LOOKUP_KEY,
                    ContactsContract.Contacts.DISPLAY_NAME,
            };
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null,null,null);

            String suspectId;
            try {
                if (c.getCount() == 0) {
                    return;
                }
                c.moveToFirst();
                String suspect = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                Log.d(TAG, "onActivityResult suspect :" + suspect);
                suspectId = c.getString(c.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                Log.d(TAG, "onActivityResult suspectId :" + suspectId);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
                mDialButton.setEnabled(true);
                mDialButton.setText(getString(R.string.crime_call_text, mCrime.getSuspect()));

            }finally {
                c.close();
            }

            Log.d(TAG, "onActivityResult begin to query number");
            contactUri  = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
            queryFields = new String[] {ContactsContract.CommonDataKinds.Phone.NUMBER};
            Log.d(TAG, "onActivityResult begin to query number queryFields = " + queryFields);

            c = getActivity().getContentResolver().query(
                    contactUri,
                    queryFields,
                   ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?",
                    new String[] {suspectId},
                   null);

            Log.d(TAG, "onActivityResult after query");
            try {
                if (c.getCount() == 0) {
                    Log.d(TAG, "onActivityResult cursor is null");
                    return;
                }
                c.moveToFirst();
                Log.d(TAG, "onActivityResult begin get number");
                String number = c.getString(c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                Log.d(TAG, "onActivityResult number :" + number);
                mCrime.setSuspectNumber(number);
            }catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                c.close();
            }

        }
    }

    private void updateTime() {
        DateFormat df = new SimpleDateFormat("hh:mm");
        String str = df.format(mCrime.getDate());
        mTimeButton.setText(str);
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSoloved()) {
            solvedString = getString(R.string.crime_report_solved); }
        else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = android.text.format.DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        }
        else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);

        return  report;
    }

    boolean getPermission() {
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_CONTACTS);
        }
        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CALL_PHONE);
        }

        if (!permissionList.isEmpty()) {
            return false;
        }
        return true;
    }

    void requestPermission() {
        String[] permissions = permissionList.toArray(new String[permissionList.size()]);
        ActivityCompat.requestPermissions(getActivity(), permissions, 1);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(getActivity().getApplicationContext(), "You must get permission", Toast.LENGTH_SHORT).show();
                        }

                        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
                        startActivityForResult(pickContact, REQUEST_CONTACT);
                    }
                }
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "unknow errors", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
