package com.mojang.tower;

import java.applet.Applet;
import java.awt.BorderLayout;

public class TowerApplet extends Applet
{
    private static final long serialVersionUID = 1L;
    private TowerComponent tower;

    public void init()
    {
        tower = new TowerComponent(getWidth()/2, getHeight()/2);
        this.setLayout(new BorderLayout());
        add(tower, BorderLayout.CENTER);
    }
    
    public void start()
    {
        tower.unpause();
    }
    
    public void stop()
    {
        tower.pause();
    }

    public void destroy()
    {
        tower.stop();
    }
}