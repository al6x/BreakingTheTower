package com.mojang.tower;

import java.awt.Graphics2D;

public class Tree extends Entity
{
    public static final int GROW_SPEED = 320;
    public static final int SPREAD_INTERVAL = 30000;
    private int age;
    private int spreadDelay;
    private int stamina = 0;
    private int yield = 0;

    public Tree(double x, double y, int age)
    {
        super(x, y, 4);
        this.yield = this.stamina = this.age = age;
        spreadDelay = random.nextInt(SPREAD_INTERVAL);
    }

    public void tick()
    {
        if (age < 15 * GROW_SPEED)
        {
            age++;
            stamina++;
            yield++;
        }
        else if (spreadDelay-- <= 0 && SPREAD_INTERVAL>0)
        {
            double xp = x + random.nextGaussian() * 8;
            double yp = y + random.nextGaussian() * 8;
            Tree tree = new Tree(xp, yp, 0);

            if (island.isFree(tree.x, tree.y, tree.r)) island.addEntity(tree);

            spreadDelay = SPREAD_INTERVAL;
        }
    }

    public void render(Graphics2D g, double alpha)
    {
        int x = (int) (xr - 4);
        int y = -(int) (yr / 2 + 16);

        g.drawImage(bitmaps.trees[15 - age / GROW_SPEED], x, y, null);
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
        return getAge()>6 && resourceId==Resources.WOOD;
    }
    
}