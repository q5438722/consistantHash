import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.*;


class Trigger extends Thread
{
    public int serverID = 0;
    public HashMap<Integer, BufferedReader> readers = new HashMap<>();
    public HashMap<Integer, Integer> idMaps = new HashMap<>();

    public void addServer(int num) throws NoSuchAlgorithmException, IOException {
        serverID = serverID + 1;
        boolean flag = false;
        for(int i = 0; i < num; i++)
        {
            int port = hash.getHash("Server" +  Integer.toString(serverID));
            if (idMaps.containsKey(port)) continue;
            flag = true;
            String[] cmd = {"java", "-cp", "out/production/center", "client", "127.0.0.1", Integer.toString(port)};
            Process p = Runtime.getRuntime().exec(cmd);
            InputStream inputStream = p.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);//将一个字节流中的字节解码成字符
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//为输入流添加缓冲
            idMaps.put(port, serverID);
            readers.put(port, bufferedReader);
            System.out.println("port"  + port);
        }
        System.out.println("Size: " + readers.size());
    }


    @Override
    public void run()
    {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String text = "";
        try {
            while ((text = br.readLine()) != null) {
                if(text.equals("AddServer")) addServer(center.Redundancy);
                System.out.println(text);
            }
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}

public class center extends JFrame {
    public static final int Redundancy = 3;

    center(Trigger _trigger)
    {
        super("Consistent Hash");
        Points points = new Points(_trigger, this);
        Container c = getContentPane();
        c.setBackground(Color.BLACK);
        c.add(points);
        this.setBackground(Color.WHITE);

    }

    static void cancelServer(int port) throws IOException {
        Socket socket = new Socket("127.0.0.1", port);
        OutputStream outputStream = socket.getOutputStream();//得到一个输出流，用于向服务器发送数据
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);//将写入的字符编码成字节后写入一个字节流

        String data = "ping\n";
        writer.write(data);
        writer.flush();//刷新缓冲
        socket.shutdownOutput();//只关闭输出流而不关闭连接
        //获取服务器端的响应数据

        InputStream inputStream = socket.getInputStream();//得到一个输入流，用于接收服务器响应的数据
        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);//将一个字节流中的字节解码成字符
        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//为输入流添加缓冲
        String info = null;

        //输出服务器端响应数据
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

    void createServer()
    {

    }


    public static void main(String[] args) throws IOException {
//        int port = 1234;
//        String[] cmd = {"java", "-cp", "out/production/center", "client", "127.0.0.1", Integer.toString(port)};
//        Process p = Runtime.getRuntime().exec(cmd);
//        InputStream inputStream = p.getInputStream();
//
//        InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
//        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
//        String info = "";
//        info = bufferedReader.readLine();
//        System.out.printf("%d %s\n", port, info);
        Trigger trigger = new Trigger();
        trigger.start();
//        cancelServer(3333);
        center c = new center(trigger);
        c.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        c.setSize(Points.Width, Points.Height);
        c.setVisible(true);
    }
}
