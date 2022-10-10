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
    private static void rdWgsRoundTrip()
    {
        DatumCoordinate     rd;
        DatumCoordinate     rdBack;
        LatLonCoordinate    wgs84;
        MapDatumConvert     mdc;

        mdc                 =new MapDatumConvert();
        
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
    }
    
    private static void projectionCompare()
    {
        MapDatumConvert     mdc;
        LatLonCoordinate    rdLatLon;
        LatLonCoordinate    wgsLatLon;

        mdc                 =new MapDatumConvert();        
        
        System.out.println("Comparison of Transverse Mercator projection to RD projection");
        System.out.println("Projection Center: OLV Amersfoort");
        System.out.println("Target           : OLV Amersfoort");
        // OLV Amersfoort, WGS84, Transverse mercator
        // The official RD lat/lon coordinate of OLV Amersfoort
        rdLatLon                        = new LatLonCoordinate();
        rdLatLon.phi                    =52.156160556;
        rdLatLon.lambda                 =5.387638889;
        rdLatLon.h                      =0.0;
        
        // As WGS84 lat/lon
        wgsLatLon=mdc.rdLatLonToWgs84(rdLatLon);
        
        TransverseMercatorProjection tm = TransverseMercatorProjection.OZI_WGS84_TM;
        DatumCoordinate result          = tm.latLonToMapDatum(wgsLatLon);
        System.out.println(String.format("Transverse Mercator: [%10.7f, %10.7f] to [%8.3f, %8.3f]",
                           wgsLatLon.phi, wgsLatLon.lambda,
                           result.easting, result.northing));      
        
        // OLV Amersfoort, Bessel1841, Stereographic
        StereographicProjection sp      =StereographicProjection.RIJKSDRIEHOEKSMETING;
        DatumCoordinate dc              = sp.latLonToMapDatum(rdLatLon);
        System.out.println(String.format("Stereographic      : [%10.7f, %10.7f] to [%8.3f, %8.3f]",
                           rdLatLon.phi, rdLatLon.lambda,
                           dc.easting, dc.northing));      
        
        System.out.println(String.format("Difference         : Easting %3.1f m, Northing %3.1f m", result.easting-dc.easting, 
                                                                       result.northing-dc.northing));        

        System.out.println("Target           : Martini Toren Groningen");
        // Martinitoren Groningen, Transverse mercator
        wgsLatLon                       = new LatLonCoordinate();
        wgsLatLon.phi                   =53.2193683;
        wgsLatLon.lambda                = 6.5682700;
        wgsLatLon.h                     = 0.0;
        rdLatLon                        =mdc.wgs84ToRdLatLon(wgsLatLon);

        result                          = tm.latLonToMapDatum(wgsLatLon);
        System.out.println(String.format("Transverse Mercator: [%10.7f, %10.7f] to [%8.3f, %8.3f]",
                           wgsLatLon.phi, wgsLatLon.lambda,
                           result.easting, result.northing));      
        
        // Martinitoren Groningen, Bessel1841, Stereographic
        sp                              =StereographicProjection.RIJKSDRIEHOEKSMETING;
        dc                              = sp.latLonToMapDatum(rdLatLon);
        System.out.println(String.format("Stereographic      : [%10.7f, %10.7f] to [%8.3f, %8.3f]",
                           rdLatLon.phi, rdLatLon.lambda,
                           dc.easting, dc.northing));      
        
        System.out.println(String.format("Difference         : Easting %3.1f m, Northing %3.1f m", 
                                                        result.easting-dc.easting, 
                                                        result.northing-dc.northing));        
        System.out.println("_________________________________________________________________________________");
    }

    private static void oziSimulation()
    {
        MapDatumConvert                 mdc;
        LatLonCoordinate                rdLatLon;
        LatLonCoordinate                wgsLatLon;
        DatumCoordinate                 dcTm;
        DatumCoordinate                 dcOs;
        TransverseMercatorProjection    rdTm;
        StereographicProjection         rdOs;

        mdc                 =new MapDatumConvert();        
        
        System.out.println("Simulation of the Ozi RD approximation");
        System.out.println("Projection Center: OLV Amersfoort");
        System.out.println("Target           : Martinitoren Groningen");

        // Martinitoren Groningen, Transverse mercator
        wgsLatLon                        = new LatLonCoordinate();
        wgsLatLon.phi                    =53.2193683;
        wgsLatLon.lambda                 = 6.5682700;
        wgsLatLon.h                      = 0.0;
        rdLatLon                         =mdc.wgs84ToRdLatLon(wgsLatLon);
          
        // Instead of a Oblique Stereographic we use the Transverse Mercator projection
        rdTm=new TransverseMercatorProjection(Ellipsoid.ELLIPSOID_BESSEL1841, 
                                                 0.9999079, 
                                                 52.156160556, 5.387638889,
                                                 463000.0, 155000.0);
        dcTm=rdTm.latLonToMapDatum(rdLatLon);
        System.out.println(String.format("Transverse Mercator   : WGS84 [%10.7f, %10.7f] to RD [%10.7f, %10.7f] to [%8.3f, %8.3f]",
                           wgsLatLon.phi, wgsLatLon.lambda,
                           rdLatLon.phi, rdLatLon.lambda,
                           dcTm.easting  , dcTm.northing));     
        rdOs=StereographicProjection.RIJKSDRIEHOEKSMETING;
        dcOs=rdOs.latLonToMapDatum(rdLatLon);
        System.out.println(String.format("Oblique Stereographic : WGS84 [%10.7f, %10.7f] to RD [%10.7f, %10.7f] to [%8.3f, %8.3f]",
                           wgsLatLon.phi, wgsLatLon.lambda,
                           rdLatLon.phi, rdLatLon.lambda,
                           dcOs.easting  , dcOs.northing));     
        
        System.out.println(String.format("Difference         : Easting %3.1f m, Northing %3.1f m", 
                                                        dcTm.easting-dcOs.easting, 
                                                        dcTm.northing-dcOs.northing));        
        System.out.println("_________________________________________________________________________________");        
    }


    
    public static void main(String args[])
    {
        System.out.println("_________________________________________________________________________________");
        System.out.println("                           MAP DATUM CONVERSION DEMO                             ");
        System.out.println("_________________________________________________________________________________");

        rdWgsRoundTrip();
        
        projectionCompare();
        
        oziSimulation();
    }    
}
