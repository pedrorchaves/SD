
//package sockets;
import java.rmi.*;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

public interface Admin extends Remote {
	public String register(String dados, int ind) throws java.rmi.RemoteException, java.io.FileNotFoundException;

	public String directories_print(ArrayList<String> Usernames, int User1) throws java.rmi.RemoteException;

	public String failover_stats(int n, int time) throws java.rmi.RemoteException;

	public ArrayList<String> get_users() throws java.rmi.RemoteException;

	public Long memory_print(ArrayList<String> Usernames, int User1) throws java.rmi.RemoteException;
}