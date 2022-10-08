/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

/**
 * Represents a coordinate according to map datum.
 */
public class DatumCoordinate
{
    public double easting;      // easting in m
    public double northing;     // northing in m
    public double h;            // height in m     

    public DatumCoordinate(double northing, double easting)
    {
        this.northing   =northing;
        this.easting    =easting;
        this.h          =0.0; 
    }

    public DatumCoordinate(double northing, double easting, double h)
    {
        this.northing   =northing;
        this.easting    =easting;
        this.h          =h; 
    }
    
    public DatumCoordinate()
    {
        
    }
}
