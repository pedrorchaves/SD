//package sockets;
import java.rmi.*;
import java.io.File;
import java.io.FileNotFoundException;

public interface Admin extends Remote {
	public String register(String dados, File FileName) throws java.rmi.RemoteException, java.io.FileNotFoundException;

	public void directories_print(String directory) throws java.rmi.RemoteException;

    public void failover_stats(int n, int time) throws java.rmi.RemoteException;
}