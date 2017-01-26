package com.nishay.workoutplanner;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.nishay.sql.SQL;

import java.util.ArrayList;
import java.util.Random;

public class GraphFragment extends Fragment {

    private ArrayList<Pair<String, String>> list;
    private int index;
    private SQL sql;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
                R.layout.fragment_graph, container, false);

        GraphView graph = (GraphView) rootView.findViewById(R.id.graph_view);

        sql = new SQL(getActivity());
        sql.open();

        Bundle bundle = getArguments();
        index = bundle.getInt("index");
        list = (ArrayList<Pair<String,String>>) bundle.getSerializable("list");

        Random r  = new Random();

        if(index == 0) {
            //show all series
            //now we have a full list of exercises and corresponding sets
            for(Pair p : list) {
                ArrayList<DataPoint> data = sql.getWeightDates((String)p.first, (String)p.second);
                DataPoint[] dataArray = new DataPoint[data.size()];
                dataArray = data.toArray(dataArray);
                LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataArray);
                graph.addSeries(series);

                //generate random color
                int red = r.nextInt(256);
                int green = r.nextInt(256);
                int blue = r.nextInt(256);
                series.setColor(Color.argb(255, red, green, blue));

                series.setTitle((String)p.second);
                series.setDrawDataPoints(true);
            }


            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graph.getLegendRenderer().setBackgroundColor(0); //transparent
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
            graph.getGridLabelRenderer().setNumHorizontalLabels(3);
            graph.getGridLabelRenderer().setVerticalAxisTitle("Pounds");

        }
        else {
            Pair<String, String> pair = list.get(index-1);
            ArrayList<DataPoint> data = sql.getWeightDates(pair.first, pair.second);
            DataPoint[] dataArray = new DataPoint[data.size()];
            dataArray = data.toArray(dataArray);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataArray);
            graph.addSeries(series);

            //generate random color
            int red = r.nextInt(256);
            int green = r.nextInt(256);
            int blue = r.nextInt(256);
            series.setColor(Color.argb(255, red, green, blue));

            series.setTitle(pair.second);
            series.setDrawDataPoints(true);

            graph.getLegendRenderer().setVisible(true);
            graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);
            graph.getLegendRenderer().setBackgroundColor(0); //transparent
            graph.getGridLabelRenderer().setLabelFormatter(new DateAsXAxisLabelFormatter(getActivity()));
            graph.getGridLabelRenderer().setNumHorizontalLabels(3);
            graph.getGridLabelRenderer().setVerticalAxisTitle("Pounds");
        }

        sql.close();

        return rootView;
    }
}