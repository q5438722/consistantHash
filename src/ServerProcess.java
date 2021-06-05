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
//            System.out.println("test\nf1\n");
            Socket nextSocket = new Socket("127.0.0.1", port);
//            System.out.println("test\nf2\n");

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

    public int getPrev(int dstPort)
    {
//        if(father.port == dstPort) return father.port;
        if(Server.middleOf(father.port, father.routeTable.get(0), dstPort)) return father.port;
        int idx = 0;
//        for(; idx < hash.hashLen - 1; idx++)//forward search
//        {
//            if(Server.middleOf(father.routeTable.get(idx), dstPort, father.routeTable.get(idx + 1))) break;
//        }

        boolean needRefresh = false;
        while(idx >= 0)
        {
            String res = sendMsg("prefix\n" + dstPort + "\n", father.routeTable.get(idx));
            if(res != null && !res.equals("")) return Integer.parseInt(res);
            idx = idx - 1;
            needRefresh = true;
        }

        if(needRefresh) refreshRouteTable();
        return -1;
    }

    public int getServer(int dstPort) //Always let prev to tell where this should be
    {
        System.out.println("test\ngetserver" + dstPort + "\n");
        if(father.port == dstPort) return father.port;
        if(Server.middleOf(father.port, father.routeTable.get(0), dstPort)) return father.routeTable.get(0);
        int idx = 0;
        for(; idx < hash.hashLen - 1; idx++)//forward search
        {
            if(Server.middleOf(father.routeTable.get(idx), father.routeTable.get(idx + 1), dstPort)) break;
        }

        boolean needRefresh = false;
        while(idx >= 0)
        {
            if(father.routeTable.get(idx) == father.port) System.out.println("xxxxxx");
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
        if(req.size() > 1) System.out.println("test\nmessagetrans" + req.get(0) + " " + req.get(1) + "\n");
        else System.out.println("test\nmessagetrans" + req.get(0) + "\n");
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
            case "freshRoute"://from center
                refreshRouteTable();
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
            case "print"://from server
                System.out.println("test\nRouteTable\n");
                for(int i = 0; i < hash.hashLen - 1; i++)
                    System.out.println("test\n" + father.routeTable.get(i) + "\n");
                System.out.println("test\nData\n");
                for(Integer i : father.dataLog.keySet())
                    System.out.println("test\n" + father.dataLog.get(i) + "\n");
                //System.out.println("ok");
                break;
            case "setNext"://from server
                index = Integer.parseInt(req.get(1));
                father.routeTable.set(0, index);
                break;
        }

        System.out.println("test\n" + req.get(0) + " res" + res + "\n");
        return res;
    }

    @Override
    public void run() {
        if(socket == null)
        {
//            System.out.println("test\nwww\n");
            String res = sendMsg("route\n" + Integer.toString((father.port + 1) % hash.hashMAX) + "\n", father.agent);
            if(res != null && !res.equals(""))
            {
//                System.out.println("test\nzzz\n");
                father.routeTable.set(0, Integer.parseInt(res));
//                System.out.println("test\nz1\n");
                //refreshRouteTable();
                System.out.println("test\nz2\n");
                father.setServerState(ServerState.run);
                int prev = getPrev(father.port);
                System.out.println("test\nprev: " + prev + "\n");
                if(prev == -1)
                {
                    father.setServerState(ServerState.zoobie);
                    System.out.println("test\nzoobie2\n");
                }
                sendMsg("setNext\n" + father.port + "\n", prev);
            }
            else {
                father.setServerState(ServerState.zoobie);
                System.out.println("test\nzoobie\n");
            }
            return;
        }

//        System.out.println("test\ninput\n");
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        OutputStream outputStream = null;
        OutputStreamWriter writer = null;
        try {
//            System.out.println("test\nr1\n");
            inputStream = socket.getInputStream();
            inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            bufferedReader = new BufferedReader(inputStreamReader);

            outputStream = socket.getOutputStream();
            writer = new OutputStreamWriter(outputStream, StandardCharsets.UTF_8);
//            System.out.println("test\nr2\n");

            ArrayList<String> req = new ArrayList<>();
            String temp = null;
            while ((temp = bufferedReader.readLine()) != null) req.add(temp);
//            System.out.println("test\nreq" + req.get(0) + "\n");

            String res = messageTrans(req);
            if(res != null) {
                writer.write(res);
                writer.flush();
            }
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

class HeartBeat extends Thread
{

    @Override
    public void run()
    {
        try {
            while(!isInterrupted()) {
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
        if(src == dst) return true;
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
        try {
            serverSocket = new ServerSocket(_port, 10, InetAddress.getByName("127.0.0.1"));
        } catch (Exception e) {
            System.out.println("test\nFail to initialize socket\n");
        }
//        System.out.println("test\n2\n");
        for(int i = 1; i < hash.hashLen - 1; i++)
        {
            routeTable.set(i, _port);
        }

        if(_agent == _port)
        {
            routeTable.set(0, _port);
            this.serverState = ServerState.run;
        }
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
//        System.out.println("test\nbefore\n");
        while(true)
        {
            System.out.println("test\npoint3" + port + "\n");
            socket = serverSocket.accept();
            System.out.println("test\nafter\n");
            ServerThread thread = new ServerThread(socket, this);
//            threadSet.add(thread);
            thread.start();
            thread.join();
//            System.out.println("test\npoint1\n");
            if(getServerState() == ServerState.zoobie)
            {
//                System.out.println("test\npoint2\n");
                break;
            }
        }
        heartBeat.interrupt();
        System.out.println("exit\n");
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
