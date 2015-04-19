package com.mojang.tower;

import java.awt.Color;
import java.awt.Graphics2D;

public class House extends Entity
{
    private static final int POPULATION_PER_RESIDENCE = 10;
    private static final int WARRIORS_PER_BARRACKS = 5;
    public static final int FOOD_PER_PEON = 5;
    public static final int WOOD_PER_WARRIOR = 5;

    private HouseType type;
    private int buildTime;
    private int buildDuration = 32 * 6;
    private int animFrame = 0;
    private int maxHp = 256;
    private int hp = maxHp;

    public House(double x, double y, HouseType type)
    {
        super(x, y, type.radius);
        this.type = type;
    }

    public void fight(Monster monster)
    {
        if (hp <= 0) return;
        if (--hp <= 0)
        {
            die();
        }
    }

    public void die()
    {
        Sounds.play(new Sound.Destroy());
        if (type == HouseType.RESIDENCE)
        {
            island.populationCap -= POPULATION_PER_RESIDENCE;
        }
        if (type == HouseType.BARRACKS)
        {
            island.warriorPopulationCap -= WARRIORS_PER_BARRACKS;
        }
        alive = false;
    }

    public void complete()
    {
        hp = maxHp;
        buildTime = buildDuration;
    }

    public boolean acceptsResource(int resourceId)
    {
        return buildTime >= buildDuration && type.acceptResource == resourceId;
    }

    public boolean submitResource(int resourceId)
    {
        if (buildTime >= buildDuration && type.acceptResource == resourceId)
        {
            Sounds.play(new Sound.Gather());
            puff();
            return true;
        }
        return false;
    }

    public boolean build()
    {
        if (buildTime < buildDuration)
        {
            buildTime++;
            if (hp < maxHp) hp += 1;
            if (hp > maxHp) hp = maxHp;
            if (buildTime == buildDuration)
            {
                Sounds.play(new Sound.FinishBuilding());
                if (type == HouseType.RESIDENCE)
                {
                    island.populationCap += POPULATION_PER_RESIDENCE;
                }
                if (type == HouseType.BARRACKS)
                {
                    island.warriorPopulationCap += WARRIORS_PER_BARRACKS;
                }
            }
        }
        return buildTime == buildDuration;
    }

    public void tick()
    {
        animFrame++;
        if (buildTime < buildDuration)
        {
            for (int i = 0; i < 2; i++)
            {
                Peon peon = getRandomPeon(100, 80, true);
                if (peon != null && peon.job == null)
                {
                    peon.setJob(new Job.Build(this));
                }
            }
        }
        else
        {
            if (hp < maxHp && random.nextInt(4) == 0)
            {
                hp++;
            }

            Peon peon = getRandomPeon(50, 50, true);
            if (peon != null && peon.job == null && peon.type == 0)
            {
                TargetFilter noMobFilter = new TargetFilter()
                {
                    public boolean accepts(Entity e)
                    {
                        return !(e instanceof Peon || e instanceof Monster);
                    }                    
                };
                if (type == HouseType.MASON)
                {
                    peon.setJob(new Job.Gather(Resources.ROCK, this));
                }
                else if (type == HouseType.WOODCUTTER)
                {
                    peon.setJob(new Job.Gather(Resources.WOOD, this));
                }
                else if (type == HouseType.WINDMILL)
                {
                    peon.setJob(new Job.Gather(Resources.FOOD, this));
                }
                else if (type == HouseType.PLANTER)
                {
                    if (getRandomTarget(6, 40, noMobFilter)==null)
                        peon.setJob(new Job.Plant(this, 0));
                }
                else if (type == HouseType.FARM)
                {
                    if (getRandomTarget(6, 40, noMobFilter)==null)
                        peon.setJob(new Job.Plant(this, 1));
                }
            }

            if (type == HouseType.GUARDPOST)
            {
                peon = getRandomPeon(80, 80, true);
                if (peon != null && peon.job == null && (peon.type==0 && random.nextInt(2)==0))
                {
                    peon.setJob(new Job.Goto(this));
                }
            }

            if (type == HouseType.BARRACKS && island.warriorPopulation < island.warriorPopulationCap && island.resources.wood >= WOOD_PER_WARRIOR)
            {
                peon = getRandomPeon(80, 80, true);
                if (peon != null && peon.job == null && peon.type == 0)
                {
                    peon.setJob(new Job.GotoAndConvert(this));
                }
            }

            if (type == HouseType.RESIDENCE && island.population < island.populationCap && island.resources.food >= FOOD_PER_PEON && random.nextInt(20) == 0)
            {
                double xt = x + (random.nextDouble() * 2 - 1) * 9;
                double yt = y + (random.nextDouble() * 2 - 1) * 9;

                peon = new Peon(xt, yt, 0);
                if (island.isFree(peon.x, peon.y, peon.r))
                {
                    puff();
                    island.resources.food -= FOOD_PER_PEON;
                    island.addEntity(peon);
                    Sounds.play(new Sound.Spawn());
                }
            }
        }
    }

    public void destroy()
    {
        if (type == HouseType.RESIDENCE)
        {
            island.populationCap -= POPULATION_PER_RESIDENCE;
        }
        if (type == HouseType.BARRACKS)
        {
            island.warriorPopulationCap -= POPULATION_PER_RESIDENCE;
        }
    }

    private Peon getRandomPeon(double r, double s, final boolean mustBeFree)
    {
        TargetFilter peonFilter = new TargetFilter()
        {
            public boolean accepts(Entity e)
            {
                return e.isAlive() && (e instanceof Peon) && (!mustBeFree || ((Peon) e).job == null);
            }
        };

        Entity e = getRandomTarget(r, s, peonFilter);
        if (e instanceof Peon)
        {
            Peon peon = (Peon) e;
            return peon;
        }
        return null;
    }

    public void render(Graphics2D g, double alpha)
    {
        int x = (int) (xr - 8);
        int y = -(int) (yr / 2 + 16 - 4);

        if (type == HouseType.GUARDPOST) y -= 2;
        if (type == HouseType.WINDMILL) y -= 1;

        if (buildTime < buildDuration)
        {
            g.drawImage(bitmaps.houses[0][buildTime * 6 / buildDuration], x, y, null);
        }
        else
        {
            g.drawImage(type.getImage(bitmaps), x, y, null);
        }

        if (hp < maxHp)
        {
            g.setColor(Color.BLACK);
            g.fillRect(x + 4, y - 2, 8, 1);
            g.setColor(Color.RED);
            g.fillRect(x + 4, y - 2, hp * 8 / maxHp, 1);
        }
    }

    public void puff()
    {
        island.addEntity(new Puff(x, y));
    }

    public void sell()
    {
        island.resources.wood += type.wood * 3 * hp / (maxHp * 4);
        island.resources.rock += type.rock * 3 * hp / (maxHp * 4);
        die();
    }
}