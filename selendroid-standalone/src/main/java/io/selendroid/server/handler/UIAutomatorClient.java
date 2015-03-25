package io.selendroid.server.handler;

import org.eclipse.jetty.util.ArrayQueue;
import org.json.JSONException;

import java.io.*;
import java.net.*;
import java.util.NoSuchElementException;
import java.util.Queue;

/**
 *
 * A complete Java class that demonstrates how to use the Socket
 * class, specifically how to open a socket, write to the socket,
 * and read from the socket.
 *
 * @author alvin alexander, devdaily.com.
 *
 */
public class UIAutomatorClient {
    String host = "192.168.56.101";
    int port = 4724;
    Socket socket;
    SocketReader readTread;

    public UIAutomatorClient () throws Exception {
        socket = openSocket(host, port);
        readTread = new SocketReader(socket);
        readTread.start();
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

    private String writeToAndReadFromSocket(Socket socket, String writeTo) throws Exception {
        // write text to the socket
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        bufferedWriter.write(writeTo);
        bufferedWriter.flush();
        return readTread.getMess();
    }

    /**
     * Open a socket connection to the given server on the given port.
     * This method currently sets the socket timeout value to 10 seconds.
     * (A second version of this method could allow the user to specify this timeout.)
     */
    private Socket openSocket(String server, int port) throws Exception
    {
        Socket socket;

        // create a socket with a timeout
        try
        {
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
        final BufferedReader in;
        final Queue<String> messList;

        public SocketReader (Socket socket) throws IOException {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            messList = new ArrayQueue<String>();
        }

        public String getMess() {
            int i = 0;
            while (messList.isEmpty() && i++ < 10) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                return messList.remove();
            } catch (NoSuchElementException e) {
                return null;
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
                        messList.add(sb.toString());
//                        System.out.println("new message:" + sb.toString());
                        sb = new StringBuilder();
                    }

                    assert(balance < 0);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}