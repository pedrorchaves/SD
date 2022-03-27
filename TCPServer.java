package sockets;

// TCPServer2.java: Multithreaded server

import java.io.FileNotFoundException;
import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.*;
import javax.xml.namespace.QName;

public class TCPServer {
    private static int serverPort = 6000;

    public static void main(String args[]) {
        ArrayList<String> Usernames = new ArrayList<>();
        ArrayList<String> Users = new ArrayList<>();
        ArrayList<String> OnlineUsers = new ArrayList<>();
        ArrayList<String> Directories = new ArrayList<>();
        int numero = 0;

        try (ServerSocket listenSocket = new ServerSocket(serverPort)) {
            System.out.println("A escuta no porto 6000");
            System.out.println("LISTEN SOCKET=" + listenSocket);
            while (true) {
                Socket clientSocket = listenSocket.accept(); // BLOQUEANTE
                System.out.println("CLIENT_SOCKET (created at accept())=" + clientSocket);
                numero++;
                new Connection(clientSocket, numero, Usernames, Users, OnlineUsers, Directories);
            }
        } catch (IOException e) {
            System.out.println("Listen:" + e.getMessage());
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
    static String username;
    int[] thread_number = new int[2];

    public Connection(Socket aClientSocket, int numero, ArrayList<String> Users, ArrayList<String> Usernames,
            ArrayList<String> OnlineUsers, ArrayList<String> Directories) {
        thread_number[0] = numero;
        this.Users = Users;
        this.Usernames = Usernames;
        this.OnlineUsers = OnlineUsers;
        this.Directories = Directories;
        try {
            clientSocket = aClientSocket;
            in = new DataInputStream(clientSocket.getInputStream());
            out = new DataOutputStream(clientSocket.getOutputStream());
            this.start();
        } catch (IOException e) {
            System.out.println("Connection:" + e.getMessage());
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
            System.out.println("Error when writing to File.");
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
            System.out.println("hehe wrong filename");
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
            System.out.println("hehe wrong filename");
        }
    }

    public static String Authenticate(String UserPass, ArrayList<String> Users, ArrayList<String> OnlineUsers) {
        if (Users.indexOf(UserPass) == -1) {
            return "Failed";
        }

        String[] arrayString = UserPass.split(" ");
        username = arrayString[0];

        if (OnlineUsers.indexOf(username) == -1) {
            return "Authenticated";
        }
        return "This user is already Online.";
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
                if (Authentication.equals("Authenticated")) {
                    out.writeUTF(Authentication);

                    int Position = Users.indexOf(UserPass);
                    thread_number[1] = Position;
                    OnlineUsers.add(Usernames.get(Position));
                    System.out.println("User: " + Usernames.get(Position) + " has logged on\n");

                    break;
                } else if (Authentication.equals("This user is already Online.")) {
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
                        "\nConsole\n\n------------------------------------\n\n1: Change Password\n2: Configurar endereços e portos de servidores primario e secundario\n3: Listar os ficheiros que existem na diretoria atual do Server\n4: Mudar a diretoria atual do servidor\n5: Listar os ficheiros que existem na diretoria atual do cliente\n6: Mudar a diretoria atual do cliente\n7: Descarregar um ficheiro do servidor\n8: Carregar um ficheiro para o servidor\n0: Sair\n\n------------------------------------\n");
                String data = in.readUTF();
                switch (data) {
                    case ("1") -> {
                        while (true) {
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
                    case ("3") -> {
                        if (OnlineUsers.indexOf(Usernames.get(thread_number[1])) == -1) {
                            out.writeUTF("-1");
                        } else {
                            String current = Directories.get(thread_number[1]);
                            out.writeUTF(currentPath + "\\" + "directories" + "\\" + current);
                        }
                    }
                    case ("4") -> {
                        String current = Directories.get(thread_number[1]);
                        File file = new File(currentPath + "\\" + "directories" + "\\" + current);
                        // https://stackoverflow.com/questions/5125242/java-list-only-subdirectories-from-a-directory-not-files
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
                        out.writeUTF(currentLocalPath);
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
                        for (String directory : directories) {
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
                            }
                            else{
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
            System.out.println("User[" + Usernames.get(thread_number[1]) + "] logged out, using CTRL + C");
            OnlineUsers.remove(Usernames.get(thread_number[1]));
        } catch (IOException e) {
            System.out.println("User[" + Usernames.get(thread_number[1])
                    + "] logged out\nMay the force be with you!\nHow u doin?");
            OnlineUsers.remove(Usernames.get(thread_number[1]));
        }
    }
}