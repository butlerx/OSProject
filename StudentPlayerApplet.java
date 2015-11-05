import javax.sound.sampled.*;
import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

class Buffer{
  private int nextIn = 0;
  private int nextOut = 0;
  //int size;
  private int occupied = 0;
  private int chunkSize;
  //int ins;
  //int outs;
  private boolean dataAvailable = false;
  private boolean roomAvailable = true;

  private byte[][] audioChunk;

  public Buffer(AudioFormat format){
  chunkSize = (int) (format.getChannels() * format.getSampleRate() * format.getSampleSizeInBits() / 8);
  audioChunk = new byte[chunkSize][10];
}

  public synchronized void insertChunk(byte[] temp) throws InterruptedException{
      while(roomAvailable == false){
      wait();
    }
  //SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
  //>line.open(format);
  //>line.start();
    System.out.println("chunk inserting " + nextIn);
    //insert a chunk
    audioChunk[nextIn] = temp;
    //s.read(audioChunk[nextIn]);
    //move nextin forward
    nextIn = (nextIn + 1)%10;
    //say there is data available
    dataAvailable = true;
    occupied++;
    if(occupied == 9)
    roomAvailable = false;
    notifyAll();
  }

  public synchronized byte[] removeChunk() throws InterruptedException{
      while(dataAvailable == false){
      wait();
    }
    System.out.println("chunk removing " + nextOut);
    //read a chunk
    byte[] temp = audioChunk[nextOut];
    //move nextout forward
    nextOut = (nextOut + 1)%10;
    //say there is room available
    roomAvailable = true;
    occupied--;
    if(occupied == 0)
    dataAvailable = false;
    notifyAll();
    return temp;
  }
}

class Producer implements Runnable{
  private Buffer buffer;
  private AudioInputStream s;
  private byte[] temp;
  private int chunkSize;

  public Producer(Buffer buffer, AudioInputStream s){
    this.buffer = buffer;
    this.s = s;
    AudioFormat format = s.getFormat();
    int chunkSize = (int) (format.getChannels() * format.getSampleRate() * format.getSampleSizeInBits() / 8);
    temp = new byte[chunkSize];
    this.chunkSize = chunkSize;
  }

  public void run(){
    try{
      while(true){
        byte[] temp = new byte[chunkSize];
        s.read(temp);
        buffer.insertChunk(temp);
      }

    } catch (InterruptedException b) {System.out.println("Producer shutting down"); return; }
    catch (IOException e) {}
  }
}

class Consumer implements Runnable{
  private Buffer buffer;
  private SourceDataLine line;
  private int chunkSize;
  public Consumer(Buffer buffer, SourceDataLine line, AudioFormat format){
    this.buffer = buffer;
    this.line = line;
    this.chunkSize = (int) (format.getChannels() * format.getSampleRate() * format.getSampleSizeInBits() / 8);
  }

  public void run(){
    try{
      while(true){
        line.write(buffer.removeChunk(), 0, chunkSize);
      }
    } catch (InterruptedException e) {System.out.println("Consumer shutting down"); return; }
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
          producerThread.interrupt();
          line.flush();
          line.stop();
          line.close();
          consumerThread.interrupt();
          System.out.println("Progam exiting.");
          System.exit(0);
          break;
        case "q":
          //raise volume
          break;
        case "a":
          //lower volume
          break;
        case "p":
          //pause playback
          break;
        case "r":
          //resume playback
          break;
        case "m":
          //mute audio
          break;
        case "u":
          //unmute
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

      buffer = new Buffer(format);

      producerThread = new Thread(new Producer(buffer, s));
      consumerThread = new Thread(new Consumer(buffer, line, format));

      producerThread.start();
      consumerThread.start();

      line.start();
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
