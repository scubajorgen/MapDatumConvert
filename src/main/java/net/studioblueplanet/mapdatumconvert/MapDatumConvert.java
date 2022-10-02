/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

/**
 * This class implements the coordinate conversion between Rijksdriehoeksmeting
 * and WGS84 and v.v. The method followed is described in 
 * https://www.johannespostgroep.nl/wp-content/uploads/2008/10/rijksdriehoeksstelsel.pdf
 * (the error in this document has been corrected).
 * This class is meant for illustration of the formulas.
 * Angles are in degrees, unless the extension rad is used (for radians).
 * @author jorgen
 */
public class MapDatumConvert
{
    /**
     * Represents a Rijksdriehoeksmeting coordinate. x = easting, y=northing, 
     * h=height.
     */
    public static class RdCoordinate
    {
        public double x;        // easting in m
        public double y;        // northing in m
        public double h;        // height in m 
    };
    
    /**
     * Represents a carthesian [X, Y, Z] coordinate
     */
    public static class CarthesianCoordinate
    {
        public double X;        // X coordinate in m
        public double Y;        // Y coordinate in m
        public double Z;        // Z coordinate in m
    };
    
    /**
     * Represents a latitude/longitude coordinate with height
     */
    public static class LatLonCoordinate
    {
        public double phi;      // latitude in degrees
        public double lambda;   // longitude in degrees
        public double h;        // height in m
    }
    
    /**
     * Represents the parameters for an ellipsoid
     */
    public static class Ellipsoid
    {
        public double a;    // Radius in m
        public double e;    // Excentricity
        
        public Ellipsoid(double a, double e)
        {
            this.a=a;
            this.e=e;
        }
    };
    
    /**
     * Represents the parameters for Datum Transformation
     */
    public static class DatumTransformParameters
    {
        public double alpha;    // angle in degrees
        public double beta;     // angle in degrees
        public double gamma;    // angle in degrees
        public double delta;    // scaling
        public double x;        // rotation center
        public double y;        // rotation center
        public double z;        // rotation center
        public double tx;       // origin transfer
        public double ty;       // origin transfer
        public double tz;       // origin transfer
    }
    
    public static Ellipsoid ellipsoidBessel1841 =new Ellipsoid(6377397.155, 0.081696831222);
    public static Ellipsoid ellipsoidWgs84      =new Ellipsoid(6378137.0, 0.0818191908426215);
    double phi0Rd               =52.156160556;                  // latitude of origin
    double phi0RdRad            =phi0Rd/360.0*2.0*Math.PI;      
    double lambda0Rd            =5.387638889;                   // longitude of origin
    double lambda0RdRad         =lambda0Rd/360.0*2.0*Math.PI;
    double B0                   =52.121097249;
    double B0Rad                =B0/360.0*2.0*Math.PI;
    double L0                   =5.387638889;
    double L0Rad                =L0/360.0*2.0*Math.PI;
    double n                    =1.00047585668;
    double m                    =0.003773953832;
    double R                    =6382644.571;                   // Radius of sphere in m
    double k                    =0.9999079;                     // scaling
    double x0                   =155000;                        // false northing
    double y0                   =463000;                        // false easting
    double N                    =-0.113;                        // height correction
    
    /**
     * Converts the RD coordinates to lat lon height with respect to the 
     * Bessel1841 Ellipsoid.
     * @param rd The RD coordinate
     * @return The lat lon height coordinate.
     */
    public LatLonCoordinate rdToLatLon(RdCoordinate rd)
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
        
        dx              =rd.x-x0;
        dy              =rd.y-y0;
        r               =Math.sqrt(Math.pow(dx, 2)+Math.pow(dy, 2));
        
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
                dq          =0.5*ellipsoidBessel1841.e*Math.log((1+ellipsoidBessel1841.e*Math.sin(phiRad))/(1-ellipsoidBessel1841.e*Math.sin(phiRad)));
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
     * Converts a lat/lon/height coordinate with respect to the Bessel1841
     * ellipsoid to a Rijksdriehoeksmeting coordinate.
     * @param latlon The input coordinate
     * @return The RD coordinate
     */
    public RdCoordinate latLonToRd(LatLonCoordinate latlon)
    {
        RdCoordinate    rd;
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
        
        rd          =new RdCoordinate();
        
        phiRad      =2.0*Math.PI*latlon.phi/360.0;
        lambdaRad   =2.0*Math.PI*latlon.lambda/360.0;

        q           =Math.log(Math.tan(phiRad/2.0+Math.PI/4.0));
        dq          =0.5*ellipsoidBessel1841.e*Math.log((1+ellipsoidBessel1841.e*Math.sin(phiRad))/(1-ellipsoidBessel1841.e*Math.sin(phiRad)));
        q           =q-dq;
        w           =n*q+m;
        B           =2*Math.atan(Math.exp(w))-Math.PI/2;
        dL          =n*(lambdaRad-lambda0RdRad);
        PsiRad      =Math.asin(Math.sqrt(Math.pow(Math.sin(0.5*(B-B0Rad)),2.0)+Math.pow(Math.sin(0.5*dL), 2.0)*Math.cos(B)*Math.cos(B0Rad)))*2.0;
        sinAlpha    =Math.sin(dL)*Math.cos(B)/Math.sin(PsiRad);
        cosAlpha    =(Math.sin(B)-Math.sin(B0Rad)*Math.cos(PsiRad))/(Math.cos(B0Rad)*Math.sin(PsiRad));
        r           =2*k*R*Math.tan(PsiRad/2.0);
        
        rd.x        =r*sinAlpha+x0;
        rd.y        =r*cosAlpha+y0;
        rd.h        =latlon.h-N;
        
        return rd;
    }
    
    /**
     * Convers lat/lon/height to Carthesian coordinate assuming given ellipsoid
     * @param latlon The coordinate to convert
     * @param el The ellipsoid to use
     * @return The Carthesian coordinate
     */
    public CarthesianCoordinate latLonToCarthesian(LatLonCoordinate latlon, Ellipsoid el)
    {
        CarthesianCoordinate    coordinate;
        double                  lambdaRad;
        double                  phiRad;
        double                  W;
        double                  N;
        
        coordinate=new CarthesianCoordinate();
        
        lambdaRad       =latlon.lambda/360.0*2.0*Math.PI;
        phiRad          =latlon.phi/360.0*2.0*Math.PI;
        W               =Math.sqrt(1-Math.pow(el.e,2)*Math.pow(Math.sin(phiRad), 2));
        N               =el.a/W;
        coordinate.X    =(N+latlon.h)*Math.cos(phiRad)*Math.cos(lambdaRad);
        coordinate.Y    =(N+latlon.h)*Math.cos(phiRad)*Math.sin(lambdaRad);
        coordinate.Z    =(N-Math.pow(el.e, 2.0)*N+latlon.h)*Math.sin(phiRad);
        return coordinate;
    }
    
    public CarthesianCoordinate datumTransform(CarthesianCoordinate in, DatumTransformParameters p)
    {
        CarthesianCoordinate out;
        
        out=new CarthesianCoordinate();
        
        out.X=in.X+( p.delta*(in.X-p.x)+p.gamma*(in.Y-p.y)-p.beta *(in.Z-p.z))+p.tx;
        out.Y=in.Y+(-p.gamma*(in.X-p.x)+p.delta*(in.Y-p.y)+p.alpha*(in.Z-p.z))+p.ty;
        out.Z=in.Z+( p.beta *(in.X-p.x)-p.alpha*(in.Y-p.y)+p.delta*(in.Z-p.z))+p.tz;
        
        return out;        
    }
    
    /**
     * 
     * @param rd
     * @return 
     */
    public CarthesianCoordinate datumTransformRdToWgs84(CarthesianCoordinate rd)
    {
        DatumTransformParameters p;
        
        p=new DatumTransformParameters();
        p.alpha    = 1.9848E-6;  // rotation angle in rad
        p.beta     =-1.7439E-6; // rotation angle in rad
        p.gamma    = 9.0587E-6;  // rotation angle in rad
        
        p.delta    =4.0772E-6;     // scaling
        p.tx       =593.032;       // origin shift in m
        p.ty       =26.000;        // origin shift in m
        p.tz       =478.741;       // origin shift in m
        p.x        =3903453.148;   // rotation center in m
        p.y        =368135.313;    //
        p.z        =5012970.306;   //
        
        return datumTransform(rd, p);
    }
    
    /**
     * 
     * @param wgs84
     * @return 
     */
    public CarthesianCoordinate datumTransformWgs84ToRd(CarthesianCoordinate wgs84)
    {
        DatumTransformParameters p;
        
        p=new DatumTransformParameters();
        p.alpha    =-1.9848E-6;     // rotation angle in rad
        p.beta     = 1.7439E-6;     // rotation angle in rad
        p.gamma    =-9.0587E-6;     // rotation angle in rad
        
        p.delta    =-4.0772E-6;     // scaling
        p.tx       =-593.032;       // origin shift in m
        p.ty       =-26.000;        // origin shift in m
        p.tz       =-478.741;       // origin shift in m
        p.x        =3904046.18;     // rotation center in m
        p.y        =368161.313;     //
        p.z        =5013449.047;    //
        
        return datumTransform(wgs84, p);
    }

    public LatLonCoordinate carthesianToLatLon(CarthesianCoordinate in, Ellipsoid el)
    {
        LatLonCoordinate    latlon;
        double              r;
        double              phiRad;
        double              lambdaRad;
        double              W;
        double              N;
        
        latlon=new LatLonCoordinate();
        
        r       =Math.sqrt(in.X*in.X+in.Y*in.Y);
        phiRad  =Math.atan(1/r*(in.Z+el.e*el.e*in.Z)); // intial guess
        for(int i=0; i<4; i++)
        {
            W=Math.sqrt(1-el.e*el.e*Math.pow(Math.sin(phiRad),2));
            N=el.a/W;
            phiRad=Math.atan(1/r*(in.Z+el.e*el.e*N*Math.sin(phiRad)));
        }
        latlon.phi      =360.0*phiRad/2/Math.PI;
        lambdaRad       =Math.atan(in.Y/in.X);
        latlon.lambda   =360.0*lambdaRad/2/Math.PI;
        latlon.h        =r*Math.cos(phiRad)+in.Z*Math.sin(phiRad)-el.a*Math.sqrt(1-el.e*el.e*Math.pow(Math.sin(phiRad),2));
        
        return latlon;
    }
    
    /**
     * This method converst RD coordinates to WGS84 coordinates
     * @param rd The RD coordinate
     * @return The WGS84 coordinate
     */
    public LatLonCoordinate RdToWgs(RdCoordinate rd)
    {
        LatLonCoordinate        rdLatLon;
        CarthesianCoordinate    rdCarthesian;
        CarthesianCoordinate    wgs84Carthesian;
        LatLonCoordinate        wgs84LatLon;
        
        rdLatLon        =this.rdToLatLon(rd);
        rdCarthesian    =this.latLonToCarthesian(rdLatLon, ellipsoidBessel1841);
        wgs84Carthesian =this.datumTransformRdToWgs84(rdCarthesian);
        wgs84LatLon     =this.carthesianToLatLon(wgs84Carthesian, ellipsoidWgs84);
        
        return wgs84LatLon;
    }
    
    public RdCoordinate wgs84ToRd(LatLonCoordinate wgs)
    {
        RdCoordinate rd;
        LatLonCoordinate        rdLatLon;
        CarthesianCoordinate    rdCarthesian;
        CarthesianCoordinate    wgs84Carthesian;
        LatLonCoordinate        wgs84LatLon;
        
        wgs84Carthesian =latLonToCarthesian(wgs, ellipsoidWgs84);
        rdCarthesian    =datumTransformWgs84ToRd(wgs84Carthesian);
        rdLatLon        =carthesianToLatLon(rdCarthesian, ellipsoidBessel1841);
        rd              =latLonToRd(rdLatLon);
        
        return rd;
    }
    

}
