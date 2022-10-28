/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

/**
 * This class represents the Oblique Stereographic map projection. This projection
 * is used for the Rijksdriehoeksmeting for example. The projection consists of
 * two conformal projections: first lat-lon on the ellipsoid to lat-lon on a calculation
 * sphere, then lat-lon on the sphere to easting, northing on the plane.
 * The method follows 
 * [1] 'Wat is het Rijksdriehoeksstelsel'
 * and
 * [2] The stereographic double projection by Thomson, Mepham and Steeves 1998 
 * @author jorgen
 */
public class StereographicProjection implements MapProjection
{
    // projection settings
    private final Ellipsoid el;
    private final double    phi0;                // latitude of origin in degrees
    private final double    phi0Rad;      
    private final double    lambda0;             // longitude of origin in degrees
    private final double    lambda0Rad;
    private final double    n;                   // constant for Gauss projection c1
    private final double    m;                   // constant for Gauss projection ln(c2)
    private final double    B0Rad;               // Latitude on sphere
    private final double    L0Rad;               // Longitude on sphere
    private final double    x0;                  // false northing
    private final double    y0;                  // false easting
    private final double    R;                   // Radius of sphere in m
    private final double    k;                   // scaling
    private final double    N;                   // height correction    

    
    public static final StereographicProjection RIJKSDRIEHOEKSMETING=
            new StereographicProjection(Ellipsoid.ELLIPSOID_BESSEL1841, 
                                        52.156160556, 5.387638889, 
                                        0.9999079, 
                                        6382644.571, 
                                        155000, 463000, 
                                        -0.113);
    
    /**
     * Constructor. Parametrizes the Stereographic projection
     * @param el Ellipse to use
     * @param phi0 Latitude of the map center in degrees
     * @param lambda0 Longitude of the map center in degrees
     * @param scale Scale factor of the projection axis
     * @param R Radius of the calculation sphere in m
     * @param easting Easting to add for false easting in m
     * @param northing Northing to add for false northing in m
     * @param N Constant to add for height.
     */
    public StereographicProjection(Ellipsoid el, double phi0, double lambda0, double scale, double R, double easting, double northing, double N)
    {
        double Psi;
        double m1;
        double m2;
        double m3;
        double m4;
        
        this.el             =el;
        this.phi0           =phi0;
        this.lambda0        =lambda0;
        this.k              =scale;
        this.N              =N;
        this.R              =R;
        phi0Rad             =phi0/360.0*2.0*Math.PI; 
        lambda0Rad          =lambda0/360.0*2.0*Math.PI;
        x0                  =easting;
        y0                  =northing;
        n                   =Math.sqrt(1+el.e2/(1-el.e2)*Math.pow(Math.cos(phi0Rad), 4.0));
        Psi                 =Math.asin(Math.sin(phi0Rad)/n);
        m1                  =Math.tan(Math.PI/4.0+Psi/2.0);
        m2                  =Math.tan(Math.PI/4.0+phi0Rad/2.0);
        m3                  =(1-el.e*Math.sin(phi0Rad))/(1+el.e*Math.sin(phi0Rad));
        m                   =Math.log(m1*Math.pow(m2*Math.pow(m3, el.e/2.0), -n));
        B0Rad               =sphereLat(phi0Rad);
        L0Rad               =sphereLon(lambda0Rad); // Is 5.39020263, but should be 5.387638889??
    }
    /**
     * Step 1a. 
     * Converts the RD coordinates to lat lon height with respect to the 
     * Bessel1841 Ellipsoid.
     * @param dc The RD coordinate
     * @return The lat lon height coordinate.
     */
    public LatLonCoordinate mapDatumToLatLon(DatumCoordinate dc)
    {
        LatLonCoordinate    latlon;
        double              dx;
        double              dy;
        double              sinAlpha;
        double              cosAlpha;
        double              r;
        double              PsiRad;
        double              BRad;
        double              DeltaL;
        double              w;
        double              q;
        double              dq;
        double              phiRad;
        
        latlon=new LatLonCoordinate();
        
        dx                  =dc.easting-x0;
        dy                  =dc.northing-y0;
        r                   =Math.sqrt(Math.pow(dx, 2)+Math.pow(dy, 2));
        
        if (r>0.0)
        {
            sinAlpha        =dx/r;
            cosAlpha        =dy/r;
            PsiRad          =2*Math.atan(r/(2*R*k));
            BRad            =Math.asin(cosAlpha*Math.cos(B0Rad)*Math.sin(PsiRad)+Math.sin(B0Rad)*Math.cos(PsiRad));
            DeltaL          =Math.asin(sinAlpha*Math.sin(PsiRad)/Math.cos(BRad));
            latlon.lambda   =(DeltaL/n)*180.0/Math.PI+lambda0;

            w               =Math.log(Math.tan(BRad/2.0+Math.PI/4.0));
            q               =(w-m)/n;
            dq              =0.0;
            phiRad          =0.0;
            for(int i=0;i<4;i++)
            {
                phiRad      =2.0*Math.atan(Math.exp(q+dq))-Math.PI/2.0;
                dq          =0.5*el.e*Math.log((1+el.e*Math.sin(phiRad))/(1-el.e*Math.sin(phiRad)));
            }
            latlon.phi      =phiRad/Math.PI*180.0;
        }
        else
        {
            latlon.phi      =phi0;
            latlon.lambda   =lambda0;
        }

        latlon.h        =dc.h+N;

        return latlon;
    }
    
    /**
     * Calculates the longitude on the sphere based on the longitude on the
     * ellipsoid
     * @param lambdaRad Longitude on the ellipsoid in rad
     * @return Longitude on the sphere in rad
     */
    private double sphereLon(double lambdaRad)
    {
        return n*lambdaRad;
    }
    
    /**
     * Calculates the latitude on the sphere based on the latitude on the
     * ellipsoid
     * @param phiRad Latitude on the ellipsoid in rad
     * @return Latitude on the sphere in rad
     */
    private double sphereLat(double phiRad)
    {
        double q; 
        double dq;
        double w;
        double B;
        q           =Math.log(Math.tan(phiRad/2.0+Math.PI/4.0));
        dq          =0.5*el.e*Math.log((1+el.e*Math.sin(phiRad))/(1-el.e*Math.sin(phiRad)));
        q           =q-dq;
        w           =n*q+m;
        B           =2*Math.atan(Math.exp(w))-Math.PI/2;       
        return B;
    }
    
    /**
     * Step 1b. 
     * Converts a lat/lon/height coordinate with respect to the Bessel1841
     * ellipsoid to a Rijksdriehoeksmeting coordinate.
     * @param latlon The input coordinate
     * @return The RD coordinate
     */
    @Override
    public DatumCoordinate latLonToMapDatum(LatLonCoordinate latlon)
    {
        DatumCoordinate dc;
        double          lambdaRad;
        double          phiRad;
        double          BRad;
        double          LRad;
        double          dLRad;
        double          PsiRad;
        double          sinAlpha;
        double          cosAlpha;
        double          r;
        
        dc          =new DatumCoordinate();
        
        if (latlon.lambda==lambda0 && latlon.phi==phi0)
        {
            dc.easting=x0;
            dc.northing=y0;
        }
        else
        {
            phiRad      =Math.PI*latlon.phi/180.0;
            lambdaRad   =Math.PI*latlon.lambda/180.0;
            BRad        =sphereLat(phiRad);         // Latitude on sphere
            LRad        =sphereLon(lambdaRad);      // Longitude on spere
            dLRad       =LRad-L0Rad;
            PsiRad      =Math.asin(Math.sqrt(Math.pow(Math.sin(0.5*(BRad-B0Rad)),2.0)+
                                   Math.pow(Math.sin(0.5*dLRad), 2.0)*Math.cos(BRad)*Math.cos(B0Rad)))*2.0;
            sinAlpha    =Math.sin(dLRad)*Math.cos(BRad)/Math.sin(PsiRad);
            cosAlpha    =(Math.sin(BRad)-Math.sin(B0Rad)*Math.cos(PsiRad))/(Math.cos(B0Rad)*Math.sin(PsiRad));
            r           =2*k*R*Math.tan(PsiRad/2.0);
            dc.easting  =r*sinAlpha+x0;
            dc.northing =r*cosAlpha+y0;
        }
        dc.h        =latlon.h-N;
        
        return dc;
    }
    
}
