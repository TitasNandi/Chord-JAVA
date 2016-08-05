/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_3;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author titas
 */
public class Stabilize extends Thread{
    Clients c;
    public Stabilize(Clients c)
    {
        this.c = c;
    }
    public void run()
    {
        while(true)
        {
            
            c.Stabilize();
            c.fix_fingers();
            try {
                Thread.sleep(3000);
                } catch (InterruptedException ex) {
            Logger.getLogger(Stabilize.class.getName()).log(Level.SEVERE, null, ex);
        }
        }
    }
}
