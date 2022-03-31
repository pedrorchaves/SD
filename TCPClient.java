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
    private static int RMIserverPort = 6969;
    private static int TCPserverPort = 6000;

	public static void main(String args[]) throws RemoteException, NotBoundException {
		Scanner sca = new Scanner(System.in);
		// args[0] <- hostname of destination
		if (args.length == 0) {
			System.out.println("java TCPClient hostname");
			System.exit(0);
		}
		if (args[0].equals("admin")) {
			System.out.println("Hello Admin!");
			Admin admin = (Admin) LocateRegistry.getRegistry(RMIserverPort).lookup("admin");
			try (Scanner sc = new Scanner(System.in)) {
				while (true) { // while consola
					// READ FROM SOCKET
					String consola = "\n Consola de Administração\n\n------------------------------------\n\n1: Registar novo utilizador\n2: Listar as directorias/ficheiros por utilizador\n3: Configurar o mecanismo de failover\n4: Listar detalhes sobre o armazenamento\n5: Validar a replicação dos dados entre os vários servidores\n0: Sair\n\n------------------------------------\n\n";
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
							for (int i = 0; i < Usernames.size(); i++) {
								String user = Usernames.get(i);
								if (user.equals(Username)) {
									System.out.println(
											"Este utilizador já se encontra registado\nDeseja modificar os seus dados?\n0: Sim\n1: Nao");
									String resposta = sca.nextLine();
									if (resposta.strip().equals("0")) {
										opt = 1;
										ind = i;
										break;
									} else if (resposta.strip().equals("1")) {
										opt = 0;
										break;
									} else {
										System.out.println("Input invalido. Por favor tente de novo.");
									}
								}
							}
							if (opt == 1) {
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
								String dados = Username + " " + Pass + " " + Dep + " " + Fac + " " + Tel + " " + Mor
										+ " " + NumCC + " " + ValCC;
								String reg = admin.register(dados, ind);
								System.out.println(reg);
							}
						}
						case ("2") -> {
							ArrayList<String> Usernames = admin.get_users();
							if (Usernames.get(0).equals("000")) {
								System.out.println("Não existem utilizadores!");
							} else if (Usernames.get(0).equals("001")) {
								System.out.println("Ficheiro não encontrado!");
							} else {
								for (int i = 0; i < Usernames.size(); i++) {
									System.out.println(i + ": " + Usernames.get(i));
								}
								String User = sca.nextLine();
								int User1 = Integer.parseInt(User);

								String out = admin.directories_print(Usernames, User1);
								System.out.println(out);
							}

						}
						case("3") -> {
							System.out.println("Escolha o número máximo de pings falhados possiveis!");
							String User = sca.nextLine();
							int n = Integer.parseInt(User);

							System.out.println("Escolha o tempo máximo entre pings!");
							String User1 = sca.nextLine();
							int time = Integer.parseInt(User1);

							String out = admin.failover_stats(n, time);
							System.out.println(out);
						}
						case ("4") -> {
							ArrayList<String> Usernames = admin.get_users();
							if (Usernames.get(0).equals("000")) {
								System.out.println("Não existem utilizadores!");
							} else if (Usernames.get(0).equals("001")) {
								System.out.println("Ficheiro não encontrado!");
							} else {
								for (int i = 0; i < Usernames.size(); i++) {
									System.out.println(i + ": " + Usernames.get(i));
								}
								System.out.println(Usernames.size() + ": Todos");
								String User = sca.nextLine();
								int User1 = Integer.parseInt(User);
								if(User1 < Usernames.size() && User1 >= 0){
									Long out = admin.memory_print(Usernames, User1);
									System.out.println(String.format("O utilizador tem %,d  bytes usados!", out));
								}else{
									Long out = 0L;
									for (int i = 0; i < Usernames.size(); i++) {
										out += admin.memory_print(Usernames, i);
									}
									System.out.println(String.format("Os utilizadores tem %,d  bytes usados!", out));
								}
							}
						}
						case ("0") -> {
							System.out.println("Saiu com sucesso!");
							System.exit(0);
						}
					}
				}
			} catch (FileNotFoundException e) {
				System.out.println("Ficheiro não encontrado!");
			} catch (IOException e) {
				System.out.println("Admin saiu\n");
			}
		} else {
			// 1o passo - criar socket
			try (Socket s = new Socket(args[0], TCPserverPort)) {
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
					if (data.equals("Autenticado")) {
						System.out.println("\nAutenticado");
						break;
					} else if (data.equals("Este utilizador já está online.")) {
						System.out.println("Este utilizador já está online.\nNão podes ter várias sessões ao mesmo tempo.");
						return;
					}
					System.out.println("\nFalha a autenticar, tente denovo.");
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
										System.out.println("\nPassword mudou!");
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
										System.out.println("Diretoria vazia");
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
								System.out.println(lenI + " - Voltar para a diretoria anterior");

								String opt = sc.nextLine();
								out.writeUTF(opt);
								String fine = in.readUTF();
								if (fine.equals("-1")) {
									System.out.println("Chegou á última diretoria!");
								} else {
									System.out.println("Diretoria mudada!");
								}
							}
							case ("5") -> {
								String data = System.getProperty("user.dir");
								out.writeUTF(data);
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
								System.out.println(lenI + " - Voltar para a diretoria anterior");
								System.out.println((lenI + 1) + " - Escreva a diretoria");

								String opt = sc.nextLine();
								out.writeUTF(opt);
								String fine = in.readUTF();
								if (fine.equals("-1")) {
									System.out.println("Chegou á última diretoria!");
								} else if (fine.equals("1")) {
									System.out.println("Escreva a diretoria.");
									String direct = sc.nextLine();
									out.writeUTF(direct);
									String doesItExist = in.readUTF();
									if (doesItExist.equals("0")) {
										System.out.println("Mudou de diretoria!");
									} else {
										System.out.println("Diretoria não existe!");
									}
								} else {
									System.out.println("Diretoria mudada!");
								}
							}
							case("7") -> {
								String data = in.readUTF();
								File fileData = new File(data);
								if (data.equals("-1")) {
									System.out.println("\nNao estas autenticado!");
								} else {
									String[] lista = fileData.list();
									File[] files = fileData.listFiles();
									int[] arrayInds = new int[lista.length];
									int k = 0;
									System.out.println("Qual ficheiro quer carregar?\n");
									for (int i = 0; i<lista.length; i++) {
										if(files[i].isFile()){
											arrayInds[k] = i;
											System.out.println(k + ": " + lista[i]);
											k++;
										}
									}
									String currentDir = in.readUTF();
									String fileInd = sc.nextLine();
									Integer fileIndex = Integer.parseInt(fileInd);
									out.writeUTF(Integer.toString(arrayInds[fileIndex]));

                            		System.out.println("A descarregar o ficheiro: " + lista[arrayInds[fileIndex]].toString());
									
									Integer size = 100;
									Socket socketDown = new Socket(args[0], 6969);
									byte[] dataDown = new byte[size];

									FileOutputStream fileOutput = new FileOutputStream(currentDir + "\\" + lista[arrayInds[fileIndex]].toString());
									BufferedOutputStream buffOutput = new BufferedOutputStream(fileOutput);
									DataInputStream inp = new DataInputStream(socketDown.getInputStream());
									int atual = 0;
									while((atual=inp.read(dataDown))!=-1){
										buffOutput.write(dataDown, 0, atual);
									}
									buffOutput.flush();
									socketDown.close();
									System.out.println("Download do ficheiro completo!");

								}
							}
							case("8") -> {
								String data = in.readUTF();
								File fileData = new File(data); //ficheiro local
								if (data.equals("-1")) {
									System.out.println("\nNao estas autenticado!");
								} else {
									String[] lista = fileData.list();
									File[] files = fileData.listFiles();
									int[] arrayInds = new int[lista.length];
									int k = 0;
									System.out.println("Qual ficheiro quer carregar?\n");
									for (int i = 0; i<lista.length; i++) {
										if(files[i].isFile()){
											arrayInds[k] = i;
											System.out.println(k + ": " + lista[i]);
											k++;
										}
									}
									String currentDir = in.readUTF(); //pasta server
									String fileInd = sc.nextLine();
									Integer fileIndex = Integer.parseInt(fileInd);
									out.writeUTF(Integer.toString(arrayInds[fileIndex]));

                            		System.out.println("A carregar o ficheiro: " + lista[arrayInds[fileIndex]].toString());
									
									Integer size = 100;
									Socket socketup = new Socket(args[0], 4200);
									

									FileInputStream fileInput = new FileInputStream(data + "\\" + lista[arrayInds[fileIndex]].toString());
									BufferedInputStream buffInput = new BufferedInputStream(fileInput);

									DataOutputStream out1 = new DataOutputStream(socketup.getOutputStream());
									long atual = 0;
									byte[] data1;
									
									File file = new File(data + "\\" + lista[arrayInds[fileIndex]].toString());
									long fileSize = file.length();
									
									while(atual != fileSize){
										if(fileSize - atual >= size){
											atual+=size;
										}
										else{
											size = (int)(fileSize-atual);
											atual = fileSize;
										}

										data1 = new byte[size];
										buffInput.read(data1,0,size);
										out1.write(data1);
									
									}
									out.flush();
									socketup.close();
									System.out.println("Upload do ficheiro completo!");

								}
							}
							case ("0") -> {
								System.out.println("Saiu com sucesso!");
								System.exit(0);
							}
						}
					}
				}

			} catch (EOFException e) {
				System.out.println("EOF:" + e.getMessage());
			} catch (IOException e) {
				System.out.println("IO:" + e.getMessage());
				if(TCPserverPort == 6000){
					TCPserverPort = 6001;
				}else if(TCPserverPort == 6001){
					TCPserverPort = 6000;
				}
			}
		}
	}

}