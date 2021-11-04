package frmVideo;

import java.awt.EventQueue;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class frmServer implements Runnable {

    private static ServerSocket server;

    private Thread server_thread;

    public static JFrame frame;
    private JLabel lblServerLog;
    private JTextArea server_log;
    JButton btnStartServer;
    JButton btnStopServer;

    static Logger log = LogManager.getLogger(frmServer.class);

    void start_server() throws IOException, ClassNotFoundException {
        server = new ServerSocket(5000);
        Socket socket = null;
        int i = 0;

        server_log.append("Server listening..\n");
        try {
            while ((socket = server.accept()) != null) {
                new ServerThread(socket, "Client#" + (i++), server_log);
                server_log.append("Thread for Client#" + (i++) + " generating...\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            start_server();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public frmServer() {
        frame = new JFrame("Streaming Server");
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);
        frame.setResizable(false);

        // Server Log Text with its scrollbar
        lblServerLog = new JLabel("Logs:");
        lblServerLog.setBounds(20, 11, 66, 14);
        frame.getContentPane().add(lblServerLog);

        server_log = new JTextArea();
        server_log.setBounds(10, 36, 424, 181);
        server_log.setWrapStyleWord(true);
        server_log.setLineWrap(true);
        frame.getContentPane().add(server_log);
        server_log.setColumns(10);

        JScrollPane scrollPane = new JScrollPane(server_log);
        scrollPane.setBounds(20, 36, 404, 181);
        frame.getContentPane().add(scrollPane);
        //--------------------------------------

        // Start Server Button
        btnStartServer = new JButton("Start");
        btnStartServer.setBounds(20, 228, 113, 23);
        frame.getContentPane().add(btnStartServer);
        //--------------------------------------

        // Stop Server Button
        btnStopServer = new JButton("Stop");
        btnStopServer.setBounds(311, 228, 113, 23);
        frame.getContentPane().add(btnStopServer);
        //--------------------------------------

        btnStopServer.setEnabled(false); // Tắt nút Start Server khi khởi động

        server_thread = new Thread(this);	// Khởi tạo thread để xử lý Server

        // Khai báo Listener để xử lý sự kiện nhấn nút Start Server
        btnStartServer.addActionListener(event -> {
            log.debug("'Start Server' button has been pressed");

            btnStartServer.setEnabled(false);
            btnStopServer.setEnabled(true);

            server_thread.start();
        });

       // Khai báo Listener để xử lý sự kiện nhấn nút Stop Server
        btnStopServer.addActionListener(event -> {
            log.debug("'Stop Server' button has been pressed");

            System.exit(0);	// Đóng giao diện Server
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                frmServer window = new frmServer();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
