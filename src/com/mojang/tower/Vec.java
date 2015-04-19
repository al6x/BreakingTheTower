package com.mojang.tower;

public class Vec
{
    public final double x, y, z;
    
    public Vec(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    public Vec(Vec v)
    {
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
    }
    
    public double distance(Vec v)
    {
        return Math.sqrt(distanceSqr(v));
    }

    public double distanceSqr(Vec v)
    {
        double xd = v.x-x;
        double yd = v.y-y;
        double zd = v.z-z;
        
        return xd*xd+yd*yd+zd*zd;
    }
    
    public Vec rotate(double sin, double cos)
    {
        double _x = x*cos+z*sin;
        double _y = y;
        double _z = x*sin-z*cos;

/*        double __x = _x;
        double __y = _y*xCos+_z*xSin;
        double __z = _y*xSin-_z*xCos;*/
        
        return new Vec(_x, _y, _z);
    }

    public Vec project()
    {
        double _x = x/z*320;
        double _y = y/z*320;
        double _z = z;

        return new Vec(_x, _y, _z);
    }

    public double lengthSqr()
    {
        return x*x+y*y+z*z;
    }

    public Vec add(Vec m)
    {
        return new Vec(x+m.x, y+m.y, z+m.z);
    }

    public Vec scale(double v)
    {
        return new Vec(x*v, y*v, z*v);
    }

    public Vec sub(Vec m)
    {
        return new Vec(x-m.x, y-m.y, z-m.z);
    }
}
