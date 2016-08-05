/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_3;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
/**
 *
 * @author titas
 */
public class Listener extends Thread
{
    ServerSocket server;
    int port;
    Clients own;
    public Listener(int port, Clients own)
    {
        try {
            this.own = own;
            this.port = port;
            server = new ServerSocket(port);
        } catch (IOException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void run()
    {
        try {
            Socket clientSocket;
            while((clientSocket = server.accept()) != null)
            {
                Scanner s = new Scanner(clientSocket.getInputStream());
                PrintWriter p = new PrintWriter(clientSocket.getOutputStream());
                String line = s.nextLine();
              //  System.out.println(line+" @");
                String[] str = line.split(" ");
                ArrayList<String> l = new ArrayList<>();
                for(int i=0; i<str.length; i++)
                {
                    if(str[i].length()!=0)
                    {
                        l.add(str[i]);
                    }
                }
                switch(l.get(0))
                {
                    case "cpf":
                    {
                        int Id = Integer.parseInt(l.get(1));
                        //System.out.println(Id);
                        Node n = own.closest_preceding_finger(Id);
                        p.println(make_String(n));
                        p.flush();
                        break;
                    }
                    case "fs":
                    {
                        int Id = Integer.parseInt(l.get(1));
                        Node n = own.find_successor(Id);
                     //   System.out.println(make_String(n));
                        p.println(make_String(n));
                        p.flush();
                        break;
                    }
                    case "pred":
                    {
                        Node n = own.predecessor;
                        p.println(make_String(n));
                        p.flush();
                        break;
                    }
                    case "asspre":
                    {
                        String y="";
                        for(int i=1; i<l.size(); i++)
                        {
                            y = y + l.get(i)+" ";
                        }
                     //   System.out.println(y+" ^");
                        Node n = own.make_Node(y);
                      //  System.out.println(n.Id);
                        own.predecessor = n;
                     //   System.out.println(own.predecessor.Id);
                        p.println("");
                        p.flush();
                        break;
                    }
                    case "not":
                    {
                        String y="";
                        for(int i=1; i<l.size(); i++)
                        {
                            y = y + l.get(i)+" ";
                        }
                        Node n = own.make_Node(y);
                        own.notify(n);
                        p.println("");
                        p.flush();
                        break;
                    }
                    case "succ":
                    {
                        Node n = own.f[1].node;
                        p.println(make_String(n));
                        p.flush();
                        break;
                    }
                    case "uft":
                    {
                        String y="";
                        for(int i=1; i<l.size()-1; i++)
                        {
                            y = y + l.get(i)+" ";
                        }
                        Node n = own.make_Node(y);
                        own.update_finger_table(n, Integer.parseInt(l.get(l.size()-1)));
                        p.println("");
                        p.flush();
                        break;
                    }
                    case "kt":
                    {
                        int start = Integer.parseInt(l.get(1));
                        int end = Integer.parseInt(l.get(2));
                        int flag = Integer.parseInt(l.get(3));
                        String key_list = "";
                        for(int i=0; i<own.keys.size(); i++)
                        {
                            if(flag == 0)
                            {
                                if((own.keys.get(i)%8) >= start && (own.keys.get(i)%8) <= end)
                                {
                                    key_list+= own.keys.get(i)+" ";
                                    own.keys.remove(i);
                                    i--;
                                }
                            }
                            else
                            {
                                if((own.keys.get(i)%8) >= start || (own.keys.get(i)%8) <= end)
                                {
                                    key_list+= own.keys.get(i)+" ";
                                    own.keys.remove(i);
                                    i--;
                                }
                            }
                        }
                        p.println(key_list);
                        p.flush();
                        break;
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(Listener.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public String make_String(Node x)
    {
        String q = x.IP+" "+x.Id+" "+x.port;
        return q;
    }
}
