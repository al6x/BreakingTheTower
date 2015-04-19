package com.mojang.tower;

import java.util.Random;

public class Job
{
    protected Random random = new Random();

    public static class Goto extends Job
    {
        private Entity target;

        public Goto(Entity target)
        {
            this.target = target;
            bonusRadius = 15;
        }

        public boolean isValidTarget(Entity e)
        {
            return e == target;
        }

        public void arrived()
        {
            peon.setJob(null);
        }
    }
    
    public static class GotoAndConvert extends Job
    {
        private Entity target;

        public GotoAndConvert(Entity target)
        {
            this.target = target;
        }

        public boolean isValidTarget(Entity e)
        {
            return e  == target;
        }

        public void arrived()
        {
            if (island.warriorPopulation<island.warriorPopulationCap && island.resources.wood >= House.WOOD_PER_WARRIOR)
            {
                island.warriorPopulation++;
                island.resources.wood -= House.WOOD_PER_WARRIOR;
                peon.setType(1);
                ((House)target).puff();
                Sounds.play(new Sound.SpawnWarrior());
            }
            peon.setJob(null);
        }
    }
    
    public static class Hunt extends Job
    {
        private Monster target;

        public Hunt(Monster target)
        {
            this.target = target;
            bonusRadius = 5;
        }

        public boolean isValidTarget(Entity e)
        {
            return e instanceof Monster;
        }

        public void arrived()
        {
            target.fight(peon);
        }
    }    


    public static class Build extends Job
    {
        private House target;

        public Build(House target)
        {
            this.target = target;
        }

        public boolean isValidTarget(Entity e)
        {
            return e == target;
        }

        public void arrived()
        {
            if (target.build()) peon.setJob(null);
        }
    }

    public static class Plant extends Job
    {
        private Entity target;
        private boolean hasSeed = false;
        private Entity toPlant;

        public Plant(Entity target, int seed)
        {
            this.target = target;

            if (seed==0)
            {
                toPlant = new Tree(0, 0, 0);
            }
            else
            {
                toPlant = new FarmPlot(0, 0, 0);
            }
        }

        public boolean isValidTarget(Entity e)
        {
            if (!hasSeed)
                return e == target;
            else
                return false;
        }

        public boolean hasTarget()
        {
            if (!hasSeed) return super.hasTarget();
            
            double xt = peon.x + Math.cos(peon.rot) * 10;
            double yt = peon.y + Math.sin(peon.rot) * 10;
            toPlant.setPos(xt, yt);
            if (island.isFree(toPlant.x, toPlant.y, 8))
            {
                island.addEntity(toPlant);
                peon.setJob(null);
            }
            
            return false;
        }

        public void arrived()
        {
            if (!hasSeed)
            {
                hasSeed = true;
                bonusRadius = 15;
                boreTime = 300;
            }
            else
            {
            }
        }

        public int getCarried()
        {
            return hasSeed ? 3 : -1;
        }
    }

    public static class Gather extends Job
    {
        boolean hasResource = false;
        public int resourceId = 0;
        private House returnTo;
        

        public Gather(int id, House returnTo)
        {
            resourceId = id;
            this.returnTo = returnTo;
        }

        public int getCarried()
        {
            return hasResource ? resourceId : -1;
        }

        public boolean isValidTarget(Entity e)
        {
            if (!hasResource && e.givesResource(resourceId))
            {
                return true;
            }
            if (hasResource && e.acceptsResource(resourceId))
            {
                return true;
            }
            return false;
        }

        public void arrived()
        {
            if (!hasResource && target != null && target.givesResource(resourceId))
            {
                if (target.gatherResource(resourceId))
                {
                    hasResource = true;
                    target = returnTo;
                    peon.rot += Math.PI;
//                    tryFindTarget();
                }
                boreTime = 1000;
            }
            else if (hasResource && target != null && target.acceptsResource(resourceId))
            {
                if (target.submitResource(resourceId))
                {
                    hasResource = false;
                    target = null;
                    island.resources.add(resourceId, 1);
                    peon.setJob(null);
                }
            }
        }
    }

    protected Peon peon;
    protected Island island;
    public double xTarget, yTarget, targetDistance;
    protected Entity target;
    protected int bonusRadius = 2;
    public int boreTime = 500;

    public void init(Island island, Peon peon)
    {
        this.island = island;
        this.peon = peon;
    }

    public final void tick()
    {
        if (boreTime>0)
        {
            if (--boreTime==0) peon.setJob(null);
        }
    }

    public boolean isValidTarget(Entity e)
    {
        return false;
    }

    protected void tryFindTarget()
    {
        for (int i = 0; i < 5; i++)
        {
            Entity e = peon.getRandomTarget(10, 80, null);
            if (e != null && isValidTarget(e))
            {
                if (target == null || e.distance(peon) < target.distance(peon))
                {
                    setTarget(e);
                }
            }
        }
    }

    public boolean hasTarget()
    {
        Entity e = peon.getRandomTarget(5, 60, null);
        if (e != null && isValidTarget(e))
        {
            if (target == null || e.distance(peon) < target.distance(peon))
            {
                setTarget(e);
            }
        }
        if (target != null && !target.isAlive()) target = null;
        if (target == null) return false;

        xTarget = target.x;
        yTarget = target.y;
        targetDistance = target.r + bonusRadius;
        return true;
    }

    public void setTarget(Entity e)
    {
        target = e;
    }

    public void arrived()
    {
    }

    public void cantReach()
    {
        if (Math.random() < 0.1)
        {
            target = null;
        }
    }

    public void collide(Entity e)
    {
        if (isValidTarget(e))
        {
            setTarget(e);
        }
        else
        {
            cantReach();
        }
    }

    public int getCarried()
    {
        return -1;
    }
}