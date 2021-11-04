package frmVideo;

import javax.swing.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import Modul.DirectorClass;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.filechooser.FileNameExtensionFilter;

public class frmClient {

    // Diretor
    DefaultListModel<String> list_model = new DefaultListModel<>();
    String input_dir = "raw_videos/";
    File[] raw_videos = new File(input_dir).listFiles();

    // VideoList
    static Logger log = LogManager.getLogger(frmClient.class);

    private Socket socket;
    private ObjectOutputStream output_stream;
    private ObjectInputStream input_stream;
    private JComboBox bitrate;
    private JComboBox format;
    private JComboBox video;
    private JComboBox protocol;

    private static JFrame frame;

    /**
     * 
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                if ((new File("raw_videos/").listFiles().length == 0) && (new File("videos/").listFiles().length == 0)) {
                    JOptionPane.showMessageDialog(frame, "Thư mục rỗng", "Exiting...", JOptionPane.ERROR_MESSAGE);
                } else {
                    frmClient window = new frmClient();
                    window.frame.setVisible(true);
                }

            } catch (ConnectException e) {
                JOptionPane.showMessageDialog(frame, "Vui lòng khởi động Server", "Thông báo", JOptionPane.OK_OPTION);
            } catch (NullPointerException npe) {
                JOptionPane.showMessageDialog(frame, "Không tìm thấy thư mục raw_videos.", "Exiting...", JOptionPane.ERROR_MESSAGE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     *
     * @throws IOException
     * @throws UnknownHostException
     */
    public frmClient() throws UnknownHostException, IOException {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     *
     * @throws IOException
     * @throws UnknownHostException
     */
    private void initialize() throws UnknownHostException, IOException {

        socket = new Socket("127.0.0.1", 5000);
        output_stream = new ObjectOutputStream(socket.getOutputStream());
        input_stream = new ObjectInputStream(socket.getInputStream());

        frame = new JFrame();
        frame.setBounds(100, 100, 1007, 575);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);

        JPanel panel = new JPanel();
        panel.setBackground(new Color(0, 102, 153));
        panel.setBounds(0, 0, 185, 536);
        frame.getContentPane().add(panel);
        panel.setLayout(null);

        // Panel cho Diretor
        JPanel pDirector = new JPanel();
        pDirector.setBounds(183, 0, 808, 536);
        frame.getContentPane().add(pDirector);
        pDirector.setLayout(null);

        // Panel cho Video
        JPanel pVideo = new JPanel();
        pVideo.setBounds(183, 0, 808, 536);
        frame.getContentPane().add(pVideo);
        pVideo.setLayout(null);

        // Set hiển thị cho các Panel
        pDirector.setVisible(false);
        pVideo.setVisible(false);

        // Lấy danh sách Video
        for (File video : raw_videos) {
            list_model.addElement(video.getName());
        }
        JList input_list = new JList(list_model);
        input_list.setBounds(33, 76, 250, 360);
        pDirector.add(input_list);

        JScrollPane scrollPane_1 = new JScrollPane();
        scrollPane_1.setBounds(340, 76, 250, 360);
        pDirector.add(scrollPane_1);
        //--------------------------------------

        // Xuất danh sách Video
        JList output_list = new JList();
        scrollPane_1.setViewportView(output_list);

        // Tạo nút Start của Director
        JButton btnStart = new JButton("Bắt đầu");
        btnStart.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                File[] output_videos = new File[0];

                try {
                    output_videos = DirectorClass.generate_videos();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

                DefaultListModel<String> updated_input_list_model = new DefaultListModel<>();
                updated_input_list_model.clear();
                input_list.setModel(updated_input_list_model);

                // Cập nhật danh sách các video output trên đường dẫn /video
                DefaultListModel<String> updated_output_list_model = new DefaultListModel<>();

                for (File video : output_videos) {
                    updated_output_list_model.addElement(video.getName());
                }

                output_list.setModel(updated_output_list_model);
                JOptionPane.showConfirmDialog(null, "Đã hoàn thành");
            }
        });

        btnStart.setFont(new Font("Tahoma", Font.PLAIN, 17));
        btnStart.setBounds(631, 76, 107, 34);
        pDirector.add(btnStart);

        // Buntton Refesh
        JButton btnRefresh = new JButton("Refresh");
        btnRefresh.setBounds(10, 11, 89, 23);
        pDirector.add(btnRefresh);
        btnRefresh.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                DefaultListModel<String> refreshListView = new DefaultListModel<>();
                refreshListView.clear();
                File[] refreshFolder = new File(input_dir).listFiles();
                for (File video : refreshFolder) {
                    refreshListView.addElement(video.getName());
                }
                input_list.setModel(refreshListView);
            }
        });

        // Button Chọn File
        final JFileChooser fileDialog = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Video Files", "mp4", "avi", "mkv");
        fileDialog.setFileFilter(filter);
        JButton showFileDialogButton = new JButton("Open File");
        showFileDialogButton.setBounds(100, 11, 89, 23);
        pDirector.add(showFileDialogButton);
        showFileDialogButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int returnVal = fileDialog.showOpenDialog(frame);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        File file = fileDialog.getSelectedFile();
                        Path newPath = Paths.get(input_dir, file.getName());
                        Files.copy(file.toPath(), newPath);
                    } catch (IOException ex) {
                    }
                } else {
                    log.debug("Failed to move the file");
                }
            }
        });

        // label ------------------------------------------
        JLabel lbListvd = new JLabel("Đầu vào");
        lbListvd.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lbListvd.setBounds(30, 45, 280, 23);
        pDirector.add(lbListvd);

        JLabel lbOut = new JLabel("Đầu ra");
        lbOut.setFont(new Font("Tahoma", Font.PLAIN, 15));
        lbOut.setBounds(340, 45, 69, 20);
        pDirector.add(lbOut);

        // Click Lable Director để chuyển Panel
        JLabel lbDirector = new JLabel("DIRECTOR");
        lbDirector.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pDirector.setVisible(true);
                pVideo.setVisible(false);

            }
        });
        lbDirector.setForeground(Color.WHITE);
        lbDirector.setFont(new Font("Tahoma", Font.PLAIN, 18));

        lbDirector.setBounds(55, 129, 95, 25);
        panel.add(lbDirector);

        // Click Lable List Video để chuyển Panel
        JLabel lbListVideo = new JLabel("LIST VIDEO");
        lbListVideo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pDirector.setVisible(false);
                pVideo.setVisible(true);

            }
        });
        lbListVideo.setForeground(Color.WHITE);
        lbListVideo.setFont(new Font("Tahoma", Font.PLAIN, 18));
        lbListVideo.setBounds(55, 179, 106, 25);
        panel.add(lbListVideo);

        // Combobox Bitrate
        bitrate = new JComboBox();
        bitrate.setBounds(32, 100, 171, 28);
        pVideo.add(bitrate);
        bitrate.addItem("0.2");
        bitrate.addItem("0.5");
        bitrate.addItem("1.0");
        bitrate.addItem("3.0");

        // Combox Format
        format = new JComboBox();
        format.setBounds(263, 100, 171, 28);
        pVideo.add(format);
        format.addItem("avi");
        format.addItem("mp4");
        format.addItem("mkv");

        JButton btnStream = new JButton("Phát");
        btnStream.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.debug("'Stream' button has been pressed");

                try {
                    // Gửi thông tin video đã chọn và phương thức streamming cho server
                    send_specs_to_server(output_stream);

                    // Đóng socket và luồng sau khi hoành thành giao tiếp
                    output_stream.close();
                    input_stream.close();
                    socket.close();

                    System.exit(0);	// Đóng giao diện của Client
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        btnStream.setBounds(32, 301, 124, 28);
        pVideo.add(btnStream);

        JButton btnSearch = new JButton("Lọc");
        btnSearch.setBounds(32, 139, 89, 28);
        pVideo.add(btnSearch);
        btnSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                log.debug("Connect button has been pressed");

                try {
                    // Gửi Bitrate và format của video tới server
                    // Nhận danh sách dựa trên Bitrate và format đã gửi trước đó
                    send_request_to_server(output_stream, input_stream);

                    // Tắt các component đã dùng khi nhận được respond từ server lần đầu tiên
                    bitrate.setEnabled(false);
                    format.setEnabled(false);
                    btnStart.setEnabled(false);
                    btnSearch.setEnabled(false);
                    // Bật các component sẽ dùng khi nhận được respond từ server lần đầu tiên   
                    video.setEnabled(true);
                    protocol.setEnabled(true);
                    btnStream.setEnabled(true);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        video = new JComboBox();
        video.setBounds(32, 211, 245, 34);
        pVideo.add(video);

        protocol = new JComboBox();
        protocol.setBounds(32, 256, 124, 34);
        pVideo.add(protocol);
        protocol.addItem("UDP");
        protocol.addItem("TCP");
        protocol.addItem("RTP/UDP");

    }

    /**
     * Code Client
     */
    void send_request_to_server(ObjectOutputStream output_stream, ObjectInputStream input_stream) throws Exception {
        ArrayList<String> request = new ArrayList<>();
        request.add(bitrate.getSelectedItem().toString());
        request.add(format.getSelectedItem().toString());

        log.debug("Sending request to server: " + bitrate.getSelectedItem().toString() + " bitrate and " + format.getSelectedItem().toString() + " format");

        output_stream.writeObject(request);	// Gửi Bitrate và format của video tới server đã chọn

        ArrayList<String> available_videos = (ArrayList<String>) input_stream.readObject(); // Nhận danh sách dựa trên Bitrate và format đã gửi trước đó
        log.debug("Received list of available videos to stream");
        // Lưu dữ liệu danh sách các video lên trên giao diện
        for (String current_video : available_videos) {
            video.addItem(current_video);
        }

        log.debug("Sent the list to the GUI");
    }

    void send_specs_to_server(ObjectOutputStream output_stream) throws Exception {
        ArrayList<String> stream_specs = new ArrayList<>();
        stream_specs.add(video.getSelectedItem().toString());
        stream_specs.add(protocol.getSelectedItem().toString());

        log.debug("Sending stream specs to server: " + video.getSelectedItem().toString() + " using " + protocol.getSelectedItem().toString());
        output_stream.writeObject(stream_specs);
        // Tạo tiến trình xử lý thông qua command line để chạy ffplay
        // Để chạy video stream với tùy chọn đã lựa chọn
        ArrayList<String> command_line_args = new ArrayList<>();

        command_line_args.add("ffmpeg/bin/ffmpeg/ffplay.exe");

        if (protocol.getSelectedItem().toString().equals("UDP")) {
            command_line_args.add("udp://127.0.0.1:6000");
        } else if (protocol.getSelectedItem().toString().equals("TCP")) {
            command_line_args.add("tcp://127.0.0.1:5100");
        } else 
        {
            command_line_args.add("-protocol_whitelist");
            command_line_args.add("file,rtp,udp");
            command_line_args.add("-i");
            command_line_args.add("video.sdp");
        }

        ProcessBuilder process_builder = new ProcessBuilder(command_line_args);
        Process streamer_client = process_builder.start();

        log.debug("Process to play the incoming stream started");
    }

}
