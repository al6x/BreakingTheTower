package com.mojang.tower;

import java.awt.image.BufferedImage;

public class HouseType
{
    public static final HouseType[] houseTypes = new HouseType[8];

    public static final HouseType MASON = new HouseType(1, "Mason", 10, 0, 15, 0).setAcceptsResource(Resources.ROCK);
    public static final HouseType WOODCUTTER = new HouseType(2, "Woodcutter", 10, 15, 0, 0).setAcceptsResource(Resources.WOOD);
    public static final HouseType PLANTER = new HouseType(0, "Planter", 10, 30, 15, 10);
    public static final HouseType FARM = new HouseType(6, "Farmer", 10, 30, 30, 0);
    public static final HouseType WINDMILL = new HouseType(7, "Miller", 8, 15, 15, 0).setAcceptsResource(Resources.FOOD);
    public static final HouseType GUARDPOST = new HouseType(5, "Guardpost", 3, 0, 30, 0);
    public static final HouseType BARRACKS = new HouseType(3, "Barracks", 10, 15, 50, 0);
    public static final HouseType RESIDENCE = new HouseType(4, "Residence", 10, 30, 30, 30);

    public final int image;
    public final int radius;
    public final String name;
    public final int wood, rock, food;
    public int acceptResource = -1;

    private static int id = 0;

    private HouseType(int image, String name, int radius, int wood, int rock, int food)
    {
        this.image = image;
        this.name = name;
        this.radius = radius;
        this.wood = wood;
        this.rock = rock;
        this.food = food;
        houseTypes[id++] = this;
    }

    private HouseType setAcceptsResource(int acceptResource)
    {
        this.acceptResource = acceptResource;
        return this;
    }

    public BufferedImage getImage(Bitmaps bitmaps)
    {
        return bitmaps.houses[image % 2 + 1][image / 2];
    }

    public String getString()
    {
        String res = name + " [";
        if (wood > 0) res += " wood:" + wood;
        if (rock > 0) res += " rock:" + rock;
        if (food > 0) res += " food:" + food;
        res += " ]";
        return res;
    }

    public String getDescription()
    {
        if (this == MASON) return "Gathers nearby stones, produces rock";
        if (this == WOODCUTTER) return "Cuts down nearby trees, produces wood";
        if (this == PLANTER) return "Plants new trees that can later be cut down";
        if (this == FARM) return "Plants crops that can later be harvested";
        if (this == WINDMILL) return "Gathers nearby grown crops, produces food";
        if (this == GUARDPOST) return "Peons and warriors generally stay near these";
        if (this == BARRACKS) return "Converts peons into warriors for 5 wood each";
        if (this == RESIDENCE) return "Produces peons for 5 food each";

        return "**unknown**";
    }
}