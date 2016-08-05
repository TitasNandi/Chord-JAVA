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
import java.util.*;
import java.io.*;


public class labAssign6
{
	public static void main(String args[])
	{
		/*
		* Enter details in a file in the following format line by line:
		* Number of sources
                * The bandwidth from switch to sink (in Mbps)
                * The packet size (in Mb)
                * Drop probability;
		* For each source, the bandwidth to the switch in a line
		* Simulation time in seconds
		* Switch Queue size
		* Sink Queue size
		*/
                Scanner s = new Scanner(System.in);
                int num_sources;
                double packet_size;
                double band_switch;
                double sim_time;
                int swi_queue;
                int sink_queue;
                double drop_prob;
                Comparator_func comp = new Comparator_func();
		PriorityQueue<packet> p = new PriorityQueue<>(comp);
                num_sources = s.nextInt();
                source[] sou_arr = new source[num_sources];
                band_switch = s.nextDouble();
                packet_size = s.nextDouble();
                drop_prob = s.nextDouble();
                for(int i=0; i<num_sources; i++)
                {
                    double rand = myRandom();
                    double band_source = s.nextDouble();
                    double RTT = (packet_size/band_source)+(packet_size/band_switch);
                    sou_arr[i] = new source(i, rand, rand, band_source, RTT);
                    for(int j=0; j<sou_arr[i].cwnd; j++)
                    {
                        packet l = new packet(i, j, rand, false);
                        double t = Math.random();
                        if(t >= drop_prob)
                        {
                            p.add(l);
                            p.add(new packet(i, j, 4*sou_arr[i].RTT, true));
                            System.out.println("Packet "+j+" of source "+i+ " generated at "+rand);
                        }
                    }
                }
                sim_time = s.nextDouble();
                swi_queue = s.nextInt();
                sink_queue = s.nextInt();
                router r = new router(band_switch, packet_size, swi_queue);
                sink t = new sink(band_switch, packet_size, sink_queue);
                simulate(sim_time, sou_arr, r, p, t, drop_prob);
	}
        static void simulate(double sim_time, source[] sou, router r, PriorityQueue<packet> p, sink t, double drop_prob)
        {
            while(!p.isEmpty())
            {
                packet pre_packet = p.poll();
                int s_id = pre_packet.s_id;
                double pre_time = pre_packet.timestamp;
                if(pre_packet.timestamp > sim_time)
                {
                    System.out.println("Simulation over");
                    break;
                }
                source pre_source = sou[s_id];
                AIMD aimd = new AIMD(sou);
            switch(pre_packet.status)
            {
                case 0:
                {
                    state0(pre_packet, pre_source, p, r, aimd, sim_time, drop_prob);
                    break;
                }
                case 1:
                {
                    state1(pre_packet, pre_source, p, r, pre_time);
                    break;
                }
                case 2:
                {
                    state2(pre_packet, pre_source, p, r);
                    break;
                }
                case 3:
                {
                    state3(pre_packet, pre_source, p, r, t, pre_time, aimd);
                    break;
                }
                case 4:
                {
                    state4(pre_packet, pre_source, p, r, t, pre_time);
                    break;
                }
                case 5:
                {
                    state5(pre_packet, pre_source, p, r, t, pre_time);
                    break;
                }
                case 6:
                {
                    state6(pre_packet, pre_source, p, r, t, pre_time);
                    break;
                }
                case 7:
                {
                    state7(pre_packet, pre_source, p, r, t, pre_time, aimd, sim_time, drop_prob);
                    break;
                }
                case 8:
                {
                    state8(pre_packet, pre_source, p, r, t, pre_time);
                    break;
                }
            }
            }
        }
        static void state0(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r, AIMD aimd, double sim_time, double drop_prob)
        {
            if(pre_packet.isTimeout == true)
            {
                aimd.handleTimeout(pre_packet, pre_source);
                pre_source.next(p, pre_packet.timestamp, sim_time, drop_prob);
                return;
            }
            if(pre_source.queue_size > 0)
            {
                pre_packet.status = 2;
                pre_packet.timestamp = pre_source.finish;
                p.add(pre_packet);
                pre_source.queue_size++;
                System.out.println("Packet "+pre_packet.p_id+ " queued at source "+pre_packet.s_id+ " at "+ pre_packet.timestamp);
            }
            else if(pre_source.transmit == false)
            {
                pre_packet.status = 1;
                pre_source.transmit = true;
                double trans = (r.packet_size/pre_source.bandwidth);
                pre_packet.timestamp += trans;
                pre_source.finish = pre_packet.timestamp;
                p.add(pre_packet);
                System.out.println("Packet "+pre_packet.p_id+ " of source "+pre_packet.s_id+ " transmitted to switch at "+ pre_packet.timestamp);
            }
            else
            {
                pre_packet.status = 2;
                pre_packet.timestamp = pre_source.finish;
                p.add(pre_packet);
                pre_source.queue_size++;
                System.out.println("Packet "+pre_packet.p_id+ " queued at source "+pre_packet.s_id+ " at "+ pre_packet.timestamp);	
            }
        }
        static void state1(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r, double pre_time)
        {
            pre_source.transmit = false;
            if(!r.in_que.isEmpty())
            {
                if(r.in_que.size() >= r.queue_size)
                {
                    System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " dropped at switch at "+ pre_packet.timestamp);
                    return;
                }
                pre_packet.status = 4;
                pre_packet.timestamp = r.finish;
		pre_packet.change_time = pre_time;
                r.in_que.add(pre_packet);
                p.add(pre_packet);
                System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " queued at switch at "+ pre_packet.timestamp);
            }
            else if(r.transmit == false)
            {
                r.transmit = true;
                pre_packet.status = 3;
                double trans = (r.packet_size/r.bandwidth_out);
                pre_packet.timestamp += trans;
                pre_packet.change_time = pre_time;
                r.finish = pre_packet.timestamp;
                p.add(pre_packet);
                System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " transmitted to sink at "+ pre_packet.timestamp);
            }
            else
            {
                pre_packet.status = 4;
		pre_packet.timestamp = r.finish;
		pre_packet.change_time = pre_time;
		p.add(pre_packet);
		r.in_que.add(pre_packet);
		System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " queued at switch at "+ pre_packet.timestamp);
            }
        }
        static void state2(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r)
        {
                if(pre_source.transmit == false)
		{
			pre_packet.status = 1;
			pre_packet.timestamp += (r.packet_size/pre_source.bandwidth);
			pre_source.finish = pre_packet.timestamp;
			pre_source.transmit = true;
                        pre_source.queue_size--;
			System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " transmitted to switch at "+ pre_packet.timestamp);
		}
		else
		{
			pre_packet.timestamp = pre_source.finish;
			System.out.println("Packet "+pre_packet.p_id+ " queued at source "+pre_packet.s_id+ " at "+ pre_packet.timestamp);
		}
		p.add(pre_packet);
        }
        static void state3(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r, sink s, double pre_time, AIMD aimd)
        {
            aimd.resp_at_sink(pre_packet, pre_source, s);
            r.transmit = false;
            if(!s.queue.isEmpty())
            {
                if(s.queue.size() >= s.queue_size)
                {
                   System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " dropped at sink at "+ pre_packet.timestamp);
                   return;
                }
                pre_packet.status = 6;
                pre_packet.timestamp = s.finish;
                pre_packet.change_time = pre_time;
                s.queue.add(pre_packet);
                System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " queued at sink at "+ pre_packet.timestamp);
            }
            else if(s.transmit == false)
            {
                pre_packet.status = 5;
                s.transmit = true;
                double trans = (r.packet_size/s.bandwidth);
                pre_packet.timestamp += trans;
                s.finish = pre_packet.timestamp;
                System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " transmitted back to switch at "+ pre_packet.timestamp);
            }
            else
            {
		pre_packet.status = 6;
		pre_packet.timestamp = s.finish;
		pre_packet.change_time = pre_time;
		s.queue.add(pre_packet);
		System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " queued at sink at "+ pre_packet.timestamp);
            }
            p.add(pre_packet);
        }
        static void state4(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r, sink s, double pre_time)
        {
            if(r.transmit == false)
		{
			packet l = r.in_que.poll();
			pre_packet.status = 3;
			pre_packet.timestamp += (r.packet_size/r.bandwidth_out);
			pre_packet.change_time = pre_time;
			r.finish = pre_packet.timestamp;
			r.transmit = true;
			System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " transmitted to sink at "+ pre_packet.timestamp);
		}
		else
		{
			pre_packet.timestamp = r.finish;
			pre_packet.change_time = pre_time;
			System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " queued at switch at "+ pre_packet.timestamp);
		}
		p.add(pre_packet);
        }
        static void state5(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r, sink s, double pre_time)
        {
                s.transmit = false;
		//System.out.println("\n"+pre_source.s_id+" transmit false \n");
		if(!r.out_que.isEmpty())
		{
			if(r.out_que.size()>=r.queue_size)
			{
				System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " dropped at switch at "+ pre_packet.timestamp);
				return;
			}
			pre_packet.status = 8;
			pre_packet.timestamp = r.finish_back;
			pre_packet.change_time = pre_time;
			r.out_que.add(pre_packet);
			p.add(pre_packet);
			System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " queued on return at switch at "+ pre_packet.timestamp);
		}
		else if(r.transmit_back == false)
		{
			pre_packet.status = 7;
			r.transmit_back = true;
			double trans = (r.packet_size/pre_source.bandwidth);
			pre_packet.change_time = pre_time;
			pre_packet.timestamp += trans;
			r.finish_back = pre_packet.timestamp;
			p.add(pre_packet);
			System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " transmitted back to source at "+ pre_packet.timestamp);
				
		}
		else
		{
			pre_packet.status = 8;
			pre_packet.timestamp = r.finish_back;
			pre_packet.change_time = pre_time;
			p.add(pre_packet);
			r.out_que.add(pre_packet);
			System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " queued on return at switch at "+ pre_packet.timestamp);
		}
        }
        static void state6(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r, sink s, double pre_time)
        {
                if(s.transmit == false)
		{
			packet l = s.queue.poll();
			pre_packet.status = 5;
			pre_packet.timestamp += (r.packet_size/r.bandwidth_out);
			s.finish = pre_packet.timestamp;
			s.transmit = true;
			//System.out.println();
			System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " transmitted back to switch at "+ pre_packet.timestamp);
		}
		else
		{
			pre_packet.timestamp = s.finish;
			System.out.println("Packet "+pre_packet.p_id+ " queued at sink "+pre_packet.s_id+ " at "+ pre_packet.timestamp+" here3");
		}
		p.add(pre_packet);
        }
        static void state7(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r, sink s, double pre_time, AIMD aimd, double sim_time, double drop_prob)
        {
            int z = aimd.handleAck(pre_packet, pre_source);
            if(z == 1)
            {
                pre_source.next(p, pre_packet.timestamp, sim_time, drop_prob);
            }
        }
        static void state8(packet pre_packet, source pre_source, PriorityQueue<packet> p, router r, sink s, double pre_time)
        {
                if(r.transmit_back == false)
		{
			packet l = r.out_que.poll();
			pre_packet.status = 7;
			pre_packet.timestamp += (r.packet_size/pre_source.bandwidth);
			r.finish_back = pre_packet.timestamp;
			r.transmit_back = true;
			//System.out.println();
			System.out.println("Packet "+pre_packet.p_id+" of source "+pre_packet.s_id+ " transmitted back to source at "+ pre_packet.timestamp);
		}
		else
		{
			pre_packet.timestamp = r.finish_back;
			System.out.println("Packet "+pre_packet.p_id+ " queued on return at switch "+pre_packet.s_id+ " at "+ pre_packet.timestamp+" here3");
		}
		p.add(pre_packet);
            
        }
        static double myRandom() {
	    Random generator = new Random();
		double number = generator.nextDouble();
		return number;
	}
}

class source
{
	int s_id;
	double rand;
	int queue_size;
	double gen_time;
	boolean transmit;
	double finish;
	int num_packet;
	int cwnd;
	int expected_ack;
        int window_end;
        int window_start;
        double RTT;
        double bandwidth;
	public source(int count, double rand, double gen_time, double bandwidth, double RTT)
	{
		
		this.num_packet = 1;
		s_id = count;
		this.rand = rand;
		this.gen_time = gen_time;
		this.transmit = false;
                this.cwnd = 1;
                this.bandwidth = bandwidth;
                this.RTT = RTT;
                this.window_start = 0;
                this.window_end = 0;
                this.expected_ack = 0;
	}
	public void next(PriorityQueue<packet> p, double gen_time, double sim_time, double drop_prob)
	{
            if(gen_time < sim_time)
            {
                for(int j=0; j<this.cwnd; j++)
                    {
                        packet l = new packet(s_id, num_packet, gen_time, false);
                        double t = Math.random();
                        if(t >= drop_prob)
                        {
                            p.add(l);
                            p.add(new packet(s_id, num_packet, 4*this.RTT, true));
                            this.num_packet++;
                            System.out.println("Packet "+(num_packet-1)+" of source "+s_id+ " generated at "+gen_time);
                        }
                    }
            }
	}
}

class router
{
	double bandwidth_out;
	double packet_size;
        Queue<packet> in_que = new LinkedList<packet>();
        Queue<packet> out_que = new LinkedList<packet>();
        boolean transmit;
        boolean transmit_back;
        int queue_size;
        double finish;
        double finish_back;
	public router(double bandwidth_out,double packet_size, int queue_size)
	{
		this.bandwidth_out = bandwidth_out;
		this.packet_size = packet_size;
                this.queue_size = queue_size;
                this.transmit = false;
                this.transmit_back = false;
	}
}

class sink
{
    double bandwidth;
    double packet_size;
    Queue<packet> queue = new LinkedList<packet>();
    boolean transmit;
    int queue_size;
    double finish;
    int last_acked;
    public sink(double bandwidth, double packet_size, int queue_size)
    {
        this.bandwidth = bandwidth;
	this.packet_size = packet_size;
        this.queue_size = queue_size;
        this.transmit = false;
        this.last_acked = -1;
    }
}
class packet
{
	int s_id;
	double change_time;
	int p_id;
	double timestamp;
	int status;
        int acked;
        boolean isTimeout;
	public packet(int s_id, int p_id, double timestamp, boolean isTimeout)
	{
		this.s_id = s_id;
		this.p_id = p_id;
		this.timestamp = timestamp;
		this.status = 0;
		this.change_time = timestamp;
                this.acked = -1;
                this.isTimeout = isTimeout;
	}
}


class Comparator_func implements Comparator<packet> {

    public int compare(packet first, packet second) {
        if(first.timestamp > second.timestamp)
            return 1;
        else if(first.timestamp==second.timestamp){
        	if(first.change_time > second.change_time)
        		return 1;
        	else if(first.change_time == second.change_time)
        		return first.p_id - second.p_id;
        	else
        		return -1;
        }
        return -1;
        
    }
}

class AIMD
{
	source source[];
	packet p;
	public AIMD(source source[])
	{
		this.source = source;
	}
        public void resp_at_sink(packet p, source s, sink t)
        {
            if(p.p_id == t.last_acked +1)
            {
                p.acked = p.p_id;
                t.last_acked = p.p_id;
            }
            else
            {
                p.acked = t.last_acked;
            }
        }
	public int handleAck(packet p, source s)
	{
		if(s.expected_ack == p.acked)
                {
                    s.expected_ack = s.expected_ack +1;
                    if(s.window_end == p.acked)
                    {
                        s.cwnd = s.cwnd + 1;
                        s.window_start = p.acked+1;
                        s.window_end = s.window_start + s.cwnd-1;
                        return 1;
                    }
                }
                else if(p.acked < s.expected_ack)
                {
                    if(s.cwnd > 1)
                    {
                        s.cwnd = s.cwnd/2;                        
                    }
                    s.window_start = s.expected_ack;
                    s.window_end = s.window_start + s.cwnd-1;
                    return 1;
                }
                return 0;
	}
        public void handleTimeout(packet p, source s)
        {
            if(p.p_id == s.expected_ack)
            {
                if(s.cwnd > 1)
                    {
                        s.cwnd = s.cwnd/2;                        
                    }
                s.window_start = s.expected_ack;
                s.window_end = s.window_start + s.cwnd-1;
            }
        }
}
