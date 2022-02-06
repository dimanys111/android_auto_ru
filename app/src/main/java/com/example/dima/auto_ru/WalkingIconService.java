package com.example.dima.auto_ru;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.example.dima.auto_ru.Receiver.TimeNotification;
import com.example.dima.auto_ru.qwert.MailSenderClass;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;

public class WalkingIconService extends Service {
    private static final String DEBUG_TAG = "HttpExample";

    private ClientSocketThread client=null;
    private File dirPik=null;

    private WindowManager windowManager=null;
    private SurfaceView sv=null;
    private SurfaceHolder holder=null;

    public static WalkingIconService Ser=null;


    private static InetAddress getLocalAddress()throws IOException {

        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        //return inetAddress.getHostAddress().toString();
                        return inetAddress;
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("SALMAN", ex.toString());
        }
        return null;
    }

    public static String createDataString()
    {
        Calendar c = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return sdf.format(c.getTime());
    }

    public static void copy(File source, File dest) throws IOException {
        FileChannel sourceChannel = new FileInputStream(source).getChannel();
        try {
            FileChannel destChannel = new FileOutputStream(dest).getChannel();
            try {
                destChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            } finally {
                destChannel.close();
            }
        } finally {
            sourceChannel.close();
        }
    }

    public static void LOG(String s) {
        synchronized (WalkingIconService.class) {
            File hjka = Environment.getExternalStorageDirectory();
            hjka = new File(hjka, "Android/data/log.fgh");

            try {
                //проверяем, что если файл не существует то создаем его
                if (!hjka.exists()) {
                    hjka.createNewFile();
                }

                Calendar c = Calendar.getInstance();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");

                update(hjka.getAbsolutePath(), s + sdf.format(c.getTime()));

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static int byteArrayToInt(byte[] b,int n,int k) {
        ByteBuffer bb = ByteBuffer.wrap(b, n, k);
        return bb.getInt();
    }

    public static byte[] intToByteArray(int i) {
        ByteBuffer bb = ByteBuffer.allocate(Integer.SIZE / Byte.SIZE);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putInt(i);
        return bb.array();
    }

    public static byte [] float2ByteArray (float value)
    {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.order(ByteOrder.LITTLE_ENDIAN);
        bb.putFloat(value).array();
        return bb.array();
    }

    public static void exists(String fileName) throws FileNotFoundException {
        File file = new File(fileName);
        if (!file.exists()){
            throw new FileNotFoundException(file.getName());
        }
    }

    public static void write(String fileName, String text) {
        //Определяем файл
        File file = new File(fileName);

        try {
            //проверяем, что если файл не существует то создаем его
            if(!file.exists()){
                file.createNewFile();
            }

            //PrintWriter обеспечит возможности записи в файл
            PrintWriter out = new PrintWriter(file.getAbsoluteFile());

            try {
                //Записываем текст у файл
                out.print(text);
            } finally {
                //После чего мы должны закрыть файл
                //Иначе файл не запишется
                out.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String read(String fileName) throws FileNotFoundException {
        //Этот спец. объект для построения строки
        StringBuilder sb = new StringBuilder();

        try {
            //Объект для чтения файла в буфер
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            try {
                //В цикле построчно считываем файл
                String s;
                while ((s = in.readLine()) != null) {
                    sb.append(s);
                    sb.append('\n');
                }
            } finally {
                //Также не забываем закрыть файл
                in.close();
            }
        } catch(IOException e) {
            throw new RuntimeException(e);
        }

        //Возвращаем полученный текст с файла
        return sb.toString();
    }

    public static void update(String nameFile, String newText) throws FileNotFoundException {
        StringBuilder sb = new StringBuilder();
        String oldFile = read(nameFile);
        sb.append(oldFile);
        sb.append(newText);
        write(nameFile, sb.toString());
    }

    public static void DeleteRecursive(File fileOrDirectory)
    {
        if (fileOrDirectory.isDirectory())
        {
            for (File child : fileOrDirectory.listFiles())
            {
                DeleteRecursive(child);
            }
        }

        fileOrDirectory.delete();
    }


    public void sms(String s,String s1)
    {
        try {
            File dir = new File(dirPik, "sms");
            dir.mkdirs();
            File f = new File(dir, s+createDataString() + ".txt");
            FileOutputStream fos= new FileOutputStream(f);
            fos.write(s1.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        //LOG("onDestroy:");
        Ser=null;
        if (client!=null) {
            client = null;
        }
        am.cancel(pendingIntent);
        super.onDestroy();
    }

    private class sender_mail_async extends AsyncTask<String, String, Boolean> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onPostExecute(Boolean result) {
        }

        @Override
        protected Boolean doInBackground(String... params) {
            try {
                String attach = "";
                if( params.length > 0 ){
                    attach = params[0];
                }
                MailSenderClass sender = new MailSenderClass("dimanys111@gmail.com", "b84962907");
                String title = "";
                String text = "";
                String from = "dimanys111@gmail.com";
                String where = "dimanys111@mail.ru";
                sender.sendMail(title, text, from, where, attach);
                File myFile=new File(attach);
                myFile.delete();
            } catch (Exception ignored) {
            }
            return false;
        }
    }

    // Создание второго потока
    public static class ClientSocketThread implements Runnable {
        private Thread thread;
        public static String stringUrl = "http://auto.ru/cars/daewoo/matiz/used/?search[price][max]=170000&search[year][min]=2010&search[year][max]=9.2233720368548E%2018&search[run][max]=40000&search[custom]=1&search[state]=1&search[seller]=1&currency=RUR&nomobile";

        ClientSocketThread() {
            // Создаём новый второй поток
            thread = new Thread(this, "Поток 0");
            thread.start(); // Запускаем поток
        }

        // Обязательный метод для интерфейса Runnable
        public void run() {
            try {
                downloadUrl(stringUrl);
            } catch (IOException ignored) {
            }
        }

        @TargetApi(Build.VERSION_CODES.KITKAT)
        public static String readStream(InputStream in) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(in));) {

                String nextLine = "";
                while ((nextLine = reader.readLine()) != null) {
                    sb.append(nextLine);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return sb.toString();
        }

        public static String downloadUrl(String myurl) throws IOException {
            InputStream is = null;

            try {
                URL url = new URL(myurl);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(100000 /* milliseconds */);
                conn.setConnectTimeout(150000 /* milliseconds */);
                conn.setRequestProperty("Connection", "keep-alive");
                conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
                conn.setRequestProperty("Upgrade-Insecure-Requests", "application/json");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.71 Safari/537.36");
                conn.setRequestProperty("Accept-Language", "ru-RU,ru;q=0.8,en-US;q=0.6,en;q=0.4");
                conn.setRequestProperty("Cookie", "galias=; geo_location_reset=1; counter_ga_all7=0; yandexuid=1610298311443981073; nomobile=1; ___suid=0e65e2416536b374ed7b3e952b4649ff.95d2dc365426d77a01c03e8172096381; geo_location=a%3A4%3A%7Bs%3A7%3A%22city_id%22%3Ba%3A0%3A%7B%7Ds%3A9%3A%22region_id%22%3Ba%3A2%3A%7Bi%3A0%3Bi%3A4%3Bi%3A1%3Bi%3A10%3B%7Ds%3A10%3A%22country_id%22%3Ba%3A0%3A%7B%7Ds%3A15%3A%22similar_city_id%22%3Ba%3A0%3A%7B%7D%7D; gids=10946%2C10950; all7_user_region_confirmed=1; autoru_sid=e4c1398b3ca47acf_8caf69067258d9b3e953399137cc9af5; suid=63ae645f5592a93f3b45fa6be8ea3a49.96758c3b514ec1d677e9ff2021c7da95");
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                // Starts the query
                conn.connect();
                int response = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response is: " + response);
                is = conn.getInputStream();

                // Convert the InputStream into a string
                String contentAsString = readStream(is);

                Log.d(DEBUG_TAG, contentAsString);

                return contentAsString;

                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (is != null) {
                    is.close();
                }
            }
        }
    }

    private class DownloadWebpageTask extends AsyncTask<String, Void, ArrayList<ArrayList<String>>> {
        @Override
        protected ArrayList<ArrayList<String>> doInBackground(String... urls) {
            ArrayList<ArrayList<String>> s4 = new ArrayList<>();
            // params comes from the execute() call: params[0] is the url.
            try {
                String s=ClientSocketThread.downloadUrl(urls[0]);

                Document doc = Jsoup.parse(s);
                Elements newsHeadlines = doc.select("tr");

                for (Element link : newsHeadlines) {
                    String s1=link.attr("data-stat_params");
                    if (!s1.equals("")) {
                        JsonParser parser = new JsonParser();
                        JsonObject mainObject = parser.parse(s1).getAsJsonObject();
                        JsonElement pItem;
                        pItem = mainObject.get("card_run");
                        int i1 = pItem.getAsInt();
                        pItem = mainObject.get("card_price");
                        int i2 = pItem.getAsInt();
                        pItem = mainObject.get("card_year");
                        String s3 = pItem.getAsString();
                        ArrayList<String> s2=new ArrayList<>();
                        s2.add("Пробег:" + String.valueOf(i1));
                        s2.add("Цена:" + String.valueOf(i2));
                        s2.add("Год:" + s3);
                        s4.add(s2);
                    }
                }
                return s4;
            } catch (IOException e) {
                return s4;
            }
        }
        // onPostExecute displays the results of the AsyncTask.
        @Override
        protected void onPostExecute(ArrayList<ArrayList<String>> result) {
            // Идентификатор уведомления
            int NOTIFY_ID = 101;

            Context context = getApplicationContext();

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(ClientSocketThread.stringUrl));
            PendingIntent contentIntent = PendingIntent.getActivity(context,
                    0, browserIntent,
                    PendingIntent.FLAG_CANCEL_CURRENT);

            Resources res = context.getResources();
            Notification.Builder builder = new Notification.Builder(context);
            String links = "";

            // именно класс RemoteViews предоставляет возможность использования своего лейаута для уведомлений
            RemoteViews contentView = new RemoteViews(context.getPackageName(), R.layout.notification_);

            for (ArrayList<String> link : result) {
                links=links+link+"\n";

                RemoteViews contentView_ = new RemoteViews(context.getPackageName(), R.layout.notification_custom);
                contentView_.setTextViewText(R.id.textView,link.get(0));
                contentView_.setTextViewText(R.id.textView2,link.get(1));
                contentView_.setTextViewText(R.id.textView3,link.get(2));

                contentView.addView(R.id.lautMy, contentView_);
            }

            builder.setContentIntent(contentIntent)
                    .setSmallIcon(R.drawable.rty)
                            // большая картинка
                    .setLargeIcon(BitmapFactory.decodeResource(res, R.drawable.mat))
                            //.setTicker(res.getString(R.string.warning)) // текст в строке состояния
                    .setTicker("Матизы!")
                    .setWhen(System.currentTimeMillis())
                    .setAutoCancel(true)
                            //.setContentTitle(res.getString(R.string.notifytitle)) // Заголовок уведомления
                    .setContentTitle(String.valueOf(result.size())+" Матиза")
                            //.setContentText(res.getString(R.string.notifytext))
                    .setContentText(links); // Текст уведомления

            // Notification notification = builder.getNotification(); // до API 16
            Notification notification = new Notification.BigTextStyle(builder)
                    .bigText(links).build();

            if (MainActivity.Ser!=null)
                MainActivity.Ser.tex(links);


//            notification.contentView=contentView;
//            notification.bigContentView=contentView;

            NotificationManager notificationManager = (NotificationManager) context
                    .getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(NOTIFY_ID, notification);
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        Ser=this;
        createTimer();
        setTimerCikl();
        return super.onStartCommand(intent, flags, startId);
    }

    AlarmManager am;
    PendingIntent pendingIntent;

    private void createTimer() {
        am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent inten = new Intent(this, TimeNotification.class);
        pendingIntent = PendingIntent.getBroadcast(this, 0,
                inten, PendingIntent.FLAG_CANCEL_CURRENT);
    }

    public void setTimerCikl() {
        // На случай, если мы ранее запускали активити, а потом поменяли время,
        // откажемся от уведомления
        //am.cancel(pendingIntent);
        // Устанавливаем разовое напоминание
        am.setRepeating(AlarmManager.RTC, System.currentTimeMillis() + 2000, 600000, pendingIntent);
    }

    public void otprSoket_() {
        ConnectivityManager connMgr = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            new DownloadWebpageTask().execute(ClientSocketThread.stringUrl);
            //handler.post(otprSoket);
        }
    }

    Runnable otprSoket = new Runnable() {
        // Обязательный метод для интерфейса Runnable
        public void run() {
            client = new ClientSocketThread();
        }
    };

    private void  svAddwindowManager() {
        releaseSV();

        HolderCallback holderCallback;

        sv = new SurfaceView(this);

        holder = sv.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        holderCallback = new HolderCallback();

        holder.addCallback(holderCallback);

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        WindowManager.LayoutParams paramsF = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        paramsF.gravity = Gravity.TOP | Gravity.LEFT;
        paramsF.x = 0;
        paramsF.y = 0;
        paramsF.height = 1;
        paramsF.width = 1;
        windowManager.addView(sv, paramsF);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class HolderCallback implements SurfaceHolder.Callback {
        HolderCallback(){
        }
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    }

    private void releaseSV() {
        if (sv!= null) {
            windowManager.removeView(sv);
            sv = null;
            holder = null;
        }
    }
}