package cn.upus.app.upems.util.updata;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * xml解析
 */
public class ParseXmlService {

    public static List<UpdateInfo> getDate() throws MalformedURLException, IOException, XmlPullParserException {
        String path = "http://www.upus.cn:8081/UpusApp/upems8_update.xml";
        HttpURLConnection con = (HttpURLConnection) new URL(path).openConnection();
        con.setConnectTimeout(15000);
        con.setRequestMethod("GET");
        int i = con.getResponseCode();
        if (i == 200) {
            InputStream in = con.getInputStream();
            return parseXML(in);
        }
        return null;
    }

    /*
     * pull方法解析xml
     */
    private static List<UpdateInfo> parseXML(InputStream in) throws XmlPullParserException, IOException {

        List<UpdateInfo> date = null;
        UpdateInfo updateInfo = null;
        XmlPullParser pullParser = Xml.newPullParser();
        pullParser.setInput(in, "gb2312");
        int event = pullParser.getEventType();
        while (event != XmlPullParser.END_DOCUMENT) {
            switch (event) {
                case XmlPullParser.START_DOCUMENT:
                    date = new ArrayList<UpdateInfo>();
                    break;
                case XmlPullParser.START_TAG:
                    if ("update".equals(pullParser.getName())) {
                        updateInfo = new UpdateInfo();
                    }
                    if ("version".equals(pullParser.getName())) {
                        updateInfo.setVersion(pullParser.nextText());
                    }
                    if ("name".equals(pullParser.getName())) {
                        updateInfo.setName(pullParser.nextText());
                    }
                    if ("url".equals(pullParser.getName())) {
                        updateInfo.setUrl(pullParser.nextText());
                    }
                    if ("title".equals(pullParser.getName())) {
                        updateInfo.setContent(pullParser.nextText());
                    }
                    break;
                case XmlPullParser.END_TAG:
                    if ("update".equals(pullParser.getName())) {
                        date.add(updateInfo);
                        updateInfo = null;
                    }
                    break;
            }
            event = pullParser.next();
        }
        return date;
    }
}
