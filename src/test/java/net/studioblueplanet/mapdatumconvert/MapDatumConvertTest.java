/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author jorgen
 */
public class MapDatumConvertTest
{
    
    public MapDatumConvertTest()
    {
    }

    @BeforeClass
    public static void setUpClass() throws Exception
    {
    }

    @AfterClass
    public static void tearDownClass() throws Exception
    {
    }

    @Before
    public void setUp() throws Exception
    {
    }

    @After
    public void tearDown() throws Exception
    {
    }


    /**
     * Test of latLonToCarthesian method, of class MapDatumConvert.
     */
    @Test
    public void testLatLonToCarthesian()
    {
        System.out.println("____________________________________________________");
        System.out.println("latLonToCarthesian");
        LatLonCoordinate latlon = new LatLonCoordinate();
        latlon.lambda   =4.388054251;
        latlon.phi      =51.98705383;
        latlon.h        =30.696;
        Ellipsoid el = Ellipsoid.ELLIPSOID_BESSEL1841;
        MapDatumConvert instance = new MapDatumConvert();
        CarthesianCoordinate result = instance.latLonToCarthesian(latlon, el);
        assertEquals(3924096.851, result.X, 0.001);
        assertEquals(301119.8207, result.Y, 0.001);
        assertEquals(5001429.896, result.Z, 0.001);
        System.out.println(String.format("[%8.7f, %8.7f, %5.3f] to [%10.3f, %10.3f, %10.3f]",
                           latlon.phi, latlon.lambda, latlon.h,
                           result.X, result.Y, result.Z));
    }    
    
    /**
     * Test of datumTransformRdToWgs84 method, of class MapDatumConvert.
     */
    @Test
    public void testDatumTransformRdToWgs84()
    {
        System.out.println("____________________________________________________");
        System.out.println("datumTransformRdToWgs84");
        CarthesianCoordinate rd = new CarthesianCoordinate();
        rd.X=3924096.851;
        rd.Y=301119.8207;
        rd.Z=5001429.896;
        MapDatumConvert instance = new MapDatumConvert();
        CarthesianCoordinate result=instance.datumTransformRdToWgs84(rd);
        assertEquals(3924689.340, result.X, 0.001);
        assertEquals( 301145.338, result.Y, 0.001);
        assertEquals(5001908.687, result.Z, 0.001);
        System.out.println(String.format("[%10.3f, %10.3f, %10.3f] to [%10.3f, %10.3f, %10.3f]",
                           rd.X, rd.Y, rd.Z,
                           result.X, result.Y, result.Z));
    }
    
    /**
     * Test of datumTransformWgs84ToRd method, of class MapDatumConvert.
     */
    @Test
    public void testDatumTransformWgs84ToRd()
    {
        System.out.println("____________________________________________________");
        System.out.println("rdCarthesianToWgsCarthesian");
        CarthesianCoordinate wgs = new CarthesianCoordinate();
        wgs.X=3924689.340;
        wgs.Y=301145.338;
        wgs.Z=5001908.687;
        MapDatumConvert instance = new MapDatumConvert();
        CarthesianCoordinate result=instance.datumTransformWgs84ToRd(wgs);
        assertEquals(3924096.851, result.X, 0.001);
        assertEquals(301119.8207, result.Y, 0.001);
        assertEquals(5001429.896, result.Z, 0.001);
        System.out.println(String.format("[%10.3f, %10.3f, %10.3f] to [%10.3f, %10.3f, %10.3f]",
                           wgs.X, wgs.Y, wgs.Z,
                           result.X, result.Y, result.Z));
    }
    
    /**
     * Test of carthesianToLatLon method, of class MapDatumConvert.
     */
    @Test
    public void testCarthesianToLatLon()
    {
        System.out.println("____________________________________________________");
        System.out.println("carthesianToLatLon");
        CarthesianCoordinate rd = new CarthesianCoordinate();
        rd.X=3924689.340;
        rd.Y= 301145.338;
        rd.Z=5001908.687;
        MapDatumConvert instance = new MapDatumConvert();
        LatLonCoordinate result=instance.carthesianToLatLon(rd, Ellipsoid.ELLIPSOID_WGS84);
        assertEquals(51.98608734, result.phi    , 0.0000001);
        assertEquals(4.387764738, result.lambda , 0.0000001);
        assertEquals(74.31246664, result.h      , 0.001);        
        System.out.println(String.format("[%10.3f, %10.3f, %10.3f] to [%8.7f, %8.7f, %5.3f]",
                           rd.X, rd.Y, rd.Z,
                           result.phi, result.lambda, result.h));    }
    
    
    /**
     * Test of rdToWgs method, of class MapDatumConvert.
     * 0.00000001 degree ~ 1 cm
     */
    @Test
    public void testRdToWgs84()
    {
        System.out.println("____________________________________________________");
        System.out.println("RdToWgs");
        DatumCoordinate rd = new DatumCoordinate();
        rd.easting=86346.784;
        rd.northing=444659.972;
        rd.h=30.809;
        MapDatumConvert instance    = new MapDatumConvert();
        LatLonCoordinate result     = instance.rdToWgs84(rd);
        assertEquals(51.98608734    , result.phi    , 0.0000001);
        assertEquals( 4.387764732   , result.lambda , 0.0000001);
        assertEquals(74.312         , result.h      , 0.001);
        System.out.println(String.format("[%8.3f, %8.3f, %5.3f] to [%8.7f, %8.7f, %5.3f]",
                           rd.easting, rd.northing, rd.h,
                           result.phi, result.lambda, result.h));
        
        // The center of Rijksdriehoeksmeting
        rd.easting=155000.0;
        rd.northing=463000.0;
        rd.h=0.0;
        result = instance.rdToWgs84(rd);
        assertEquals(52.1551722  , result.phi    , 0.0000001);
        assertEquals( 5.3872035  , result.lambda , 0.0000001);
        assertEquals(43.2345     , result.h      , 0.001);        
        System.out.println(String.format("[%8.3f, %8.3f, %5.3f] to [%8.7f, %8.7f, %5.3f]",
                           rd.easting, rd.northing, rd.h,
                           result.phi, result.lambda, result.h));
        
        
        // Rijksdriehoeksmeting reference point Steen RD Westerpad Ameland
        // Compared to coordinate conversion https://epsg.io/transform#s_srs=28992&t_srs=4326&x=179640.6536000&y=607984.4519000
        // Results in: 53.4575106° 5.7581475°
        rd.easting  =179640.6536;
        rd.northing =607984.4519;
        rd.h        =0.0;
        result = instance.rdToWgs84(rd);
        assertEquals(53.4575106, result.phi    , 0.0000001);
        assertEquals( 5.7581475, result.lambda , 0.0000001);
        assertEquals(40.7992   , result.h      , 0.001);        
        System.out.println(String.format("[%8.3f, %8.3f, %5.3f] to [%8.7f, %8.7f, %5.3f]",
                           rd.easting, rd.northing, rd.h,
                           result.phi, result.lambda, result.h));
        
    }

    /**
     * Test of rdLatLonToWgs method, of class MapDatumConvert.
     */
    @Test
    @org.junit.Ignore
    public void testRdLatLonToWgs84()
    {
        System.out.println("____________________________________________________");
        System.out.println("RdLatLonToWgs");
    }

    
    @Test
    public void testWgs84ToRd()
    {
        System.out.println("____________________________________________________");
        System.out.println("wgs84ToRd");
        LatLonCoordinate wgs        = new LatLonCoordinate();
        wgs.phi                     =51.98608734;
        wgs.lambda                  =4.387764732;
        wgs.h                       =74.312;
        MapDatumConvert instance    = new MapDatumConvert();
        DatumCoordinate result         = instance.wgs84ToRd(wgs);
        assertEquals(86346.784 , result.easting     , 0.001);
        assertEquals(444659.972 , result.northing   , 0.001);
        assertEquals(30.809     , result.h          , 0.001);
        System.out.println(String.format("[%8.7f, %8.7f, %5.3f] to [%8.3f, %8.3f, %5.3f]",
                           wgs.phi, wgs.lambda, wgs.h,
                           result.easting, result.northing, result.h));

        wgs.phi                     =52.1551722;
        wgs.lambda                  =5.3872035;
        wgs.h                       =43.2345;
        result                      = instance.wgs84ToRd(wgs);
        assertEquals(155000.000 , result.easting    , 0.01);
        assertEquals(463000.000 , result.northing   , 0.01);
        assertEquals(0.000      , result.h          , 0.01);
        System.out.println(String.format("[%8.7f, %8.7f, %5.3f] to [%8.3f, %8.3f, %5.3f]",
                           wgs.phi, wgs.lambda, wgs.h,
                           result.easting, result.northing, result.h));
        
        // Rijksdriehoeksmeting reference point Steen RD Westerpad Ameland
        // Compared to coordinate conversion https://epsg.io/transform#s_srs=4326&t_srs=28992&x=5.7581475&y=53.4575106
        // Results in: 179640.653, 607984.450
        wgs.phi                     =53.4575105;
        wgs.lambda                  =5.7581475;
        wgs.h                       =40.7992;
        result                      = instance.wgs84ToRd(wgs);
        assertEquals(179640.653 , result.easting    , 0.01);
        assertEquals(607984.450 , result.northing   , 0.01);
        assertEquals(0.000       , result.h          , 0.01);
        System.out.println(String.format("[%8.7f, %8.7f, %5.3f] to [%8.3f, %8.3f, %5.3f]",
                           wgs.phi, wgs.lambda, wgs.h,
                           result.easting, result.northing, result.h));       
    }
    
    /**
     * Test of wgsToRdLatLon method, of class MapDatumConvert.
     */
    @Test
    @org.junit.Ignore
    public void testWgs84ToRdLatLon()
    {
        System.out.println("____________________________________________________");
        System.out.println("wgs84ToRdLatLon");
    }
    
    
    @Test
    public void testRoundTrip()
    {
        System.out.println("____________________________________________________");
        System.out.println("Test: roundtrip: WGS84 -> RD -> WGS84");
        LatLonCoordinate wgs        = new LatLonCoordinate();
        wgs.phi                     =51.98608734;
        wgs.lambda                  =4.387764732;
        wgs.h                       =74.312;
        MapDatumConvert instance    = new MapDatumConvert();
        DatumCoordinate rd         = instance.wgs84ToRd(wgs);
        LatLonCoordinate result     = instance.rdToWgs84(rd);
        assertEquals( wgs.phi   , result.phi    , 0.0000001);
        assertEquals(wgs.lambda , result.lambda , 0.0000001);
        assertEquals(wgs.h      , result.h      , 0.001);    
        System.out.println(String.format("[%8.7f, %8.7f, %5.3f] to [%8.7f, %8.7f, %5.3f]",
                           wgs.phi, wgs.lambda, wgs.h,
                           result.phi, result.lambda, result.h));
    }
}
