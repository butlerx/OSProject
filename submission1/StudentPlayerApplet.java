import javax.sound.sampled.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;

class Buffer{
  private int size;
  private int nextIn = 0;
  private int nextOut = 0;
  private int occupied = 0;
  private int chunkSize;
  private boolean dataAvailable = false;
  private boolean roomAvailable = true;
  private boolean finished = false;

  private byte[][] audioChunk;

  public Buffer(AudioFormat format, int size){
    chunkSize = (int) (format.getChannels() * format.getSampleRate() * format.getSampleSizeInBits() / 8);
    audioChunk = new byte[chunkSize][size];
    this.size = size;
  }

  public synchronized void insertChunk(byte[] temp) throws InterruptedException{
      while(roomAvailable == false){
      wait();
    }
    //insert a chunk into buffer
    audioChunk[nextIn] = temp;
    //move nextin forward
    nextIn = (nextIn + 1)%size;
    dataAvailable = true;
    occupied++;
    if(occupied == size) {
      roomAvailable = false;
    }
    notifyAll();
  }

  public synchronized byte[] removeChunk() throws InterruptedException{
      while(dataAvailable == false){
      wait();
    }
    //read a chunk
    byte[] temp = audioChunk[nextOut];
    //move nextout forward
    nextOut = (nextOut + 1)%size;
    roomAvailable = true;
    occupied--;
    if(occupied == 0){
      dataAvailable = false;
    }
    notifyAll();
    return temp;
  }
  //set the finished variable to true indicating there is no more data to read
  public synchronized void finish() throws InterruptedException {
    finished = true;
  }
  //check the finished variable to see if there willl be mroe data inserted
  public synchronized boolean finished() throws InterruptedException {
    return finished;
  }
}

class Producer implements Runnable{
  private Buffer buffer;
  private AudioInputStream s;
  private int chunkSize;

  public Producer(Buffer buffer, AudioInputStream s){
    this.buffer = buffer;
    this.s = s;
    AudioFormat format = s.getFormat();
    int chunkSize = (int) (format.getChannels() * format.getSampleRate() * format.getSampleSizeInBits() / 8);
    this.chunkSize = chunkSize;
  }

  public void run(){
    try{
      while(s.available() > 0){
        byte[] temp = new byte[chunkSize];
        s.read(temp);
        buffer.insertChunk(temp);
      }
      buffer.finish();
    } catch (IOException e) {}
    catch (InterruptedException b) {
    } finally {
        System.out.println("Producer shutting down");
        return;
    }
  }
}

class Consumer implements Runnable{
  private Buffer buffer;
  private SourceDataLine line;
  private int chunkSize;
  public AtomicBoolean playing;
  public Consumer(Buffer buffer, SourceDataLine line, AudioFormat format, AtomicBoolean b){
    this.playing = b;
    this.buffer = buffer;
    this.line = line;
    this.chunkSize = (int) (format.getChannels() * format.getSampleRate() * format.getSampleSizeInBits() / 8);
  }

  public void run(){
    try{
      while(!buffer.finished()){
        if(playing.get()){
          line.write(buffer.removeChunk(), 0, chunkSize);
        }
      }
    } catch (InterruptedException e) {
    } finally {
      System.out.println("Consumer shutting down");
      return;
    }
  }
}

class Player extends Panel implements Runnable{
  private static final long serialVersionUID = 1L;
  private TextField textfield;
  private TextArea textarea;
  private Font font;
  private String filename;
  private Thread producerThread;
  private Thread consumerThread;
  private Buffer buffer;
  private SourceDataLine line;
  private FloatControl gainControl;
  private float volume = (float) 0.0;
  private boolean muted = false;
  public AtomicBoolean playing = new AtomicBoolean(true);

  public Player(String filename)
  {
    font = new Font(Font.SANS_SERIF, Font.PLAIN, 12);
    textfield = new TextField();
    textarea = new TextArea();
    textarea.setFont(font);
    textfield.setFont(font);
    setLayout(new BorderLayout());
    add(BorderLayout.SOUTH, textfield);
    add(BorderLayout.CENTER, textarea);

    textfield.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e){
        textarea.append("You said: " + e.getActionCommand() + "\n");
        switch (e.getActionCommand()){
        case "x":
          //closes the program
          producerThread.interrupt();
          line.flush();
          line.stop();
          line.close();
          consumerThread.interrupt();
          try { //force thread shutdown order
            producerThread.join();
            consumerThread.join();
          }catch (InterruptedException f) {}
          break;
        case "q":
          //raise volume
	  if(!muted){
	    if(volume < (float) 6){
	      volume = volume + (float) 1.0;
	      textarea.append("volume set to " + volume + "\n");
	      gainControl.setValue(volume);
            }
	  }
          break;
        case "a":
          //lower volume
          if(!muted){
	    if(volume > (float) -80.0){
	      volume = volume - (float) 1.0;
	      textarea.append("volume set to " + volume + "\n");
	      gainControl.setValue(volume);
            }
	  }
          break;
        case "p":
          //pause playback
          if(playing.get()){
            playing.set(false);
            textarea.append("playback paused \n");
          }
          break;
        case "r":
          //resume playback
          if(!playing.get()){
            playing.set(true);
            textarea.append("playback resumed \n");
          }
          break;
        case "m":
          //mute audio
	  if(!muted){
            gainControl.setValue((float) -80.0);
            muted = true;
            textarea.append("playback muted \n");
	  }
          break;
        case "u":
          //unmute audio
	  if(muted){
	    gainControl.setValue(volume);
	    muted = false;
            textarea.append("playback unmuted \n");
	  }
          break;
        }
        textfield.setText("");
      }
    });
    this.filename = filename;
    new Thread(this).start();
  }

  public void run(){
    try{
      AudioInputStream s = AudioSystem.getAudioInputStream(new File(filename));
      AudioFormat format = s.getFormat();

      DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

      if (!AudioSystem.isLineSupported(info)){
        throw new UnsupportedAudioFileException();
      }
      line = (SourceDataLine) AudioSystem.getLine(info);
      line.open(format);

      gainControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);

      buffer = new Buffer(format, 10);

      producerThread = new Thread(new Producer(buffer, s));
      consumerThread = new Thread(new Consumer(buffer, line, format, playing));

      producerThread.start();
      consumerThread.start();

      line.start();
      while(consumerThread.isAlive()) {}
      System.out.println("Progam exiting.");
      System.exit(0);
    }

    catch (UnsupportedAudioFileException e ){
    System.out.println("Player initialisation failed");
    e.printStackTrace();
    System.exit(1);
    }

    catch (LineUnavailableException e){
    System.out.println("Player initialisation failed");
    e.printStackTrace();
    System.exit(1);
    }

    catch (IOException e){
    System.out.println("Player initialisation failed");
    e.printStackTrace();
    System.exit(1);
    }
  }
}

public class StudentPlayerApplet extends Applet{
  private static final long serialVersionUID = 1L;
  public void init(){
    setLayout(new BorderLayout());
    add(BorderLayout.CENTER, new Player(getParameter("file")));
  }
}
