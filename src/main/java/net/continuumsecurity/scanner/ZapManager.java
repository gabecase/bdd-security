package net.continuumsecurity.scanner;

import org.apache.commons.lang3.StringUtils;
import org.zaproxy.clientapi.core.ClientApi;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by stephen on 12/04/15.
 */
public class ZapManager {
    private final static Logger log = Logger.getLogger(ZapManager.class.getName());
    private static ZapManager instance = null;
    private int port;
    String HOST = "127.0.0.1";
    int CONNECTION_TIMEOUT = 15000; //milliseconds
    String apiKey = "";
    Process process;

    private ZapManager() {
    }

    public static synchronized ZapManager getInstance() {
        if (instance == null) instance = new ZapManager();
        return instance;
    }

    public int startZAP(String zapPath, String apiKey) throws Exception {
        this.apiKey = apiKey;
        if (process == null) {
            File zapProgramFile = new File(zapPath);

            port = findOpenPortOnAllLocalInterfaces();
            List<String> cmd = new ArrayList<>();
            cmd.add(zapProgramFile.getAbsolutePath());
            cmd.add("-daemon");
            cmd.add("-host"); cmd.add(HOST);
            cmd.add("-port"); cmd.add(String.valueOf(port));
            cmd.add("-config"); cmd.add("api.enabled=true");
            if (apiKey == null || apiKey.length() == 0) {
                cmd.add("-config"); cmd.add("api.disablekey=true");
            } else {
                cmd.add("-config"); cmd.add("api.disablekey=false");
                cmd.add("-config"); cmd.add("api.key=" + apiKey);
            }

            log.info("Start ZAProxy [" + zapProgramFile.getAbsolutePath() + "] on port: " + port);
            log.info("\t with options: "+ StringUtils.join(cmd," "));
            ProcessBuilder pb = new ProcessBuilder().inheritIO();
            pb.directory(zapProgramFile.getParentFile());

            process = pb.command(cmd).start();
            waitForSuccessfulConnectionToZap();
        } else {
            log.info("ZAP already started.");
        }
        return port;
    }

    public void stopZap() {
        if (process == null) return; //ZAP not running
        try {
            log.info("Stopping ZAP");
            ClientApi client = new ClientApi(HOST,port);
            client.core.shutdown(this.apiKey);
            Thread.sleep(2000);
            process.destroy();
        } catch (final Exception e) {
            log.warning("Error shutting down ZAP.");
            log.warning(e.getMessage());
            e.printStackTrace();
        }
    }

    private void waitForSuccessfulConnectionToZap() {
        int timeoutInMs = CONNECTION_TIMEOUT;
        int connectionTimeoutInMs = timeoutInMs;
        int pollingIntervalInMs = 1000;
        boolean connectionSuccessful = false;
        long startTime = System.currentTimeMillis();
        Socket socket = null;
        do {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(HOST, port), connectionTimeoutInMs);
                connectionSuccessful = true;
            } catch (SocketTimeoutException ignore) {
                throw new RuntimeException("Unable to connect to ZAP's proxy after " + timeoutInMs + " milliseconds.");
            } catch (IOException ignore) {
                // and keep trying but wait some time first...
                try {
                    Thread.sleep(pollingIntervalInMs);
                } catch (InterruptedException e) {
                    throw new RuntimeException("The task was interrupted while sleeping between connection polling.", e);
                }
                long ellapsedTime = System.currentTimeMillis() - startTime;
                if (ellapsedTime >= timeoutInMs) {
                    throw new RuntimeException("Unable to connect to ZAP's proxy after " + timeoutInMs + " milliseconds.");
                }
                connectionTimeoutInMs = (int) (timeoutInMs - ellapsedTime);
            } finally {
                if(socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } while (!connectionSuccessful);
    }

    private Integer findOpenPortOnAllLocalInterfaces() throws IOException {
        try (
                ServerSocket socket = new ServerSocket(0);
        ) {
            port = socket.getLocalPort();
            socket.close();
            return port;
        }
    }

}
