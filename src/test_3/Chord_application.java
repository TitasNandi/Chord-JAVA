/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_3;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author titas
 */
public class Chord_application 
{
    public static void main(String[] args)
    {
            
            System.out.println("Welcome to Chord application!");
            Scanner s = new Scanner(System.in);
            int base_port = 5500;
            boolean b = false;
            Clients new_peer=null;
            while(true)
            {
                System.out.println();
                System.out.println("------------------------------------------------------------------------");
                System.out.println("Choose one of the following options!");
                System.out.println(" 1. Node Join\n 2. Print IP Address and Id\n 3. Information of clockwise neighbour\n 4. File key Ids of given node\n 5. Print Finger Table\n 6. Print predecessor\n 0. Exit");
                int option = s.nextInt();
                switch(option)
                {
                    case 0:
                    {
                        b = true;
                        break;
                    }
                    case 1:
                    {
                        System.out.println("Enter node Id"); //0 if first node in network
                        int Id = s.nextInt();
                        new_peer = new Clients("0.0.0.0", Id, base_port+Id);
                        System.out.println("Enter Id of known peer"); //0 if first node in network
                        int peer = s.nextInt();
                        String u = "0.0.0.0 "+peer+" "+(base_port+peer);
                        Node q = new_peer.make_Node(u);
                        if(Id == 0)
                        {
                            ArrayList<Integer> list = new ArrayList<Integer>();
                            for (int i=0; i<100; i++) {
                                list.add(new Integer(i));
                            }
                            Collections.shuffle(list);
                            for(int i=0; i<50; i++)
                            {
                                //System.out.println(list.get(i));
                                new_peer.keys.add(list.get(i));
                            }
                            new_peer.Initialize(null);
                        }
                        else
                        {
                            new_peer.Initialize(q);
                        }
                        Stabilize thread = new Stabilize(new_peer);
                        thread.start();
                        break;
                    }
                    case 2:
                    {
                        System.out.println("Your IP address is "+new_peer.n.IP+" and the Id is "+new_peer.n.Id);
                        break;
                    }
                    case 3:
                    {
                        Node q = new_peer.f[1].node;
                        System.out.println("The IP address of immediate clockwise neighbour is "+q.IP+" and Id is "+q.Id+" and port is "+q.port);
                        break;
                    }
                    case 4:
                    {
                        new_peer.key_transfer();
                        System.out.println("The file key IDs you have are ");
                        if(new_peer.keys.isEmpty())
                        {
                            System.out.println("No keys!");
                            break;
                        }
                        for(int i=0; i<new_peer.keys.size(); i++)
                        {
                            System.out.print(new_peer.keys.get(i)+" ");
                        }
                        System.out.println();
                        break;
                    }
                    case 5:
                    {
                        System.out.println("Your finger table");
                        System.out.println("Start\tInterval  Successor");
                        for(int i=1; i<4; i++)
                        {
                            System.out.println(new_peer.f[i].start+"\t["+new_peer.f[i].interval_start+","+new_peer.f[i].interval_end+")\t  "+new_peer.f[i].node.Id);
                        }
                        break;
                    }
                    case 6:
                    {
                        Node q = new_peer.predecessor;
                        System.out.println("The IP address of predecessor is "+q.IP+" and Id is "+q.Id+" and port is "+q.port);
                        break;
                    }
 
                }
                if(b)
                {
                    break;
                }
            }
            System.out.println("Node "+new_peer.Id+" exited");
        }
    }
