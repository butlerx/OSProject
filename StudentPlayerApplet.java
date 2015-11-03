import javax.sound.sampled.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

class Player extends Panel implements Runnable {
  private static final long serialVersionUID = 1L;
  private TextField textfield;
  private TextArea textarea;
  private Font font;
  private String filename;

  public Player(String filename){

    font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    textfield = new TextField();
    textarea = new TextArea();
    textarea.setFont(font);
    textfield.setFont(font);
    setLayout(new BorderLayout());
    add(BorderLayout.SOUTH, textfield);
    add(BorderLayout.CENTER, textarea);

    textfield.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
		  textarea.append("You said: " + e.getActionCommand() + "\n");
		  textfield.setText("");
      }
    });

    this.filename = filename;
    new Thread(this).start();
  }

  public void run() {

  	try {
      AudioInputStream s = AudioSystem.getAudioInputStream(new File(filename));
  	  AudioFormat format = s.getFormat();
  	  System.out.println("Audio format: " + format.toString());

  	  DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);
  	  if (!AudioSystem.isLineSupported(info)) {
        throw new UnsupportedAudioFileException();
      }

  	  int oneSecond = (int) (format.getChannels() * format.getSampleRate() *
        format.getSampleSizeInBits() / 8);
  	  byte[] audioChunk = new byte[oneSecond];

  	  SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
  	  line.open(format);
  	  line.start();
  	  int bytesRead = s.read(audioChunk);
  	  line.write(audioChunk, 0, bytesRead);
  	  line.drain();
  	  line.stop();
  	  line.close();
    } catch (UnsupportedAudioFileException e ) {
        System.out.println("Player initialisation failed");
  	    e.printStackTrace();
  	    System.exit(1);
  	} catch (LineUnavailableException e) {
  	    System.out.println("Player initialisation failed");
  	    e.printStackTrace();
  	    System.exit(1);
  	} catch (IOException e) {
  	    System.out.println("Player initialisation failed");
  	    e.printStackTrace();
  	    System.exit(1);
    }
  }
}

public class StudentPlayerApplet extends Applet {
  private static final long serialVersionUID = 1L;
	public void init() {
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, new Player(getParameter("file")));
	}
}
