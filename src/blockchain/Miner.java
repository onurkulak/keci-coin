/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;

/**
 * assume miners are completely separate entities from clients, they don't make transactions
 * @author onur
 */
public class Miner extends UnicastRemoteObject implements MinerInterface{

    
    protected Miner() throws RemoteException {
        super();
    }
    
    @Override
    public double getBalance(Key userPublicKey) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean sendMoney(Key senderPublicKey, byte[] message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double register(Key senderPublicKey, double initialBalance) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void createBlockHash(Block b){
        
    }
    
    public void announceNewBlock(){
        
    }

    @Override
    public boolean newBlockAnnouncement(Block block) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
