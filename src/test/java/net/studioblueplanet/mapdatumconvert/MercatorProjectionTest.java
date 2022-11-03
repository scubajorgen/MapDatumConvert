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
public class MercatorProjectionTest
{
    
    public MercatorProjectionTest()
    {
    }
    
    @BeforeClass
    public static void setUpClass()
    {
    }
    
    @AfterClass
    public static void tearDownClass()
    {
    }
    
    @Before
    public void setUp()
    {
    }
    
    @After
    public void tearDown()
    {
    }

    /**
     * Test of latLonToMapDatum method, of class MercatorProjection.
     */
    @Test
    public void testLatLonToMapDatum()
    {
        System.out.println("latLonToMapDatum");
        // Martini Toren Groningen
        LatLonCoordinate latlon = new LatLonCoordinate();
        latlon.phi      =53.2193683;
        latlon.lambda   =6.56827;
        MercatorProjection instance = new MercatorProjection(Ellipsoid.ELLIPSOID_WGS84, 0.0);
        DatumCoordinate result = instance.latLonToMapDatum(latlon);
        assertEquals( 731176.471792735, result.easting , 0.001);
        assertEquals(6989431.562216234, result.northing, 0.002);
        
        latlon.phi      =53.2193683;
        latlon.lambda   =6.56827;
        instance        = new MercatorProjection(Ellipsoid.ELLIPSOID_WGS84, 6.56827);
        result          = instance.latLonToMapDatum(latlon);
        assertEquals(      0.000      , result.easting , 0.001);
        assertEquals(6989431.562216234, result.northing, 0.002);

        instance = MercatorProjection.WORLD_MERCATOR;
        result = instance.latLonToMapDatum(latlon);
        assertEquals( 731176.471792735, result.easting , 0.001);
        assertEquals(6989431.562216234, result.northing, 0.002);
    }

    /**
     * Test of mapDatumToLatLon method, of class MercatorProjection.
     */
    @Test
    public void testMapDatumToLatLon()
    {
        System.out.println("mapDatumToLatLon");
        DatumCoordinate md = new DatumCoordinate();
        md.easting  = 731176.471792735;
        md.northing =6989431.562216234;
        MercatorProjection instance = new MercatorProjection(Ellipsoid.ELLIPSOID_WGS84, 0.0);
        LatLonCoordinate result = instance.mapDatumToLatLon(md);
        assertEquals(53.2193683, result.phi   , 0.0000001);
        assertEquals(   6.56827, result.lambda, 0.0000001);

        md.easting      =      0.000;
        md.northing     =6989431.562216234;
        instance        = new MercatorProjection(Ellipsoid.ELLIPSOID_WGS84, 6.56827);
        result          = instance.mapDatumToLatLon(md);
        assertEquals(53.2193683, result.phi   , 0.0000001);
        assertEquals(   6.56827, result.lambda, 0.0000001);
    }
    
}
