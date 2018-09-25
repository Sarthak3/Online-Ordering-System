import java.io.*; 
import java.net.*; 
import java.util.Scanner; 
  
//connects to Server  
//connection made everytime an order is placed
public class Client  
{ 
    public static String yo(String tosend) throws IOException  
    { 
    	String received="";  //stores the received string
        try
        { 
            Scanner scn = new Scanner(System.in); 
            InetAddress ip = InetAddress.getByName("localhost"); 
            Socket s = new Socket(ip, 5056); 
            DataInputStream dis = new DataInputStream(s.getInputStream()); 
            DataOutputStream dos = new DataOutputStream(s.getOutputStream());    
            dos.writeUTF(tosend);  //sends the string to be sent
            received = dis.readUTF();  //receives the string returned
            //closes once done
            s.close(); 
            scn.close(); 
            dis.close(); 
            dos.close(); 
        }
        catch(Exception e)
        { 
            e.printStackTrace(); 
        } 
        return received;
    } 
} 
