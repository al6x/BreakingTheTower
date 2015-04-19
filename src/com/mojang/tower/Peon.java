package com.mojang.tower;

import java.awt.Color;
import java.awt.Graphics2D;

public class Peon extends Entity
{
    private static final int[] animSteps = { 0, 1, 0, 2 };
    private static final int[] animDirs = { 2, 0, 3, 1 };
    public double rot = 0;
    public double moveTick = 0;
    public int type;
    private int wanderTime = 0;
    protected Job job;

    protected double xTarget, yTarget;

    private int hp = 100;
    private int maxHp = 100;
    private int xp = 0;
    private int nextLevel = 1;
    private int level = 0;

    public Peon(double x, double y, int type)
    {
        super(x, y, 1);
        this.type = type;
        rot = random.nextDouble() * Math.PI * 2;
        moveTick = random.nextInt(4 * 3);
    }

    public void init(Island island, Bitmaps bitmaps)
    {
        super.init(island, bitmaps);
        island.population++;
    }

    public void fight(Monster monster)
    {
        if (job == null && (type == 1 || random.nextInt(10) == 0))
        {
            setJob(new Job.Hunt(monster));
        }
        if (type == 0)
        {
            monster.fight(this);
            if ((hp -= 4) <= 0) die();
        }
        else
        {
            monster.fight(this);
            if (--hp <= 0) die();
        }
    }

    public void die()
    {
        Sounds.play(new Sound.Death());
        island.population--;
        if (type == 1)
        {
            island.warriorPopulation--;
        }
        alive = false;
    }

    public void setJob(Job job)
    {
        this.job = job;
        if (job != null) job.init(island, this);
    }

    public void tick()
    {
        if (job != null)
        {
            job.tick();
        }

        if (type == 1 || job == null) for (int i = 0; i < 15 && (job==null || job instanceof Job.Goto); i++)
        {
            TargetFilter monsterFilter = new TargetFilter()
            {
                public boolean accepts(Entity e)
                {
                    return e.isAlive() && (e instanceof Monster);
                }                  
            };
            Entity e = type == 0 ? getRandomTarget(30, 15, monsterFilter) : getRandomTarget(70, 80, monsterFilter);
            if (e instanceof Monster)
            {
                setJob(new Job.Hunt((Monster) e));
            }
        }

        if (hp < maxHp && random.nextInt(5) == 0)
        {
            hp++;
        }
        /*        if (target == null || !target.isAlive() || random.nextInt(200) == 0)
                {
                    target = getRandomTarget();
                    if (!(target instanceof Tree))
                    {
                        target = null;
                    }
                }*/

        double speed = 1;
        if (wanderTime == 0 && job != null && job.hasTarget())
        {
            double xd = job.xTarget - x;
            double yd = job.yTarget - y;
            double rd = job.targetDistance + r;
            if (xd * xd + yd * yd < rd * rd)
            {
                job.arrived();
                speed = 0;
            }
            rot = Math.atan2(yd, xd);
//            rot += (random.nextDouble() - 0.5) * random.nextDouble();
        }
        else
        {
            rot += (random.nextDouble() - 0.5) * random.nextDouble()*2;
        }

        if (wanderTime > 0) wanderTime--;
        
        speed+=level*0.1;

        double xt = x + Math.cos(rot) * 0.4 * speed;
        double yt = y + Math.sin(rot) * 0.4 * speed;
        if (island.isFree(xt, yt, r, this))
        {
            x = xt;
            y = yt;
        }
        else
        {
            if (job != null)
            {
                Entity collided = island.getEntityAt(xt, yt, r, null, this);
                if (collided != null)
                {
                    job.collide(collided);
                }
                else
                {
                    job.cantReach();
                }
            }
//            rot += random.nextInt(2) * 2 - 1 * Math.PI / 2 + (random.nextDouble() - 0.5);
            rot = (random.nextDouble())*Math.PI*2;
            wanderTime = random.nextInt(30)+3;
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

        int carrying = -1;
        if (job != null) carrying = job.getCarried();

        if (carrying >= 0)
        {
            g.drawImage(bitmaps.peons[2][animDirs[rotStep & 3] * 3 + animStep], x, y, null);
            g.drawImage(bitmaps.carriedResources[carrying], x, y - 3, null);
        }
        else
        {
            g.drawImage(bitmaps.peons[type][animDirs[rotStep & 3] * 3 + animStep], x, y, null);
        }

        if (hp < maxHp)
        {
            g.setColor(Color.BLACK);
            g.fillRect(x + 2, y - 2, 4, 1);
            g.setColor(Color.RED);
            g.fillRect(x + 2, y - 2, hp * 4 / maxHp, 1);
        }
        
        if (level>0)
        {
            
        }
    }

    public void setType(int i)
    {
        this.type = i;
        hp = maxHp = type == 0 ? 20 : 100;
    }

    public void addXp()
    {
        xp++;
        if (xp==nextLevel)
        {
            nextLevel = nextLevel*2+1;
            island.addEntity(new InfoPuff(x, y, 0));
            hp+=10;
            maxHp+=10;
            level++;
            Sounds.play(new Sound.Ding());
        }
    }
}