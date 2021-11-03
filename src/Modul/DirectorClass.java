package Modul;

import java.io.*;

import java.nio.file.Files;
import java.util.HashMap;
import org.apache.logging.log4j.*;
import net.bramp.ffmpeg.*;
import net.bramp.ffmpeg.builder.FFmpegBuilder;


public class DirectorClass {
	static Logger log = LogManager.getLogger(DirectorClass.class);
	
	public static File[] generate_videos() throws NullPointerException, IOException
	{
		// setting up the relative paths of the directories to fetch and store videos
		String input_dir_str = "raw_videos/";
		String output_dir_str = "videos/";
		File output_dir = new File(output_dir_str);
		
		FFmpeg ffmpeg = null;
		FFprobe ffprobe = null;

		try
		{
			log.debug("Initialising FFMpegClient");
			ffmpeg = new FFmpeg("ffmpeg/bin/ffmpeg/ffmpeg.exe");
			ffprobe = new FFprobe("ffmpeg/bin/ffprobe/ffprobe.exe");
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		// array of raw videos from the /raw_videos directory
		File[] raw_videos = new File(input_dir_str).listFiles();
		
		// array of video formats to generate
		String[] video_formats = {".avi", ".mp4", ".mkv"};
		
		// hashmap of the video bitrates in both float (Mbps) and long (bps) data types
		HashMap<Float, Long> video_bitrates = new HashMap<>();
		video_bitrates.put(0.2f, 200000L);		//0.2Mbps
		video_bitrates.put(0.5f, 500000L);		//0.5Mbps
		video_bitrates.put(1.0f, 1000000L);		//1Mbps
		video_bitrates.put(3.0f, 3000000L);		//3Mbps

		if(!output_dir.exists())
			Files.createDirectories(output_dir.toPath());
		
		// scanning each raw video...
		for(File video : raw_videos)
		{
			System.out.println("Raw video found: " + video.getName());
			String current_video_name = video.getName().replaceFirst("[.][^.]+$", "").replaceAll(" ", "_");

			// for each video format...
			for(String format : video_formats)
			{
				// and for each bitrate...
				for (Float bitrate : video_bitrates.keySet()) 
				{
					System.out.println("Converting '" + current_video_name + "' to '" + format + "' with " + bitrate + "Mbps bitrate");
					
					// generate the video file 
					// with the appropriate bitrate tag at the title
					// and the appropriate video format extension
					log.debug("Creating the transcoding");
					FFmpegBuilder builder = (new FFmpegBuilder()
								.setInput(input_dir_str + video.getName())
								.addOutput(output_dir_str + current_video_name + "-" + bitrate + "Mbps" + format))
								.setVideoBitRate(video_bitrates.get(bitrate))
								.done();
					
					log.debug("Creating the executor");
					FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
					
					log.debug("Starting the transcoding");
					// Run a one-pass encode
					executor.createJob(builder).run();
					
					log.debug("Transcoding finished");	
				}
			}
		}	
		
		// deleting all videos in the /raw_videos directory
		for(File video : raw_videos)
		{
			System.out.println("Deleting '" + video.getName() + "'...");
			video.delete();
		}
		
		System.out.println("Done!");
		
		return output_dir.listFiles();
	}
}
