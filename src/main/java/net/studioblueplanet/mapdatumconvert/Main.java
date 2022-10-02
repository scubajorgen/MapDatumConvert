/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

import net.studioblueplanet.mapdatumconvert.MapDatumConvert.RdCoordinate;
import net.studioblueplanet.mapdatumconvert.MapDatumConvert.LatLonCoordinate;

/**
 *
 * @author jorgen
 */
public class Main
{
    
    public static void main(String args[])
    {
        MapDatumConvert     mdc;
        RdCoordinate        rd;
        LatLonCoordinate    wgs84;
        mdc=new MapDatumConvert();
        
        rd=new RdCoordinate();
        rd.x=24895.0;
        rd.y=559589.0;
        rd.h=13.0;
        wgs84=mdc.RdToWgs(rd);
        System.out.println(String.format("RD [%6.1f, %6.1f, %4.1f] maps to WGS84 [%10.7f, %10.7f, %5.1f]",
                           rd.x, rd.y, rd.h, wgs84.phi, wgs84.lambda, wgs84.h));
        
        
        
        
        
    }    
}
