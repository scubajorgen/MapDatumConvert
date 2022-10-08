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
public class Ellipsoid
{
    public double a;    // long radius in m
    public double b;    // short radius in m
    public double f;    // flattening
    public double e;    // excentricity
    public double e2;   // excentricity squared
    public double n;    // n
        
    public final static Ellipsoid ELLIPSOID_AIRY        =new Ellipsoid(6377563.396, 6356256.910, null);
    public final static Ellipsoid ELLIPSOID_WGS84       =new Ellipsoid(6378137.000, 6356752.314, null);        
    public final static Ellipsoid ELLIPSOID_BESSEL1841  =new Ellipsoid(6377397.155, null       , 1.0/ 299.15281285);        
        
    /**
     * Constructor. The radius a must be specified. At least one of b or f 
     * must be specified.
     * @param a Long radius in m
     * @param b Short radius in m
     * @param f Flattening
     */
    public Ellipsoid(Double a, Double b, Double f)
    {
        this.a  =a;
        if (b!=null)
        {
            this.b  =b;
            this.f  =(a-b)/a;
            e2      =2.0*this.f-this.f*this.f;
            e       =Math.sqrt(e2);
            n       =(a-b)/(a+b);
        }
        else if (f!=null)
        {
            this.b  =a-a*f;
            this.f  =f;
            e2      =2.0*f-f*f;
            e       =Math.sqrt(e2);
            n       =(a-this.b)/(a+this.b);
        }
        else
        {
            System.err.println("At least one of b or f must be specified");
        }
    }    
}
