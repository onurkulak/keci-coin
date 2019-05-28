package blockchain;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.PublicKey;
import java.util.Scanner;
import java.util.UUID;
import java.security.*;
import java.util.HashMap;
import java.rmi.*;

import java.rmi.registry.*;
/**
 *
 * @author onur
 */
public class Test{
    public static void main(String args[]){
        HashMap<String, KeyPair> nameKey = new HashMap<String, KeyPair>();
        HashMap<String, Client> nameClient = new HashMap<String, Client>();
        Miner firstMiner = null;
        Miner miner;
        try{
        //Registry reg = LocateRegistry.createRegistry(2121);
        Scanner in = new Scanner(System.in);
        boolean exit = false;
        String input;
        int counter = 0;
        do{
            System.out.println( "To create miner, type 'miner'");
            System.out.println( "To create client, type 'client'");
            System.out.println( "To send money, type 'send'");
            System.out.println( "To get balance, type 'balance'");
            System.out.println( "To get chain legth, type 'chain'");
            System.out.println( "To quit, type 'quit'");
            input = in.nextLine();
            String sender;
            String receiver;
            double transferAmount;
            
            switch(input){
                case "miner":
                    try{
                        KeyPair kp = generateKeyPair();
                        System.out.println( "1");
                        if( counter == 0){
                            firstMiner = new Miner( kp.getPublic());
                            System.out.println( "2");
                            String name = ">globalMiner";// "M-" + kp.getPublic(); //UUID.randomUUID().toString();
                            System.out.println( "3");
                            Client c = createClient( name, kp);
                            nameKey.put( name, kp);
                            nameClient.put( name, c);
                            System.out.println( "Miner with name " + name + " created");
                            
                        }
                        else{
                            miner = new Miner(kp.getPublic());
                        System.out.println( "2");
                        String name = "M-" + kp.getPublic(); //UUID.randomUUID().toString();
                        System.out.println( "3");
                        Client c = createClient( name, kp);
                        nameKey.put( name, kp);
                        nameClient.put( name, c);
                            System.out.println( "Miner with name " + name + " created");
                            
                        }
                        counter++;
                    } catch( Exception e){
                        System.out.println( "exception: " + e);
                    }
                    break;
                case "client":
                    System.out.println( "Name for client: ");
                    sender = in.nextLine();
                    try{
                        KeyPair kp2 = generateKeyPair();
                        Client c3 = createClient( sender, kp2);
                        nameKey.put( sender, kp2);
                        nameClient.put( sender, c3);
                    } catch(Exception e){}
                    break;
                case "send":
                    System.out.println( "Name of sender:");
                    sender = in.nextLine();
                    System.out.println( "Name of receiver: ");
                    receiver = in.nextLine();
                    System.out.println( "Amount to be transferred (double): ");
                    transferAmount = in.nextInt();
                    PublicKey senderPublicKey = nameKey.get(sender).getPublic();
                    PublicKey receiverPublicKey = nameKey.get(receiver).getPublic();
                    Client c1 = nameClient.get(sender);
                    if(c1 == null || receiverPublicKey == null)
                    {
                        System.out.println("one of the user names do not exist in the system");
                        break;
                    }
                    c1.sendMoney( receiverPublicKey, transferAmount);
                    break;
                case "balance":
                    System.out.println( "Name: ");
                    String name2 = in.nextLine();
                    Client c2 = nameClient.get(name2);
                    if(c2 == null)
                    {
                        System.out.println("user name does not exist in the system");
                        break;
                    }
                    double balance = c2.getBalance();
                    System.out.println( "Balance: " + balance);
                    break;
                case "chain":
                    try{
                        int chainLength = firstMiner.getChainLength();
                        System.out.println( "Chain length: " + chainLength);
                    } catch(Exception e){}
                    
                    break;
                case "quit":
                    exit = true;
                    return;
            }
        } while( !exit);
        }catch(Exception e )
        {}
        return;
    }
    
    public static Client createClient( String name, KeyPair kp){
        Client client = new Client( name, kp);
        System.out.println( "***");
        return client;
    }
    
    private static KeyPair generateKeyPair() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize( 2048, new SecureRandom());
        KeyPair pair = generator.generateKeyPair();
        
        return pair;
    }
}

/*
 test
 
 miner oluşturma
 client ol.
 para yollama
 getbalance
 blok uzunluğu -> minerlardan
 */
