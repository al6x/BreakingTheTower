package com.mojang.tower;

import java.awt.Graphics2D;
import java.util.Random;

public class Entity implements Comparable<Entity>
{
    public double x, y, r;
    public double xr, yr;

    protected Island island;
    protected Bitmaps bitmaps;
    protected Random random = new Random();
    protected boolean alive = true;

    public Entity(double x, double y, double r)
    {
        this.x = x;
        this.y = y;
        this.r = r;
    }

    public void updatePos(double sin, double cos, double alpha)
    {
        xr = x * cos + y * sin;
        yr = x * sin - y * cos;
    }

    public void init(Island island, Bitmaps bitmaps)
    {
        this.island = island;
        this.bitmaps = bitmaps;
    }

    public void tick()
    {
    }

    public boolean isAlive()
    {
        return alive;
    }

    public boolean collides(Entity e)
    {
        return collides(e.x, e.y, e.r);
    }

    public boolean collides(double ex, double ey, double er)
    {
        if (r < 0) return false;

        double xd = x - ex;
        double yd = y - ey;
        return (xd * xd + yd * yd) < (r * r + er * er);
    }

    public int compareTo(Entity s)
    {
        return Double.compare(s.yr, yr);
    }

    public void render(Graphics2D g, double alpha)
    {
    }

    public double distance(Entity e)
    {
        double xd = x - e.x;
        double yd = y - e.y;
        return xd * xd + yd * yd;
    }

    public boolean givesResource(int resourceId)
    {
        return false;
    }

    public boolean gatherResource(int resourceId)
    {
        return false;
    }

    public boolean acceptsResource(int resourceId)
    {
        return false;
    }

    public boolean submitResource(int resourceId)
    {
        return false;
    }
    
    public Entity getRandomTarget(double radius, double rnd, TargetFilter filter)
    {
        double xt = x + (random.nextDouble() * 2 - 1) * rnd;
        double yt = y + (random.nextDouble() * 2 - 1) * rnd;
        return island.getEntityAt(xt, yt, radius, filter);
    }

    public void setPos(double xp, double yp)
    {
        x = xp;
        y = yp;
    }

    public void fight(Monster monster)
    {
    }
}