package com.mojang.tower;

import java.awt.Graphics2D;

public class Tower extends Entity
{
    private static final boolean DEBUG = false;
    private int h = 0;
    private int staminaPerLevel = 4096 * 2;
    private int stamina = staminaPerLevel;
    private int minMonsters = 3;

    public Tower(double x, double y)
    {
        super(x, y, 16);
        h = 80;
    }

    public void tick()
    {
        if (random.nextInt(100) == 0 && island.monsterPopulation < minMonsters)
        {
            double xt = x + (random.nextDouble() * 2 - 1) * (r + 5);
            double yt = y + (random.nextDouble() * 2 - 1) * (r + 5);

            Monster monster = new Monster(xt, yt);
            if (island.isFree(monster.x, monster.y, monster.r))
            {
                if (!DEBUG) island.addEntity(monster);
            }
        }
    }

    public void render(Graphics2D g, double alpha)
    {
        int x = (int) (xr - 16);
        int y = -(int) (yr / 2);

        for (int i = 0; i < h / 8; i++)
        {
            g.drawImage(bitmaps.towerMid, x, y - 8 - i * 8, null);
        }
        g.drawImage(bitmaps.towerMid, x, y - h - 1, null);
        g.drawImage(bitmaps.towerBot, x, y, null);
        g.drawImage(bitmaps.towerTop, x, y - h - 8, null);
    }

    public boolean gatherResource(int resourceId)
    {
        stamina -= 64;
        if (stamina <= 0)
        {
            for (int i = 0; i < 1;)
            {
                double xt = x + (random.nextDouble() * 2 - 1) * (r + 5);
                double yt = y + (random.nextDouble() * 2 - 1) * (r + 5);

                Monster monster = new Monster(xt, yt);
                if (island.isFree(monster.x, monster.y, monster.r))
                {
                    if (!DEBUG) island.addEntity(monster);
                    i++;
                }
            }
            stamina += staminaPerLevel;
            if (DEBUG)
            {
                stamina = 0;
            }
            if (h % 20 == 0) minMonsters++;
            if (--h <= 4)
            {
                island.win();
                alive = false;
            }
            return true;
        }
        return false;
    }

    public boolean givesResource(int resourceId)
    {
        return resourceId == Resources.ROCK;
    }
}