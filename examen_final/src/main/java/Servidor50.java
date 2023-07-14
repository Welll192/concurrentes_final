import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Servidor50 {

    TCPServer50 mTcpServer;
    Scanner sc;
    private boolean shaEncontrado = false;

    private static List<String> palabras;
    private static int contador=0;

    private static int dificultad;
    public static void main(String[] args) {
        Servidor50 objser = new Servidor50();

        objser.iniciar();
    }

    void iniciar() {
        new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        mTcpServer = new TCPServer50(
                                new TCPServer50.OnMessageReceived() {
                                    @Override
                                    public void messageReceived(String message) {
                                        synchronized (this) {
                                            ServidorRecibe(message);
                                        }
                                    }
                                }
                        );
                        mTcpServer.run();
                    }
                }
        ).start();
        //-----------------
        String salir = "n";
        sc = new Scanner(System.in);
        System.out.println("Servidor bandera 01");
        while (!salir.equals("s")) {
            salir = sc.nextLine();
            ServidorEnvia(salir);
        }
        System.out.println("Servidor bandera 02");

    }

    int contarcliente = 0;
    int epoca=1;
    double[] rptacli = new double[20];
    double sumclient = 0;
    double[] almacenaError = new double[2];
    List<String> almacenaPesos = new ArrayList<>();
    Map<Double,String> errorPeso = new HashMap<>();
    void ServidorRecibe(String llego) {


        String[] parts = llego.split("\\[", 2);

        double error = 9999;
        double[] wPesos;

        if (parts.length==2) {
            // Obtener las partes separadas
            double errorCli =Double.parseDouble(parts[0].trim());
            String wPesosCli = parts[1];
            almacenaError[contarcliente] = errorCli;
            errorPeso.put(errorCli,wPesosCli);
            contarcliente++;
            if (contarcliente == this.mTcpServer.nrcli  && epoca<=120) {
                 for (int i=0;i<almacenaError.length;i++){
                     if(almacenaError[0]<error){
                         error = almacenaError[0];
                     }
                 }
                 String wPFinal = errorPeso.get(error);

                String numerosString = wPFinal.substring(1, wPFinal.length() - 1);


                String[] numerosArray = numerosString.split(",");


                double[] resultadoPesos = new double[numerosArray.length];


                for (int i = 0; i < numerosArray.length; i++) {
                    resultadoPesos[i] = Double.parseDouble(numerosArray[i].trim());
                }

                System.out.println("Epoca ("+epoca +") -> error: "+error+" "+ Arrays.toString(resultadoPesos));

                mTcpServer.sendMessageTCPServerRango(epoca,resultadoPesos);
                contarcliente=0;
                epoca++;

            }

        } else {
            System.out.println("No se encontró una coincidencia.");
        }

    }

    void ServidorEnvia(String envia) {//El servidor tiene texto de envio
        if (envia != null) {

            System.out.println("Soy Server y envio" + envia);
            if (envia.trim().contains("envio")) {   // [envio, palabra]
                System.out.println("SI TIENE ENVIO!!!");

                String[] arrayString = envia.split("\\s+");

                if (mTcpServer != null) {
                    mTcpServer.sendMessageTCPServerRango(epoca);
                }

            } else {
                System.out.println("NO TIENE ENVIO!!!");
            }
        }
    }
}
