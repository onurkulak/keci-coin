/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

import java.util.Date;
import java.security.PublicKey;
/**
 *
 * @author onur
 */
public class Transaction {
    PublicKey sender, receiver;
    double amount;
    Date date;
}
