//package sockets;

import java.io.File;
import java.io.FileNotFoundException;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import java.net.*;

import java.util.*;

import javax.sound.sampled.SourceDataLine;

import java.io.*;

public class TCPClient {
	private static int serverPort = 6000;

	public static void main(String args[]) throws RemoteException, NotBoundException {
		Scanner sca = new Scanner(System.in);
		// args[0] <- hostname of destination
		if (args.length == 0) {
			System.out.println("java TCPClient hostname");
			System.exit(0);
		}
		if(args[0].equals("admin")){
			System.out.println("Hello Admin!");
			Admin admin = (Admin) LocateRegistry.getRegistry(1099).lookup("admin");
			try (Scanner sc = new Scanner(System.in)) {
				while (true) { // while consola
					// READ FROM SOCKET
					String consola = "\nAdmin Console\n\n------------------------------------\n\n1: Registar novo utilizador\n2: Listar as directorias/ficheiros por utilizador\n3: Configurar o mecanismo de failover\n4: Listar detalhes sobre o armazenamento\n5: Validar a replicação dos dados entre os vários servidores\n0: Sair\n\n------------------------------------\n\n";
					System.out.println(consola);

					// READ STRING FROM KEYBOARD
					String texto = sc.nextLine();

					switch (texto) {
						case ("1") -> {
							int opt = 1;
							int ind = -1;
							System.out.println("Username: ");
							String Username = sca.nextLine();
							ArrayList<String> Usernames = admin.get_users();
							for(int i = 0; i< Usernames.size(); i++){
								String user = Usernames.get(i);
								if(user.equals(Username)){
									System.out.println("Este utilizador já se encontra registado\nDeseja modificar os seus dados?\n0: Sim\n1: Nao");
									String resposta = sca.nextLine();
									if(resposta.strip().equals("0")){
										opt = 1;
										ind = i;
										break;
									}
									else if(resposta.strip().equals("1")){
										opt = 0;
										break;
									}
									else{
										System.out.println("Input invalido. Por favor tente de novo.");
									}
								}
							}
							if(opt == 1){
								System.out.println("Password: ");
								String Pass = sca.nextLine();
								System.out.println("Departamento: ");
								String Dep = sca.nextLine();
								System.out.println("Faculdade: ");
								String Fac = sca.nextLine();
								System.out.println("Contacto telefónico: ");
								String Tel = sca.nextLine();
								System.out.println("Morada: ");
								String Mor = sca.nextLine();
								System.out.println("Número do CC: ");
								String NumCC = sca.nextLine();
								System.out.println("Validade do CC: ");
								String ValCC = sca.nextLine();
								String dados = Username + " " + Pass + " " + Dep + " " + Fac + " " + Tel + " " + Mor + " " + NumCC + " " + ValCC;
								String reg = admin.register(dados, ind);
								System.out.println(reg);
							}
						} case("2") ->{
							ArrayList<String> Usernames = admin.get_users();
							if(Usernames.get(0).equals("000")){
								System.out.println("No users!");
							}else if(Usernames.get(0).equals("001")){
								System.out.println("File not found!");
							}else{
								for(int i = 0; i < Usernames.size();i++){
									System.out.println(i + ": " + Usernames.get(i));
								}
								String User = sca.nextLine();
								int User1 = Integer.parseInt(User);
	
								String out = admin.directories_print(Usernames, User1);
								System.out.println(out);
							}
							
						} case("0") -> {
							System.out.println("Logged out successfully");
							System.exit(0);
						}
					}
				}
			}catch(FileNotFoundException e) {
				System.out.println("File not found!");
			}catch (IOException e) {
				System.out.println("Admin logged out\nNice!");
			}
		}else{
			// 1o passo - criar socket
			try (Socket s = new Socket(args[0], serverPort)) {
				System.out.println("SOCKET=" + s);

				// 2o passo
				DataInputStream in = new DataInputStream(s.getInputStream());
				DataOutputStream out = new DataOutputStream(s.getOutputStream());

				// READ user and password FROM KEYBOARD
				while (true) {
					System.out.println("Username: ");
					String Username = sca.nextLine();
					System.out.println("Password: ");
					String Pass = sca.nextLine();

					String UserPass = Username.concat(" " + Pass);

					// WRITE INTO THE SOCKET
					out.writeUTF(UserPass);

					// READ FROM SOCKET
					String data = in.readUTF();
					if (data.equals("Authenticated")) {
						System.out.println("\nAuthenticated");
						break;
					} else if (data.equals("This user is already Online.")) {
						System.out.println("This user is already Online.\nYou can't have two sessions at once.");
						return;
					}
					System.out.println("\nCouldn't authenticate, please try again.");
				}

				// 3o passo
				try (Scanner sc = new Scanner(System.in)) {
					while (true) { // while consola
						// READ FROM SOCKET
						String consola = in.readUTF();
						System.out.println(consola);

						// READ STRING FROM KEYBOARD
						String texto = sc.nextLine();

						// WRITE INTO THE SOCKET
						out.writeUTF(texto);

						switch (texto) {
							case ("1") -> {
								while (true) {
									// READ request
									String data = in.readUTF();

									if (data.equals("-1")) {
										System.out.println("\nNao estas autenticado!");
										break;
									} else if (data.equals("0")) {
										System.out.println("\nPassword Changed!");
										break;
									}

									// DISPLAY WHAT WAS READ
									System.out.println(data);

									// READ STRING FROM KEYBOARD
									String newPass = sc.nextLine();

									// WRITE password
									out.writeUTF(newPass);
								}
								break;
							} /*
								* case ("2") -> {
								* System.out.
								* println("Que servidor deseja alterar?\n0 - Principal \n1 - Secundario\n");
								* String resp = sc.nextLine();
								* out.writeUTF(resp);
								* while(true){
								* System.out.
								* println("O que quer alterar?\n0 - Endereco IP\n1 - Porto\n2 - IP e Porto\n3 - Nenhum\n"
								* );
								* String opt = sc.nextLine();
								* out.writeUTF(opt);
								* if(opt.equals("0")){
								* System.out.println("Escreva o novo IP\n");
								* String newIP = sc.nextLine();
								* out.writeUTF(newIP);
								* }else if(opt.equals("1")){
								* System.out.println("Escreva o novo Porto\n");
								* String newIP = sc.nextLine();
								* out.writeUTF(newIP);
								* }else if(opt.equals("2")){
								* System.out.println("Escreva IP:Porto\n");
								* String newIP = sc.nextLine();
								* out.writeUTF(newIP);
								* }else if(opt.equals("3")){
								* System.out.println("Leaving already\n");
								* break;
								* }else{
								* System.out.println("Invalid Option NOOB!");
								* continue;
								* }
								* String check = in.readUTF();
								* if(check.equals("-1")){
								* System.out.println("Thats not an IPv4!!");
								* }else if(check.equals("0")){
								* System.out.println("Got it!");
								* break;
								* }
								* }
								* }
								*/
							case ("3") -> {
								String data = in.readUTF();
								File fileData = new File(data);
								if (data.equals("-1")) {
									System.out.println("\nNao estas autenticado!");
								} else {
									String[] lista = fileData.list();
									System.out.println("Ficheiros na diretoria:");
									if (lista.length >= 1) {
										for (String ficheiro : lista) {
											System.out.println(ficheiro);
										}

									} else {
										System.out.println("This directory is Empty, like my soul :)");
									}

								}
							}
							case ("4") -> {
								String len = in.readUTF();
								int lenI = Integer.parseInt(len);
								ArrayList<String> directories = new ArrayList<>();
								for (int i = 0; i < lenI; i++) {
									String data = in.readUTF();
									directories.add(data);
									System.out.println(i + " - " + data);
								}
								System.out.println(lenI + " - Go back on the directories");

								String opt = sc.nextLine();
								out.writeUTF(opt);
								String fine = in.readUTF();
								if (fine.equals("-1")) {
									System.out.println("You can't go back from here!!");
								} else {
									System.out.println("Nice Move!!");
								}
							}
							case ("5") -> {
								String data = in.readUTF();
								File fileData = new File(data);
								if (data.equals("-1")) {
									System.out.println("\nNao estas autenticado!");
								} else {
									String[] lista = fileData.list();
									System.out.println("Ficheiros na diretoria:");
									for (String ficheiro : lista) {
										System.out.println(ficheiro);
									}

								}
							}
							case ("6") -> {
								String len = in.readUTF();
								int lenI = Integer.parseInt(len);
								ArrayList<String> directories = new ArrayList<>();
								for (int i = 0; i < lenI; i++) {
									String data = in.readUTF();
									directories.add(data);
									System.out.println(i + " - " + data);
								}
								System.out.println(lenI + " - Go back on the directories");
								System.out.println((lenI + 1) + " - Write directories");

								String opt = sc.nextLine();
								out.writeUTF(opt);
								String fine = in.readUTF();
								if (fine.equals("-1")) {
									System.out.println("You can't go back from here!!");
								} else if (fine.equals("1")) {
									System.out.println("Escreva a diretoria.");
									String direct = sc.nextLine();
									out.writeUTF(direct);
									String doesItExist = in.readUTF();
									if (doesItExist.equals("0")) {
										System.out.println("High risk, low reward!-Elden Ring");
									} else {
										System.out.println("To bad!");
									}
								} else {
									System.out.println("Nice Move!!");
								}
							}
							case ("0") -> {
								System.out.println("Logged out successfully");
								System.exit(0);
							}
						}
					}
				}

			} catch (EOFException e) {
				System.out.println("EOF:" + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO:" + e.getMessage());
			}
		}
	}
		
}