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

        // IOGP Publication 373-7-2 – Geomatics Guidance Note number 7, part 2 – September 2019, p55-56
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

        // IOGP Publication 373-7-2 – Geomatics Guidance Note number 7, part 2 – September 2019, page 55-56
        lat=50.0+1/60.0*(30.0+0.0/60.0);
        lon= 0.0+1/60.0*(30.0+0.0/60.0);
        tm=instance.latLonToMapDatum2(new LatLonCoordinate(lat, lon));   
        assertEquals(69740.50, tm.northing, 0.01);
        assertEquals(577274.99, tm.easting, 0.01);    
    }
    
    /**
     * Example from IOGP Publication 373-7-2 – Geomatics Guidance Note number 7, 
     * part 2 – September 2019, page 55-56
     */
    @Test
    public void testMapDatumToLatLon()
    {
        LatLonCoordinate llc;
        System.out.println("mapDatumToLatLon");   
        TransverseMercatorProjection instance=TransverseMercatorProjection.BRITISH_NATIONAL_GRID;     
        DatumCoordinate dc  =new DatumCoordinate();

        // IOGP Publication 373-7-2 – Geomatics Guidance Note number 7, part 2 – September 2019, page 55-56
        dc.easting          =577274.984;
        dc.northing         = 69740.493;
        llc=instance.mapDatumToLatLon(dc);
        assertEquals(50.5, llc.phi      , 0.00000001);
        assertEquals( 0.5, llc.lambda   , 0.00000001);
        

        // Example from http://fgg-web.fgg.uni-lj.si/~/mkuhar/Zalozba/TM_projection.pdf
        // 9.3, page 13 Caister water tower
        dc.easting          =651409.903;
        dc.northing         =313177.270;
        llc=instance.mapDatumToLatLon(dc);
        assertEquals(52.0+1/60.0*(39.0+27.2531/60.0), llc.phi     , 0.0000001);
        assertEquals( 1.0+1/60.0*(43.0+ 4.5177/60.0), llc.lambda  , 0.0000001);
    }

    
    @Test
    public void testUtm31N()
    {
        System.out.println("____________________________________________________");
        System.out.println("UTM 31N");
        // OLV Amersfoort, UTM coordinates from geocachingtoolbox.com
        LatLonCoordinate rdLatLon       = new LatLonCoordinate();
        rdLatLon.phi                    =52.15615833;
        rdLatLon.lambda                 =5.387633333;
        rdLatLon.h                      =0.0;
        TransverseMercatorProjection instance= TransverseMercatorProjection.UTM31N;
        DatumCoordinate result          = instance.latLonToMapDatum(rdLatLon);
        assertEquals( 663330  , result.easting , 1);
        assertEquals(5781095  , result.northing, 1);
        System.out.println(String.format("[%8.6f, %8.6f] to [31U %8.3f, %8.3f]",
                           rdLatLon.phi, rdLatLon.lambda,
                           result.easting, result.northing));        
    }    

    /**
     * This method calculates the northing/easting of the Martini Toren
     * in Groningen according to two projections: 1. the 'Rijksdriehoeksmeting' 
     * which is Stereographic and uses Bessel1841 and 2. The 
     * Transverse mercator projection, with OLV Amersfoort as center and
     * an identical scaling and false easting/northing as the RD.
     * It then calculates the difference in coordinates of the 
     * Transverse Mercator with respect to the RD.
     */
    @Test
    public void testCompareTransverseMercatorToRDProjection()
    {
        System.out.println("____________________________________________________");
        System.out.println("Comapre Transverse Mercator to Stereographic northing/easting");
        
        // Martinitoren Groningen, WGS84, Transverse mercator
        LatLonCoordinate rdLatLon       = new LatLonCoordinate();
        rdLatLon.phi                    =53.21936;
        rdLatLon.lambda                 =6.568298333;
        rdLatLon.h                      =0.0;
        TransverseMercatorProjection instance= TransverseMercatorProjection.OZI_WGS84_TM;
        DatumCoordinate result          = instance.latLonToMapDatum(rdLatLon);
        assertEquals( 233884.1  , result.easting , 0.1);
        assertEquals( 582063.5  , result.northing, 0.1);
        System.out.println(String.format("[%8.6f, %8.6f] to [%8.3f, %8.3f]",
                           rdLatLon.phi, rdLatLon.lambda,
                           result.easting, result.northing));      
        
        // Martinitoren Groningen, Bessel1841, Stereographic
        MapDatumConvert mdc=new MapDatumConvert();
        DatumCoordinate dc=mdc.wgs84ToRd(rdLatLon);
        assertEquals( 233889.9  , dc.easting , 0.1);
        assertEquals( 582062.9  , dc.northing, 0.1);        
        System.out.println(String.format("[%8.6f, %8.6f] to [%8.3f, %8.3f]",
                           rdLatLon.phi, rdLatLon.lambda,
                           dc.easting  , dc.northing));   
        
        System.out.println(String.format("Difference: %3.1f, %3.1f m", result.easting-dc.easting, 
                                                                       result.northing-dc.northing));
    }    
}
