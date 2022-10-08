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
public class TransverseMercatorTest
{
    
    public TransverseMercatorTest()
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
    
    @Test
    public void testLatLonToXY()
    {
        double          lat; 
        double          lon;
        DatumCoordinate    tm;
        TransverseMercatorProjection instance;
        
        // OLV Amersfoort
        lat=52.15516833;
        lon=5.387198333;
        //tm=TransverseMercator.latLonToXY(lat, lon);
        //assertEquals(5780609.0, tm.x, 1);
        //assertEquals(30.0, tm.y, 1);
        
        // Example from http://fgg-web.fgg.uni-lj.si/~/mkuhar/Zalozba/TM_projection.pdf
        // 9.3, page 13 Caister water tower
        instance=TransverseMercatorProjection.BRITISH_NATIONAL_GRID;
        lat=52.0+1/60.0*(39.0+27.2531/60.0);
        lon= 1.0+1/60.0*(43.0+ 4.5177/60.0);
        tm=instance.latLonToMapDatum(new LatLonCoordinate(lat, lon));
        assertEquals(313177.270, tm.northing, 0.001);
        assertEquals(651409.903, tm.easting, 0.001);

        // 9.3, page 14 Framingham
        lat=52.0+1/60.0*(34.0+26.8915/60.0);
        lon= 1.0+1/60.0*(20.0+21.1080/60.0);
        tm=instance.latLonToMapDatum(new LatLonCoordinate(lat, lon));
        assertEquals(302646.412, tm.northing, 0.001);
        assertEquals(626238.248, tm.easting, 0.001);

        // OGP Surveying and Positioning Guidance Note number 7, part 2 – May 2009
        lat=50.0+1/60.0*(30.0+0.0/60.0);
        lon= 0.0+1/60.0*(30.0+0.0/60.0);
        tm=instance.latLonToMapDatum(new LatLonCoordinate(lat, lon));
        assertEquals(69740.50, tm.northing, 0.01);
        assertEquals(577274.99, tm.easting, 0.01);    
    }


    @Test
    public void testLatLonToXY2()
    {
        double          lat; 
        double          lon;
        DatumCoordinate    tm;
        TransverseMercatorProjection instance;
        
        // OLV Amersfoort
        lat=52.15516833;
        lon=5.387198333;
        //tm=TransverseMercator.latLonToXY(lat, lon);
        //assertEquals(5780609.0, tm.x, 1);
        //assertEquals(30.0, tm.y, 1);
        
        // Example from http://fgg-web.fgg.uni-lj.si/~/mkuhar/Zalozba/TM_projection.pdf
        // 9.3, page 13 Caister water tower
        instance=TransverseMercatorProjection.BRITISH_NATIONAL_GRID;
        lat=52.0+1/60.0*(39.0+27.2531/60.0);
        lon= 1.0+1/60.0*(43.0+ 4.5177/60.0);
        tm=instance.latLonToMapDatum2(new LatLonCoordinate(lat, lon));

        assertEquals(313177.270, tm.northing, 0.01);
        assertEquals(651409.903, tm.easting, 0.001);

        // 9.3, page 14 Framingham
        lat=52.0+1/60.0*(34.0+26.8915/60.0);
        lon= 1.0+1/60.0*(20.0+21.1080/60.0);
        tm=instance.latLonToMapDatum2(new LatLonCoordinate(lat, lon));
        assertEquals(302646.412, tm.northing, 0.01);
        assertEquals(626238.248, tm.easting, 0.001);

        // OGP Surveying and Positioning Guidance Note number 7, part 2 – May 2009
        lat=50.0+1/60.0*(30.0+0.0/60.0);
        lon= 0.0+1/60.0*(30.0+0.0/60.0);
        tm=instance.latLonToMapDatum2(new LatLonCoordinate(lat, lon));   
        assertEquals(69740.50, tm.northing, 0.01);
        assertEquals(577274.99, tm.easting, 0.01);    
    }
    
}
