package com.mojang.tower;

import java.util.Random;

public abstract class Sound
{
    public static class Select extends Sound
    {
        public Select() { super(10); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                buffer[i] += ((i*3000/Sounds.SAMPLE_RATE)%2*180-90)*(duration-p)/duration;
            }
        }
    }
    
    public static class Plant extends Sound
    {
        int val = 0;
        public Plant() { super(250); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                if (p%20==0) val = random.nextInt(180)-90;
                buffer[i] += (val)*(duration-p)/duration;
            }
        }
    }
    
    public static class FinishBuilding extends Sound
    {
        int val = 0;
        public FinishBuilding() { super(50); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                if (p%11==0) val = random.nextInt(100)-50; 
                buffer[i] += (val);
            }
        }
    }

    public static class Gather extends Sound
    {
        int val = 0;
        public Gather() { super(10); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                if (p%4==0) val = random.nextInt(100)-50; 
                buffer[i] += (val);
            }
        }
    }
    
    public static class Destroy extends Sound
    {
        int val = 0;
        public Destroy() { super(1000); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                if (p%60==0) val = random.nextInt(256)-128; 
                buffer[i] += (val)*(duration-p)/duration*(duration-p)/duration;
            }
        }
    }
    
    public static class Spawn extends Sound
    {
        public Spawn() { super(200); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                int rate = 1000+p*2000/duration*p/duration;
                buffer[i] += ((i*rate/Sounds.SAMPLE_RATE)%2*180-90)*(duration-p)/duration*(duration-p)/duration/2;
            }
        }
    }
    
    public static class SpawnWarrior extends Sound
    {
        public SpawnWarrior() { super(400); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                int rate = 800-p*200/duration*p/duration;
                buffer[i] += ((i*rate/Sounds.SAMPLE_RATE)%2*180-90)*(duration-p)/duration*(duration-p)/duration/2;
            }
        }
    }
    
    public static class Ding extends Sound
    {
        public Ding() { super(200); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                double rate = 2000+p*1000/duration;
                if (p<duration/2)
                    rate = 5000-rate;
                buffer[i] += (int)(Math.sin(p*rate/Sounds.SAMPLE_RATE*Math.PI*2)*120)*(duration-p)/duration;
            }
        }
    }
    
    public static class WinSound extends Sound
    {
        public WinSound() { super(200*5); };
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                int d = duration/5;
                double volume = 1-(p%d)/(double)d;
                double rate = 440*Math.pow(2,p/d/6.0);
                double tone = Math.sin(p*rate/Sounds.SAMPLE_RATE*Math.PI*2)*120;
                buffer[i] += (int)(tone*volume);
            }
        }
    }

    public static class Death extends Sound
    {
        public Death() { super(250); };
        double noise = 0;
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                double rate = 3000-p*100/duration*p/duration;
                rate/=2;

                noise = noise+(random.nextDouble()*200-100-noise)*0.6;
                double smp2 = Math.sin(p*rate/Sounds.SAMPLE_RATE*Math.PI*2)*120;
                double smp1 =noise;
                double smp = smp1+(smp2-smp1)*p/duration;
                buffer[i] += (int)(smp)*(duration-p)/duration;
            }
        }
    }    
    
    public static class MonsterDeath extends Sound
    {
        public MonsterDeath() { super(250); };
        double noise = 0;
        
        protected void fill(int[] buffer, int len)
        {
            for (int i=0; i<len; i++, p++)
            {
                double rate = 1000+p*100/duration*p/duration;
                rate/=2;

                noise = noise+(random.nextDouble()*200-100-noise)*0.8;
                double smp2 = Math.sin(p*rate/Sounds.SAMPLE_RATE*Math.PI*2)*120;
                double smp1 =noise;
                double smp = smp1+(smp2-smp1)*p/duration;
                buffer[i] += (int)(smp)*(duration-p)/duration;
            }
        }
    }    
    
    protected int p = 0;
    protected int duration;
    protected Random random = new Random();
    
    protected Sound(int ms)
    {
        duration = Sounds.SAMPLE_RATE*ms/1000;
    }
    
    public boolean read(int[] buffer, int len)
    {
        if (p+len>duration) len = duration-p;
        fill(buffer, len);
        return p<=duration;
    }

    protected abstract void fill(int[] buffer, int len);
}
