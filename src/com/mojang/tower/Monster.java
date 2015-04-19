package com.mojang.tower;

import java.awt.Color;
import java.awt.Graphics2D;

public class Monster extends Entity
{
    private static final int[] animSteps = { 0, 1, 0, 2 };
    private static final int[] animDirs = { 2, 0, 3, 1 };
    public double rot = 0;
    public double moveTick = 0;
    private int wanderTime = 0;

    protected Entity target;
    private int hp = 100;
    private int maxHp = 100;

    public Monster(double x, double y)
    {
        super(x, y, 2);
        rot = random.nextDouble() * Math.PI * 2;
        moveTick = random.nextInt(4 * 3);
    }

    public void init(Island island, Bitmaps bitmaps)
    {
        super.init(island, bitmaps);
        island.monsterPopulation++;
    }

    public void die()
    {
        Sounds.play(new Sound.MonsterDeath());
        island.monsterPopulation--;
        alive = false;
    }

    public void tick()
    {
        if (hp < maxHp && random.nextInt(16) == 0)
        {
            hp++;
        }

        if (target == null || random.nextInt(100) == 0)
        {
            Entity e = getRandomTarget(60, 30, new TargetFilter()
            {
                public boolean accepts(Entity e)
                {
                    return e.isAlive() && (e instanceof House || e instanceof Peon);
                }
            });
            if (e instanceof House || e instanceof Peon)
            {
                target = e;
            }
        }
        if (target != null && !target.isAlive()) target = null;

        double speed = 1;
        if (wanderTime == 0 && target != null)
        {
            double xd = target.x - x;
            double yd = target.y - y;
            double rd = target.r + r + 2;
            if (xd * xd + yd * yd < rd * rd)
            {
                speed = 0;
                target.fight(this);
            }
            rot = Math.atan2(yd, xd);
        }
        else
        {
            rot += (random.nextDouble() - 0.5) * random.nextDouble();
        }

        if (wanderTime > 0) wanderTime--;

        double xt = x + Math.cos(rot) * 0.3 * speed;
        double yt = y + Math.sin(rot) * 0.3 * speed;
        if (island.isFree(xt, yt, r, this))
        {
            x = xt;
            y = yt;
        }
        else
        {
            rot += random.nextInt(2) * 2 - 1 * Math.PI / 2 + (random.nextDouble() - 0.5);
            wanderTime = random.nextInt(30);
        }

        moveTick += speed;


        super.tick();
    }

    public void render(Graphics2D g, double alpha)
    {
        int rotStep = (int) Math.floor((rot - island.rot) * 4 / (Math.PI * 2) + 0.5);
        int animStep = animSteps[(int) (moveTick / 4) & 3];

        int x = (int) (xr - 4);
        int y = -(int) (yr / 2 + 8);
        g.drawImage(bitmaps.peons[3][animDirs[rotStep & 3] * 3 + animStep], x, y, null);

        if (hp < maxHp)
        {
            g.setColor(Color.BLACK);
            g.fillRect(x + 2, y - 2, 4, 1);
            g.setColor(Color.RED);
            g.fillRect(x + 2, y - 2, hp * 4 / maxHp, 1);
        }
    }

    public void fight(Peon peon)
    {
        if (hp <= 0) return;
        if (random.nextInt(5) == 0) target = peon;
        if (--hp <= 0)
        {
            die();
            peon.addXp();
        }
    }
}