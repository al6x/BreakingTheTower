package com.mojang.tower;

import java.util.ArrayList;
import java.util.List;

import javax.sound.sampled.*;

public class Sounds implements Runnable
{
    public static Sounds instance = new Sounds();
    private boolean soundAvailable = true;
    private static boolean isMute = false;
    public static final int SAMPLE_RATE = 44100;
    
    private SourceDataLine dataLine;
    private List<Sound> sounds = new ArrayList<Sound>();
    

    private Sounds()
    {
        if (!soundAvailable) return;
        
        try
        {
            AudioFormat audioFormat = new AudioFormat(SAMPLE_RATE, 8, 1, true, true);
            dataLine = AudioSystem.getSourceDataLine(audioFormat);
            dataLine.open(audioFormat, SAMPLE_RATE/10);
            dataLine.start();

            Thread thread = new Thread(this);
            thread.setDaemon(true);
            thread.start();
        }
        catch (Throwable e)
        {
            e.printStackTrace();
            soundAvailable = false;
        }
    }
    
    public static void play(Sound sound)
    {
        instance.addSound(sound);
    }
    
    public static void setMute(boolean mute)
    {
        isMute = mute;
    }

    public static boolean isMute()
    {
        return isMute;
    }

    private void addSound(Sound sound)
    {
        if (!soundAvailable) return;
        
        synchronized (sounds)
        {
            sounds.add(sound);
        }
    }

    public void run()
    {
        int bufferSize = 4096;
        int[] buffer = new int[4096];
        byte[] outBuffer = new byte[4096];
        while (true)
        {
            while (dataLine.available() < bufferSize)
            {
                try
                {
                    Thread.sleep(2);
                }
                catch (InterruptedException e)
                {
                }
            }

            int toRead = dataLine.available();
            if (toRead > bufferSize) toRead = bufferSize;

            synchronized (sounds)
            {
                for (int i=0; i<sounds.size(); i++)
                {
                    if (!sounds.get(i).read(buffer, toRead))
                    {
                        sounds.remove(i--);
                    }
                }
            }

            int val = 0;
            for (int i = 0; i < toRead; i++)
            {
                if (!isMute)
                {
                    val = buffer[i];
                    if (val<-128) val = -128;
                    if (val>127) val = 127;
                }
                
                buffer[i]=0;
                outBuffer[i] = (byte)val;
            }
            dataLine.write(outBuffer, 0, toRead);
        }
    }
}