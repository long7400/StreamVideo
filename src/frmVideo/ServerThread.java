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
                // receive the request (bitrate and format) from client and
                ArrayList<String> received_request = (ArrayList<String>) input_stream.readObject();
                float selected_bitrate = Float.parseFloat(received_request.get(0));
                String selected_format = received_request.get(1);

                server_log.append("Received request for " + selected_bitrate + " bitrate and " + selected_format + " format from " + name+"\n");

                ArrayList<String> available_videos = new ArrayList<>(); // list of videos available to stream

                // for each video in the /videos/ directory...
                for (File video : videos_list) {
                    String current_video = video.getName();

                    // take the last 3 characters of the filenames (e.g. .avi, .mp4, .mkv) 
                    // and compare them to the received format to filter out the rest of the videos at another format
                    if (current_video.substring(current_video.length() - 3).equals(selected_format)) {
                        // split the current video filename at the '-' character (e.g. 'Test-0.2Mbps.avi' to 'Test' and '0.2Mbps.avi')
                        // to filter out the 3 characters that signify the bitrate of the video
                        // and turn it to a floating point number
                        String[] splitted_video_name = (current_video).split("-");

                        for (String s : splitted_video_name) {
                            if (s.contains("Mbps")) {
                                float current_video_bitrate = Float.parseFloat(s.substring(0, 3));
                                // if the current video is in equal or less bitrate, it can be streamed
                                // so add it to the list of  videos available to stream
                                if (selected_bitrate >= current_video_bitrate) {
                                    available_videos.add(current_video);
                                }
                            }
                        }

                    }
                }

                // send the list of videos that are available to stream based on the specified format and bitrate
                output_stream.writeObject(available_videos);

                // receive the selected video and the protocol specification to stream with
                ArrayList<String> stream_specs = (ArrayList<String>) input_stream.readObject();
                String selected_video = stream_specs.get(0);
                String selected_protocol = stream_specs.get(1);

                server_log.append("Using the " + selected_protocol + " protocol to stream '" + selected_video + "' from " + name + "\n");

                String videos_dir_fullpath = System.getProperty("user.dir") + "/videos";

                // create a process through the command line to run the ffplay program
                // to play the incoming streamed video with the appropriate arguments
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
                //log for debug
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
