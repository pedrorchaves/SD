//package sockets;

// TCPServer2.java: Multithreaded server

import java.lang.Object;
import java.net.*;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.StandardOpenOption;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import javax.xml.namespace.QName;

public class TCPServer extends UnicastRemoteObject implements Admin, Runnable {
    UDPServer[] getPings = new UDPServer[2];
    private static int TCPserverPort = 6001;
    private static int TCPserverPortPrim = 6000;
    private static int RMIserverPort = 6970;
    private static int RMIserverPortPrim = 6969;
    private static int UDPserverPortPrimer = 4200;
    private static int UDPserverPortSec = 4201;

    private static final int bufsize = 4096;
    private static int maxfailedrounds = 5;
    private static int timeout = 5000;
    private static int period = 1000;
    private static int port = 4200;
    private static int flag = -1;

    private static String hostname;

    private static int thread_count = 0;
    ServerSocket listenSocket;
    Socket clientSocket;

    public TCPServer() throws RemoteException {
        super();
    }

    public String register(String dados, int ind) throws RemoteException {
        String currentPath = System.getProperty("user.dir");
        String[] temp = dados.split(" ");
        currentPath = currentPath + "\\directories\\" + temp[0] + "\\home";
        File FileName = new File("auth.txt");
        Path pathName = Paths.get("auth.txt");

        if (ind == -1) {
            try (FileWriter writerOfFiles = new FileWriter(FileName, true)) {
                writerOfFiles.write(dados + "\n");
                writerOfFiles.close();
                new File(currentPath).mkdirs();

                return "Sucesso!";
            } catch (Exception e) {
                System.out.println("Erro a escrever no ficheiro");
                return "Erro a escrever no ficheiro";
            }
        } else {

            try {
                List<String> fileContent = new ArrayList<>(Files.readAllLines(pathName, StandardCharsets.UTF_8));
                fileContent.set(ind, dados);
                Files.write(pathName, fileContent, StandardCharsets.UTF_8);
                return "Sucesso!";
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return "-1";

    }

    public String directories_print(ArrayList<String> Usernames, int User1) throws RemoteException {

        String currentPath = System.getProperty("user.dir");
        String current = "";
        File file = new File(currentPath + "\\directories\\" + Usernames.get(User1) + "\\home");
        String[] lista = file.list();
        String out = "Ficheiros nas diretorias de " + Usernames.get(User1) + "\\home:\n";
        out += getFolders(file, Usernames.get(User1), 0);
        return out;
    }

    public String failover_stats(int n, int time) throws RemoteException {
        String out = "";
        out += "Numero de pings perdidos máximo: " + Integer.toString(n);
        out += "\nTempo entre pings máximo: " + Integer.toString(time);
        maxfailedrounds = n;
        timeout = time;
        return out;
    }

    public ArrayList<String> get_users() throws RemoteException {
        ArrayList<String> Usernames = new ArrayList<>();
        String out = "";
        // Reading Usernames and Passwords from File
        File authentication = new File("auth.txt");
        try (Scanner readFile = new Scanner(authentication)) {
            while (readFile.hasNextLine()) {
                String Line = readFile.nextLine();
                String[] arrayLine = Line.split(" ");
                Usernames.add(arrayLine[0]);
            }
        } catch (FileNotFoundException e) {
            Usernames.clear();
            Usernames.add("001");
            return Usernames;
        }
        if (Usernames.size() < 1) {
            Usernames.clear();
            Usernames.add("000");
            return Usernames;
        }

        return Usernames;
    }

    public Long memory_print(ArrayList<String> Usernames, int User1) throws java.rmi.RemoteException {
        String currentPath = System.getProperty("user.dir");
        String current = "";
        Long bytes = 0L;
        File file = new File(currentPath + "\\directories\\" + Usernames.get(User1) + "\\home");
        bytes = getFolderSize(file);
        return bytes;
    }

    private long getFolderSize(File folder) {
        long length = 0;
        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                length += files[i].length();
            } else {
                length += getFolderSize(files[i]);
            }
        }

        return length;
    }

    private String getFolders(File folder, String User, Integer prof) {
        String out = "";
        File[] files = folder.listFiles();

        int count = files.length;

        for (int i = 0; i < count; i++) {
            if (files[i].isFile()) {
                String direct = files[i].toString();
                for (int k = 0; k < prof; k++){
                    out += "    ";
                }
                out += "-> " + direct.substring(direct.lastIndexOf("\\") + 1) + "\n";
            } else {
                String direct = files[i].toString();
                for (int k = 0; k < prof; k++){
                    out += "    ";
                }
                out += "-> " + direct.substring(direct.lastIndexOf("\\") + 1) + "\n";
                out += getFolders(files[i], User, prof + 1);
            }
        }

        return out;
    }

    public static void main(String args[]) throws RemoteException {
        
        hostname = args[0];

        int count = 1;
        try (DatagramSocket ds = new DatagramSocket()) {
            InetAddress ia = InetAddress.getByName(hostname);
            ds.setSoTimeout(timeout);
            int failedheartbeats = 0;
            try {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                dos.writeInt(count++);
                byte [] buf = baos.toByteArray();
                
                DatagramPacket dp = new DatagramPacket(buf, buf.length, ia, port);                
                ds.send(dp);
                
                byte [] rbuf = new byte[bufsize];
                DatagramPacket dr = new DatagramPacket(rbuf, rbuf.length);

                ds.receive(dr);
                failedheartbeats = 0;
                ByteArrayInputStream bais = new ByteArrayInputStream(rbuf, 0, dr.getLength());
                DataInputStream dis = new DataInputStream(bais);
                int n = dis.readInt();
                //System.out.println("Got: " + n + ".");
            }
            catch (SocketTimeoutException ste) {
                failedheartbeats++;
                System.out.println("Teste: " + failedheartbeats);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if(failedheartbeats == 1){
                port = UDPserverPortPrimer;
                System.out.println("Servidor primario não existe");
                flag = 0;
            }else{
                port = UDPserverPortSec;
                System.out.println("Servidor primario existe");
            }
        } catch (SocketException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (UnknownHostException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        for(int i = 0;i<2;i++){
            TCPServer TCPandUDP = new TCPServer();
            Thread thread = new Thread(TCPandUDP);
            thread.start();
        }
        
    }

    public void run(){
        ArrayList<String> Usernames = new ArrayList<>();
        ArrayList<String> Users = new ArrayList<>();
        ArrayList<String> OnlineUsers = new ArrayList<>();
        ArrayList<String> Directories = new ArrayList<>();
        int numero = 0;
        thread_count++;

        if(thread_count == 1){ //tcp
            //rmi
            if(flag == 0){
                RMIserverPort = RMIserverPortPrim;
                TCPserverPort = TCPserverPortPrim;
            }
            //System.out.println("RMI");
            try {
                Admin h = new TCPServer();
                Registry r = LocateRegistry.createRegistry(RMIserverPort);
                r.rebind("admin", h);
                System.out.println("Servidor de RMI á espera.");
            } catch (RemoteException re) {
                System.out.println("Exceção em TCPServer.run: " + re);
            }
            //rmi

            try (ServerSocket listenSocket = new ServerSocket(TCPserverPort)) {
                System.out.println("A escuta no porto 6000");
                System.out.println("LISTEN SOCKET=" + listenSocket);
                while (true) {
                    Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                    System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                    numero++;
                    new Connection(listenSocket, TCPserverPort, clientSocket, numero, Usernames, Users, OnlineUsers,
                            Directories);
                }
            } catch (IOException e) {
                System.out.println("A ouvir:" + e.getMessage());
            }
            
        }else if(thread_count == 2){ //udp
            //criar 3 threads, 1 para receber pings e 1 para enviar e 1 para enviar copias de ficheiros
            for(int i = 0;i<2;i++){
                getPings[i] = new UDPServer();
                Thread thread = new Thread(getPings[i]);
                getPings[i].setPort(port);
                thread.start();
            }
        }
        
    }
}
class UDPServer implements Runnable{
    private int primerPort = 4200;
    private static final int bufsize = 4096;

    private  int secundPort = 4201;
    private int maxfailedrounds = 5;
    private int timeout = 5000;
    private int period = 1000;
    private int port;
    private static int thread_number = 0;

    public void setPort(int port){
        this.port = port;
    }
    public void setTimeout(int timeout){
        this.timeout = timeout;
    }
    public void setMFR(int maxfailedrounds){
        this.maxfailedrounds = maxfailedrounds;
    }
    public void setPrimerPort(int primerPort){
        this.primerPort = primerPort;
    }
    public void setSecundPort(int secundPort){
        this.secundPort = secundPort;
    }
    
    public void run(){
        int count = 1;
        thread_number++;
        if(thread_number == 1){
            try (DatagramSocket ds = new DatagramSocket(port)) {
                InetAddress ia = InetAddress.getByName("localhost");
                System.out.println("Servidor UDP á espera");
                while (true) {
                    byte buf[] = new byte[bufsize];
                    DatagramPacket dp = new DatagramPacket(buf, buf.length);
                    ds.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(buf, 0, dp.getLength());
                    DataInputStream dis = new DataInputStream(bais);
                    int countS = dis.readInt();
    
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    DataOutputStream dos = new DataOutputStream(baos);
                    dos.writeInt(countS);
                    byte resp[] = baos.toByteArray();
                    DatagramPacket dpresp = new DatagramPacket(resp, resp.length, dp.getAddress(), dp.getPort());
                    ds.send(dpresp);
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else if(thread_number == 2){
            if(port == primerPort){
                port = secundPort;
            }else if(port == secundPort){
                port = primerPort;
            }
            try (DatagramSocket ds = new DatagramSocket()) {
                InetAddress ia = InetAddress.getByName("localhost");
                ds.setSoTimeout(timeout);
                int failedheartbeats = 0;
                while (failedheartbeats < maxfailedrounds) {
                    try {
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        DataOutputStream dos = new DataOutputStream(baos);
                        dos.writeInt(count++);
                        byte [] buf = baos.toByteArray();
                        
                        DatagramPacket dp = new DatagramPacket(buf, buf.length, ia, port);                
                        ds.send(dp);
                        
                        byte [] rbuf = new byte[bufsize];
                        DatagramPacket dr = new DatagramPacket(rbuf, rbuf.length);
    
                        ds.receive(dr);
                        failedheartbeats = 0;
                        ByteArrayInputStream bais = new ByteArrayInputStream(rbuf, 0, dr.getLength());
                        DataInputStream dis = new DataInputStream(bais);
                        int n = dis.readInt();
                        //System.out.println("Got: " + n + ".");
                    }
                    catch (IOException ste) {
                        failedheartbeats++;
                        System.out.println("Pings falhados: " + failedheartbeats);
                    }
                    Thread.sleep(period);
                }
            } catch (SocketException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }else if(thread_number == 3){
            //enviar ficheiros
        }

    }
}

// = Thread para tratar de cada canal de comunicação com um cliente
class Connection extends Thread {
    String FileName = "auth.txt";
    ArrayList<String> Users;
    ArrayList<String> Usernames;
    ArrayList<String> OnlineUsers;
    ArrayList<String> Directories;
    DataInputStream in;
    DataOutputStream out;
    Socket clientSocket;
    ServerSocket listenSocket;
    int serverPort;
    static String username;
    int[] thread_number = new int[2];

    public Connection(ServerSocket listenSocketa, int aserverPort, Socket aClientSocket, int numero,
            ArrayList<String> Users,
            ArrayList<String> Usernames,
            ArrayList<String> OnlineUsers, ArrayList<String> Directories) {
        thread_number[0] = numero;
        this.Users = Users;
        this.Usernames = Usernames;
        this.OnlineUsers = OnlineUsers;
        this.Directories = Directories;
        try {
            listenSocket = listenSocketa;
            clientSocket = aClientSocket;
            serverPort = aserverPort;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch (IOException e) {
            System.out.println("Conexão:" + e.getMessage());
        }
    }

    public static void writeToFile(String FileName, ArrayList<String> Users) {
        try (FileWriter writerOfFiles = new FileWriter(FileName)) {
            writerOfFiles.write("");
            for (int i = 0; i < Users.size(); i++) {
                writerOfFiles.write(Users.get(i) + "\n");
            }
            writerOfFiles.close();
        } catch (Exception e) {
            System.out.println("Erro a escrever no ficheiro.");
        }
    }

    public static void ReadFromFileAuth(String FileName, ArrayList<String> Users, ArrayList<String> Usernames) {
        Users.clear();
        Usernames.clear();
        // Reading Usernames and Passwords from File
        File authentication = new File(FileName);
        try (Scanner readFile = new Scanner(authentication)) {
            while (readFile.hasNextLine()) {
                String Line = readFile.nextLine();
                String[] arrayLine = Line.split(" ");
                Usernames.add(arrayLine[0]);
                Users.add(Line);

            }
        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro errado");
        }

    }

    public static void ReadFromFile(String FileName, ArrayList<String> directories) {
        directories.clear();
        File dirs = new File(FileName);
        try (Scanner readFile = new Scanner(dirs)) {
            while (readFile.hasNextLine()) {
                String Line = readFile.nextLine();
                String[] arrayLine = Line.split(" ");
                directories.add(arrayLine[1]);

            }
        } catch (FileNotFoundException e) {
            System.out.println("Ficheiro Errado");
        }
    }

    public static String Authenticate(String UserPass, ArrayList<String> Users, ArrayList<String> OnlineUsers) {

        String[] arrayString = UserPass.split(" ");

        for (int i = 0; i < Users.size(); i++) {
            String[] toCompare = Users.get(i).split(" ");
            if (toCompare[0].equals(arrayString[0]) && (toCompare[1].equals(arrayString[1]))) {
                username = arrayString[0];

                if (OnlineUsers.indexOf(username) == -1) {
                    return "Autenticado";
                }
                return "Este utilizador já está online.";
            }

        }
        return "O utilizador não está registado. Por favor fale com um admin.";

    }

    public static String uploadFiles(String destination, int portDown, int size, String localhost){
        try {
            
            ServerSocket socketDown = new ServerSocket(portDown);
            Socket connectionDown = socketDown.accept();
            InetAddress IA = InetAddress.getByName(localhost);

            DataInputStream inp = new DataInputStream(connectionDown.getInputStream());
            FileOutputStream fileOutput = new FileOutputStream(destination);
            BufferedOutputStream buffOutput = new BufferedOutputStream(fileOutput);
            
            byte[] dataDown = new byte[size];
            int atual = 0;

            while((atual=inp.read(dataDown))!=-1){
                buffOutput.write(dataDown, 0, atual);
            }
									
            buffOutput.flush();
            
            connectionDown.close();
            socketDown.close();
            return "Download completo!";

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Algo está em falta";
        }
    }

    public static String downloadFiles(File ficheiro, int portDown, int size, String localhost){
        try {
            ServerSocket socketDown = new ServerSocket(portDown);
            Socket connectionDown = socketDown.accept();
            InetAddress IA = InetAddress.getByName(localhost);

            DataOutputStream out = new DataOutputStream(connectionDown.getOutputStream());
            
            FileInputStream fileInput = new FileInputStream(ficheiro);
            BufferedInputStream buffInput = new BufferedInputStream(fileInput);

            long atual = 0;
            byte[] data;
            
            long fileSize = ficheiro.length();
            
            while(atual != fileSize){
                if(fileSize - atual >= size){
                    atual+=size;
                }
                else{
                    size = (int)(fileSize-atual);
                    atual = fileSize;
                }

                data = new byte[size];
                buffInput.read(data,0,size);
                out.write(data);
            
            }
            out.flush();
            connectionDown.close();
            socketDown.close();
            return "Download completo!";

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "Algo está faltando";
        }
    }
    // =============================
    public void run() {

        ReadFromFileAuth(FileName, Users, Usernames);
        ReadFromFile("dir.txt", Directories);
        String currentPath = System.getProperty("user.dir");
        String currentLocalPath = System.getProperty("user.dir");
        String resposta;
        // Pre-Authentication
        try {
            while (true) {
                if (OnlineUsers.size() > 0) {
                    System.out.println(OnlineUsers.get(0));
                }
                String UserPass = in.readUTF();
                String Authentication = Authenticate(UserPass, Users, OnlineUsers);
                System.out.println(Authentication);
                if (Authentication.equals("Autenticado")) {
                    out.writeUTF(Authentication);
                    String[] arrayString = UserPass.split(" ");
                    int Position = -1;

                    for (int i = 0; i < Users.size(); i++) {
                        String[] toCompare = Users.get(i).split(" ");
                        if (toCompare[0].equals(arrayString[0]) && (toCompare[1].equals(arrayString[1]))) {
                            Position = i;
                            break;
                        }
                    }

                    thread_number[1] = Position;
                    OnlineUsers.add(Usernames.get(Position));
                    System.out.println("Utilizador: " + Usernames.get(Position) + " entrou\n");

                    break;
                } else if (Authentication.equals("Este utilizador já está autenticado.")) {
                    out.writeUTF(Authentication);

                } else {
                    out.writeUTF(Authentication);
                }
            }
        } catch (Exception e) {

        }
        try {
            while (true) {
                // an echo server
                out.writeUTF(
                        "\nConsola\n\n------------------------------------\n\n1: Mudar Password\n2: Configurar endereços e portos de servidores primario e secundario\n3: Listar os ficheiros que existem na diretoria atual do Server\n4: Mudar a diretoria atual do servidor\n5: Listar os ficheiros que existem na diretoria atual do cliente\n6: Mudar a diretoria atual do cliente\n7: Descarregar um ficheiro do servidor\n8: Carregar um ficheiro para o servidor\n0: Sair\n\n------------------------------------\n");
                String data = in.readUTF();
                switch (data) {
                    case ("1") -> {
                        while (true) {
                            ReadFromFileAuth("auth.txt", Users, Usernames);
                            if (OnlineUsers.indexOf(Usernames.get(thread_number[1])) == -1) {
                                out.writeUTF("-1");
                                break;
                            }
                            out.writeUTF("Digite a nova password: ");
                            data = in.readUTF();
                            String temp = data;
                            out.writeUTF("Digite-a novamente: ");
                            data = in.readUTF();
                            if (temp.equals(data)) {
                                int position = thread_number[1];
                                String[] arrayString = Users.get(position).split(" ");
                                arrayString[1] = temp;
                                String UserChanged = "";
                                for (int i = 0; i < arrayString.length; i++) {
                                    if (i == arrayString.length - 1) {
                                        UserChanged += arrayString[i];
                                    } else {
                                        UserChanged += arrayString[i] + " ";
                                    }
                                }
                                Users.set(position, UserChanged);
                                writeToFile(FileName, Users);
                                ReadFromFileAuth(FileName, Users, Usernames);
                                out.writeUTF("0");
                                break;
                            }
                        }
                        break;

                    }
                    /*
                     * case ("2") -> {
                     * String answer = in.readUTF();
                     * while (true) {
                     * String choose = in.readUTF();
                     * if (choose.equals("0")) { // IP
                     * String newVal = in.readUTF();
                     * String[] temp = newVal.split(".");
                     * Boolean t = true;
                     * if (temp.length != 4) {
                     * out.writeUTF("-1");
                     * 
                     * } else {
                     * for (int i = 0; i < temp.length; i++) {
                     * int tempInt = Integer.parseInt(temp[i]);
                     * if (tempInt > 255 || tempInt < 0) {
                     * out.writeUTF("-1");
                     * t = false;
                     * break;
                     * }
                     * }
                     * if (t) {
                     * if (answer.equals("0")) {
                     * SocketAddress socketAddress = new SocketAddress(newVal, serverPort);
                     * listenSocket.bind(socketAddress);
                     * out.writeUTF("0");
                     * } else {
                     * // Envia para o 2º server
                     * }
                     * }
                     * }
                     * } else if (choose.equals("1")) {// port
                     * String newVal = in.readUTF();
                     * if (answer.equals("0")) {
                     * if (listenSocket.getLocalSocketAddress() != null) {
                     * String ip = listenSocket.getLocalSocketAddress().toString();
                     * SocketAddress socketAddress = new SocketAddress(ip, newVal);
                     * listenSocket.bind(socketAddress);
                     * out.writeUTF("0");
                     * } else {
                     * SocketAddress socketAddress = new InetSocketAddress("0.0.0.0", newVal);
                     * listenSocket.bind(socketAddress);
                     * out.writeUTF("0");
                     * }
                     * 
                     * } else {
                     * // Envia para o 2º server
                     * }
                     * break;
                     * } else if (choose.equals("2")) {// ip e port
                     * String newVal = in.readUTF();
                     * String[] temp = newVal.split(".");
                     * Boolean t = true;
                     * if (answer.equals("0")) {
                     * String[] ipPort = newVal.split(":");
                     * String[] ip = ipPort[0].split(".");
                     * int tempPort = Integer.parseInt(ipPort[1]);
                     * if (tempPort < 0 || tempPort > 65535) {
                     * out.writeUTF("-1");
                     * } else if (ip.length != 4) {
                     * out.writeUTF("-1");
                     * } else {
                     * for (int i = 0; i < 4; i++) {
                     * int tempInt = Integer.parseInt(ip[0]);
                     * if (tempInt > 255 || tempInt < 0) {
                     * out.writeUTF("-1");
                     * t = false;
                     * break;
                     * }
                     * }
                     * }
                     * if (t) {
                     * SocketAddress socketAddress = new SocketAddress(ipPort[0], ipPort[1]);
                     * listenSocket.bind(socketAddress);
                     * out.writeUTF("0");
                     * }
                     * } else {
                     * // Envia para o 2º server
                     * }
                     * break;
                     * } else if (choose.equals("3")) {// leave
                     * break;
                     * }
                     * }
                     * }
                     */
                    case ("3") -> {
                        ReadFromFileAuth("auth.txt", Users, Usernames);
                        if (OnlineUsers.indexOf(Usernames.get(thread_number[1])) == -1) {
                            out.writeUTF("-1");
                        } else {
                            String current = Directories.get(thread_number[1]);
                            out.writeUTF(currentPath + "\\" + "directories" + "\\" + current);
                        }
                    }
                    case ("4") -> {
                        ReadFromFileAuth("auth.txt", Users, Usernames);
                        String current = Directories.get(thread_number[1]);
                        File file = new File(currentPath + "\\" + "directories" + "\\" + current);
                        String[] directories = file.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File current, String name) {
                                return new File(current, name).isDirectory();
                            }
                        });
                        out.writeUTF(Integer.toString(directories.length));
                        for (String directory : directories) {
                            out.writeUTF(directory);
                        }

                        String newPathInde = in.readUTF();
                        int newPathIndex = Integer.parseInt(newPathInde);
                        if (newPathIndex == directories.length) {
                            if (current.equals(Usernames.get(thread_number[1]) + "\\home")) {
                                out.writeUTF("-1");
                            } else {
                                String[] temp = current.split("\\\\");
                                StringBuilder str = new StringBuilder();
                                for (int i = 0; i < temp.length - 1; i++) {
                                    if (i < temp.length - 2) {
                                        str.append(temp[i]);
                                        str.append("\\");
                                    } else {
                                        str.append(temp[i]);
                                    }
                                }
                                current = str.toString();
                                out.writeUTF("0");
                            }
                        } else {
                            StringBuilder str = new StringBuilder(current);
                            str.append("\\");
                            str.append(directories[newPathIndex]);
                            current = str.toString();
                            out.writeUTF("0");
                        }
                        Directories.set(thread_number[1], current);
                    }
                    case ("5") -> {
                        currentLocalPath = in.readUTF();
                    }
                    case ("6") -> {
                        File file = new File(currentLocalPath);
                        String[] directories = file.list(new FilenameFilter() {

                            @Override
                            public boolean accept(File current, String name) {
                                return new File(current, name).isDirectory();
                            }
                        });
                        out.writeUTF(Integer.toString(directories.length));
                        for (

                        String directory : directories) {
                            out.writeUTF(directory);
                        }

                        String newPathInde = in.readUTF();
                        int newPathIndex = Integer.parseInt(newPathInde);
                        if (newPathIndex == directories.length) {
                            String[] temp = currentLocalPath.split("\\\\");
                            StringBuilder str = new StringBuilder();
                            if (temp.length == 1) {
                                out.writeUTF("-1");
                            } else {
                                for (int i = 0; i < temp.length - 1; i++) {
                                    if (i < temp.length - 2) {
                                        str.append(temp[i]);
                                        str.append("\\");
                                    } else {
                                        str.append(temp[i]);
                                    }
                                }
                                currentLocalPath = str.toString();
                                out.writeUTF("0");
                            }
                        } else if (newPathIndex == (directories.length + 1)) {
                            out.writeUTF("1");
                            currentLocalPath = in.readUTF();
                            File currentPathNew = new File(currentLocalPath);
                            if (currentPathNew.exists()) {
                                out.writeUTF("0");
                            } else {
                                out.writeUTF("-1");
                            }
                        } else {
                            StringBuilder str = new StringBuilder(currentLocalPath);
                            str.append("\\");
                            str.append(directories[newPathIndex]);
                            currentLocalPath = str.toString();
                            out.writeUTF("0");
                        }
                    }
                    case("7") -> {
                        ReadFromFileAuth("auth.txt", Users, Usernames);
                        if (OnlineUsers.indexOf(Usernames.get(thread_number[1])) == -1) {
                            out.writeUTF("-1");
                        } else {
                            String current = Directories.get(thread_number[1]);
                            out.writeUTF(currentPath + "\\" + "directories" + "\\" + current);
                            
                            File fileData = new File(currentPath + "\\" + "directories" + "\\" + current);
                            String[] lista = fileData.list();
                            out.writeUTF(currentLocalPath);
                            String fileInd = in.readUTF();
                            Integer fileIndex = Integer.parseInt(fileInd);

                            System.out.println("A carregar o ficheiro: " + lista[fileIndex].toString());
                            File ficheiro = new File(currentPath + "\\" + "directories" + "\\" + current + "\\" + lista[fileIndex].toString());
                            String work = downloadFiles(ficheiro , 6969, 100, "localhost");
                            System.out.println(work);
                        }
                    }
                    case("8") -> {
                        ReadFromFileAuth("auth.txt", Users, Usernames);
                        if (OnlineUsers.indexOf(Usernames.get(thread_number[1])) == -1) {
                            out.writeUTF("-1");
                        } else {
                            String current = Directories.get(thread_number[1]);
                            out.writeUTF(currentLocalPath);
                            
                            File fileData = new File(currentLocalPath);
                            String[] lista = fileData.list();
                            out.writeUTF(currentPath + "\\" + "directories" + "\\" + current);
                            String fileInd = in.readUTF();
                            Integer fileIndex = Integer.parseInt(fileInd);

                            System.out.println("A carregar o ficheiro: " + lista[fileIndex].toString());
                            String ficheiro = currentPath + "\\" + "directories" + "\\" + current + "\\" + lista[fileIndex].toString();
                            String work = uploadFiles(ficheiro , 4200, 100, "localhost");
                            System.out.println(work);
                        }
                    }
                    
                    case ("0") -> {
                        OnlineUsers.remove(Usernames.get(thread_number[1]));
                        // System.out.println("User[" + Usernames.get(thread_number[1]) + "] logged
                        // out");
                    }
                }
                /*
                 * System.out.println("User[" + Usernames.get(thread_number[1]) + "] enviou: " +
                 * data);
                 * resposta = data.toUpperCase();
                 * out.writeUTF(resposta);
                 */
            }
        } catch (EOFException e) {
            System.out.println("Utilizador[" + Usernames.get(thread_number[1]) + "] saiu, usou CTRL + C");
            OnlineUsers.remove(Usernames.get(thread_number[1]));
        } catch (IOException e) {
            System.out.println("Utilizador[" + Usernames.get(thread_number[1])
                    + "] saiu\nMay the force be with you!\n");
            OnlineUsers.remove(Usernames.get(thread_number[1]));
        }
    }
}