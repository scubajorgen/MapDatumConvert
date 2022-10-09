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
        System.out.println("_________________________________________________________________________________");
        System.out.println("                           MAP DATUM CONVERSION DEMO                             ");
        System.out.println("_________________________________________________________________________________");
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
        System.out.println("RD to WGS84");
        System.out.println(String.format("RD [%6.1f, %6.1f, %4.1f] maps to WGS84 [%10.7f, %10.7f, %5.1f]",
                           rd.easting, rd.northing, rd.h, wgs84.phi, wgs84.lambda, wgs84.h));
        System.out.println("_________________________________________________________________________________");
        
        System.out.println("WGS84 to RD");
        rdBack=mdc.wgs84ToRd(wgs84);
        System.out.println(String.format("WGS84 [%10.7f, %10.7f, %5.1f] maps to RD [%6.1f, %6.1f, %4.1f]",
                           wgs84.phi, wgs84.lambda, wgs84.h, rdBack.easting, rdBack.northing, rdBack.h));
        System.out.println("_________________________________________________________________________________");
        
        System.out.println("Comparison of Transverse Mercator projection to RD projection");
        System.out.println("Projection Center: OLV Amersfoort");
        System.out.println("Target           : Martini Toren Groningen");
        // Martinitoren Groningen, WGS84, Transverse mercator
        LatLonCoordinate rdLatLon       = new LatLonCoordinate();
        rdLatLon.phi                    =53.21936;
        rdLatLon.lambda                 =6.568298333;
        rdLatLon.h                      =0.0;
        TransverseMercatorProjection tm= TransverseMercatorProjection.OZI_WGS84_TM;
        DatumCoordinate result          = tm.latLonToMapDatum(rdLatLon);
        System.out.println(String.format("Transverse Mercator: [%8.6f, %8.6f] to [%8.3f, %8.3f]",
                           rdLatLon.phi, rdLatLon.lambda,
                           result.easting, result.northing));      
        
        // Martinitoren Groningen, Bessel1841, Stereographic
        DatumCoordinate dc=mdc.wgs84ToRd(rdLatLon);
        System.out.println(String.format("Stereographic      : [%8.6f, %8.6f] to [%8.3f, %8.3f]",
                           rdLatLon.phi, rdLatLon.lambda,
                           dc.easting  , dc.northing));   
        
        System.out.println(String.format("Difference         : Easting %3.1f m, Northing %3.1f m", result.easting-dc.easting, 
                                                                       result.northing-dc.northing));        
        System.out.println("_________________________________________________________________________________");
    }    
}
