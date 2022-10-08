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
public class StereoGraphicProjection implements MapProjection
{
    double phi0Rd               =52.156160556;                  // latitude of origin
    double phi0RdRad            =phi0Rd/360.0*2.0*Math.PI;      
    double lambda0Rd            =5.387638889;                   // longitude of origin
    double lambda0RdRad         =lambda0Rd/360.0*2.0*Math.PI;
    double B0                   =52.121097249;
    double B0Rad                =B0/360.0*2.0*Math.PI;
    double L0                   =5.387638889;
    double L0Rad                =L0/360.0*2.0*Math.PI;
    double x0                   =155000;                        // false northing
    double y0                   =463000;                        // false easting
    double n                    =1.00047585668;
    double m                    =0.003773953832;
    double R                    =6382644.571;                   // Radius of sphere in m
    double k                    =0.9999079;                     // scaling
    double N                    =-0.113;                        // height correction NAP - Bessel1841    
    Ellipsoid el                =Ellipsoid.ELLIPSOID_BESSEL1841;

    /**
     * Step 1a. 
     * Converts the RD coordinates to lat lon height with respect to the 
     * Bessel1841 Ellipsoid.
     * @param rd The RD coordinate
     * @return The lat lon height coordinate.
     */
    public LatLonCoordinate mapDatumToLatLon(DatumCoordinate rd)
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
        
        dx                  =rd.easting-x0;
        dy                  =rd.northing-y0;
        r                   =Math.sqrt(Math.pow(dx, 2)+Math.pow(dy, 2));
        
        if (r>0.0)
        {
            sinAlpha        =dx/r;
            cosAlpha        =dy/r;
            PsiRad          =2*Math.atan(r/(2*R*k));
            BRad            =Math.asin(cosAlpha*Math.cos(B0Rad)*Math.sin(PsiRad)+Math.sin(B0Rad)*Math.cos(PsiRad));
            DeltaL          =Math.asin(sinAlpha*Math.sin(PsiRad)/Math.cos(BRad));
            latlon.lambda   =(DeltaL/n)*360.0/2/Math.PI+lambda0Rd;

            w               =Math.log(Math.tan(BRad/2+Math.PI/4.0));
            q               =(w-m)/n;
            dq              =0.0;
            phiRad          =0.0;
            for(int i=0;i<4;i++)
            {
                phiRad      =2.0*Math.atan(Math.exp(q+dq))-Math.PI/2;
                dq          =0.5*el.e*Math.log((1+el.e*Math.sin(phiRad))/(1-el.e*Math.sin(phiRad)));
            }
            latlon.phi      =phiRad/2.0/Math.PI*360.0;
        }
        else
        {
            latlon.phi      =phi0Rd;
            latlon.lambda   =lambda0Rd;
        }

        latlon.h        =rd.h+N;

        return latlon;
    }
    
    /**
     * Step 1b. 
     * Converts a lat/lon/height coordinate with respect to the Bessel1841
     * ellipsoid to a Rijksdriehoeksmeting coordinate.
     * @param latlon The input coordinate
     * @return The RD coordinate
     */
    public DatumCoordinate latLonToMapDatum(LatLonCoordinate latlon)
    {
        DatumCoordinate rd;
        double          lambdaRad;
        double          phiRad;
        double          q;
        double          dq;
        double          w;
        double          B;
        double          dL;
        double          PsiRad;
        double          sinAlpha;
        double          cosAlpha;
        double          r;
        
        rd          =new DatumCoordinate();
        
        phiRad      =2.0*Math.PI*latlon.phi/360.0;
        lambdaRad   =2.0*Math.PI*latlon.lambda/360.0;

        q           =Math.log(Math.tan(phiRad/2.0+Math.PI/4.0));
        dq          =0.5*el.e*Math.log((1+el.e*Math.sin(phiRad))/(1-el.e*Math.sin(phiRad)));
        q           =q-dq;
        w           =n*q+m;
        B           =2*Math.atan(Math.exp(w))-Math.PI/2;
        dL          =n*(lambdaRad-lambda0RdRad);
        PsiRad      =Math.asin(Math.sqrt(Math.pow(Math.sin(0.5*(B-B0Rad)),2.0)+Math.pow(Math.sin(0.5*dL), 2.0)*Math.cos(B)*Math.cos(B0Rad)))*2.0;
        sinAlpha    =Math.sin(dL)*Math.cos(B)/Math.sin(PsiRad);
        cosAlpha    =(Math.sin(B)-Math.sin(B0Rad)*Math.cos(PsiRad))/(Math.cos(B0Rad)*Math.sin(PsiRad));
        r           =2*k*R*Math.tan(PsiRad/2.0);
        
        rd.easting  =r*sinAlpha+x0;
        rd.northing =r*cosAlpha+y0;
        rd.h        =latlon.h-N;
        
        return rd;
    }
    
}
