/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.Key;
/**
 *
 * @author onur
 */
public interface MinerInterface {
    // we might need human readable names
    double getBalance(Key userPublicKey) throws RemoteException;
    // return true if successful
    boolean sendMoney(Key senderPublicKey, byte[] message) throws RemoteException ;
    // just for demos, a method to initialize an account with some money
    boolean register(Key senderPublicKey, double initialBalance) throws RemoteException ;
    // return true if block is accepted
    boolean newBlockAnnouncement(Block block) throws RemoteException ;
}
