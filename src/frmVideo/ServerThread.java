/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package frmVideo;

import static frmVideo.frmServer.log;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import javax.swing.JTextArea;

/**
 *
 * @author longf
 */
public class ServerThread implements Runnable {

    private Socket socket;
    private String name;
    private JTextArea server_log;
    private ObjectInputStream input_stream = null;
    private ObjectOutputStream output_stream = null;

    public ServerThread(Socket socket, String name, JTextArea server_log) throws IOException {
        this.socket = socket;
        this.name = name;
        this.server_log = server_log;
        this.input_stream = new ObjectInputStream(this.socket.getInputStream());
        this.output_stream = new ObjectOutputStream(this.socket.getOutputStream());
        new Thread(this).start();
    }

    public void run() {
        try {
            log.debug("Listening for requests...\n");
            File[] videos_list = new File("videos/").listFiles();
            while (true) {

//                ObjectInputStream input_stream = new ObjectInputStream(socket.getInputStream());
//                ObjectOutputStream output_stream = new ObjectOutputStream(socket.getOutputStream());
                // Nhận thông tin từ request gồm (bitrate and format) từ client và xử lý
                ArrayList<String> received_request = (ArrayList<String>) input_stream.readObject();
                float selected_bitrate = Float.parseFloat(received_request.get(0));
                String selected_format = received_request.get(1);

                server_log.append("Received request for " + selected_bitrate + " bitrate and " + selected_format + " format from " + name+"\n");

                ArrayList<String> available_videos = new ArrayList<>(); // list of videos available to stream

                // Vòng lặp lấy danh sách các file trong đường dẫn /videos
                for (File video : videos_list) {
                    String current_video = video.getName();

                    // Lấy 3 ký tự cuối từ filenames (e.g. .avi, .mp4, .mkv) 
                    // So sánh với format mà Client request để lọc
                    if (current_video.substring(current_video.length() - 3).equals(selected_format)) {
                        // Tách chuỗi tên ra tên video và bitrate của nó (e.g. 'Test-0.2Mbps.avi' to 'Test' and '0.2Mbps.avi')
                        // Lọc chuỗi để lấy số bitrate và chuyển nó thành số
                        String[] splitted_video_name = (current_video).split("-");

                        for (String s : splitted_video_name) {
                            if (s.contains("Mbps")) {
                                float current_video_bitrate = Float.parseFloat(s.substring(0, 3));
                                // Nếu bitrate của video đang xét thấp hơn hoặc bằng với bitrate đã chọn từ client thì sẽ có thể stream
                                // Thêm nó vào danh sách các video có thể stream
                                if (selected_bitrate >= current_video_bitrate) {
                                    available_videos.add(current_video);
                                }
                            }
                        }

                    }
                }

                // Gửi dữ liệu các video có thể stream dựa trên bitrate và format
                output_stream.writeObject(available_videos);

                // Nhận được thông tin của video đã chọn từ client và phương thức sử dụng
                ArrayList<String> stream_specs = (ArrayList<String>) input_stream.readObject();
                String selected_video = stream_specs.get(0);
                String selected_protocol = stream_specs.get(1);

                server_log.append("Using the " + selected_protocol + " protocol to stream '" + selected_video + "' from " + name + "\n");

                String videos_dir_fullpath = System.getProperty("user.dir") + "/videos";

                // Khởi tạo tiến trình thông qua commandline dựa vào các thông tin để chạy ffplay
                // Để chạy video stream với tùy chọn đã lựa chọn
                ArrayList<String> command_line_args = new ArrayList<>();

                command_line_args.add("ffmpeg/bin/ffmpeg/ffmpeg.exe");

                if (selected_protocol.equals("UDP")) {
                    command_line_args.add("-i");
                    command_line_args.add(videos_dir_fullpath + "/" + selected_video);
                    command_line_args.add("-f");
                    command_line_args.add("mpegts");
                    command_line_args.add("udp://127.0.0.1:6000?pkt_size=1316");
                } else if (selected_protocol.equals("TCP")) {
                    command_line_args.add("-re");
                    command_line_args.add("-i");
                    command_line_args.add(videos_dir_fullpath + "/" + selected_video);
                    command_line_args.add("-codec");
                    command_line_args.add("copy");
                    command_line_args.add("-f");
                    command_line_args.add("mpegts");
                    command_line_args.add("tcp://127.0.0.1:5100?listen");
                } else {
                    command_line_args.add("-re");
                    command_line_args.add("-i");
                    command_line_args.add(videos_dir_fullpath + "/" + selected_video);
                    command_line_args.add("-an");
                    command_line_args.add("-c:v");
                    command_line_args.add("copy");
                    command_line_args.add("-f");
                    command_line_args.add("rtp");
                    command_line_args.add("-sdp_file");
                    command_line_args.add(System.getProperty("user.dir") + "/video.sdp");
                    command_line_args.add("rtp://127.0.0.1:5004?rtcpport=5008");
                }

                ProcessBuilder process_builder = new ProcessBuilder(command_line_args);
                Process streamer_host = process_builder.start();
                //log để debug
                BufferedReader br = new BufferedReader(new InputStreamReader(streamer_host.getErrorStream()));
                String line = "";
                while ((line = br.readLine()) != null) {
                    log.debug("Video Info [" + line + "] ");
                }

            }
        } catch (Exception ex) {
            server_log.append(name + " has departed\n\n");
        } finally {
            try {
                output_stream.close();
                input_stream.close();
                socket.close();
            } catch (IOException e) {
            }
        }
    }

}
