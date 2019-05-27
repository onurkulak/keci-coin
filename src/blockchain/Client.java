/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.util.HashMap;
import java.security.*;
import java.util.Base64;
import java.util.Date;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.rmi.registry.*;

/**
 *
 * @esra will take care of this part :d
 * use Signature.getInstance("SHA256withRSA")
 * 
 * 
 */
public class Client{
    //HashMap<String, PublicKey> nameKey;// = new HashMap<String, PublicKey>();
    double balance;
    MinerInterface firstMiner; //INITIALIZE
    //Client firstClient;
    KeyPair ownKeyPair;
    ArrayList<String> knownMiners;
    Signature signature;
    Registry reg;
    public static void main(){
        return;
    }
    
    public Client( String name, KeyPair kp){
        super();
        firstMiner = null;
        try{
            reg = LocateRegistry.getRegistry(2323);
            firstMiner = (MinerInterface) reg.lookup("globalMiner");
        }catch (Exception e){
            System.out.println(e);
            
        }
        
        balance = 0.0;
        try{
        knownMiners = firstMiner.getknownMiners();
        }catch(Exception e){
            System.out.println("known Miners fail "+e);
        }
        ownKeyPair = kp;
        try{
            signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(kp.getPrivate());
        }
        catch( Exception e){}
        
        MinerInterface temp;
        for( String minerName: knownMiners){
            try{
                if( minerName != null )
                {
                byte byteData = 0;
                temp = (MinerInterface) reg.lookup(minerName);
                temp.register( ownKeyPair.getPublic(), 10.0, byteData) ;
                    
                }
            } catch( Exception e) {
                System.out.println("Register fail "+ e);
            }
        }
    }
    
    //creates client
    public boolean createClient( String name){
        try{
            KeyPair kp = generateKeyPair();
            Client c = new Client( name, kp);
        } catch( Exception e){}
        
        return true;
    }
    
    
    public boolean createClient( String name, KeyPair kp){
        //Miner firstMiner;
        Client c = new Client( name, kp);
        return true;
    }
    
    //send money to another client
    public void sendMoney( PublicKey receiverPublicKey, double transferAmount){
        byte[] message = new byte[264];
        byte[] keyBytes = ownKeyPair.getPublic().getEncoded();
        for( int i = 0; i < 256; i++){
            message[i] = keyBytes[i];
        }
        
        byte[] doubleBytes = new byte[8];
        ByteBuffer.wrap(doubleBytes).putDouble(transferAmount);
        for( int i = 256; i < 264; i++){
            message[i] = doubleBytes[i-256];
        }

        MinerInterface temp;
        for( String minerName: knownMiners){
            try{
                signature.update(message);
                byte[] sign = signature.sign();
                temp = (MinerInterface) reg.lookup(minerName);
                temp.sendMoney( ownKeyPair.getPublic(), sign, message); //????
            } catch( Exception e) {}            
        }
    }
    
    /*
     To get balance, client uses RMI on each miner to get its balance on that miner; but only the account value from the longest chain is considered as valid.
     */
    //public double getBalance(PublicKey userPublicKey)
    public double getBalance(){
        double balance = 0.0;
        MinerInterface temp = null;
        int maxChainLength = 0;
        MinerInterface maxMiner = null;
        
        try{
            for( String minerName: knownMiners){
                temp = (MinerInterface) reg.lookup(minerName);
                if( temp.getChainLength() > maxChainLength){
                    maxChainLength = temp.getChainLength();
                    maxMiner = temp;
                }
            }
            balance = maxMiner.getBalance(ownKeyPair.getPublic());
        } catch( Exception e) {}
        return balance;
    }
    
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize( 2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        
        return pair;
    }
}

//To get balance, client uses RMI on each miner to get its balance on that miner; but only the account value from the longest chain is considered as valid.
//All the operations in our system between miners and clients use RM.

