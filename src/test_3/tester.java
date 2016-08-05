/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test_3;

/**
 *
 * @author titas
 */
public class tester {
    public static void main(String[] args)
    {
        q in = new q(5, 10);
        q in2 = new q(7, 12);
        in = in2;
        q in3 = in2;
        System.out.println(in3.a+" "+in.b);
        String p ="";
        System.out.println(p.length());
    }
}

class q
{
    int a;
    int b;
    public q(int a, int b)
    {
        this.a = a;
        this.b = b;
    }
}