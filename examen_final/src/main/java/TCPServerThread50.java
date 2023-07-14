import java.io.*;
import java.net.Socket;


public class TCPServerThread50 extends Thread{
    
    private Socket client;
    private TCPServer50 tcpserver;
    private int clientID;                 
    private boolean running ;
    private boolean reanuda;
    private boolean interrumpe;
    public PrintWriter mOut;
    public BufferedReader in;
    private TCPServer50.OnMessageReceived messageListener = null;
    private String message;
    TCPServerThread50[] cli_amigos;

    public TCPServerThread50(Socket client_, TCPServer50 tcpserver_, int clientID_,TCPServerThread50[] cli_ami_) {
        this.client = client_;
        this.tcpserver = tcpserver_;
        this.clientID = clientID_;
        this.cli_amigos = cli_ami_;
        this.running = true;
        this.reanuda = false;
        this.interrumpe = true;

    }
    
     public void trabajen(int cli){      
         mOut.println("TRABAJAMOS ["+cli+"]...");
    }

    public void interrumpeCliente(){

        interrumpe = false;

    }
    public void stopClient() {

        running = false;

    }
    public void levantaClient() {
        reanuda = true;
        interrumpe = true;
    }
    public void run() {

        try {
            try {               

                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                System.out.println("TCP Server"+ "C: Sent.");
                messageListener = tcpserver.getMessageListener();

                in = new BufferedReader(new InputStreamReader(client.getInputStream()));

                while (running) {

                    if(!reanuda) message = in.readLine();

                    if(reanuda){
                        message = "      ";
                        message = in.readLine();
                    }

                    if (message != null && messageListener != null && interrumpe) {
                        messageListener.messageReceived(message);

                    }



                }


                if(reanuda){
                    start();
                }
            } catch (Exception e) {
                System.out.println("TCP Server"+ "S: Error"+ e);
            } finally {
                client.close();
            }

        } catch (Exception e) {
            System.out.println("TCP Server"+ "C: Error"+ e);
        }
    }
    

    
    public void sendMessage(String message){//funcion de trabajo
        if (mOut != null && !mOut.checkError()) {
            mOut.println( message);
            mOut.flush();
        }
    }


}
