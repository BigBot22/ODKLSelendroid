package io.selendroid.server.handler;

import org.eclipse.jetty.util.ArrayQueue;

import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.logging.Logger;

public class UIAutomatorClient {
    String host;
    int port = 4724;
    Socket socket;
    SocketReader socketReader;
    private static final Logger log = Logger.getLogger(RequestRedirectHandler.class.getName());
    private static final int TIMEOUT = 10;

    public UIAutomatorClient (String deviceIp) throws Exception {
        host = deviceIp;
        socket = openSocket(host, port);
        socketReader = new SocketReader(socket);
    }

    public String send(String mess)
    {
        try {
            return writeToAndReadFromSocket(socket, mess);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void connect() {
        socketReader.start();
    }

    public void disconnect() {
        if (socketReader != null) {
            socketReader.disconnect();
            socketReader.interrupt();
            socketReader = null;
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = null;
        }
    }


    private String writeToAndReadFromSocket(Socket socket, String writeTo) throws Exception {
        // write text to the socket
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedWriter.write(writeTo);
        bufferedWriter.flush();
        return socketReader.getMess();
    }

    private Socket openSocket(String server, int port) throws Exception
    {
        // create a socket with a timeout
        try
        {
            log.info("connecting to host:" + server + " port:" + port);
            InetAddress inteAddress = InetAddress.getByName(server);
            SocketAddress socketAddress = new InetSocketAddress(inteAddress, port);

            // create a socket
            socket = new Socket();

            // this method will block no more than timeout ms.
            int timeoutInMs = 10*1000;   // 10 seconds
            socket.connect(socketAddress, timeoutInMs);

            return socket;
        }
        catch (SocketTimeoutException ste)
        {
            System.err.println("Timed out waiting for the socket.");
            ste.printStackTrace();
            throw ste;
        }
    }

    static class SocketReader extends Thread {
        BufferedReader in;
        final Queue<String> messList;

        public SocketReader (Socket socket) throws IOException {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            messList = new ArrayQueue<String>();
        }

        public String getMess() {
            int i = 0;
            while (messList.isEmpty() && i++ < TIMEOUT) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                return messList.remove();
            } catch (NoSuchElementException e) {
                return "{'value':'timeout " + TIMEOUT + "s on uiautomator client','status':1}";
            }
        }

        @Override
        public void run() {

            char c;
            StringBuilder sb = new StringBuilder();
            int balance = 0;
            try {
                while ((c = (char)in.read()) != '\n') {
                    sb.append(c);
                    if( c == '{') {
                        balance++;
                    }
                    if (c == '}') {
                        balance--;
                    }
                    if (balance == 0) {
                        messList.offer(sb.toString());
//                        System.out.println("new message:" + sb.toString());
                        sb = new StringBuilder();
                    }

                    if (balance < 0) {
                        messList.offer("Error message:" + sb.toString());
                        break;
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void disconnect () {
            this.interrupt();
            in = null;
        }
    }
}