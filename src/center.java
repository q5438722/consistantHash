import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;


public class center {

    static void cancelServer(int port) throws IOException {
        Socket socket = new Socket("127.0.0.1", port);
        OutputStream outputStream = socket.getOutputStream();//得到一个输出流，用于向服务器发送数据
        OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);//将写入的字符编码成字节后写入一个字节流

        String data = "wow\n";
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
        cancelServer(3333);

    }
}
