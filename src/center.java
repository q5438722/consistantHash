import com.sun.org.apache.xpath.internal.operations.Bool;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;


class DataLog
{
    public int src, dst;
    public String trans, data;
    DataLog(int _src, int _dst, String _trans, String _data)
    {
        src = _src;
        dst = _dst;
        trans = _trans;
        data = _data;
    }

    public String getOutputLabel()
    {
        if(trans.equals("test")) return src + " : " + data;
        if(data != null) return trans + " from " + src + " to " + dst + " : " + data;
        else return trans + " from " + src + " to " + dst;
    }

}


class Trigger extends Thread
{
    public static final int livePeriod = 5000;
    public final Boolean flag = false;
    public boolean refresh = false;
    public int serverID = 0;
    public Map <Integer, BufferedReader> readers = Collections.synchronizedMap(new HashMap<>());
    public Map <Integer, Integer> idMaps = Collections.synchronizedMap(new HashMap<>());
    public TreeMap<Long, DataLog> curLogs = new TreeMap<>(), pastLog = new TreeMap<>();

    public void addServer(int num) throws NoSuchAlgorithmException, IOException, InterruptedException {
        synchronized (flag)
        {
            serverID = serverID + 1;
            for(int i = 0; i < num; i++)
            {
                int port = hash.getHash("Server" +  Integer.toString(serverID));
                if (idMaps.containsKey(port)) continue;

                boolean closed = false;
                for(int used: idMaps.keySet())
                {
                    int dis = Math.abs(used - port);
                    if(dis > hash.hashMAX / 2) dis = hash.hashMAX - dis;
                    if(dis < center.minDistance) closed = true;
                }
                if(closed) continue;

                Iterator<Integer> it = idMaps.keySet().iterator();
                int agent = port;
                if(it.hasNext()) agent = it.next();
                System.out.println("port " + port + " agent " + agent);
                String[] cmd = {"java", "-cp", "out/production/center", "ServerProcess",
                        "127.0.0.1", Integer.toString(port), Integer.toString(agent)};
                Process p = Runtime.getRuntime().exec(cmd);
                InputStream inputStream = p.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);//将一个字节流中的字节解码成字符
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//为输入流添加缓冲
    //            String s;
    //
    //            while((s = bufferedReader.readLine()) != null) {
    //                System.out.println("s" + s);
    //            }
                idMaps.put(port, serverID);
                readers.put(port, bufferedReader);
                System.out.println("port"  + port);
            }
            System.out.println("Size: " + readers.size());
        }
    }

    void cancelServer(int port) throws IOException {
        Socket socket = new Socket("127.0.0.1", port);
        OutputStream outputStream = socket.getOutputStream();//得到一个输出流，用于向服务器发送数据
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);//将写入的字符编码成字节后写入一个字节流

        String data = "cancelServer\n";
        writer.write(data);
        writer.flush();//刷新缓冲
        socket.shutdownOutput();//只关闭输出流而不关闭连接
        //获取服务器端的响应数据

//        InputStream inputStream = socket.getInputStream();//得到一个输入流，用于接收服务器响应的数据
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);//将一个字节流中的字节解码成字符
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//为输入流添加缓冲
//        String info = null;
//
//        //输出服务器端响应数据
//        while ((info = bufferedReader.readLine()) != null) {
//            System.out.println(info);
//        }
        //关闭资源
//        bufferedReader.close();
//        inputStreamReader.close();
//        inputStream.close();
        writer.close();
        outputStream.close();
        socket.close();
    }

    void freshRoute(int port) throws IOException {
        Socket socket = new Socket("127.0.0.1", port);
        OutputStream outputStream = socket.getOutputStream();//得到一个输出流，用于向服务器发送数据
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);//将写入的字符编码成字节后写入一个字节流

        String data = "freshRoute\n";
        writer.write(data);
        writer.flush();//刷新缓冲
        socket.shutdownOutput();//只关闭输出流而不关闭连接
        //获取服务器端的响应数据

//        InputStream inputStream = socket.getInputStream();//得到一个输入流，用于接收服务器响应的数据
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);//将一个字节流中的字节解码成字符
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//为输入流添加缓冲
//        String info = null;
//
//        //输出服务器端响应数据
//        while ((info = bufferedReader.readLine()) != null) {
//            System.out.println(info);
//        }
        //关闭资源
//        bufferedReader.close();
//        inputStreamReader.close();
//        inputStream.close();
        writer.close();
        outputStream.close();
        socket.close();
    }


    void addData(String data) throws NoSuchAlgorithmException, IOException {
        int port = 0;
        synchronized (flag)
        {
            Iterator<Integer> it = idMaps.keySet().iterator();
            if(it.hasNext()) port = it.next();
            else return;
        }
        Socket socket = new Socket("127.0.0.1", port);
        OutputStream outputStream = socket.getOutputStream();//得到一个输出流，用于向服务器发送数据
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);//将写入的字符编码成字节后写入一个字节流

        writer.write("addDataLog\n");
        writer.write(data);
        writer.flush();//刷新缓冲
        socket.shutdownOutput();//只关闭输出流而不关闭连接
        //获取服务器端的响应数据

//        InputStream inputStream = socket.getInputStream();//得到一个输入流，用于接收服务器响应的数据
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);//将一个字节流中的字节解码成字符
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//为输入流添加缓冲
//        String info = null;
//
//        //输出服务器端响应数据
//        while ((info = bufferedReader.readLine()) != null) {
//            System.out.println(info);
//        }
        //关闭资源
//        bufferedReader.close();
//        inputStreamReader.close();
//        inputStream.close();
        writer.close();
        outputStream.close();
        socket.close();

    }

    void printData(int port) throws IOException {
        Socket socket = new Socket("127.0.0.1", port);
//        System.out.println("print" + port);
        OutputStream outputStream = socket.getOutputStream();//得到一个输出流，用于向服务器发送数据
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);//将写入的字符编码成字节后写入一个字节流

        writer.write("print\n");
        writer.flush();//刷新缓冲
        socket.shutdownOutput();//只关闭输出流而不关闭连接
        //获取服务器端的响应数据

        InputStream inputStream = socket.getInputStream();//得到一个输入流，用于接收服务器响应的数据
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);//将一个字节流中的字节解码成字符
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//为输入流添加缓冲
        String info = null;
//
//        //输出服务器端响应数据
        while ((info = bufferedReader.readLine()) != null) {
            System.out.println(info);
        }
        //关闭资源
        bufferedReader.close();
        inputStreamReader.close();
        inputStream.close();
        writer.close();
        outputStream.close();
        socket.close();

    }

    @Override
    public void run()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String text = "";
        try {
            while ((text = br.readLine()) != null) {
                if(text.equals("")) continue;
                if(text.charAt(0) == 'A' || text.charAt(0) == 'a') addServer(center.Redundancy);
                else if(text.charAt(0) == 'D' || text.charAt(0) == 'd')
                {
                    String data = br.readLine();
                    addData(data);
                }
                else if(text.charAt(0) == 'C' || text.charAt(0) == 'c')
                {
                    String data = br.readLine();
                    cancelServer(Integer.parseInt(data));
                }
                else if(text.charAt(0) == 'P' || text.charAt(0) == 'p')
                {
                    String data = br.readLine();
                    printData(Integer.parseInt(data));
                }
                else if(text.charAt(0) == 'R' || text.charAt(0) == 'r')
                {
                    synchronized (flag) {
                        refresh = true;
                    }
                }
                else if(text.charAt(0) == 'F' || text.charAt(0) == 'f')
                {
                    String data = br.readLine();
                    freshRoute(Integer.parseInt(data));
                }
                System.out.println(text);
            }
        } catch (IOException | NoSuchAlgorithmException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

public class center extends JFrame {
    public static final int Redundancy = 1, minDistance = 100;

    center(Trigger _trigger)
    {
        super("Consistent Hash");
        Points points = new Points(_trigger, this);
        Container c = getContentPane();
        c.setBackground(Color.BLACK);
        c.add(points);
        this.setBackground(Color.WHITE);

    }




    public static void main(String[] args) throws IOException, InterruptedException {


        Trigger trigger = new Trigger();
        trigger.start();
        center c = new center(trigger);
        c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        c.setSize(Points.Width, Points.Height);
        c.setVisible(true);
    }
}
