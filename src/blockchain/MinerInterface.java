/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Date;
import java.util.ArrayList;
/**
 *
 * @author onur
 */
public interface MinerInterface extends Remote {
    // we might need human readable names
    double getBalance(PublicKey userPublicKey) throws RemoteException;
    // return true if successful
    // message array has the format: include receiver public key followed by a double amont
    boolean sendMoney(PublicKey senderPublicKey, byte[] sign, byte[] message) throws RemoteException ;
    // just for demos, a method to initialize an account with some money
    boolean register(PublicKey senderPublicKey, double initialBalance, byte logicalTime) throws RemoteException ;
    // return true if block is accepted
    boolean newBlockAnnouncement(Block block, PublicKey miner) throws RemoteException ;
    int getChainLength() throws RemoteException;
    ArrayList<String> getknownMiners() throws RemoteException;
    void addMiner( String name) throws RemoteException;
}
