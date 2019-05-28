/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.math.BigInteger;
import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.nio.ByteBuffer;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.rmi.*;
import java.rmi.registry.*;
/**
 * assume miners are completely separate entities from clients, they don't make transactions
 * @author onur
 */
public class Miner extends UnicastRemoteObject implements MinerInterface{
    
    PublicKey ID;
    Block lastBlock;
    Transaction[] pendingTransactions;
    int pendingCntr;
    volatile boolean hashFoundBySomeoneElse, hashFoundByMe;
    ArrayList<String> knownMiners;
    Registry reg;
    public Miner(PublicKey ID) throws RemoteException {
        super();
        pendingTransactions = new Transaction[Block.SIZE];
        pendingCntr = 0;
        lastBlock = null;
        hashFoundBySomeoneElse = false;
        this.ID = ID;
        reg = LocateRegistry.getRegistry(2323);
        try{
        MinerInterface temp = (MinerInterface) reg.lookup("globalMiner");
       
            MinerInterface m2;
            knownMiners = temp.getknownMiners();
            for(String minerName: knownMiners)
            {
                m2 = (MinerInterface) reg.lookup(minerName);
                m2.addMiner(ID.toString());
            }
            knownMiners.add(ID.toString());
            reg.rebind(ID.toString() , this  );
            
        }catch(Exception e){
            reg.rebind("globalMiner" , this);
            knownMiners = new ArrayList<String>();
            knownMiners.add("globalMiner");
            System.out.println("I'm the first!!");
        }
    }
    
    @Override
    public double getBalance(PublicKey userPublicKey) throws RemoteException {
        double balance = 0;
        System.out.println("SOO");
        for(int i = 0; i < pendingCntr; i++){
            if(pendingTransactions[i].receiver.equals(userPublicKey))
                balance += pendingTransactions[i].amount;
            if( pendingTransactions[i].sender != null && pendingTransactions[i].sender.equals(userPublicKey))
                balance -= pendingTransactions[i].amount;
        }
            
        
        for(Block cb = lastBlock; cb != null; cb = cb.previousBlock){
            for(Transaction t: cb.transactions){
                if(t.receiver.equals(userPublicKey))
                    balance += t.amount;
                if(t.sender!=null && t.sender.equals(userPublicKey))
                    balance -= t.amount;
            }
                
        }
        return balance;
    }

    @Override
    public boolean sendMoney(PublicKey senderPublicKey, byte[] sign, byte[] message) throws RemoteException {
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(senderPublicKey);
            verifier.update(message);
            // sign is correct
            if(verifier.verify(sign)){
                int publicKeyByteSize = message.length - 9;
                byte[] encodedPublicKey = new byte[publicKeyByteSize];
                byte[] encodedAmount = new byte[8];
                System.arraycopy(message, 0, encodedPublicKey, 0, encodedPublicKey.length);
                System.arraycopy(message, publicKeyByteSize, encodedAmount, 0, 8);
                PublicKey receiver =  KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedPublicKey));
                double amount = ByteBuffer.wrap(encodedAmount).getDouble();
                System.out.println("checking account");
                if(getBalance(senderPublicKey)>=amount){
                    Transaction transaction = new Transaction();
                    transaction.amount = amount;
                    transaction.receiver = receiver;
                    transaction.sender = senderPublicKey;
                    // get date from sender
                    transaction.logicalDate = message[message.length-1];
                    addTransaction(transaction);
                    System.out.println("transaction added");
                    return true;
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    @Override
    public boolean register(PublicKey senderPublicKey, double initialBalance, byte logicalTime)throws RemoteException  {
        //System.out.println("ASDASDASDADSA");
        if(getBalance(senderPublicKey) != 0){
            return false;
        }
        else{
            Transaction t = new Transaction();
            t.receiver = senderPublicKey;
            t.amount = initialBalance;
            t.logicalDate = logicalTime;
            addTransaction(t);
            return true;
        }
    }
    
    // returns true if finds a hash, 
    //return false if another miner finds and notifies by changing volatile value 
    // should set hash, nonce, and creation date for the block
    private boolean createBlockHash(Block b){
        boolean successful = false;
        String base = "";
        String withDate = "";
          for( int i = 0; i < Block.SIZE; i++){
              base = base + b.transactions[i].toString();
          }
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            while ( !successful )
            {
              b.creationDate = new Date(System.currentTimeMillis());
              withDate = base + b.creationDate.toString();
              for( b.randomNonce = 0; b.randomNonce < 100000; b.randomNonce++)
              {
                b.hash = new BigInteger(1, md.digest((withDate+b.randomNonce).getBytes()));
                    if( b.hash.getLowestSetBit() > Block.DIFFICULTY )
                    {
                       successful = true;
                       break;
                    }
                    if( hashFoundBySomeoneElse ) return false;
                }
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(successful){
            System.out.println(b.hash + "  " + b.randomNonce);
        }
        return successful;
    }
    
    private void announceNewBlock() {
        Miner temp;
        for(String minerName: knownMiners)
        {
            try {
                temp = (Miner)reg.lookup(minerName);
                temp.newBlockAnnouncement(lastBlock, ID);
            } catch (Exception ex) {
                //Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    // check if new block is valid, with all transactions and hashes. sami bunu sen yap
    private boolean checkValidity(Block block) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String hashinput = "";
            for( int i = 0; i < Block.SIZE; i++){
              hashinput = hashinput + block.transactions[i].toString();
            }
            hashinput = hashinput + block.creationDate;
            if( block.hash.equals( new BigInteger(1, md.digest((hashinput+ block.randomNonce).getBytes())))  && block.hash.getLowestSetBit() > Block.DIFFICULTY )
                return true;
            
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    // called when someone else notifies for a new block
    @Override
    public boolean newBlockAnnouncement(Block block, PublicKey minerID) throws RemoteException {
        if(!hashFoundBySomeoneElse && !hashFoundByMe && checkValidity(block))
        {
            hashFoundBySomeoneElse = true;
            block.previousBlock = lastBlock;
            lastBlock = block;
            reset();
            giveMiningReward(minerID);
            return true;
        }
        else return false;
    }

    private void giveMiningReward(PublicKey minerID) {
        Transaction miningReward = new Transaction();
        miningReward.receiver = minerID;
        miningReward.amount = 1;
        miningReward.logicalDate = 0;
        addTransaction(miningReward);
    }

    private void addTransaction(Transaction t) {
        System.out.println(t);
        pendingTransactions[pendingCntr++] = t;
        if(pendingCntr == pendingTransactions.length){
            System.out.println(pendingCntr);
            Block newBlock = new Block();
            newBlock.previousBlock = lastBlock;
            newBlock.transactions = pendingTransactions;
            if(createBlockHash(newBlock) && checkValidity(newBlock)){
                hashFoundByMe = true;
                lastBlock = newBlock;
                announceNewBlock();
                reset();
                giveMiningReward(ID);
            }
        }
    }

    private void reset() {
        pendingTransactions = new Transaction[Block.SIZE];
        pendingCntr = 0;
        hashFoundBySomeoneElse = false;
        hashFoundByMe = false;
    }

    @Override
    public int getChainLength() throws RemoteException {
        int length = 0;
        for(Block cb = lastBlock; cb != null; cb = cb.previousBlock){
            length++;
        }
        return length;
    }
    
    public ArrayList<String> getknownMiners() throws RemoteException{
        return knownMiners;
    }
    
    public void addMiner( String name) throws RemoteException{
        knownMiners.add(name);
    }
}
