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

    /**
     * Step 2a, 4b.
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
    
    /**
     * Executes the datum transform of carthesian coordinates. It is a 3D
     * similarity transform consisting of ratation, translation and scaling.
     * @param in Input coordinate
     * @param p  Transform parameters
     * @return The coordinate in against the new coordinate axes
     */
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
     * Step 3a.
     * Datum transfer from RD to WGS84
     * @param rd RD coordinate [X, Y, Z]
     * @return WGS84 coordinate [X, Y, Z]
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
     * Step 3b
     * Datum transfer from WGS84 to RD
     * @param wgs84 WGS84 coordinate [X, Y, Z]
     * @return RD coordinate [X, Y, Z]
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

    /**
     * Step 2b, 4a.
     * Conversion from Carthesian coordinate to lat, lon, height
     * @param in Input coordinate [X,Y,Z]
     * @param el Ellipsoid to use
     * @return The lat/lon/height coordinate with respect to the ellipsoid
     */
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
     * This method converst RD lat/lon coordinates to WGS84 lat/lon coordinates
     * @param rd The RD coordinate as latitude,longitude
     * @return The WGS84 coordinate
     */
    public LatLonCoordinate rdLatLonToWgs84(LatLonCoordinate rd)
    {
        LatLonCoordinate        rdLatLon;
        CarthesianCoordinate    rdCarthesian;
        CarthesianCoordinate    wgs84Carthesian;
        LatLonCoordinate        wgs84LatLon;
        StereographicProjection p;
        
        p               =StereographicProjection.RIJKSDRIEHOEKSMETING;
        rdCarthesian    =this.latLonToCarthesian(rd, Ellipsoid.ELLIPSOID_BESSEL1841);
        wgs84Carthesian =this.datumTransformRdToWgs84(rdCarthesian);
        wgs84LatLon     =this.carthesianToLatLon(wgs84Carthesian, Ellipsoid.ELLIPSOID_WGS84);
        
        return wgs84LatLon;
    }
    

    
    /**
     * This method converst RD coordinates to WGS84 coordinates
     * @param rd The RD coordinate
     * @return The WGS84 coordinate
     */
    public LatLonCoordinate rdToWgs84(DatumCoordinate rd)
    {
        LatLonCoordinate        rdLatLon;
        CarthesianCoordinate    rdCarthesian;
        CarthesianCoordinate    wgs84Carthesian;
        LatLonCoordinate        wgs84LatLon;
        StereographicProjection p;
        
        p               =StereographicProjection.RIJKSDRIEHOEKSMETING;
        rdLatLon        =p.mapDatumToLatLon(rd);
        rdCarthesian    =this.latLonToCarthesian(rdLatLon, Ellipsoid.ELLIPSOID_BESSEL1841);
        wgs84Carthesian =this.datumTransformRdToWgs84(rdCarthesian);
        wgs84LatLon     =this.carthesianToLatLon(wgs84Carthesian, Ellipsoid.ELLIPSOID_WGS84);
        
        return wgs84LatLon;
    }

    /**
     * This method converts a WGS84 lat/lon coordinate to RD lat/lon
     * coordinate
     * @param wgs The WGS84 lat/lon coordinate
     * @return The RD lat/lon coordinate
     */
    public LatLonCoordinate wgs84ToRdLatLon(LatLonCoordinate wgs)
    {
        LatLonCoordinate        rdLatLon;
        CarthesianCoordinate    rdCarthesian;
        CarthesianCoordinate    wgs84Carthesian;
        LatLonCoordinate        wgs84LatLon;
        StereographicProjection p;
        
        p               =StereographicProjection.RIJKSDRIEHOEKSMETING;
        wgs84Carthesian =latLonToCarthesian(wgs, Ellipsoid.ELLIPSOID_WGS84);
        rdCarthesian    =datumTransformWgs84ToRd(wgs84Carthesian);
        rdLatLon        =carthesianToLatLon(rdCarthesian, Ellipsoid.ELLIPSOID_BESSEL1841);
        
        return rdLatLon;
    }


    /**
     * This method converts a WGS84 lat/lon coordinate to RD easting/northing
     * coordinate
     * @param wgs The WGS84 lat/lon coordinate
     * @return The RD easting/northing coordinate
     */
    public DatumCoordinate wgs84ToRd(LatLonCoordinate wgs)
    {
        DatumCoordinate rd;
        LatLonCoordinate        rdLatLon;
        CarthesianCoordinate    rdCarthesian;
        CarthesianCoordinate    wgs84Carthesian;
        LatLonCoordinate        wgs84LatLon;
        StereographicProjection p;
        
        p               =StereographicProjection.RIJKSDRIEHOEKSMETING;
        wgs84Carthesian =latLonToCarthesian(wgs, Ellipsoid.ELLIPSOID_WGS84);
        rdCarthesian    =datumTransformWgs84ToRd(wgs84Carthesian);
        rdLatLon        =carthesianToLatLon(rdCarthesian, Ellipsoid.ELLIPSOID_BESSEL1841);
        rd              =p.latLonToMapDatum(rdLatLon);
        
        return rd;
    }
    

}
