package sockets;

import java.io.File;
import java.io.FileNotFoundException;

import java.net.*;
import java.util.Scanner;

import javax.sound.sampled.SourceDataLine;

import java.io.*;

public class TCPClient {
	private static int serversocket = 6000;

	public static void main(String args[]) {
		Scanner sca = new Scanner(System.in);
		// args[0] <- hostname of destination
		if (args.length == 0) {
			System.out.println("java TCPClient hostname");
			System.exit(0);
		}

		// 1o passo - criar socket
		try (Socket s = new Socket(args[0], serversocket)) {
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
				}
				else if (data.equals("This user is already Online.")){
					System.out.println("This user is already Online.\nYou can't have two sessions at once.");
					return;
				}
				System.out.println("\nCouldn't authenticate, please try again.");
			}
			

			// 3o passo
			try (Scanner sc = new Scanner(System.in)) {
				while (true) { //while consola
					// READ FROM SOCKET
					String consola = in.readUTF();
					System.out.println(consola);

					// READ STRING FROM KEYBOARD
					String texto = sc.nextLine();

					// WRITE INTO THE SOCKET
					out.writeUTF(texto);

					switch(texto){
						case("1") -> {
							while(true){
								// READ request
								String data = in.readUTF();

								if(data.equals("-1")){
									System.out.println("\nNao estas autenticado!");
									break;
								}else if(data.equals("0")){
									System.out.println("\nPassword Changed!");
									break;
								}
			
								// DISPLAY WHAT WAS READ
								System.out.println("Receive: " + data);

								// READ STRING FROM KEYBOARD
								String newPass = sc.nextLine();

								// WRITE password
								out.writeUTF(newPass);
							}
							break;
						}
						case("9") -> {
							System.out.println("Logged out successfully");
							System.exit(0);
						}
					}
				}
			}

		} catch (UnknownHostException e) {
			System.out.println("Sock:" + e.getMessage());
		} catch (EOFException e) {
			System.out.println("EOF:" + e.getMessage());
		} catch (IOException e) {
			System.out.println("IO:" + e.getMessage());
		}
	}
}