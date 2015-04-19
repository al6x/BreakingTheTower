package com.mojang.tower;

import java.awt.Graphics2D;

public class InfoPuff extends Entity
{
    public void updatePos(double sin, double cos, double alpha)
    {
        super.updatePos(sin, cos, alpha);
        yr-=2;
    }

    public double xa, ya, za;
    public double z = 0;
    public int life;
    public int lifeTime;
    private int image;

    public InfoPuff(double x, double y, int image)
    {
        super(x, y, -1);
        this.image = image;
        z = 12;
        za = 0.3;
        life = 0;
        lifeTime = 80+random.nextInt(60);
    }

    public void tick()
    {
        xa *= 0.99;
        ya *= 0.99;
        za *= 0.99;
        x += xa;
        y += ya;
        z += za;

        if (life++ == lifeTime) alive = false;
    }

    public void render(Graphics2D g, double alpha)
    {
        int x = (int) (xr - 8);
        int y = -(int) (yr / 2 + 4 + (z + za * alpha));

            g.drawImage(bitmaps.infoPuffs[image], x, y, null);
    }
}