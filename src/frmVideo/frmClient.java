package frmVideo;


import javax.swing.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import Modul.DirectorClass;

public class frmClient {
	
	
	DefaultListModel<String> list_model = new DefaultListModel<>();
	String input_dir = "raw_videos/";
	File[] raw_videos = new File(input_dir).listFiles();
	/*
	 * Client
	 */
	static Logger log = LogManager.getLogger(frmClient.class);
	
	private Socket socket;
	private ObjectOutputStream output_stream;
	private ObjectInputStream input_stream;
	private JComboBox bitrate;
	private JComboBox format;
	private JComboBox video;
	private JComboBox protocol;
	
	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					frmClient window = new frmClient();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 */
	public frmClient() throws UnknownHostException, IOException {
		initialize();
	}
		/**
	 * Initialize the contents of the frame.
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


		JPanel pDirector = new JPanel();
		pDirector.setBounds(183, 0, 808, 536);
		frame.getContentPane().add(pDirector);
		pDirector.setLayout(null);
		
		JPanel pVideo = new JPanel();
		pVideo.setBounds(183, 0, 808, 536);
		frame.getContentPane().add(pVideo);
		pVideo.setLayout(null);
		pDirector.setVisible(false);
		pVideo.setVisible(false);
		for(File video : raw_videos)
			  list_model.addElement(video.getName());
			JList input_list = new JList(list_model);
			input_list.setBounds(33, 76, 250, 360);
			pDirector.add(input_list);
			
			JScrollPane scrollPane_1 = new JScrollPane();
			scrollPane_1.setBounds(340, 76, 250, 360);
			pDirector.add(scrollPane_1);
			//--------------------------------------
			
			// Output Videos List Layout
			JList output_list = new JList();
			scrollPane_1.setViewportView(output_list);
			
			JButton btnStart = new JButton("B\u1EAFt \u0111\u1EA7u");
			btnStart.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					File[] output_videos = new File[0];

					try
					{
						output_videos = DirectorClass.generate_videos();
					}
					catch (IOException ioException)
					{
						ioException.printStackTrace();
					}

					DefaultListModel<String> updated_input_list_model = new DefaultListModel<>();
					updated_input_list_model.clear();
					input_list.setModel(updated_input_list_model);

					// update the output video list from /videos
					DefaultListModel<String> updated_output_list_model = new DefaultListModel<>();

					for(File video : output_videos)
						updated_output_list_model.addElement(video.getName());

					output_list.setModel(updated_output_list_model);
					JOptionPane.showConfirmDialog(null, "Đã hoàn thành");
				}
			});
			btnStart.setFont(new Font("Tahoma", Font.PLAIN, 17));
			btnStart.setBounds(631, 76, 107, 34);
			pDirector.add(btnStart);
			
			JButton btnRefresh = new JButton("Refresh");
			btnRefresh.setBounds(10, 11, 89, 23);
			pDirector.add(btnRefresh);
			
			JLabel lbListvd = new JLabel("Đầu vào");
			lbListvd.setFont(new Font("Tahoma", Font.PLAIN, 15));
			lbListvd.setBounds(30, 45, 280, 23);
			pDirector.add(lbListvd);
			
			JLabel lbOut = new JLabel("Đầu ra");
			lbOut.setFont(new Font("Tahoma", Font.PLAIN, 15));
			lbOut.setBounds(340, 45, 69, 20);
			pDirector.add(lbOut);
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
		
		JLabel lbListVdieo = new JLabel("LIST VIDEO");
		lbListVdieo.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pDirector.setVisible(false);
				pVideo.setVisible(true);

			}
		});
		lbListVdieo.setForeground(Color.WHITE);
		lbListVdieo.setFont(new Font("Tahoma", Font.PLAIN, 18));
		lbListVdieo.setBounds(55, 179, 106, 25);
		panel.add(lbListVdieo);
		
		
		
		bitrate = new JComboBox();
		bitrate.setBounds(32, 100, 171, 28);
		pVideo.add(bitrate);
		bitrate.addItem("0.2");
		bitrate.addItem("0.5");
		bitrate.addItem("1.0");
		bitrate.addItem("3.0");
		
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

				try
				{
					// send the specifications (selected video and protocol) to the server
					// and stream the incoming video through ffplay
					send_specs_to_server(output_stream);

					// close the socket and streams from the client when all communications are done
					output_stream.close();
					input_stream.close();
					socket.close();

					System.exit(0);	// close the GUI window of the client
				}
					catch (Exception e1)
					{
					e1.printStackTrace();
				}
			}
		});
		btnStream.setBounds(32, 301, 124, 28);
		pVideo.add(btnStream);
		
		JButton btnSreach = new JButton("Lọc");
		btnSreach.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				log.debug("Connect button has been pressed");

				try
				{
					// send the request (bitrate and format) to the server
					// and receive a list of videos based on the request
					send_request_to_server(output_stream, input_stream);

					// gray out the components already used for the first response of the server
					bitrate.setEnabled(false);
					format.setEnabled(false);
					btnStart.setEnabled(false);

					// enable the components to be used for the second response of the server
					video.setEnabled(true);
					protocol.setEnabled(true);
					btnStream.setEnabled(true);
				}
				catch (Exception e1)
				{
					e1.printStackTrace();
				}
			}
		});
		btnSreach.setBounds(32, 139, 89, 28);
		pVideo.add(btnSreach);
		
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

	void send_request_to_server(ObjectOutputStream output_stream, ObjectInputStream input_stream) throws Exception
	{
		ArrayList<String> request = new ArrayList<>();
		request.add(bitrate.getSelectedItem().toString());
		request.add(format.getSelectedItem().toString());
		
		log.debug("Sending request to server: " + bitrate.getSelectedItem().toString() + " bitrate and " + format.getSelectedItem().toString() + " format");
		
		output_stream.writeObject(request);	// send the request with the selected bitrate and format
		
		ArrayList<String> available_videos = (ArrayList<String>) input_stream.readObject(); // receive a list of videos based on the request
		log.debug("Received list of available videos to stream");
		
		// fill the dropdown menu of the videos on the gui with the contents of this list
		for(String current_video : available_videos)
			video.addItem(current_video);	
		
		log.debug("Sent the list to the GUI");
	}
	
	void send_specs_to_server(ObjectOutputStream output_stream) throws Exception
	{
		ArrayList<String> stream_specs = new ArrayList<>();
		stream_specs.add(video.getSelectedItem().toString());
		stream_specs.add(protocol.getSelectedItem().toString());
	
		log.debug("Sending stream specs to server: " + video.getSelectedItem().toString() + " using " + protocol.getSelectedItem().toString());
		output_stream.writeObject(stream_specs);
		
		// create a process through the command line to run the ffplay program
		// to play the incoming streamed video with the appropriate arguments
		ArrayList<String> command_line_args = new ArrayList<>();

		command_line_args.add("ffplay");
		
		if(protocol.getSelectedItem().toString().equals("UDP"))
			command_line_args.add("udp://127.0.0.1:6000");
		else if(protocol.getSelectedItem().toString().equals("TCP"))
			command_line_args.add("tcp://127.0.0.1:5100");
		else	// for RTP/UDP, mention the session description protocol file to the server 
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
