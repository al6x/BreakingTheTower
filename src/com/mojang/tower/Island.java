package com.mojang.tower;

import java.awt.image.*;
import java.util.*;

public class Island
{
    private TowerComponent tower;
    public BufferedImage image;
    private int[] pixels;

    List<Entity> entities = new ArrayList<Entity>();
    private Random random = new Random(8844);

    public Resources resources = new Resources();

    public double rot;
    public int population = 0;
    public int populationCap = 10;
    public int monsterPopulation = 0;

    public int warriorPopulation = 0;
    public int warriorPopulationCap = 0;

    public Island(TowerComponent tower, BufferedImage image)
    {
        this.tower = tower;
        this.image = image;

        pixels = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        for (int i = 0; i < 1;)
        {
            double x = (random.nextDouble() * 256 - 128) * 1.5;
            double y = (random.nextDouble() * 256 - 128) * 1.5;

            Tower t = new Tower(x, y);
            if (isFree(t.x, t.y, t.r))
            {
                addEntity(t);
                i++;
            }
        }

        for (int i = 0; i < 7; i++)
        {
            double x = (random.nextDouble() * 256 - 128) * 1.5;
            double y = (random.nextDouble() * 256 - 128) * 1.5;
            addRocks(x, y);
        }
        for (int i = 0; i < 20; i++)
        {
            double x = (random.nextDouble() * 256 - 128) * 1.5;
            double y = (random.nextDouble() * 256 - 128) * 1.5;
            addForrest(x, y);
        }

        double xStart = 40;
        double yStart = -120;
        House house = new House(xStart, yStart, HouseType.GUARDPOST);
        house.complete();
        addEntity(house);

        for (int i = 0; i < 10;)
        {
            double x = xStart + (random.nextDouble() * 32 - 16);
            double y = yStart + (random.nextDouble() * 32 - 16);

            Peon peon = new Peon(x, y, 0);
            if (isFree(peon.x, peon.y, peon.r))
            {
                addEntity(peon);
                i++;
            }
        }
    }

    private void addRocks(double xo, double yo)
    {
        for (int i = 0; i < 100; i++)
        {
            double x = xo + random.nextGaussian() * 10;
            double y = yo + random.nextGaussian() * 10;
            Rock rock = new Rock(x, y);

            if (isFree(rock.x, rock.y, rock.r))
            {
                addEntity(rock);
            }
        }
    }

    private void addForrest(double xo, double yo)
    {
        for (int i = 0; i < 200; i++)
        {
            double x = xo + random.nextGaussian() * 20;
            double y = yo + random.nextGaussian() * 20;
            Tree tree = new Tree(x, y, random.nextInt(16 * Tree.GROW_SPEED));

            if (isFree(tree.x, tree.y, tree.r))
            {
                addEntity(tree);
            }
        }
    }

    public void addEntity(Entity entity)
    {
        entity.init(this, tower.bitmaps);
        entities.add(entity);
        entity.tick();
    }

    public boolean isFree(double x, double y, double r)
    {
        return isFree(x, y, r, null);
    }

    public boolean isFree(double x, double y, double r, Entity source)
    {
        if (!isOnGround(x, y)) return false;
        for (int i = 0; i < entities.size(); i++)
        {
            Entity e = entities.get(i);
            if (e != source)
            {
                if (e.collides(x, y, r)) return false;
            }
        }
        return true;
    }

    public Entity getEntityAt(double x, double y, double r, TargetFilter filter)
    {
        return getEntityAt(x, y, r, filter, null);
    }

    public Entity getEntityAt(double x, double y, double r, TargetFilter filter, Entity exception)
    {
        double closest = 1000000;
        Entity closestEntity = null;

        for (int i = 0; i < entities.size(); i++)
        {
            Entity e = entities.get(i);
            if (e == exception) continue;
            if (filter != null && !filter.accepts(e)) continue;

            if (e.collides(x, y, r))
            {
                double dist = (e.x - x) * (e.x - x) + (e.y - y) * (e.y - y);
                if (dist < closest)
                {
                    closest = dist;
                    closestEntity = e;
                }
            }
        }

        return closestEntity;
    }

    public void tick()
    {
        if (monsterPopulation<0)
        {
            System.out.println("Monster population is less than 0!!");
            monsterPopulation = 0;
        }
        
        for (int i = 0; i < entities.size(); i++)
        {
            Entity entity = entities.get(i);
            entity.tick();
            if (!entity.isAlive()) entities.remove(i--);
        }
    }

    public boolean isOnGround(double x, double y)
    {
        x /= 1.5;
        y /= 1.5;
        int xp = (int) (x + 128);
        int yp = (int) (y + 128);
        if (xp < 0 || yp < 0 || xp >= 256 || yp >= 256) return false;

        return (pixels[yp << 8 | xp] >>> 24) > 128;
    }

    public Entity getEntityAtMouse(double x, double y, TargetFilter filter)
    {
        x *= 0.5;
        y *= -1;
        double sin = Math.sin(rot);
        double cos = Math.cos(rot);
        double xp = x * cos + y * sin;
        double yp = x * sin - y * cos;

        return getEntityAt(xp, yp, 8, filter);
    }

    public boolean canPlaceHouse(double x, double y, HouseType type)
    {
        if (resources.canAfford(type))
        {
            x *= 0.5;
            y *= -1;
            double sin = Math.sin(rot);
            double cos = Math.cos(rot);
            double xp = x * cos + y * sin;
            double yp = x * sin - y * cos;

            House house = new House(xp, yp, type);
            if (isFree(house.x, house.y, house.r))
            {
                return true;
            }
        }

        return false;
    }

    public void placeHouse(double x, double y, HouseType type)
    {
        if (resources.canAfford(type))
        {
            x *= 0.5;
            y *= -1;
            double sin = Math.sin(rot);
            double cos = Math.cos(rot);
            double xp = x * cos + y * sin;
            double yp = x * sin - y * cos;

            House house = new House(xp, yp, type);
            if (isFree(house.x, house.y, house.r))
            {
                Sounds.play(new Sound.Plant());
                addEntity(house);
                resources.charge(type);
            }
        }
    }

    public void win()
    {
        tower.win();
    }
}