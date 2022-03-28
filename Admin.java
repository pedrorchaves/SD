//package sockets;
import java.rmi.*;
import java.util.ArrayList;
import java.io.File;
import java.io.FileNotFoundException;

public interface Admin extends Remote {
	public String register(String dados) throws java.rmi.RemoteException, java.io.FileNotFoundException;

	public String directories_print(ArrayList<String> Usernames,int User1) throws java.rmi.RemoteException;

    public void failover_stats(int n, int time) throws java.rmi.RemoteException;
}