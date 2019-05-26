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

/**
 * assume miners are completely separate entities from clients, they don't make transactions
 * @author onur
 */
public class Miner extends UnicastRemoteObject implements MinerInterface{
    
    PublicKey ID;
    Block lastBlock;
    Transaction[] pendingTransactions;
    int pendingCntr;
    volatile boolean hashFoundBySomeoneElse;
    ArrayList<String> knownMiners;
    
    protected Miner() throws RemoteException {
        super();
        pendingTransactions = new Transaction[Block.SIZE];
        pendingCntr = 0;
        lastBlock = null;
        hashFoundBySomeoneElse = false;
        knownMiners = new ArrayList<>();
    }
    
    @Override
    public double getBalance(PublicKey userPublicKey) throws RemoteException {
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
    public boolean sendMoney(PublicKey senderPublicKey, byte[] sign, byte[] message) throws RemoteException {
        try {
            Signature verifier = Signature.getInstance("SHA256withRSA");
            verifier.initVerify(senderPublicKey);
            verifier.update(message);
            // sign is correct
            if(verifier.verify(sign)){
                int publicKeyByteSize = message.length - 8;
                byte[] encodedPublicKey = new byte[publicKeyByteSize];
                byte[] encodedAmount = new byte[8];
                System.arraycopy(message, 0, encodedPublicKey, 0, encodedPublicKey.length);
                System.arraycopy(message, encodedPublicKey.length, encodedAmount, 0, 8);
                
                PublicKey receiver =  KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(encodedPublicKey));
                double amount = ByteBuffer.wrap(encodedAmount).getDouble();
                if(getBalance(senderPublicKey)>=amount){
                    Transaction transaction = new Transaction();
                    transaction.amount = amount;
                    transaction.receiver = receiver;
                    transaction.sender = senderPublicKey;
                    transaction.date = new Date();
                    addTransaction(transaction);
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
    public boolean register(PublicKey senderPublicKey, double initialBalance)throws RemoteException  {
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
        return successful;
    }
    
    private void announceNewBlock() {
        Miner temp;
        for(String minerName: knownMiners)
        {
            try {
                temp = (Miner)Naming.lookup(minerName);
                temp.newBlockAnnouncement(lastBlock, ID);
            } catch (NotBoundException ex) {
                Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (MalformedURLException ex) {
                Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
            } catch (RemoteException ex) {
                Logger.getLogger(Miner.class.getName()).log(Level.SEVERE, null, ex);
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
        if(checkValidity(block))
        {
            hashFoundBySomeoneElse = true;
            block.previousBlock = lastBlock;
            lastBlock = block;
            reset();
            giveMiningReward(minerID, block);
            return true;
        }
        else return false;
    }

    private void giveMiningReward(PublicKey minerID, Block block) {
        Transaction miningReward = new Transaction();
        miningReward.receiver = minerID;
        miningReward.amount = 1;
        miningReward.date = block.creationDate;
        addTransaction(miningReward);
    }

    private void addTransaction(Transaction t) {
        pendingTransactions[pendingCntr++] = t;
        if(pendingCntr == pendingTransactions.length){
            Block newBlock = new Block();
            newBlock.previousBlock = lastBlock;
            newBlock.transactions = pendingTransactions;
            if(createBlockHash(newBlock)){
                lastBlock = newBlock;
                announceNewBlock();
                reset();
                giveMiningReward(ID, lastBlock);
            }
        }
    }

    private void reset() {
        pendingTransactions = new Transaction[Block.SIZE];
        pendingCntr = 0;
        hashFoundBySomeoneElse = false;    
    }
}
