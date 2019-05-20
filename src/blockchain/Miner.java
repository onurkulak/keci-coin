/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.Key;
import java.util.Date;

/**
 * assume miners are completely separate entities from clients, they don't make transactions
 * @author onur
 */
public class Miner extends UnicastRemoteObject implements MinerInterface{
    
    Block lastBlock;
    Transaction[] pendingTransactions;
    int pendingCntr;
    
    public static void main(String[] args){
        
    }
    
    protected Miner() throws RemoteException {
        super();
        pendingTransactions = new Transaction[Block.SIZE];
        pendingCntr = 0;
        lastBlock = null;
    }
    
    @Override
    public double getBalance(Key userPublicKey) throws RemoteException {
        double balance = 0;
        for(int i = 0; i < pendingCntr; i++)
            if(pendingTransactions[i].receiver.equals(userPublicKey))
                balance += pendingTransactions[i].amount;
            else if(pendingTransactions[i].sender.equals(userPublicKey))
                balance -= pendingTransactions[i].amount;
        for(Block cb = lastBlock; cb != null; cb = cb.previousBlock){
            for(Transaction t: cb.transactions)
                if(t.receiver.equals(userPublicKey))
                    balance += t.amount;
                else if(t.sender.equals(userPublicKey))
                    balance -= t.amount;
        }
        return balance;
    }

    @Override
    public boolean sendMoney(Key senderPublicKey, byte[] message) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean register(Key senderPublicKey, double initialBalance)throws RemoteException  {
        if(getBalance(senderPublicKey) != 0)
            return false;
        else{
            Transaction t = new Transaction();
            t.receiver = senderPublicKey;
            t.amount = initialBalance;
            t.date = new Date();
            addTransaction(t);
            return true;
        }
    }
    
    private void createBlockHash(Block b){
        
    }
    
    private void announceNewBlock() {
        
    }

    // called when someone else notifies for a new block
    @Override
    public boolean newBlockAnnouncement(Block block) throws RemoteException {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void addTransaction(Transaction t) {
        pendingTransactions[pendingCntr++] = t;
        if(pendingCntr == pendingTransactions.length){
            
        }
    }
}
