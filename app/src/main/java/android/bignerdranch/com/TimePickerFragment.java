package android.bignerdranch.com;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

public class TimePickerFragment extends DialogFragment {

    private static final String TAG = "cc";
    private static final  String ARG_TIME = "time";
    public static final String EXTRA_TIME = "com.bignerdranch.android.criminalintent.time";
    private TimePicker mTimePicker;

    public static TimePickerFragment newInstance(Date date) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_TIME, date);
        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
//        Date date = (Date) getArguments().getSerializable(ARG_TIME);
//        Calendar calendar = Calendar.getInstance();
//        calendar.setTime(date);
//        int hour = calendar.get(calendar.HOUR);
//        int min = calendar.get(calendar.MINUTE);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_time,null);

        mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_picker);
//        mTimePicker.setIs24HourView(true);
//        mTimePicker.setHour(hour);
//        mTimePicker.setMinute(min);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Time of crime: ")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(calendar.YEAR);
                        int month = calendar.get(calendar.MONTH);
                        int day = calendar.get(calendar.DAY_OF_MONTH);
                        int hour = mTimePicker.getHour();
                        int min = mTimePicker.getMinute();
                        Date date = new GregorianCalendar(year, month, day, hour, min).getTime();
                        sendResult(Activity.RESULT_OK, date);
                    }
                })
                .create();
    }

    private void sendResult(int resultCode, Date date) {
        if (getTargetFragment() == null) {
            return;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_TIME, date);

        getTargetFragment().onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
