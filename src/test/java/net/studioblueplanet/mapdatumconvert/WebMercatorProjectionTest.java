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
public class WebMercatorProjectionTest
{
    
    public WebMercatorProjectionTest()
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
     * Test of latLonToMapDatum method, of class WebMercatorProjection.
     */
    @Test
    public void testLatLonToMapDatum()
    {
        System.out.println("latLonToMapDatum");
        LatLonCoordinate latlon = new LatLonCoordinate();
        latlon.lambda=9.770602;
        latlon.phi=47.6035525;
        WebMercatorProjection instance = new WebMercatorProjection();
        DatumCoordinate result = instance.latLonToMapDatum(latlon);
        assertEquals(6041151.705173198, result.northing, 0.001);
        assertEquals(1087658.4393837405, result.easting, 0.001);
    }

    /**
     * Test of mapDatumToLatLon method, of class WebMercatorProjection.
     */
    @Test
    public void testMapDatumToLatLon()
    {
        System.out.println("mapDatumToLatLon");
        DatumCoordinate md = new DatumCoordinate();
        md.northing  =6041151.705173198;
        md.easting   =1087658.4393837405;
        WebMercatorProjection instance = new WebMercatorProjection();
        LatLonCoordinate result = instance.mapDatumToLatLon(md);
        assertEquals(47.6035525, result.phi, 0.00001);
        assertEquals(9.770602, result.lambda, 0.00001);

    }
    
}
