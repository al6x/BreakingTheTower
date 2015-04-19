package com.mojang.tower;

import java.awt.Graphics2D;

public class FarmPlot extends Entity
{
    public static final int GROW_SPEED = 200;
    private int age;
    private int stamina = 0;
    private int yield = 0;

    public FarmPlot(double x, double y, int age)
    {
        super(x, y, 0);
        this.yield = this.stamina = this.age = age;
    }

    public void tick()
    {
        if (age < 7 * GROW_SPEED)
        {
            age++;
            stamina++;
            yield++;
        }
    }

    public void render(Graphics2D g, double alpha)
    {
        int x = (int) (xr - 4);
        int y = -(int) (yr / 2 + 5);

        g.drawImage(bitmaps.farmPlots[7 - age / GROW_SPEED], x, y, null);
    }

    public void cut()
    {
        alive = false;
    }

    public boolean gatherResource(int resourceId)
    {
        stamina -= 64;
        if (stamina <= 0)
        {
            alive = false;
            return true;
        }
        return false;
    }

    public int getAge()
    {
        return age/GROW_SPEED;
    }
    
    public boolean givesResource(int resourceId)
    {
        return getAge()>6 && resourceId==Resources.FOOD;
    }
    
}