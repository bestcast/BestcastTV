package com.bestcasttv.bestcasttvapp.app_utils;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AppUtils {

    Context mContext;

    public AppUtils(Context context) {
        this.mContext = context;
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public String isNetworkSpeedAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //should check null because in airplane mode it will be null
        NetworkCapabilities nc = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
        }
        int downSpeed = nc.getLinkDownstreamBandwidthKbps()/1000;
        int upSpeed = nc.getLinkUpstreamBandwidthKbps()/1000;

        boolean isSpeedReady;

        if (downSpeed>20){
            isSpeedReady = true;
        }else {
            isSpeedReady = false;
        }

//        Log.d("Internet_speed","Up Speed: "+downSpeed+" Mbps \nDown Speed: "+upSpeed+" Mbps");

//        return isSpeedReady;
        return "Up Speed: "+downSpeed+" Mbps \nDown Speed: "+upSpeed+" Mbps";
    }



    public void infoAlert(String info) {
        AlertDialog alertDialog = new AlertDialog.Builder(mContext).create();
        alertDialog.setMessage(info);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ok",
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        alertDialog.show();
    }

    public String getCurrentDateAndTimeFileFormat() throws ParseException {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd_MM_yyyy_HHmmss");
        String strcurrentdate = df.format(calendar.getTime());

        return strcurrentdate;
    }

    public void datepeakdialog(final TextView dateTextView) {

        final Calendar calendar = Calendar.getInstance();

        final DatePickerDialog.OnDateSetListener dialogtodate = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String myFormat = "dd-MM-yyyy";
                SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
                dateTextView.setText(sdf.format(calendar.getTime()));
            }
        };

        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(mContext, dialogtodate, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
    }

    public Bitmap StringToBitMap(String encodedString) {
        Log.d("Encoded String : ", encodedString);
        Bitmap bitmap = null;
        try {
            byte[] encodeByte = Base64.decode(encodedString, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(encodeByte, 0, encodeByte.length);
            return bitmap;
        } catch (Exception e) {
            e.printStackTrace();
            e.getMessage();
            return null;
        }
    }

    public void showToast(String info) {
        Toast toast = Toast.makeText(mContext, info, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

    public String getCurrentDate() throws ParseException {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        String strcurrentdate = df.format(calendar.getTime());

        return strcurrentdate;
    }

    public String getCurrentDateOnly() throws ParseException {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        String strcurrentdate = df.format(calendar.getTime());

        return strcurrentdate;
    }


    public String getBeforeDaysOfDateOnly() throws ParseException {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MM-yyyy");
        calendar.setTime(calendar.getTime());
        calendar.add(Calendar.DATE, -60);
        String strcurrentdate = df.format(calendar.getTime());
        return strcurrentdate;
    }

    public String getCurrentTimeOnly() throws ParseException {

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("HH:mm:ss");
        String strcurrentdate = df.format(calendar.getTime());

        return strcurrentdate;
    }

    public String convertDBDateTODisplayDate(String strdate) throws ParseException {

        String outputDateStr = "";
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
//        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date cdate = inputFormat.parse(strdate);

        outputDateStr = outputFormat.format(cdate);

        return outputDateStr;
    }

    public String convertDisplayDateToDBDate(String strdate) throws ParseException {

        String outputDateStr = "";
        DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
//        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");

        Date cdate = inputFormat.parse(strdate);

        outputDateStr = outputFormat.format(cdate);

        return outputDateStr;
    }

    public String convertDBDate(String strdate) throws ParseException {

        String outputDateStr = "";
        DateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        DateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy");

        Date cdate = inputFormat.parse(strdate);

        outputDateStr = outputFormat.format(cdate);

        return outputDateStr;
    }

    public String getDateTimeToYear(String strdate) throws ParseException {

        String outputDateStr = "";
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        DateFormat outputFormat = new SimpleDateFormat("yyyy");

        Date cdate = inputFormat.parse(strdate);

        outputDateStr = outputFormat.format(cdate);

        return outputDateStr;
    }

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 0;
        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.AT_MOST);
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    public static void setListViewDynamicHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            // pre-condition
            return;
        }

        int totalHeight = 25;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            // listItem.measure(0, 0);
            listItem.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight
                + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }
}
