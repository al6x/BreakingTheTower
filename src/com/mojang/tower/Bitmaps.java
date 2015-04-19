package com.mojang.tower;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Bitmaps
{
    public BufferedImage[] trees;
    public BufferedImage[] farmPlots;
    public BufferedImage[] rocks;
    public BufferedImage[] carriedResources;
    public BufferedImage[][] peons;
    public BufferedImage island;
    public BufferedImage towerTop;
    public BufferedImage towerMid;
    public BufferedImage towerBot;
    public BufferedImage[] smoke;
    public BufferedImage[] infoPuffs;
    public BufferedImage[][] houses;
    public BufferedImage delete, help;
    public BufferedImage[] soundButtons;
    
    public BufferedImage logo, wonScreen;

    public void loadAll() throws IOException
    {
        logo = ImageIO.read(Bitmaps.class.getResource("/logo.gif"));
        wonScreen = ImageIO.read(Bitmaps.class.getResource("/winscreen.gif"));
        BufferedImage src = ImageIO.read(Bitmaps.class.getResource("/sheet.gif"));
        trees = new BufferedImage[16];
        for (int i=0; i<16; i++)
            trees[i] = clip(src, 32+i*8, 0, 8, 16);

        farmPlots = new BufferedImage[9];
        for (int i=0; i<9; i++)
            farmPlots[i] = clip(src, 32+i*8, 11*8, 8, 8);

        rocks = new BufferedImage[4];
        for (int i=0; i<4; i++)
            rocks[i] = clip(src, 32+12*8+i*8, 16, 8, 8);
        
        carriedResources = new BufferedImage[4];
        for (int i=0; i<4; i++)
            carriedResources[i] = clip(src, 32+12*8+i*8, 16+16, 8, 8);

        delete = clip(src, 32+16*8+3*16, 0, 16, 16);
        help = clip(src, 32+16*8+3*16, 16, 16, 16);
        soundButtons = new BufferedImage[2];
        for (int i=0; i<2; i++)
            soundButtons[i] = clip(src, 32+16*8+3*16, 32+i*16, 16, 16);
        
        houses = new BufferedImage[3][8];
        for (int x=0; x<3; x++)
            for (int y=0; y<8; y++)
                houses[x][y] = clip(src, 32+16*8+x*16, y*16, 16, 16);
        
        peons = new BufferedImage[4][3*4];
        for (int i=0; i<4; i++)
            for (int j=0; j<3*4; j++)
                peons[i][j] = clip(src, 32+j*8, 16+i*8, 8, 8);
        
        towerTop = clip(src, 0, 0, 32, 16);
        towerMid = clip(src, 0, 16, 32, 8);
        towerBot = clip(src, 0, 24, 32, 8);
        
        smoke = new BufferedImage[5];
        for (int i=0; i<5; i++)
            smoke[i] = clip(src, 256-8, i*8, 8, 8);
        
        infoPuffs = new BufferedImage[5];
        for (int i=0; i<5; i++)
            infoPuffs[i] = clip(src, 256-8-16, i*8, 16, 8);
        
        island = new BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB);
        int[] pixels = new int[256*256];
        ImageIO.read(Bitmaps.class.getResource("/island.gif")).getRGB(0, 0, 256, 256, pixels, 0, 256);
        island.setRGB(0, 0, 256, 256, pixels, 0, 256);
    }

    public static BufferedImage clip(BufferedImage src, int x, int y, int w, int h)
    {
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        BufferedImage newImage = null;

        try
        {
            GraphicsDevice screen = ge.getDefaultScreenDevice();
            GraphicsConfiguration gc = screen.getDefaultConfiguration();
            newImage = gc.createCompatibleImage(w, h, Transparency.BITMASK);
        }
        catch (Exception e)
        {
        }

        if (newImage == null)
        {
            newImage = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }

        int[] pixels = new int[w * h];
        src.getRGB(x, y, w, h, pixels, 0, w);
        newImage.setRGB(0, 0, w, h, pixels, 0, w);

        return newImage;
    }
}