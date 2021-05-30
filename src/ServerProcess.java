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

    public String sendMsg(String req, int port)
    {
        String res = null;
        System.out.print(req);
        System.out.println(port);

        try {

            Socket nextSocket = new Socket(socket.getLocalAddress().getHostAddress(), port);
            OutputStream outputStream = nextSocket.getOutputStream();
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);

            writer.write(req);
            writer.flush();
            nextSocket.shutdownOutput();

            InputStream inputStream = nextSocket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

            res = bufferedReader.readLine();

            bufferedReader.close();
            inputStreamReader.close();
            inputStream.close();
            writer.close();
            outputStream.close();
            nextSocket.close();
        } catch (IOException ignored) {}

        return res;
    }

    public void refreshRouteTable()
    {
        for(int i = 1; i < hash.hashLen - 1; i++)
        {
            father.routeTable.set(i, getServer((father.port + (1 << i)) % hash.hashMAX));
        }
    }

    public int getPrev(int disPort)
    {
        return 0;
    }

    public int getServer(int dstPort) //Always let prev to tell where this should be
    {
        if(father.port == dstPort) return father.port;
        if(Server.middleOf(father.port, father.routeTable.get(0), dstPort)) return father.routeTable.get(0);
        int idx = 0;
        for(; idx < hash.hashLen - 1; idx++)//forward search
        {
            if(!Server.middleOf(father.routeTable.get(idx), father.routeTable.get(idx + 1), dstPort)) break;
        }

        boolean needRefresh = false;
        while(idx >= 0)
        {
            String res = sendMsg("route\n" + dstPort + "\n", father.routeTable.get(idx));
            if(res != null && !res.equals("")) return Integer.parseInt(res);
            idx = idx - 1;
            needRefresh = true;
        }

        if(needRefresh) refreshRouteTable();
        return father.port;
    }


    public String messageTrans(ArrayList<String> req) throws NoSuchAlgorithmException {
        String res = null;
        int index = 0;
        if (father.serverState != ServerState.run) return null;

        switch (req.get(0)) {
            case "cancelServer"://from center
                int pre = getPrev(father.port);
                sendMsg("setNext\n" + father.routeTable.get(0) + "\n", pre);
                for(int log: father.dataLog.keySet())
                    sendMsg("transDataLog\n" + father.dataLog.get(log), father.routeTable.get(0));
                father.setServerState(ServerState.zoobie);
                break;
            case "route"://from server
                index = Integer.parseInt(req.get(1));
                res = Integer.toString(getServer(index));
                break;
            case "prefix"://from server
                index = Integer.parseInt(req.get(1));
                res = Integer.toString(getPrev(index));
                break;
            case "addDataLog"://from center
                String data = req.get(1);
                int dst = getServer(hash.getHash(data));
                if(dst != father.port) sendMsg("transDataLog\n" + data + "\n", dst);
                else father.addDataLog(data);
                break;
            case "transDataLog"://give another server datalog without verification, from server
                father.addDataLog(req.get(1));
                break;
            case "newPrefix"://from server
                index = Integer.parseInt(req.get(1));
                for(int log : father.dataLog.keySet())
                {
                    if(!Server.middleOf(index, father.port, log)) continue;
                    sendMsg("transDataLog\n" + father.dataLog.get(log) + "\n", index);
                }
                break;
            case "ping"://not used
                System.out.println("ok");
                break;
            case "setNext"://from server
                index = Integer.parseInt(req.get(1));
                father.routeTable.set(0, index);
                break;
        }
        
        return res;
    }

    @Override
    public void run() {
        if(socket == null)
        {
            String res = sendMsg("route\n" + Integer.toString((father.port + 1) % hash.hashMAX) + "\n", father.agent);
            if(res != null && !res.equals(""))
            {
                father.routeTable.set(0, Integer.parseInt(res));
                refreshRouteTable();
                father.setServerState(ServerState.run);
                int prev = getPrev(father.port);
                String preRes = sendMsg("setnext\n" + father.port + "\n", prev);
                if(preRes == null || preRes.equals("")) father.setServerState(ServerState.zoobie);
            }
            else father.setServerState(ServerState.zoobie);
            return;
        }

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

            ArrayList<String> req = new ArrayList<>();
            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) req.add(temp);

            String res = messageTrans(req);
            writer.write(res);
            writer.flush();

            socket.shutdownInput();

        } catch (IOException | NoSuchAlgorithmException e) {
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
//
//class clientThread extends Thread
//{
//    public int port;
//    public String req;
//    public ArrayList<String> res = new ArrayList<>();
//    clientThread(int _port, String _req)
//    {
//        port = _port;
//        req = _req;
//    }
//
//    @Override
//    public void run() {
//        try {
//            Socket socket = new Socket("127.0.0.1", 10068);
//            OutputStream outputStream = socket.getOutputStream();//得到一个输出流，用于向服务器发送数据
//            OutputStreamWriter writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);//将写入的字符编码成字节后写入一个字节流
//
//            writer.write(req);
//            writer.flush();//刷新缓冲
//            socket.shutdownOutput();//只关闭输出流而不关闭连接
//            //获取服务器端的响应数据
//
//            InputStream inputStream = socket.getInputStream();//得到一个输入流，用于接收服务器响应的数据
//            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);//将一个字节流中的字节解码成字符
//            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//为输入流添加缓冲
//            String temp = null;
//
//
//            while ((temp = bufferedReader.readLine()) != null) res.add(temp);
//
//            bufferedReader.close();
//            inputStreamReader.close();
//            inputStream.close();
//            writer.close();
//            outputStream.close();
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
//

class HeartBeat extends Thread
{

    @Override
    public void run()
    {
        try {
            while(true) {
                sleep(1000);
                System.out.println("HeartBeat");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

class Server
{
    public String ip;
    public int port, agent;
    public ArrayList<Integer> routeTable = new ArrayList<Integer>();//the last point is server itself
    public ServerSocket serverSocket;

    public Socket socket = null;
    public int cnt = 0;
//    public HashSet<ServerThread> threadSet = new HashSet<>();
    public ServerState serverState = ServerState.init;

    synchronized public ServerState getServerState(){return serverState;}
    synchronized public void setServerState(ServerState s) {serverState = s;}

    public HashMap<Integer, String> dataLog = new HashMap<>();

    public static boolean middleOf(int src, int dst, int index)
    {
        if(src < dst && index <= dst && index > src) return true;
        if(src > dst && (index <= dst || index > src)) return true;
        return false;
    }


    synchronized public void addDataLog(String inputLine) throws NoSuchAlgorithmException {
        int hashRes = hash.getHash(inputLine);
        dataLog.put(hashRes, inputLine);
    }

//    synchronized public String removeDataLog(int left, int right)//left closed right open
//    {
//        StringBuilder res = new StringBuilder();
//        for(Integer i : dataLog.keySet())
//        {
//            if(middleOf(left, right, i)) res.append(dataLog.get(i)).append("\n");
//        }
//        return res.toString();
//    }

    Server(String _ip, int _port, int _agent) throws IOException, InterruptedException {
//        System.out.println("test\n" + _agent + "\n");
        for(int i = 0; i < hash.hashLen; i++) routeTable.add(0);
        ip = _ip;
        port = _port;
        agent = _agent;
//        System.out.println("test\n1\n");
        serverSocket = new ServerSocket(_port, 10, InetAddress.getByName("127.0.0.1"));
//        System.out.println("test\n2\n");

        if(_agent == _port) routeTable.set(0, _port);
        else
        {
            ServerThread initThread = new ServerThread(null, this);
            initThread.start();
            initThread.join();
        }
    }
    void runServer() throws IOException, InterruptedException {

        HeartBeat heartBeat = new HeartBeat();
        heartBeat.start();
//        System.out.println("wow");
        while(true)
        {
            socket = serverSocket.accept();
            ServerThread thread = new ServerThread(socket, this);
//            threadSet.add(thread);
            thread.start();
            thread.join();

            if(getServerState() == ServerState.zoobie) break;
        }
    }
}

public class ServerProcess {
    public static void main(String[] args) throws IOException, InterruptedException {
//        ServerSocket serverSocket = new ServerSocket(Integer.parseInt(args[0]));
//        Socket socket = null;
        System.out.println("start");
        Server myserver = new Server(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
        myserver.runServer();
    }

}
