package com.example.user.jotime.net;


import android.util.Log;

import com.example.user.jotime.data.model.ItemListModel;
import com.example.user.jotime.data.model.UserModel;
import com.example.user.jotime.ui.LoadDataListener;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class TimeRunnable implements Runnable {

    private UserModel model;
    private LoadDataListener loadDataListener;

    public TimeRunnable(UserModel model, LoadDataListener loadDataListener) {
        this.model = model;
        this.loadDataListener = loadDataListener;
    }

    @Override
    public void run() {
        final Map<String, String> params = new HashMap<>();
        params.put("card_code_dec", String.valueOf(model.getId()));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        params.put("date_from", formatter.format(model.getFromDate()));
        params.put("date_till", formatter.format(model.getTillDate()));
        Log.i("TimeRunnable", "formatter.format(model.getTillDate()): " + formatter.format(model.getTillDate()));

        final Element body;
        List<ItemListModel> listModels = new ArrayList<>();
        try {
            String URL = "http://jo.time";
            body = Jsoup.connect(URL).data(params).post().body();
            Log.i("TimeRunnable", body.text());

            List<String> missingTimeList = getMissingTime(body);
            List<String> datesList = getDates(body);
            List<String> listLog = getListLog(body);

            int i = 0;
            for (String missingTime : missingTimeList) {
                listModels.add(new ItemListModel(datesList.get(i), missingTime));
                i++;
            }

            i = -1;
            for (String str : listLog) {
                if (str.lastIndexOf("Дата") == 0) {
                    i++;
                } else
                    listModels.get(i).getLogList().add(str);
            }

            loadDataListener.success(listModels);

        } catch (IOException e) {
            e.printStackTrace();
            loadDataListener.error();
        }
    }

    private static List<String> getMissingTime(Element body) {
        final List<String> list = new ArrayList<>();
        Elements elements = body.select("td.red b");
        elements.select("span").remove();
        for (Element e : elements) list.add(e.text());
        return list;
    }

    private static List<String> getDates(Element body) {
        final List<String> list = new ArrayList<>();
        Elements elements = body.select("table.stat tr:lt(2)>td:lt(1)");
        for (Element e : elements) list.add(e.text());
        return list;
    }

    private static List<String> getListLog(Element body) {
        final List<String> list = new ArrayList<>();
        Elements elements = body.select("table.events tr");
        for (Element e : elements) {
            if (e.text().lastIndexOf("Дата") == 0) Log.i("TimeRunnable", "******************");
            list.add(e.text());
            Log.i("TimeRunnable", e.text());
        }
        return list;
    }
}
