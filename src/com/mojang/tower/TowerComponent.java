package com.mojang.tower;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import java.io.IOException;
import java.util.Collections;

public class TowerComponent extends Canvas implements Runnable, MouseListener, MouseMotionListener
{
    public static final int TICKS_PER_SECOND = 30;
    private static final int MAX_TICKS_PER_FRAME = 10;
    private static final long serialVersionUID = 1L;

    private boolean running;
    private int width, height;
    private VolatileImage image;
    private Thread thread;
    private int tickCount;
    private int frames;
    private boolean paused;

    private int xMouse = -1, yMouse;
    private double xRot, xRotA;

    Bitmaps bitmaps = new Bitmaps();
    private Island island;

    private boolean scrolling = false;
    private double xScrollStart;

    private int xCenter;
    private int yCenter;
    private int selectedHouseType = 0;
    private boolean titleScreen = true, won = false;
    private int gameTime = 0, winScore = 0, wonTime;

    public TowerComponent(int width, int height)
    {
        this.width = width;
        this.height = height;
        setSize(width * 2, height * 2);
        addMouseMotionListener(this);
        addMouseListener(this);

        xCenter = width / 2;
        yCenter = height * 43 / 70;
    }

    public void unpause()
    {
        if (thread == null)
        {
            start();
        }
        paused = false;
    }

    public void pause()
    {
        paused = true;
    }

    public void paint(Graphics g)
    {
    }

    public void update(Graphics g)
    {
    }

    public void start()
    {
        thread = new Thread(this);
        thread.start();
    }

    public void stop()
    {
        running = false;
        try
        {
            if (thread != null) thread.join();
        }
        catch (InterruptedException e)
        {
        }
    }

    private void init()
    {
        try
        {
            bitmaps.loadAll();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        island = new Island(this, bitmaps.island);
    }

    public void run()
    {
        init();
        float lastTime = (System.nanoTime() / 1000000) / 1000.0f;
        running = true;

        double msPerTick = 1.0 / TICKS_PER_SECOND;

        while (running)
        {
            synchronized (this)
            {
                float now = (System.nanoTime() / 1000000) / 1000.0f;
                int frameTicks = 0;
                while (now - lastTime > msPerTick)
                {
                    if (!paused && frameTicks++ < MAX_TICKS_PER_FRAME) tick();

                    lastTime += msPerTick;
                }

                if (!paused)
                {
                    render((now - lastTime) / msPerTick);
                }
            }

            try
            {
                Thread.sleep(paused ? 200 : 4);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }

    private void tick()
    {
        if (won) wonTime++;
        xRot += xRotA;
        xRotA *= 0.7;

        if (titleScreen || won)
        {
            xRotA -= 0.002;
        }
        else
        {
            if (scrolling)
            {
                double xd = xMouse - xScrollStart;
                xRotA -= xd / 10000.0;
            }
            else
            {
                if (xMouse >= 0 && yMouse<height*2-20*2 && yMouse>80)
                {
                    if (xMouse < 80) xRotA += 0.02;
                    if (xMouse > width * 2 - 80) xRotA -= 0.02;
                }
            }
        }

        island.rot = xRot;

        tickCount++;
        if (tickCount % TICKS_PER_SECOND == 0)
        {
            //            System.out.println(frames + " fps");
            frames = 0;
        }

        if (!titleScreen && !won)
        {
            gameTime++;
        }
        if (!titleScreen) island.tick();
    }

    private void render(double alpha)
    {
        frames++;
        BufferStrategy bs = getBufferStrategy();
        if (bs == null)
        {
            createBufferStrategy(2);
            bs = getBufferStrategy();
        }

        if (image == null)
        {
            image = createVolatileImage(width, height);
        }

        if (bs != null)
        {
            Graphics2D g = image.createGraphics();
            renderGame(g, alpha);
            g.dispose();

            Graphics gg = bs.getDrawGraphics();
            gg.drawImage(image, 0, 0, width * 2, height * 2, 0, 0, width, height, null);
            gg.dispose();
            bs.show();
        }
    }

    private void renderGame(Graphics2D g, double alpha)
    {
        int seconds = gameTime / TowerComponent.TICKS_PER_SECOND;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;

        String timeStr = "";
        if (hours > 0)
        {
            timeStr += hours + ":";
            if (minutes < 10) timeStr += "0";
        }
        timeStr += minutes + ":";
        if (seconds < 10) timeStr += "0";
        timeStr += seconds;


        g.setColor(new Color(0x4379B7));
        g.fillRect(0, 0, width, height);

        if (!titleScreen && !won)
        {
            g.setColor(new Color(0x87ADFF));
            g.fillRect(0, 0, width, 40);
        }

        double rot = xRot + xRotA * alpha;
        double sin = Math.sin(rot);
        double cos = Math.cos(rot);

        for (int i = 0; i < island.entities.size(); i++)
        {
            Entity e = island.entities.get(i);
            e.updatePos(sin, cos, alpha);
        }

        Collections.sort(island.entities);

        AffineTransform af = g.getTransform();
        g.translate(xCenter, yCenter);
        g.scale(1.5, 1.5);
        g.scale(1, 0.5);
        g.rotate(-rot);
        g.translate(-128, -128);
        g.drawImage(bitmaps.island, 0, 0, null);
        g.setTransform(af);

        g.translate(xCenter, yCenter);

        for (int i = 0; i < island.entities.size(); i++)
            island.entities.get(i).render(g, alpha);

        if (!titleScreen && !won)
        {
            if (selectedHouseType >= 0)
            {
                boolean canPlace = island.canPlaceHouse(xMouse - xCenter * 2, yMouse - yCenter * 2, HouseType.houseTypes[selectedHouseType]);
                if (canPlace)
                {
                    g.drawImage(HouseType.houseTypes[selectedHouseType].getImage(bitmaps), xMouse / 2 - xCenter - 8, yMouse / 2 - yCenter - 11, null);
                }
            }
            else
            {
                Entity e = island.getEntityAtMouse(xMouse - xCenter * 2, yMouse - yCenter * 2 + 3, new TargetFilter()
                {
                    public boolean accepts(Entity e)
                    {
                        return e instanceof House;
                    }
                });

                if (e != null)
                {
                    g.drawImage(bitmaps.delete, xMouse / 2 - xCenter - 8, yMouse / 2 - yCenter - 11, null);
                }
            }
        }

        g.setTransform(af);
        g.setFont(new Font("Sans-Serif", Font.PLAIN, 10));

        if (titleScreen)
        {
            g.drawImage(bitmaps.logo, (width - bitmaps.logo.getWidth()) / 2, 16, null);

            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(0x000000));
            for (int i = 0; i < 2; i++)
            {
                if ((tickCount / 10 % 2) == 0)
                {
                    String str = "Click to start the game";
                    g.drawString(str, (width - fm.stringWidth(str)) / 2 - i, height - 16 - i);
                }

                g.setColor(new Color(0xffffff));
            }
        }
        else if (won)
        {
            g.drawImage(bitmaps.wonScreen, (width - bitmaps.logo.getWidth()) / 2, 16, null);
            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(0x000000));
            for (int i = 0; i < 2; i++)
            {

                if (i == 1) g.setColor(new Color(0xffff00));
                String str = "Time: " + timeStr + ", Score: " + winScore;
                g.drawString(str, (width - fm.stringWidth(str)) / 2 - i, height * 45 / 100 - i);

                if (i == 1) g.setColor(new Color(0xffffff));
                if (wonTime >= TICKS_PER_SECOND * 3 && (tickCount / 10 % 2) == 0)
                {
                    str = "Click to continue playing";
                    g.drawString(str, (width - fm.stringWidth(str)) / 2 - i, height - 16 - i);
                }

            }
        }
        else
        {
            for (int i = -1; i < HouseType.houseTypes.length; i++)
            {
                int x = i * 20 + (width - (HouseType.houseTypes.length + 1) * 20) / 2;
                int y = 4;
                if (i == -1)
                {
                    g.drawImage(bitmaps.delete, x, y, null);
                }
                else
                {
                    g.drawImage(HouseType.houseTypes[i].getImage(bitmaps), x, y, null);
                }
                if (i == selectedHouseType)
                {
                    g.setColor(Color.WHITE);
                    g.drawRect(x - 2, y - 2, 19, 19);
                }
            }

            FontMetrics fm = g.getFontMetrics();
            g.setColor(new Color(0x477D8F));
            for (int i = 0; i < 2; i++)
            {
                int x = selectedHouseType * 20 + (width - (HouseType.houseTypes.length + 1) * 20) / 2;
                int y = 4 + 28;

                if (selectedHouseType >= 0)
                {
                    HouseType ht = HouseType.houseTypes[selectedHouseType];

                    String s = ht.getString();
                    g.drawString(s, x + 8 - fm.stringWidth(s) / 2 - i, y - i);
                    s = ht.getDescription();
                    g.drawString(s, x + 8 - fm.stringWidth(s) / 2 - i, y - i + 11);
                }
                else
                {
                    String s = "Sell building";
                    g.drawString(s, x + 8 - fm.stringWidth(s) / 2 - i, y - i);
                    s = "Returns 75% of wood and rock used";
                    g.drawString(s, x + 8 - fm.stringWidth(s) / 2 - i, y - i + 11);
                }

                String tmp = "Wood: 9999";
                g.drawString("Wood: " + island.resources.wood, width - 4 - i - fm.stringWidth(tmp), 12 - i + 11 * 0);
                g.drawString("Rock: " + island.resources.rock, width - 4 - i - fm.stringWidth(tmp), 12 - i + 11 * 1);
                g.drawString("Food: " + island.resources.food, width - 4 - i - fm.stringWidth(tmp), 12 - i + 11 * 2);

                String pop = "Population: " + island.population + " / " + island.populationCap;
                g.drawString(pop, 4 - i, 12 - i + 11 * 1);
                pop = "Warriors: " + island.warriorPopulation + " / " + island.warriorPopulationCap;
                g.drawString(pop, 4 - i, 12 - i + 11 * 2);

                g.drawString("Time: " + timeStr, 4 - i, 12 - i + 11 * 0);
                g.setColor(Color.WHITE);
            }
        }
        
        g.drawImage(bitmaps.soundButtons[Sounds.isMute()?1:0], width-20, height-20, null);
    }

    public void mouseClicked(MouseEvent me)
    {
    }

    public void mouseEntered(MouseEvent me)
    {
    }

    public void mouseExited(MouseEvent me)
    {
        xMouse = -1;
    }

    public void mousePressed(MouseEvent me)
    {
        synchronized (this)
        {
            if (me.getX()>=width*2-40 && me.getY()>=height*2-40 && me.getX()<=width*2-40+32 && me.getY()<=height*2-40+32)
            {
                Sounds.setMute(!Sounds.isMute());
                return;
            }
            
            if (titleScreen)
            {
                titleScreen = false;
                return;
            }
            if (won)
            {
                if (wonTime >= TICKS_PER_SECOND * 3)
                {
                    won = false;
                }
                return;
            }
            if (me.getButton() == 1)
            {
                int xm = me.getX() / 2;
                int ym = me.getY() / 2;
                for (int i = -1; i < HouseType.houseTypes.length; i++)
                {
                    int x = i * 20 + (width - (HouseType.houseTypes.length + 1) * 20) / 2;
                    int y = 4;

                    if (xm >= x - 2 && ym >= y - 2 && xm < x + 18 && ym < y + 18)
                    {
                        if (selectedHouseType!=i)
                        {
                            selectedHouseType = i;
                            Sounds.play(new Sound.Select());
                        }
                    }
                }

                if (selectedHouseType >= 0)
                {
                    island.placeHouse(me.getX() - xCenter * 2, me.getY() - yCenter * 2, HouseType.houseTypes[selectedHouseType]);
                }
                else
                {
                    Entity e = island.getEntityAtMouse(xMouse - xCenter * 2, yMouse - yCenter * 2 + 3, new TargetFilter()
                    {
                        public boolean accepts(Entity e)
                        {
                            return e instanceof House;
                        }
                    });
                    if (e != null)
                    {
                        ((House) e).sell();
                    }
                }
            }
            if (me.getButton() == 3)
            {
                xScrollStart = me.getX();
                scrolling = true;
            }
        }
    }

    public void mouseReleased(MouseEvent me)
    {
        if (me.getButton() == 3)
        {
            scrolling = false;
        }
    }

    public void mouseDragged(MouseEvent me)
    {
        xMouse = me.getX();
        yMouse = me.getY();
    }

    public void mouseMoved(MouseEvent me)
    {
        xMouse = me.getX();
        yMouse = me.getY();
    }


    public static void main(String[] args)
    {
        final TowerComponent tower = new TowerComponent(512, 320);

        Frame frame = new Frame("Breaking the Tower");
        frame.add(tower);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.addWindowListener(new WindowAdapter()
        {
            public void windowClosing(WindowEvent we)
            {
                tower.stop();
                System.exit(0);
            }
        });
        frame.setVisible(true);
        tower.start();
    }

    public void win()
    {
        won = true;
        winScore = 100000 * (TowerComponent.TICKS_PER_SECOND * 60 * 30) / (gameTime);
    }
}
