/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.util.Date;
import java.security.PublicKey;
import java.util.Base64;
/**
 *
 * @author onur
 */
public class Transaction {
    PublicKey sender, receiver;
    double amount;
    byte logicalDate;
    
    @Override
    public String toString(){
        String senderString = sender == null ? "none" : Base64.getEncoder().encodeToString(sender.getEncoded());
        String rcvString = Base64.getEncoder().encodeToString(receiver.getEncoded());
        return "Amount:\t" + amount + "\nFrom:\t" + senderString + "\nTo:\t" + rcvString;
    }
}
