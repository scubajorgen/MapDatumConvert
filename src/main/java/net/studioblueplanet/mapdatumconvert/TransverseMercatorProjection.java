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
public class TransverseMercatorProjection implements MapProjection
{


    private final Ellipsoid    el;         // Ellipse to use
    private final double       F0;         // Scaling of axis
    private final double       phi0;       // Center latitude in degrees
    private final double       lambda0;    // Center longitude in degrees, central meridian
    private final double       phi0Rad;    // Center latitude in radians
    private final double       lambda0Rad; // Center longitude in radians
    private final double       X0;         // Norhting constant in m
    private final double       Y0;         // Easting constant in m

    public  final static TransverseMercatorProjection UTM31N=
            new TransverseMercatorProjection(Ellipsoid.ELLIPSOID_WGS84, 0.9996     , 0.0, 3.0, 0, 500000.0);

    public  final static TransverseMercatorProjection BRITISH_NATIONAL_GRID=
            new TransverseMercatorProjection(Ellipsoid.ELLIPSOID_AIRY, 0.9996012717, 49.0, -2.0, -100000.0, 400000.0);

    public  final static TransverseMercatorProjection OZI_WGS84_TM=
            new TransverseMercatorProjection(Ellipsoid.ELLIPSOID_WGS84, 0.9999079  ,52.1551722, 5.3872035, 463000.0, 155000.0);
    
    /**
     * Constructor. Parametrizes the projection.
     * @param el Ellipse to use
     * @param F0 Scaling factor
     * @param phi0 Latitude of origin in degrees
     * @param lambda0 Longitude of origin in degrees
     * @param X0 Constant Northing for false origin 
     * @param Y0 Constant Easting for false origin
     */
    public TransverseMercatorProjection(Ellipsoid el, double F0, double phi0, double lambda0, double X0, double Y0)
    {
        this.el         =el;
        this.F0         =F0;
        this.phi0       =phi0;
        this.lambda0    =lambda0;
        this.X0         =X0;
        this.Y0         =Y0;
        this.phi0Rad    =phi0/180.0*Math.PI;
        this.lambda0Rad =lambda0/180.0*Math.PI;
    }
    
    /**
     * Converts lat lon to northing (x) and easting (y) according to the
     * transverse mercator map projection
     * https://www.icao.int/safety/pbn/Documentation/EUROCONTROL/Eurocontrol%20WGS%2084%20Implementation%20Manual.pdf
     * Appendix G
     * and 'The ellipsoid and the Transverse Mercator Projection' 8.2.
     * @param latLon Latitude/Longitude coordinate
     * @return The x,y coordinate (Northing, Easting) in m
     */
    public DatumCoordinate latLonToMapDatum2(LatLonCoordinate latLon)
    {
        DatumCoordinate tm;
        double          X;
        double          Y;
        double          nu;
        double          rho;
        double          phiRad;
        double          lambdaRad;
        double          eta2;
        double          A,B,C,D,E,F;
        double          M, Ma, Mb, Mc, Md;
        double          n;
        double          P;
     

        phiRad      =latLon.phi/180.0*Math.PI;
        lambdaRad   =latLon.lambda/180.0*Math.PI;
        nu          =el.a*F0/Math.sqrt(1-el.e2*Math.pow(Math.sin(phiRad), 2.0));        // N = nu = a/W
        rho         =el.a*F0*(1-el.e2)/Math.pow(1-el.e2*Math.pow(Math.sin(phiRad), 2.0), 1.5);
        eta2        =nu/rho-1.0;
        A           =nu/  2*Math.sin(phiRad)*Math.cos(phiRad); // II
        B           =nu/ 24*Math.sin(phiRad)*Math.pow(Math.cos(phiRad),3.0)*( 5-   Math.pow(Math.tan(phiRad),2.0)+9*eta2); // III
        C           =nu/720*Math.sin(phiRad)*Math.pow(Math.cos(phiRad),5.0)*(61-58*Math.pow(Math.tan(phiRad),2.0)+
                                                    Math.pow(Math.tan(phiRad), 4.0)); // IIIA
        D           =nu    *Math.cos(phiRad); // IV
        E           =nu/6  *Math.pow(Math.cos(phiRad),3.0)*(nu/rho-Math.pow(Math.tan(phiRad), 2.0)); // V
        F           =nu/120*Math.pow(Math.cos(phiRad),5.0)*(5.0-18*Math.pow(Math.tan(phiRad), 2.0)+Math.pow(Math.tan(phiRad), 4.0)+
                                                    14*eta2-58*Math.pow(Math.tan(phiRad), 2.0)*eta2); // VI
        
        n   =el.n;
        Ma  =(1.0+n+5.0/4.0*n*n+5.0/4.0*n*n*n)*(phiRad-phi0Rad);
        Mb  =(3.0*n+3.0*n*n+21.0/8.0*n*n*n)   *Math.sin(phiRad-phi0Rad)*Math.cos(phiRad+phi0Rad);
        Mc  =(15.0/8.0*n*n+15.0/8.0*n*n*n)    *Math.sin(2.0*(phiRad-phi0Rad))*Math.cos(2.0*(phiRad+phi0Rad));
        Md  =(25.0/24.0*n*n*n)                *Math.sin(3.0*(phiRad-phi0Rad))*Math.cos(3.0*(phiRad+phi0Rad)); // 25.0=35.0?
        M   =F0*el.b*(Ma-Mb+Mc-Md);
        
        P       =lambdaRad-lambda0Rad;
        X       =X0+M+Math.pow(P,2.0)*A+Math.pow(P,4.0)*B+Math.pow(P,6.0)*C;
        Y       =Y0+P*D+Math.pow(P, 3.0)*E+Math.pow(P, 5.0)*F; // Easting
  
        tm=new DatumCoordinate(X, Y);
        return tm;
    }

    /**
     * Converts lat lon to northing (X) and easting (Y) according to the
     * transverse mercator map projection ESPG method according to
     * OGP Surveying and Positioning Guidance Note number 7, part 2 – May 2009
     * @param lat Latitude in degrees
     * @param lon Longitude in degrees
     * @return The x,y coordinate in m
     */
    @Override
    public DatumCoordinate latLonToMapDatum(LatLonCoordinate latLon)
    {
        DatumCoordinate tm;
        double          X;
        double          Y;
        double          phiRad;
        double          lambdaRad;
        double          nu;
        double          T;
        double          C;
        double          A;
        double          M;
        double          M0;
        double          e2;

     
        phiRad      =latLon.phi/180.0*Math.PI;     // NS
        lambdaRad   =latLon.lambda/180.0*Math.PI;  // EW
        
        e2          =el.e2;
        nu          =el.a/Math.sqrt(1-e2*Math.pow(Math.sin(phiRad), 2.0));        // N = nu = a/W
        T           =Math.tan(phiRad)*Math.tan(phiRad);
        C           =e2*Math.pow(Math.cos(phiRad), 2.0)/(1-e2);
        A           =(lambdaRad-lambda0Rad)*Math.cos(phiRad);
        M           =el.a*
                     ((1-e2/4.0-3.0*e2*e2/64.0-5.0*e2*e2*e2/256.0)    *(phiRad-phi0Rad)-
                      (3.0*e2/8.0+3.0*e2*e2/32.0+45.0*e2*e2*e2/1024.0)*(Math.sin(2.0*phiRad)-Math.sin(2.0*phi0Rad))+
                      (15.0*e2*e2/256.0+45.0*e2*e2*e2/1024.0)         *(Math.sin(4.0*phiRad)-Math.sin(4.0*phi0Rad))-
                      (35.0*e2*e2*e2/3072.0)                          *(Math.sin(6.0*phiRad)-Math.sin(6.0*phi0Rad)));  // M - M-M0
        
        X       =X0+F0*(M+nu*Math.tan(phiRad)*((A*A/2)+(5.0-T+9.0*C+4.0*C*C)*A*A*A*A/24.0+
                                               (61.0-58.0*T+T*T+600.0*C-330.0*e2/(1-e2))*Math.pow(A, 6.0)/720.0));  // Northing
        Y       =Y0+F0*nu*(A+(1-T+C)*A*A*A/6.0+(5.0-18.0*T+T*T+72.0*C-58.0*e2/(1-e2))   *Math.pow(A, 5.0)/120.0);            // Easting
        tm=new DatumCoordinate(X, Y);
        return tm;
    }
    
    /**
     * Converts x,y to lat,lon according to the transverse mercator projection
     * @param x Northing
     * @param y Easting
     * @return The WGS84 lat lon coordinate
     */
    @Override
    public LatLonCoordinate mapDatumToLatLon(DatumCoordinate dc)
    {
        LatLonCoordinate    ll;
        double              lat;
        double              lon;
        
        // TO DO
        lat=0;
        lon=0;
        ll=new LatLonCoordinate(lat, lon);
        return ll;
    }
}
