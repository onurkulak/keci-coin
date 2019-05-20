/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blockchain;

// not sure whether the hash result is actually a key..
import java.util.Date;
/**
 *
 * @author onur
 */
public class Block {
    Block previousBlock;
    static final int SIZE = 4;
    // not sure about the type, sha256 seems to return an int
    int hash;
    
    // this part can be changed, Sami's part
    int randomNonce;
    Transaction[] transactions;
    
    Date creationDate;
}
