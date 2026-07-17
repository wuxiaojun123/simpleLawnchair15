package com.simplepdf.pdfeditor.DateTimePIckerDialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import androidx.appcompat.app.AppCompatDialogFragment;

import com.simplepdf.pdfeditor.CallbackListener.DatePickerDialogListener;

import java.util.Calendar;


public class DateTimePickerDialogFragment extends AppCompatDialogFragment implements DatePickerDialog.OnDateSetListener {
    public static DatePickerDialogListener listener;

    public static DateTimePickerDialogFragment newInstance(DatePickerDialogListener datePickerDialogListener) {
        listener = datePickerDialogListener;
        return new DateTimePickerDialogFragment();
    }

    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
    }

    @Override 
    public Dialog onCreateDialog(Bundle bundle) {
        Calendar calendar = Calendar.getInstance();
        return new DatePickerDialog(getActivity(), this, calendar.get(1), calendar.get(2), calendar.get(5));
    }

    @Override 
    public void onDateSet(DatePicker datePicker, int i, int i2, int i3) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(i, i2, i3);
        listener.onDatePicked(calendar);
    }
}
