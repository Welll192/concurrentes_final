import java.util.Arrays;
import java.util.Scanner;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
class Cliente50 {


    public double[] sum = new double[40];
    TCPClient50 mTcpClient;
    Scanner sc;

    public static void main(String[] args) {
        Cliente50 objcli = new Cliente50();
        objcli.iniciar();
    }

    void iniciar() {
        new Thread(
                () -> {
                    mTcpClient = new TCPClient50("127.0.0.1", this::ClienteRecibe);
                    mTcpClient.run();
                }
        ).start();
        //---------------------------

        String salir = "n";
        sc = new Scanner(System.in);

        while (!salir.equals("s")) {
            salir = sc.nextLine();
            ClienteEnvia(salir);
        }


    }

    void ClienteRecibe(String llego) {

        if (llego.trim().contains("evalua")) {

            Pattern pattern = Pattern.compile("(\\w+) (\\d+) (.+)");
            Matcher matcher = pattern.matcher(llego);

            if (matcher.matches()) {
                // Obtener las partes separadas
                String parte1 = matcher.group(1);
                String epoca = matcher.group(2);
                String wNews = matcher.group(3);

                String numerosString = wNews.substring(1, wNews.length() - 1);

                // Dividir la cadena en subcadenas individuales
                String[] numerosArray = numerosString.split(",");

                // Crear un nuevo arreglo de tipo double[]
                double[] wNew = new double[numerosArray.length];

                // Convertir las subcadenas en números double
                for (int i = 0; i < numerosArray.length; i++) {
                    wNew[i] = Double.parseDouble(numerosArray[i].trim());
                }
                procesar(Integer.parseInt(epoca),wNew);

            } else {
                System.out.println("No se encontró una coincidencia.");
            }


        }
    }


    void ClienteEnvia(String envia) {

        if (mTcpClient != null && !envia.equals("s")) {

            mTcpClient.sendMessage(envia);
        }
    }


    void procesar(int epoca,double[] wNew ) { // implementacion para 6 hilos

        Rna01[] rna01s = new Rna01[6];
        Thread[] threads = new Thread[6];
        double [] peso = new double[0];
        double error = 9999;
        for (int i = 0; i < 6; i++) {
            rna01s[i] = new Rna01(7,6,1,epoca, wNew);
            threads[i] = new Thread(rna01s[i]);
            threads[i].start();
        }
        try {
            for (int i = 0; i < 6; i++) {
                threads[i].join();
                if( rna01s[i].getError() < error){
                    error = rna01s[i].getError();
                    peso = rna01s[i].getPesos();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ClienteEnvia(error+" "+Arrays.toString(peso));

    }
}


class Rna01 implements Runnable{
    static final Random rand = new Random();
    int ci;
    int co;
    int cs;

    double xin[][];//={{0,1,0},{0,1,1},{1,0,0},{1,0,1}};
    //double xin[][]={{0,1,0},{0,1,1},{1,0,0},{1,0,1}};
    double xout[][];//={{1},{0},{1},{0}};

    double y[];

    //   double w[]={2,-2,0,1,3,-1,3,-2};
//   double s[]={0,0,0};
    double s[];
    double g[];
    //   double g[]={0,0,0};
    double w[];

    double error;

    //   int c[] = {3,2,1};//capas de datos
    int c[]=new int[3];//capas de datos

    public Rna01(int ci_,int co_,int cs_,int epoca, double [] wNew){

        this.ci=ci_;//2
        this.co=co_;//3
        this.cs=cs_;//1

        y = new double[co+cs];
        s = new double[co+cs];
        g = new double[co+cs];
        w = new double[ci*co+co*cs];

        c[0]=ci;
        c[1]=co;
        c[2]=cs;

        for(int i=0;i<y.length;i++){
            y[i]=0;s[i]=0;g[i]=0;
        }
        for(int i=0;i<w.length;i++){
            if(epoca==1) w[i]=getRandom();
            else w[i] = wNew[i];
        }


    }

    public double fun(double d){
        return 1/(1+Math.exp(-d));
    }

    public void printxingreso(){
        //visualizar x ingreso
        for(int i=0;i<xin.length;i++)
            for(int j=0;j<xin[i].length;j++)
                System.out.println("xingreso["+i+","+j+"]="+xin[i][j]);
        System.out.println("                ");
    }


    double getRandom() {
        return (rand.nextDouble() * 2 - 1); // [-1;1[
    }

    public void entrenamiento(double[][] in,double[][] sal,int veces){
        xin=in;
        xout=sal;
        for(int v=0;v<veces;v++)
            for(int i=0;i<xin.length;i++){
                entreno(i);
            }

    }
    public double[] getPesos(){
        return w;
    }
    public void entreno(int cii){
        int ii;
        double pls;
        int ci;

        //entrenamiento

        //////******** Ida**********//////
        //+++++++capa1
        ///ci=0;//entrenamiento primero   /////HOPE
        ci=cii;
        ii = 0;//capa0*capa1
        pls=0;
        for(int i=0;i<c[1];i++){
            for(int j=0;j<c[0];j++){
                pls=pls+w[ii]*xin[ci][j];
                ii++;
            }
            s[i]=pls;  //i = i+ capa0
            y[i]=fun(s[i]); //i = i+ capa0
            pls=0;
        }
        //++++++capa2
        pls=0;
        ii = c[0]*c[1];//capa1*capa2
        for(int i=0;i<c[2];i++){
            for(int j=0;j<c[1];j++){
                pls=pls+w[ii]*y[j];
                ii++;
            }
            s[i+c[1]]=pls;  //i = i + capa1
            y[i+c[1]]=fun(s[i+c[1]]); //i = i + capa1
            pls=0;
        }



        //////----------Fin Ida--------/////
        //////******** Vuelta**********/////
        //++++capa2 g
        for(int i=0;i<c[2];i++){
            g[i+c[1]]=(xout[ci][i]-y[i+c[1]])*y[i+c[1]]*(1-y[i+c[1]]);
            error = g[i+c[1]];
        }

        //++++capa1 g
        pls=0;
        for(int i=0;i<c[1];i++){
            for(int j=0;j<c[2];j++){
                pls=pls+w[c[0]*c[1]+j*c[1]+i]*g[c[1]+j];
            }
            g[i]=y[i]*(1-y[i])*pls;
            pls=0;
        }

        //++++capa2 w
        ii = c[0]*c[1];//capa1*capa2
        for(int i=0;i<c[2];i++){
            for(int j=0;j<c[1];j++){
                w[ii]=w[ii]+g[i+c[1]]*y[j];
                ii++;
            }
        }

        //++++capa1 w
        ii = 0;//capa0*capa1
        for(int i=0;i<c[1];i++){
            for(int j=0;j<c[0];j++){
                w[ii]=w[ii]+g[i]*xin[ci][j];
                ii++;
            }
        }

    }

    public double getError() {
        return error;
    }

    @Override
    public void run() {
            double[][] datos = new double[][]{{0.5,0.5,0.0,0.5,1.0,1.0,1.0},
                                                {0.5,0.0,0.0,1.0,1.0,1.0,0.5},
                                                {0.5,0.5,0.0,0.5,0.0,1.0,0.0},
                                                {0.5,0.5,0.0,1.0,0.0,1.0,1.0},
                                                {0.0,0.0,0.0,0.5,0.0,1.0,0.0},
                                                {0.0,0.0,1.0,0.5,1.0,1.0,1.0},
                                                {0.0,0.5,0.0,0.5,0.0,1.0,1.0}};

            double[][] salida = new double[][]{{0.5},
                                                {0.5},
                                                {1.0},
                                                {0.5},
                                                {1.0},
                                                {0.5},
                                                {1.0}};

           entrenamiento(datos,salida,20);

    }
}
