package webcam;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.apache.commons.codec.binary.Base64;
import org.bridj.Pointer;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamUtils;
import com.github.sarxos.webcam.ds.buildin.natives.Device;
import com.github.sarxos.webcam.ds.buildin.natives.DeviceList;
import com.github.sarxos.webcam.ds.buildin.natives.OpenIMAJGrabber;
import com.github.sarxos.webcam.util.ImageUtils;

public class LocalView {

	public static Boolean running = false;
	static Webcam webcam;
	static DataOutputStream os ;

	public static void main(String[] args) {

		/**
		 * This example show how to use native OpenIMAJ API to capture raw bytes
		 * data as byte[] array. It also calculates current FPS.
		 */
		// OpenIMAJGrabber grabber = new OpenIMAJGrabber();
		Socket s = null;
		read r = null;
		while (true) {
			while (s == null) {
				try {
					System.out.println("loading...");
					s = new Socket("113.250.156.162", 11111);
				} catch (Exception e) {
					try {
						Thread.sleep(6000);
					} catch (InterruptedException e1) {
					}
					continue;
				}
				r = new read(s);
				r.start();
			}

			try {
				os = new DataOutputStream(s.getOutputStream());

				if(webcam == null)
					webcam = Webcam.getDefault();

				while (running) {

					// creates test1.jpg
					WebcamUtils.capture(webcam, "test1", ImageUtils.FORMAT_JPG);
					byte[] raw_image = WebcamUtils.getImageBytes(webcam, "jpg");
					System.out.println(raw_image.length);
					/* Apply a crude kind of image compression. */
					byte[] compressed_image = Compressor.compress(raw_image);
					/* Prepare the date to be sent in a text friendly format. */
					byte[] base64_image = Base64.encodeBase64(compressed_image);

					os.writeInt(base64_image.length);
					os.write(base64_image);
					os.flush();
				}
				System.out.println("read to looked");
				Thread.sleep(6000);
			} catch (Exception e) {
				try {
					System.out.println("closed");
					running = false;
					s.close();
					r.interrupt();
					r = null;
					s = null;
				} catch (IOException e1) {
				}
				continue;
			}
		}
	}

}

class read extends Thread {
	Socket socket;

	public read(Socket s) {
		this.socket = s;
	}

	public void run() {
		DataInputStream dis;
		try {
			dis = new DataInputStream(socket.getInputStream());

			while (true && socket != null) {

				if (dis.read() > 0) {
					if (LocalView.running == false) {
						LocalView.running = true;
					} else {
						socket.close();
						LocalView.webcam.close();
						LocalView.webcam = null;
						LocalView.running = false;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
