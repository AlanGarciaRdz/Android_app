package com.development.android.airezmg;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class DetailActivity extends AppCompatActivity {

    private static final String LOG_TAG = "DetailActivity";

    private ArrayList<String> xVals = new ArrayList<String>();
    private ArrayList<Entry> xConcentration = new ArrayList<>();
    private int selectedCont = 0;
    private ImageView background;

    private enum states{
        buena,
        mala,
        regular
    }

    LineDataSet setComp1;
    ArrayList<ILineDataSet> dataSets;
    LineData linedata;
    LineChart chart;

    ImageButton btnContPM10;
    ImageButton btnContO3;
    ImageButton btnContCO;
    ImageButton btnContNO2;
    ImageButton btnContSO2;

    private ArrayList<TreeMap> contData;

    private enum contaminants{
        PM10,
        O3,
        CO,
        NO2,
        SO2
    }
    private ArrayList<ImageButton> contButtons = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        background = (ImageView)findViewById(R.id.imageViewBackground);

        Intent intent = getIntent();
        final ArrayList<HashMap> dataExtras = (ArrayList<HashMap>)intent.getSerializableExtra("extra_data");
//        HashMap<Integer, String> dataExtra = (HashMap<Integer, String>)intent.getSerializableExtra("extra_data");

        contData = new ArrayList<>();
        for(HashMap<Integer, String> hmap: dataExtras) {
            TreeMap<Integer, String> data = new TreeMap<>();
            for (Map.Entry<Integer, String> e : hmap.entrySet()) {
                data.put(e.getKey(), e.getValue());
            }
            contData.add(data);
        }

        btnContPM10 = (ImageButton) findViewById(R.id.btn_cont_pm10);
        btnContO3   = (ImageButton) findViewById(R.id.btn_cont_o3);
        btnContCO   = (ImageButton) findViewById(R.id.btn_cont_co);
        btnContNO2  = (ImageButton) findViewById(R.id.btn_cont_no2);
        btnContSO2  = (ImageButton) findViewById(R.id.btn_cont_so2);


        contButtons.add(btnContPM10);
        contButtons.add(btnContO3);
        contButtons.add(btnContCO);
        contButtons.add(btnContNO2);
        contButtons.add(btnContSO2);
        contButtons.get(selectedCont).setSelected(true);

        View.OnClickListener buttonClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean dewit = false;
                switch(view.getId()){
                    case R.id.btn_cont_pm10:
                        if(selectedCont != contaminants.PM10.ordinal()){
                            selectedCont = contaminants.PM10.ordinal();
                            dewit = true;
                        }
                        break;
                    case R.id.btn_cont_o3:
                        if(selectedCont != contaminants.O3.ordinal()){
                            selectedCont = contaminants.O3.ordinal();
                            dewit = true;
                        }
                        break;
                    case R.id.btn_cont_co:
                        if(selectedCont != contaminants.CO.ordinal()){
                            selectedCont = contaminants.CO.ordinal();
                            dewit = true;
                        }
                        break;
                    case R.id.btn_cont_no2:
                        if(selectedCont != contaminants.NO2.ordinal()){
                            selectedCont = contaminants.NO2.ordinal();
                            dewit = true;
                        }
                        break;
                    case R.id.btn_cont_so2:
                        if(selectedCont != contaminants.SO2.ordinal()){
                            selectedCont = contaminants.SO2.ordinal();
                            dewit = true;
                        }
                        break;
                }
                if(dewit) {
                    unselectAllButtons();
                    contButtons.get(selectedCont).setSelected(true);
                    updateXValues(contData.get(selectedCont));
                }
            }
        };

        btnContPM10.setOnClickListener(buttonClickListener);
        btnContO3  .setOnClickListener(buttonClickListener);
        btnContCO  .setOnClickListener(buttonClickListener);
        btnContNO2 .setOnClickListener(buttonClickListener);
        btnContSO2 .setOnClickListener(buttonClickListener);

        chart = (LineChart) findViewById(R.id.chart);
        chart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String qlty = getStateOfValue((int)e.getY());
                if(qlty != null){
                    qlty = qlty.toLowerCase();
                    switch (qlty) {
                        case "buena":
                            background.setImageResource(R.mipmap.graphback_good);
                            break;
                        case "mala":
                            background.setImageResource(R.mipmap.graphback_bad);
                            break;
                        case "regular":
                            background.setImageResource(R.mipmap.graphback_med);
                            break;
                    }
                }
            }

            @Override
            public void onNothingSelected() {

            }
        });
//        chart.setDragEnabled(false);
//        chart.setTouchEnabled(false);

        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setDrawLabels(false);
        YAxis leftAxis = chart.getAxisLeft();

        LimitLine ll = new LimitLine(140f, "Concentración");
        ll.setLineColor(Color.RED);
        ll.setLineWidth(4f);
        ll.setTextColor(Color.BLACK);
        ll.setTextSize(12f);

        leftAxis.addLimitLine(ll);

        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(10f);
        xAxis.setTextColor(Color.BLUE);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);

        AxisValueFormatter avf = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {

                Calendar c = Calendar.getInstance();
                int hourofday = c.get(Calendar.HOUR_OF_DAY);
                int time = (int)value + hourofday;
                if(time >= 24){
                    time = time - 24;
                }


                String tag = (time<10?"0"+time:time) + ":00";
                return tag;
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        xAxis.setValueFormatter(avf);
        updateXValues(contData.get(0));
        selectedCont = 0;
    }

    private void unselectAllButtons(){
        btnContPM10.setSelected(false);
        btnContO3  .setSelected(false);
        btnContCO  .setSelected(false);
        btnContNO2 .setSelected(false);
        btnContSO2 .setSelected(false);
    }


    private String getStateOfValue(int yval){
        TreeMap<Integer, String> data = contData.get(selectedCont);
        String retval = null;
        for(Map.Entry<Integer, String> entry : data.entrySet()) {
            try {
                JSONObject obj = new JSONObject(entry.getValue());
                float y = obj.optInt("conc");
                if(y == yval) {
                    retval = obj.optString("calidad");
                    return retval;
                }
            }catch (JSONException jex){

            }
        }
        return retval;
    }

    private void updateXValues(TreeMap<Integer, String> data){
        xVals = new ArrayList<>();
        xConcentration = new ArrayList<>();

        int counter = 0;
        for(Map.Entry<Integer, String> entry : data.entrySet()){
            try {
                JSONObject obj = new JSONObject(entry.getValue());
                float conc = obj.optInt("conc");

                int hour = entry.getKey();
                if(hour >= 24){
                    hour = hour - 24;
                }

                String tag = hour + ":00";
                xVals.add(tag);
                Entry entConc = new Entry(counter,conc);
                xConcentration.add(entConc);

                counter++;
            }catch (JSONException e){

            }
        }

        setComp1 = new LineDataSet(xConcentration, "Concentración");
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);

        dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);

        linedata = new LineData();
        linedata.addDataSet(setComp1);

        chart.setData(linedata);
        chart.fitScreen();
        chart.invalidate(); // refresh
    }
}
