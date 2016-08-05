/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_3;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
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
public class Clients 
{
    Node predecessor = new Node(this.IP, this.Id, this.port);
    Scanner s;
    PrintWriter p;
    String IP;
    int Id;
    ArrayList<Integer> keys;
    finger[] f;
    int port;
    Node n;
    public Clients(String IP, int Id, int port)
    {
        this.IP = IP;
        this.Id = Id;
        this.port = port; 
        f = new finger[4];
        keys = new ArrayList<>();
        n = new Node(IP, Id, port);   
    }
    public void Initialize(Node s)
    {
        Listener l = new Listener(this.port, this);
        l.start();
        for(int i=1; i<4; i++)
        {
            f[i] = new finger();
            int z = (int)Math.pow(2, i-1);
            f[i].start = (this.n.Id + z)%8;
            f[i].interval_start = (this.n.Id + z)%8;
            f[i].interval_end = (f[i].interval_start + z)%8;
            f[i].node = new Node(this.IP, this.Id, this.port);
        }
        if(s != null)
        {
            Join(s);
        }
        else
        {
            predecessor = this.n;
            f[1].node = this.n;
        }
       // Node_join(s);
    }
    public void key_transfer()
    {
        int start = (this.predecessor.Id)+1;
        int end = this.n.Id;
        int flag = 0;
        if(start <= end)
        {
            flag = 0;
        }
        else
        {
            flag = 1;
        }
        String l = start+" "+end+" "+flag;
        try {    
            Socket sock = new Socket(this.f[1].node.IP, this.f[1].node.port);
            s = new Scanner(sock.getInputStream());
            p = new PrintWriter(sock.getOutputStream());
            p.println("kt "+l);
            p.flush();
            String str = s.nextLine();
            String[] st = str.split(" ");
            for(int i=0; i<st.length; i++)
            {
                if(st[i].length()!=0)
                {
                    this.keys.add(Integer.parseInt(st[i]));
                }
            }
            Collections.sort(this.keys);
            sock.close();
        } catch (IOException ex) {
            Logger.getLogger(Clients.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    public void Node_join(Node s)
    {
        if(s != null)
        {
            Init_finger_table(s);
            update_others();
         //   System.out.println(predecessor.Id);
            key_transfer();
        }
        else
        {
            for(int i=1; i<=3; i++)
            {
                f[i].node = this.n;   
            }
            
            predecessor = this.n;
        }
    }
    public void Init_finger_table(Node s)
    {
        f[1].node = Node_Query(s.IP, s.port, "fs "+f[1].start);
       // System.out.println(f[1].node.Id+" (");
        this.predecessor = Node_Query(f[1].node.IP, f[1].node.port, "pred");
       // System.out.println(predecessor.Id+" )");
        String encode = this.n.IP+" "+this.n.Id+" "+this.n.port;
        Node_Query(f[1].node.IP, f[1].node.port, "asspre "+encode);
        for(int i=1; i<3; i++)
        {
         //   System.out.println(f[i].node.Id);
            if(n.Id < f[i].node.Id)
            {
                if(f[i+1].start >= this.n.Id && f[i+1].start < f[i].node.Id)
                {
                    f[i+1].node = f[i].node;
                } 
            }
            else if(n.Id == f[i].node.Id)
            {
                f[i+1].node = f[i].node;
            }
            else if(n.Id > f[i].node.Id)
            {
           //     System.out.println(f[i+1].start+" >");
                if(f[i+1].start >= this.n.Id || f[i+1].start < f[i].node.Id)
                {
                    f[i+1].node = f[i].node;
                } 
            }
            else
            {
             //   System.out.println(f[i+1].start);
                f[i+1].node.Id = (Node_Query(s.IP, s.port, "fs "+f[i+1].start)).Id;
            }
        }
    }
    public void update_others()
    {
      //  System.out.println("check");
        for(int i=1; i<=3; i++)
        {
            int l = (int)Math.pow(2, i-1);
         //   System.out.println(l+" "+n.Id);
            Node p = find_predecessor((this.n.Id - l+8)%8);
            if(p != this.n)
            {
                String encode = this.n.IP+" "+this.n.Id+" "+this.n.port+" "+i;
                Node_Query(p.IP, p.port, "uft "+encode);
            }
        }
    }
    public void update_finger_table(Node s, int i)
    {
        int id = f[i].node.Id;
        boolean flag = false;
        if(id == n.Id)
        {
            f[i].node = s;
        //    System.out.println(f[i].node.Id+" !");
            flag = true;
        }
        else if(n.Id < f[i].node.Id)
        {
            if(s.Id >= this.n.Id && s.Id < f[i].node.Id)
            {
                f[i].node = s;
          //      System.out.println(f[i].node.Id+" !");
                flag = true;
            } 
        }
        else if(n.Id == f[i].node.Id)
        {
            f[i].node = s;
        //    System.out.println(f[i].node.Id+" !");
            flag = true;
        }
        else if(n.Id > f[i].node.Id)
        {
            if(s.Id >= this.n.Id || s.Id < f[i].node.Id)
            {
                f[i].node = s;
          //      System.out.println(f[i].node.Id+" !");
                flag = true;                
            } 
        }
        if(flag == true && predecessor!=this.n && predecessor!=s)
        {
            Node p = predecessor;
            String encode = s.IP+" "+s.Id+" "+s.port+" "+i;
            Node_Query(p.IP, p.port, "uft "+encode);
        }
    }
    public Node closest_preceding_finger(int Id)
    {
      //  System.out.println("hello");
        for(int i=3; i>0; i--)
        {
            if(n.Id < Id)
            {
                if(f[i].node.Id > this.n.Id && f[i].node.Id < Id)
                {
                    return f[i].node;
                }
            }
            else if(n.Id == Id)
            {
                return f[i].node;
            }
            else if(n.Id > Id)
            {
                if(f[i].node.Id > this.n.Id || f[i].node.Id < Id)
                {
               //     System.out.println(f[i].node.Id+" +");
                    return f[i].node;
                }
            }           
        }
        return this.n;
    }
    public Node find_successor(int Id)
    {
        Node n1 = find_predecessor(Id);
        Node succ = Node_Query(n1.IP, n1.port, "succ");
     //   System.out.println(succ.Id+" *");
        return succ;
    }
    public Node find_predecessor(int Id)
    {
       Node n1 = this.n;
       Node p = f[1].node;
    //    System.out.println(p.Id);
       while(true)
       {
           if(n1.Id == n.Id)
           {
           //    System.out.println("go");
               if(n1.Id < p.Id)
               {
                   if(Id <= n1.Id || Id > f[1].node.Id)
                   {
                       n1 = closest_preceding_finger(Id);
                       if(n1.Id == n.Id)
                           break;
                   }
                   else
                   {
                       break;
                   }
               }
               else if(n1.Id == p.Id)
               {
                   break;
               }
               else if(n1.Id > p.Id)
               {
             //      System.out.println("gone");
                   if(Id <= n1.Id && Id > f[1].node.Id)
                   {
                       n1 = closest_preceding_finger(Id);
                       if(n1.Id == n.Id)
                           break;
                   }
                   else
                   {
                       break;
                   }
               }
           }
           else
           {
               Node x = Node_Query(n1.IP, n1.port, "succ");
               if(n1.Id < x.Id)
               {
                   if(Id <= n1.Id || Id > x.Id)
                   {
                       n1 = Node_Query(n1.IP, n1.port, "cpf "+Id);
                       if(n1.Id == n.Id)
                           break;
                   }
                   else
                   {
                       break;
                   }
               }
               else if(n1.Id == x.Id)
               {
                   break;
               }
               else if(n1.Id > p.Id)
               {
                   if(Id <= n1.Id && Id > x.Id)
                   {
                       n1 = Node_Query(n1.IP, n1.port, "cpf "+Id);
                       if(n1.Id == n.Id)
                           break;
                   }
                   else
                   {
                       break;
                   }
               }
               else
               {
                   break;
               }
           }
       }
       return n1;
    }
    public void Join(Node s)
    {
        predecessor = null;
        f[1].node = Node_Query(s.IP, s.port, "fs "+this.n.Id);
    }
    public void Stabilize()
    {
        Node x = Node_Query(f[1].node.IP, f[1].node.port, "pred");
        Node p = f[1].node;
        if(n.Id < p.Id)
        {
            if(x.Id > this.n.Id && x.Id < p.Id)
            {
                f[1].node = x;
            }
        }
        else if(n.Id == p.Id)
        {
            f[1].node = x;
        }
        else
        {
            if(x.Id > this.n.Id || x.Id < f[1].node.Id)
            {
                f[1].node = x;
            }
        }
        String encode = this.n.IP+" "+this.n.Id+" "+this.n.port;
        Node_Query(f[1].node.IP, f[1].node.port, "not "+encode);
    }
    public void notify(Node s)
    {
        if(predecessor != null)
        {
            if(predecessor.Id < n.Id)
            {
                if(s.Id > predecessor.Id && s.Id < n.Id)
                {
                    predecessor = s;
                }
            } 
            else if(predecessor.Id == n.Id)
            {
                predecessor = s;
            }
            else
            {
                if(s.Id > predecessor.Id || s.Id < n.Id)
                {
                    predecessor = s;
                }
            }
        }
        else
        {
            predecessor = s;
        }
    }
    public void fix_fingers()
    {
      for(int i=1; i<=3; i++)
      {
          f[i].node = find_successor(f[i].start);
      }
    }
    public Node Node_Query(String IP, int port, String msg)
    {
        if(port == this.port)
        {
         //   System.out.println(port+" # "+msg);
            String[] str = msg.split(" ");
            ArrayList<String> str_1 = new ArrayList<>();
            for(int i=0; i<str.length; i++)
            {
                if(str[i].length()!=0)
                {
                    str_1.add(str[i]);
                }
            }
            switch(str_1.get(0))
            {
                case "cpf":
                {
                    
                    return closest_preceding_finger(Integer.parseInt(str_1.get(1)));
                }
                case "succ":
                {
                    return f[1].node;
                }
                case "asspre":
                {                    
                    predecessor = this.n;
                }
                case "pred":
                {
                    return predecessor;
                }
                case "uft":
                {
                    String y="";
                    for(int i=1; i<str_1.size()-1; i++)
                    {
                        y = y + str_1.get(i)+" ";
                    }
                //    System.out.println(y+" #");
                    Node n = make_Node(y);
                    update_finger_table(n,Integer.parseInt(str_1.get(str_1.size()-1)));
                    
                }
            }
        }
        try {
            Socket sock = new Socket(IP, port);
            s = new Scanner(sock.getInputStream());
            p = new PrintWriter(sock.getOutputStream());
            p.println(msg);
        //    System.out.println(msg+" &");
            p.flush();
            String str = s.nextLine();
         //   System.out.println(str);
            Node q=null;
            if(str.length()!=0)
            {
               q = make_Node(str); 
            }
            sock.close();
            return q;
        } catch (IOException ex) {
            Logger.getLogger(Clients.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return null;
    }
    
    public Node make_Node(String s)
    {
        String[] str = s.split(" ");
        String[] str_1 = new String[3];
        int k = 0;
        for(int i=0; i<str.length; i++)
        {
            if(str[i].length()!=0)
            {
                str_1[k] = str[i];
                k++;
            }
        }
        String IP = str_1[0];
        int Id = Integer.parseInt(str_1[1]);
        int port = Integer.parseInt(str_1[2]);
        Node l = new Node(IP, Id, port);
        return l;
    }
}


class finger
{
    int start;
    int interval_start;
    int interval_end;
    Node node;
}

class Node
{
    String IP;
    int Id;
    int port;
    public Node(String IP, int Id, int port)
    {
        this.IP = IP;
        this.Id = Id;
        this.port = port;
    }
            
}