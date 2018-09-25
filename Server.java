import java.io.*; 
import java.text.*; 
import java.util.*; 
import java.net.*; 
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.locks.ReentrantLock; 

//global variables
class glob
{
    static int cook;  //the number of cookies shared resource
    static int order;  //order number
    static int snacks;  //the number of snacks shared resource
    static int flag1, flag2;  //the threshold flags so its displayed once
    static List<String> lname;  //name of people placing orders
    static List<Integer> lprice;  //total price of order
    static List<Integer> ltea;  //quantity of tea of order
    static List<Integer> lcoff;  //quantity of coffee of order
    static List<Integer> lcook;  //quantity of cookie of order
    static List<Integer> lsnacks;  //quantity of snacks of order
    static List<Date> ldt;
    int iter;  //order numbers displayed till last time
}

//stores time of order placed 
class Times 
{
    private int tm;
    
    public Times(int t)
    {
        this.tm=t;
    }

    public int gettm() 
    {
        return tm;
    }
}

public class Server  
{ 
    public static void main(String[] args) throws IOException  
    { 
        ServerSocket ss = new ServerSocket(5056); 
        //maximum orders placed=1000
        BlockingQueue<Times> queue = new ArrayBlockingQueue<>(1000);
        glob g=new glob();
        //setting cookie number and snacks number
        g.cook=50;
        g.order=1;
        g.snacks=25;
        g.flag1=0;
        g.flag2=0;
        g.lname=new ArrayList<>();
        g.lprice=new ArrayList<>();
        g.ltea=new ArrayList<>();
        g.lcoff=new ArrayList<>();
        g.lcook=new ArrayList<>();
        g.lsnacks=new ArrayList<>();
        g.ldt=new ArrayList<>();
        g.iter=0;
        OrderMaker om = new OrderMaker(queue);  //starts thread that makes orders
        new Thread(om).start();
        while (true)  
        { 
            Socket s = null;
            //starts new thread for every client that connects to the server 
            try 
            { 
                s = ss.accept(); 
                DataInputStream dis = new DataInputStream(s.getInputStream()); 
                DataOutputStream dos = new DataOutputStream(s.getOutputStream()); 
                Thread t = new ClientHandler(s, dis, dos, queue); 
                t.start();   
            } 
            catch (Exception e)
            { 
                s.close(); 
                e.printStackTrace(); 
            } 
        } 
    } 
} 

//this creates orders in the list
class OrderMaker implements Runnable
{
    private BlockingQueue<Times> queue;
    static int cnt=0, prev=0;  //cnt is time counter and prev is the time of last order
    //constructor for calling it the first time to initilaize the blocking queue
    public OrderMaker(BlockingQueue<Times> q)
    {
        this.queue=q;
        cnt=0;
        prev=0;
    }
    //constructor for later calls
    public OrderMaker()
    {

    }

    @Override
    public void run() 
    {
        glob g=new glob();
        // DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        // LocalDate localDate = LocalDate.now();
        // System.out.println(dtf.format(localDate));
        while(true)  //continues to run forever
        {
            System.out.println(cnt+" "+ prev);  //displays the times
            try
            {
                Thread.sleep(3000);  //makes it easy to see
            }
            catch(Exception e)
            {

            }
            if(cnt%20==0)  //displays the list every 20 counter times
            {
                int i=0;
                String x="";
                x+=("Tea Rate: 1\nCoffee Rate: 1\nCookie Rate: 1\n Snacks Rate: 1\nDate\t\t\tName\tPrice\tTea\tCoffee\tCookie\tSnacks\n");
                for(i=g.iter;i<g.lname.size();i++)  //goes from last order displayed to current
                {
                    x+=(g.ldt.get(i)+"\t"+g.lname.get(i)+"\t"+
                        g.lprice.get(i)+"\t"+g.ltea.get(i)+"\t"+
                        g.lcoff.get(i)+"\t"+g.lcook.get(i)+"\t"+
                        g.lsnacks.get(i)+"\n");
                }
                g.iter=i;
                NewJPanel njp=new NewJPanel(x);  //displays through new panel
                njp.setVisible(true);
        
            }
                    
            cnt++;
            if(queue.size()==0)
            {
                prev=cnt;  //if queue is empty then no time of last order is currrent time so we can start making it immediately
                continue;
            }
            System.out.println(cnt+" "+ prev);    
            try
            {
                int x;
                Times msg;
                msg=queue.take();  //removes the first item on the list
                x=msg.gettm();  //gets the time taken to make it
                while(cnt!=x)  //make the order
                {
                    System.out.println(cnt+" "+ prev);
                    if(cnt%20==0)  //if in this time it gets to 20 display the newly placed order
                    {
                        int i=0;
                        String x2="";
                        x2+=("Tea Rate: 1\nCoffee Rate: 1\nCookie Rate: 1\n Snacks Rate: 1\nDate\t\t\tName\tPrice\tTea\tCoffee\tCookie\tSnacks\n");
                        for(i=g.iter;i<g.lname.size();i++)
                        {
                           x2+=(g.ldt.get(i)+"\t"+g.lname.get(i)+"\t"+
                                g.lprice.get(i)+"\t"+g.ltea.get(i)+"\t"+
                                g.lcoff.get(i)+"\t"+g.lcook.get(i)+"\t"+
                                g.lsnacks.get(i)+"\n");
                        }
                        g.iter=i;
                        NewJPanel njp=new NewJPanel(x2);
                        njp.setVisible(true);
        
                    }
                    try
                    {
                        Thread.sleep(3000);  //sleep to make it easy to find
                    }
                    catch(Exception e)
                    {
                        
                    }
                    cnt++;
                }
                cnt--;
                // Thread.sleep(x*500);
                System.out.println("Made at time "+x);
            }
            catch(InterruptedException e) 
            {
                // e.printStackTrace();
            }
        }
    }
}
  
//understands the packet given and converts it into order
class ClientHandler extends Thread  
{ 
    final DataInputStream dis; 
    final DataOutputStream dos; 
    final Socket s; 
    private BlockingQueue<Times> queue;
    public ClientHandler(Socket s, DataInputStream dis, DataOutputStream dos, BlockingQueue<Times> q)  
    { 
        this.s = s; 
        this.dis = dis; 
        this.dos = dos; 
        this.queue=q;
    } 
  
    @Override
    public void run()  
    { 
        String received;  //received string
        String toreturn;  //the string to be returned
        try 
        { 
            received = dis.readUTF(); 
            String x="",y="",z="", w="", nm="";
            int i=0, a,b,c,d;
            for(;received.charAt(i)!='#';i++)
                x+=received.charAt(i);  //tea
            i++;
            for(;received.charAt(i)!='#';i++)
                y+=received.charAt(i);  //coffee
            i++;
            for(;received.charAt(i)!='#';i++)
                z+=received.charAt(i);  //cookie
            i++; 
            for(;received.charAt(i)!='#';i++)
                w+=received.charAt(i);  //snacks
            i++;
            for(;i<received.length();i++)
                nm+=received.charAt(i);  //name
            a=Integer.parseInt(x);
            b=Integer.parseInt(y);
            c=Integer.parseInt(z);
            d=Integer.parseInt(w);
            Orderer o = new Orderer(queue,a,b,c,d,nm);
            Thread t=new Thread(o);
            t.start();  //starts the thread to order
            while(t.isAlive())  //till the ordering is complete stall
                ;
            // System.out.println(producer.esttime);
            toreturn=o.esttime;
            if(o.esttime.charAt(0)!='-')  //if esttime does not starts with -ve order placed
            {
                toreturn+="#";
                toreturn+=Integer.toString(o.ord);  //return the ordered numebr with the price
            }
            dos.writeUTF(toreturn);  //eelse return the string as it is
            this.s.close();  
        } 
        catch (IOException e) 
        { 
            e.printStackTrace(); 
        } 
        try
        { 
            this.dis.close(); 
            this.dos.close();       
        }
        catch(IOException e)
        { 
            e.printStackTrace(); 
        } 
    } 
} 

class Orderer implements Runnable 
{
    private BlockingQueue<Times> queue;
    int tea,coff,cook, snacks, ord;
    String esttime, nam;
    ReentrantLock lock = new ReentrantLock();  //locking mechanism
    public Orderer(BlockingQueue<Times> q, int te, int cof, int coo, int snak, String nm)
    {
        this.queue=q;
        this.tea=te;
        this.coff=cof;
        this.cook=coo;
        this.snacks=snak;
        this.nam=nm;
    }
    
    @Override
    public void run() 
    {
        int x=tea+coff;  //time to make order 1min for each tea and coffee
        lock.lock();
        try
        {
            glob g=new glob();
            if(g.cook<cook || g.snacks<snacks)  //if it is below threshold order could not placed so return the available number of cookies and snacks
            {
                this.esttime=Integer.toString(-1*(g.cook+1));  //shifted by one to avoid sending 0 as 0*-1=0
                this.esttime+="#";
                this.esttime+=Integer.toString(-1*(g.snacks+1));
            }
            else
            {
                g.cook-=cook;
                g.snacks-=snacks;
                if(g.flag1==0 && g.cook<10)  //displays if first time goes below threshold
                {
                    g.flag1=1;
                    NewJPanel njp=new NewJPanel("Cookie below Threshold (10)");
                    njp.setVisible(true);
                }
                if(g.flag2==0 && g.snacks<10)
                {
                    g.flag2=1;
                    NewJPanel njp=new NewJPanel("Snacks below Threshold (10)");
                    njp.setVisible(true);
                }
                OrderMaker obj=new OrderMaker();
                Times msg=new Times(x+obj.prev+1);  //time of completion=the last ordered time + the time to make current order
                this.esttime=Integer.toString(obj.prev-obj.cnt+x);  //time of completion is time for this order + time to make the order before it
                this.ord=g.order;
                g.order++;
                obj.prev+=x;  //updaing it to be last order
                //adding to list
                g.lname.add(nam);
                g.ltea.add(tea);
                g.lcoff.add(coff);
                g.lcook.add(cook);
                g.lsnacks.add(snacks);
                g.lprice.add(tea+coff+cook+snacks);  //assuming all have cost Re1
                Date d1=new Date();
                g.ldt.add(d1);
                try 
                {
                    queue.put(msg);
                } 
                catch (InterruptedException e) 
                {
                    e.printStackTrace();
                }
            }
        }
        finally
        {
            lock.unlock();
        }
    }

}

//displays the things on UI
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

class NewJPanel extends javax.swing.JFrame {

    /**
     * Creates new form NewJPanel
     */
    public NewJPanel(String z) {
        initComponents();
        jTextArea1.setText(z);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        // this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(54, 54, 54)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 283, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(36, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 337, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(27, 27, 27))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(25, 25, 25)
                .addComponent(jLabel1)
                .addGap(18, 18, 18)
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 61, Short.MAX_VALUE)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>                        


    // Variables declaration - do not modify                     
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextArea1;
    // End of variables declaration                   
}
