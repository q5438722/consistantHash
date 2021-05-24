import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.*;

enum ServerState
{
    init, run, zoobie;
};


class ServerThread extends Thread {
    Socket socket;
    Server father;

    public ServerThread(Socket socket, Server _father) {
        this.socket = socket;
        this.father = _father;
    }

    public void messageTrans(BufferedReader bufferedReader, OutputStreamWriter writer) throws IOException {
        String info = "";
        while ((info = bufferedReader.readLine()) != null)
        {
            System.out.println(info);
        }

        writer.write("next\n");
        writer.flush();//清空缓冲区数据

    }

    @Override
    public void run() {
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        OutputStreamWriter writer = null;
        try {
            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);

            outputStream = socket.getOutputStream();
            writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            messageTrans(bufferedReader, writer);

            socket.shutdownInput();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) writer.close();
                if (outputStream != null) outputStream.close();
                if (bufferedReader != null) bufferedReader.close();
                if (inputStreamReader != null) inputStreamReader.close();
                if (inputStream != null) inputStream.close();
                if (socket != null) socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}

class clientThread extends Thread
{

//    public int lowerBound()
//    {
//
//    }

    @Override
    public void run() {
        try {
            Socket socket = new Socket("127.0.0.1", 10068);
            OutputStream outputStream = socket.getOutputStream();//得到一个输出流，用于向服务器发送数据
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);//将写入的字符编码成字节后写入一个字节流

            Scanner sc = new Scanner(System.in);
            String data = sc.nextLine();
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
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



class Server
{
    public String ip;
    public int port, prev, next;
    public ServerSocket serverSocket;

    public Socket socket = null;
    public int cnt = 0;
//    public HashSet<ServerThread> threadSet = new HashSet<>();
    public ServerState serverState = ServerState.init;

    synchronized public ServerState getServerState(){return serverState;}
    synchronized public void setServerState(ServerState s) {serverState = s;}

    public HashMap<Integer, String> dataLog = new HashMap<>();
    synchronized public void addDataLog(String inputLine) throws NoSuchAlgorithmException {
        int hashRes = hash.getHash(inputLine);
        dataLog.put(hashRes, inputLine);
    }

    synchronized public String removeDataLog(int left, int right)//left closed right open
    {
        StringBuilder res = new StringBuilder();
        for(Integer i : dataLog.keySet())
        {
            if(left < right && i >= left && i < right) res.append(dataLog.get(i)).append("\n");
            if(left > right && (i >= left || i < right)) res.append(dataLog.get(i)).append("\n");
        }
        return res.toString();
    }

    Server(String _ip, int _port, int agent) throws IOException {
        ip = _ip;
        port = _port;
        serverSocket = new ServerSocket(_port);
        if(agent == 0) prev = next = _port;
        else
        {
            //complex process
        }
    }
    void runServer() throws IOException, InterruptedException {
        while(true)
        {
            socket = serverSocket.accept();
            ServerThread thread = new ServerThread(socket, this);
//            threadSet.add(thread);
            thread.run();
            System.out.println(thread.getId());
            thread.join();

            if(getServerState() == ServerState.zoobie) break;
        }
    }
}

public class ServerProcess {
    public static void main(String[] args) throws IOException, InterruptedException {
//        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
//        Socket socket = null;
        Server myserver = new Server(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        myserver.runServer();
    }

}
