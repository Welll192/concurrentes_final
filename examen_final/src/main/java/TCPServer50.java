import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPServer50 {

    
    int nrcli = 0;

    public static final int SERVERPORT = 4444;
    private OnMessageReceived messageListener = null;
    private boolean running = false;
    TCPServerThread50[] sendclis = new TCPServerThread50[10];

    PrintWriter mOut;
    BufferedReader in;
    
    ServerSocket serverSocket;

    //el constructor pide una interface OnMessageReceived
    public TCPServer50(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }
    
    public OnMessageReceived getMessageListener(){/////¨
        return this.messageListener;
    }
    
    public void sendMessageTCPServer(boolean shaEncontrado){
        for (int i = 1; i <= nrcli; i++) {
            sendclis[i].sendMessage(String.valueOf(shaEncontrado));
            System.out.println("ENVIANDO A Minero " + (i));
        }
    }
    public void sendMessageTCPServerRango(String palabra, int dificultad, boolean shaEncontrado){

        for (int i = 1; i <= nrcli; i++) {

            sendclis[i].sendMessage("evalua " + palabra + " " + dificultad + " " + i + " " + shaEncontrado);
            System.out.println("ENVIANDO A MINERO " + (i) + " -> dificultad: " + dificultad);
        }

    }
    public void sendMessageTCPServerRango(String palabra ){

        for (int i = 1; i <= nrcli; i++) {

            sendclis[i].sendMessage("evalua " + palabra );
            System.out.println("ENVIANDO A MINERO " + (i) + " -> palabra: " + palabra);
        }

    }
    public void sendMessageTCPServerRango(int epoca ){

        for (int i = 1; i <= nrcli; i++) {

            double[] wNew = new double[]{5,6,7};
            sendclis[i].sendMessage("evalua " + epoca + " "+ Arrays.toString(wNew));
        }

    }
    public void sendMessageTCPServerRango(int epoca, double[] wNew ){

        for (int i = 1; i <= nrcli; i++) {

            sendclis[i].sendMessage("evalua " + epoca + " "+ Arrays.toString(wNew));

        }

    }
    public void sendMessageTCPServerRango(String palabra, int dificultad, boolean shaEncontrado, boolean reenvia){

        for (int i = 1; i <= nrcli; i++) {
            sendclis[i].levantaClient();
            sendclis[i].sendMessage("evalua " + palabra + " " + dificultad + " " + i + " " + shaEncontrado);
            System.out.println("ENVIANDO A MINERO " + (i) + " -> dificultad: "  + dificultad );
        }

    }

    public void pararClientes(int numero) {
        sendclis[numero].interrumpeCliente();
    }

    public void run(){
        running = true;
        try{
            System.out.println("TCP Server"+"S : Connecting...");
            serverSocket = new ServerSocket(SERVERPORT);
            
            while(running){
                Socket client = serverSocket.accept();

                System.out.println("TCP Server"+"S: Receiving...");
                nrcli++;
                System.out.println("Minero " + nrcli);
                sendclis[nrcli] = new TCPServerThread50(client,this,nrcli,sendclis);
                Thread t = new Thread(sendclis[nrcli]);
                t.start();
                System.out.println("Nuevo conectado:"+ nrcli+" mineros conectados");
                
            }

        }catch( Exception e){
            System.out.println("Error"+e.getMessage());
        }finally{

        }
    }
    public  TCPServerThread50[] getClients(){
        return sendclis;
    } 

    public  interface OnMessageReceived {
        public void messageReceived(String message) throws IOException;
    }
}
