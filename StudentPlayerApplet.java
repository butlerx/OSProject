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

  public Player(String filename) { //constuctor for the Player class

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
        if(e.getActionCommand() == "x") {
          //system should exit
        }
        else if(e.getActionCommand() == "q") {
          //raises the volume
        }
        else if(e.getActionCommand() == "a") {
          //lowers the volume
        }
        else if(e.getActionCommand() == "p") {
          //pauses playback
        }
        else if(e.getActionCommand() == "r") {
          //resumes playback
        }
        else if(e.getActionCommand() == "m") {
          //mutes audio
        }
        else if(e.getActionCommand() == "u") {
          //unmutes audio
        }
        else {
    		  textarea.append("You said: " + e.getActionCommand() + "\n");
    		  textfield.setText("");
        }
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

      //BoundedBuffer buffer = new BoundedBuffer(10);
      //Thread cthread = new Thread(new Consumer(buffer));
      //Thread pthread = new Thread(new Producer(buffer));

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

class BoundedBuffer {
  private int nextIn; //pointer to the next available open index
  private int nextOut;  //pointer to the next audio chunk to be read
  private int ins;  //counter of chunks inserted
  private int outs; //counter of chunks read
  private int size; //size you want the buffer to be
  private boolean roomAvailable;  //true when the buffer has at least one free slot
  private boolean dataAvailable;  //true when the buffer has at least one full slot

  public BoundedBuffer(int size) {  //initialzes an empty buffer
    this.size = size;
    nextIn = 0;
    nextOut = 0;
    ins = 0;
    outs = 0;
    roomAvailable = true;
    dataAvailable = false;
  }
  private byte[][] buffer = new byte[size][]; //array of 1 second audio chunks

  public synchronized void insertChunk(byte [] chunk){ //inserts a single audio chunk into the buffer
    try {
      while(!roomAvailable){
        wait();
      }
      for(int i = 0; i < chunk.length; i++) {
        buffer[nextIn][i] = chunk[i];
      }
      nextIn = (nextIn++)%size;
      ins++;
      dataAvailable = true;
      if(ins - outs >= size) {
        roomAvailable = false;
      }
      if(!roomAvailable) {
        notifyAll();
      }
    } catch(InterruptedException e) {
        System.out.println("Producer Failed");
        e.printStackTrace();
        System.exit(1);
    }
  }

  public synchronized byte[] removeChunk(){ //returns a single audio chunk from the buffer
    byte [] chunk = new byte [buffer[nextOut].length];
    try {
      while(!dataAvailable) {
        wait();
      }
      for(int i = 0; i < chunk.length; i++) {
        chunk [i] = buffer[nextOut][i];
      }
      nextOut = (nextOut++)%size;
      outs++;
      roomAvailable = true;
      if(ins - outs == 0) {
        dataAvailable = false;
      }
      if(!dataAvailable) {
        notifyAll();
      }
    } catch(InterruptedException e) {
      System.out.println("Consumer Failed");
      e.printStackTrace();
      System.exit(1);
    }
    return chunk;
  }
}

public class StudentPlayerApplet extends Applet {
  private static final long serialVersionUID = 1L;
	public void init() {
		setLayout(new BorderLayout());
		add(BorderLayout.CENTER, new Player(getParameter("file")));
	}
}
