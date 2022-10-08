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
public class EllipsoidTest
{
    
    public EllipsoidTest()
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
    public void testInstance()
    {
        // Bessel 1841
        Ellipsoid instance=new Ellipsoid(6377397.155, null, 1.0/299.15281285);
        assertEquals(6377397.155        , instance.a, 0.001);
        assertEquals(6356078.963        , instance.b, 0.001);
        assertEquals(0.08169683122      , instance.e, 0.00000000001);
        assertEquals(1.0/299.15281285   , instance.f, 0.00000000001);
    }

    @Test
    public void testInstance2()
    {
        // Airy
        Ellipsoid instance=new Ellipsoid(6377563.396, 6356256.910, null);;
        assertEquals(6377563.396        , instance.a, 0.001);
        assertEquals(6356256.910        , instance.b, 0.001);
        assertEquals(0.0816734          , instance.e, 0.0000001);
    }    
}
