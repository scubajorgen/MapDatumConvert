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
public class Main
{
    
    public static void main(String args[])
    {
        MapDatumConvert     mdc;
        DatumCoordinate rd;
        DatumCoordinate rdBack;
        LatLonCoordinate    wgs84;
        mdc=new MapDatumConvert();
        
        rd=new DatumCoordinate();
        rd.easting=24895.0;
        rd.northing=559589.0;
        rd.h=13.0;
        wgs84=mdc.rdToWgs84(rd);
        System.out.println(String.format("RD [%6.1f, %6.1f, %4.1f] maps to WGS84 [%10.7f, %10.7f, %5.1f]",
                           rd.easting, rd.northing, rd.h, wgs84.phi, wgs84.lambda, wgs84.h));
        
        rdBack=mdc.wgs84ToRd(wgs84);
        System.out.println(String.format("WGS84 [%10.7f, %10.7f, %5.1f] maps to RD [%6.1f, %6.1f, %4.1f]",
                           wgs84.phi, wgs84.lambda, wgs84.h, rdBack.easting, rdBack.northing, rdBack.h));        
        
        
    }    
}
