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
public class StereographicProjectionTest
{
    
    public StereographicProjectionTest()
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
     * Test of RdToLatLon method, of class MapDatumConvert.
     */
    @Test
    public void testRdToLatLon()
    {
        System.out.println("____________________________________________________");
        System.out.println("RdToLatLon");
        DatumCoordinate rd                  = new DatumCoordinate();
        rd.easting                          =86346.784;
        rd.northing                         =444659.972;
        rd.h                                =30.809;
        
        StereographicProjection instance    = StereographicProjection.RIJKSDRIEHOEKSMETING;
        LatLonCoordinate result             = instance.mapDatumToLatLon(rd);
        assertEquals(4.388054251, result.lambda , 0.0000001);
        assertEquals(51.98705383, result.phi    , 0.0000001);
        assertEquals(30.696     , result.h      , 0.0000001);
        System.out.println(String.format("[%8.3f, %8.3f, %5.3f] to [%8.6f, %8.6f, %5.3f]",
                           rd.easting, rd.northing, rd.h,
                           result.phi, result.lambda, result.h));
    }
    
    /**
     * Test of RdToLatLon method, of class MapDatumConvert.
     */
    @Test
    public void testLatLonToRd()
    {
        System.out.println("____________________________________________________");
        System.out.println("latLonToRd");
        LatLonCoordinate rdLatLon       = new LatLonCoordinate();
        rdLatLon.phi                    =51.987053833;
        rdLatLon.lambda                 =4.388054251;
        rdLatLon.h                      =30.696;
        StereographicProjection instance= StereographicProjection.RIJKSDRIEHOEKSMETING;
        DatumCoordinate result          = instance.latLonToMapDatum(rdLatLon);
        assertEquals(86346.784  , result.easting , 0.001);
        assertEquals(444659.972 , result.northing , 0.001);
        assertEquals(30.809     , result.h , 0.001);    
        System.out.println(String.format("[%8.6f, %8.6f, %5.3f] to [%8.3f, %8.3f, %5.3f]",
                           rdLatLon.phi, rdLatLon.lambda, rdLatLon.h,
                           result.easting, result.northing, result.h));
        
        rdLatLon.phi                    =51.5871380136242;
        rdLatLon.lambda                 =4.59391846497836;
        rdLatLon.h                      =0;
        result                          = instance.latLonToMapDatum(rdLatLon);
        assertEquals(100000.000 , result.easting , 0.001);
        assertEquals(400000.000 , result.northing , 0.001);
        assertEquals(0.113      , result.h , 0.001);        
        System.out.println(String.format("[%8.6f, %8.6f, %5.3f] to [%8.3f, %8.3f, %5.3f]",
                           rdLatLon.phi, rdLatLon.lambda, rdLatLon.h,
                           result.easting, result.northing, result.h));
        
        // The origin of Rijksdriehoeksmeting stelsel
        rdLatLon.phi                    =52.156160556;
        rdLatLon.lambda                 =5.387638889;
        rdLatLon.h                      =-0.113;
        result                          = instance.latLonToMapDatum(rdLatLon);
        assertEquals(155000.000 , result.easting , 0.001);
        assertEquals(463000.000 , result.northing , 0.001);
        assertEquals(0.000      , result.h , 0.001);        
        System.out.println(String.format("[%8.6f, %8.6f, %5.3f] to [%8.3f, %8.3f, %5.3f]",
                           rdLatLon.phi, rdLatLon.lambda, rdLatLon.h,
                           result.easting, result.northing, result.h));
    }
}
