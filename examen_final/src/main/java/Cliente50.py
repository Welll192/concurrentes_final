import threading
import math
import random
import re
import socket
import threading

sumaarray = [0] * 40

class TCPClient50:
    def __init__(self, ip, listener):
        self.servermsj = None
        self.SERVERIP = ip
        self.SERVERPORT = 4444
        self.mMessageListener = listener
        self.mRun = False
        self.out = None
        self.incoming = None

    def sendMessage(self, message):
        if self.out is not None:
            self.out.write(message + '\n')
            self.out.flush()

    def stopClient(self):
        self.mRun = False

    def run(self):
        self.mRun = True
        try:
            serverAddr = socket.gethostbyname(self.SERVERIP)
            print("TCP Client - C: Conectando...")
            clientSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            clientSocket.connect((serverAddr, self.SERVERPORT))
            try:
                self.out = clientSocket.makefile('w')
                print("TCP Client - C: Sent.")
                print("TCP Client - C: Done.")
                self.incoming = clientSocket.makefile('r')
                while self.mRun:
                    self.servermsj = self.incoming.readline()
                    if self.servermsj is not None and self.mMessageListener is not None:
                        self.mMessageListener(self.servermsj)
                    self.servermsj = None
            except Exception as e:
                print("TCP - S: Error", e)
            finally:
                clientSocket.close()
        except Exception as e:
            print("TCP - C: Error", e)

class Cliente50:
    def __init__(self):
        self.sum = [0.0] * 40
        self.mTcpClient = None
        self.sc = None

    def iniciar(self):
        threading.Thread(target=self.runTcpClient).start()

        salir = "n"
        while salir != "s":
            salir = input()
            self.clienteEnvia(salir)

    def runTcpClient(self):
        self.mTcpClient = TCPClient50("127.0.0.1", self.clienteRecibe)
        self.mTcpClient.run()

    def clienteRecibe(self, llego):
        if "evalua" in llego.strip():

            pattern = re.compile(r"(\w+) (\d+) (.+)")
            matcher = pattern.match(llego)

            if matcher:
                parte1 = matcher.group(1)
                epoca = matcher.group(2)
                wNews = matcher.group(3)

                numerosString = wNews[1:-1]
                numerosArray = numerosString.split(",")
                wNew = [float(num.strip()) for num in numerosArray]

                self.procesar(int(epoca), wNew)
            else:
                print("No se encontró una coincidencia.")

    def clienteEnvia(self, envia):
        if self.mTcpClient is not None and envia != "s":
            self.mTcpClient.sendMessage(envia)

    def procesar(self, epoca, wNew):
        rna01s = []
        threads = []
        peso = []
        error = 9999

        for _ in range(6):
            rna01 = Rna01(7, 6, 1, epoca, wNew)
            rna01s.append(rna01)
            thread = threading.Thread(target=rna01.run)
            threads.append(thread)
            thread.start()

        try:
            for thread in threads:
                thread.join()

                if rna01.getError() < error:
                    error = rna01.getError()
                    peso = rna01.getPesos()
        except Exception as e:
            print(e)

        self.clienteEnvia(str(error) + " " + str(peso))


class Rna01:
    rand = random.Random()

    def __init__(self, ci, co, cs, epoca, wNew):
        self.ci = ci
        self.co = co
        self.cs = cs

        self.xin = []
        self.xout = []
        self.y = []
        self.s = []
        self.g = []
        self.w = []

        self.c = [0] * 3

        self.c[0] = ci
        self.c[1] = co
        self.c[2] = cs

        for _ in range(co + cs):
            self.y.append(0)
            self.s.append(0)
            self.g.append(0)

        for _ in range(ci * co + co * cs):
            if epoca == 1:
                self.w.append(self.getRandom())
            else:
                self.w.append(wNew.pop(0))

    def getRandom(self):
        return self.rand.uniform(-1, 1)

    def fun(self, d):
        return 1 / (1 + math.exp(-d))

    def entrenamiento(self, inp, out, veces):
        self.xin = inp
        self.xout = out

        for _ in range(veces):
            for i in range(len(self.xin)):
                self.entreno(i)

    def getPesos(self):
        return self.w

    def entreno(self, cii):
        ii = 0
        pls = 0
        ci = cii

        # entrenamiento

        # Ida
        # capa1
        ii = 0
        pls = 0

        for i in range(self.c[1]):
            for j in range(self.c[0]):
                pls += self.w[ii] * self.xin[ci][j]
                ii += 1

            self.s[i] = pls
            self.y[i] = self.fun(self.s[i])
            pls = 0

        # capa2
        pls = 0
        ii = self.c[0] * self.c[1]

        for i in range(self.c[2]):
            for j in range(self.c[1]):
                pls += self.w[ii] * self.y[j]
                ii += 1

            self.s[i + self.c[1]] = pls
            self.y[i + self.c[1]] = self.fun(self.s[i + self.c[1]])
            pls = 0

        # Vuelta
        # capa2 g
        for i in range(self.c[2]):
            self.g[i + self.c[1]] = (self.xout[ci][i] - self.y[i + self.c[1]]) * self.y[i + self.c[1]] * (
                    1 - self.y[i + self.c[1]])
            self.error = self.g[i + self.c[1]]

        # capa1 g
        pls = 0
        for i in range(self.c[1]):
            for j in range(self.c[2]):
                pls += self.w[self.c[0] * self.c[1] + j * self.c[1] + i] * self.g[self.c[1] + j]

            self.g[i] = self.y[i] * (1 - self.y[i]) * pls
            pls = 0

        # capa2 w
        ii = self.c[0] * self.c[1]
        for i in range(self.c[2]):
            for j in range(self.c[1]):
                self.w[ii] += self.g[i + self.c[1]] * self.y[j]
                ii += 1

        # capa1 w
        ii = 0
        for i in range(self.c[1]):
            for j in range(self.c[0]):
                self.w[ii] += self.g[i] * self.xin[ci][j]
                ii += 1

    def getError(self):
        return self.error

    def run(self):
        datos = [[0.5, 0.5, 0.0, 0.5, 1.0, 1.0, 1.0],
                 [0.5, 0.0, 0.0, 1.0, 1.0, 1.0, 0.5],
                 [0.5, 0.5, 0.0, 0.5, 0.0, 1.0, 0.0],
                 [0.5, 0.5, 0.0, 1.0, 0.0, 1.0, 1.0],
                 [0.0, 0.0, 0.0, 0.5, 0.0, 1.0, 0.0],
                 [0.0, 0.0, 1.0, 0.5, 1.0, 1.0, 1.0],
                 [0.0, 0.5, 0.0, 0.5, 0.0, 1.0, 1.0]]

        salida = [[0.5],
                  [0.5],
                  [1.0],
                  [0.5],
                  [1.0],
                  [0.5],
                  [1.0]]

        self.entrenamiento(datos, salida, 20)


# Ejemplo de uso
if __name__ == "__main__":
    objcli = Cliente50()
    objcli.iniciar()
