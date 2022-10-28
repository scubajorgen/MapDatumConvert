/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

/**
 *
 * @author jorgen
 */
public class LatLonCoordinate
{
    public double phi;      // latitude in degrees
    public double lambda;   // longitude in degrees
    public double h;        // height in m    

    public LatLonCoordinate(double lat, double lon, double h)
    {
        this.phi    =lat;
        this.lambda =lon;
        this.h      =h;
    }

    public LatLonCoordinate(double lat, double lon)
    {
        this.phi    =lat;
        this.lambda =lon;
        this.h      =0;
    }

    
    public LatLonCoordinate()
    {
        
    }
}
