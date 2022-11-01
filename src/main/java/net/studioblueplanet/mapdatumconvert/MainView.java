/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.studioblueplanet.mapdatumconvert;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
 
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This view presents and compars the outline of the Netherlands 
 * assuming two projections.
 * @author jorgen
 */
public class MainView extends javax.swing.JFrame
{
    public static class PolygonLatLon
    {
        List<LatLonCoordinate> coordinates=new ArrayList<>();
    }
    
    public enum TestType {TEST1, TEST2, TEST3, TEST4, TEST5, TEST6};
    
    public enum DatumType {DATUMTYPE_RD, DATUMTYPE_WGS84};
    
    
    private static final    String              USER_AGENT          = "Mozilla/5.0";

    private static final    String              POST_PARAMS         = "userName=JohnDoe";   
    private static final    String              GEBIEDSDEEL_URL     ="https://service.pdok.nl/cbs/gebiedsindelingen/2022/wfs/v1_0?"+
                                                                     "request=GetFeature&service=WFS&typenames=landsdeel_gegeneraliseerd&version=2.0.0&"+
                                                                     "outputformat=json&srsname=urn:ogc:def:crs:EPSG::4326";
    private static final    boolean             READ_FROM_PDOK      =true;
    
    // Martinitoren
    private static final    LatLonCoordinate    MARTINI_WGS84       =new LatLonCoordinate(53.2193683, 6.56827);
    private static          LatLonCoordinate    MARTINI_RD;

    // OLV Amersfoort
    private static final    LatLonCoordinate    OLV_WGS84           =new LatLonCoordinate(52.1551722, 5.3872035);    
    private static          LatLonCoordinate    OLV_RD;
    
    private final           LatLonCoordinate    TOPLEFT_WGS84       =new LatLonCoordinate(53.80, 3.10);
    private final           LatLonCoordinate    BOTTOMRIGHT_WGS84   =new LatLonCoordinate(50.70, 7.30);
    private final           LatLonCoordinate    TOPLEFT_RD;
    private final           LatLonCoordinate    BOTTOMRIGHT_RD;
    
    private final           int                 MARGIN              =25;
    

    private                 DatumCoordinate     topLeft;
    private                 DatumCoordinate     bottomRight;
    private                 int                 topLeftX;
    private                 int                 topLeftY;
    private                 int                 bottomRightX;
    private                 int                 bottomRightY;
    private                 double              meterPerPixelX;     // scaling for fit, X direction
    private                 double              meterPerPixelY;     // scaling for fit, Y direction
    private                 double              meterPerPixel;      
    
    private final           List<PolygonLatLon> polygonsWGS84       =new ArrayList<>();    
    private final           List<PolygonLatLon> polygonsRD          =new ArrayList<>();    
    
    private                 TestType            test                =TestType.TEST1;
   
    /**
     * Creates new form MainView
     */
    public MainView()
    {
        String jsonString;
   
        if (READ_FROM_PDOK)
        {
            jsonString=sendGet(GEBIEDSDEEL_URL).toString();
        }
        else
        {
            jsonString=this.readFile("landsdeel_WGS84.json");
        }
        readMap(jsonString);
        convertToRD();
        initComponents();
        
        MapDatumConvert mdc =new MapDatumConvert();
        OLV_RD              =mdc.wgs84ToRdLatLon(OLV_WGS84);
        MARTINI_RD          =mdc.wgs84ToRdLatLon(MARTINI_WGS84);
        TOPLEFT_RD          =mdc.wgs84ToRdLatLon(TOPLEFT_WGS84);
        BOTTOMRIGHT_RD      =mdc.wgs84ToRdLatLon(BOTTOMRIGHT_WGS84);
    }

    /**
     * Executes a HTTP request
     * @param getUrl The URL to request
     * @return The response as list of strings
     * @throws IOException 
     */
    private StringBuffer sendGet(String getUrl)
    {
        StringBuffer response = new StringBuffer();
        try
        {
            URL obj = new URL(getUrl);
            HttpURLConnection con = (HttpURLConnection)obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("User-Agent", USER_AGENT);
            int responseCode = con.getResponseCode();
            System.out.println("GET Response Code :: " + responseCode);
            if (responseCode == HttpURLConnection.HTTP_OK) 
            { // success
                BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) 
                {
                    response.append(inputLine);
                }
                in.close();
            } 
            else 
            {
                System.err.println("GET request not worked");
            }
        }
        catch(IOException e)
        {
            System.out.println("Error reading PDOK: "+e.getMessage());
        }
        return response;
    }
    
    /**
     * Reads JSON file
     * @param filename Filename
     * @return File as String
     */
    private String readFile(String filename)
    {
        List<String>    strings;
        String          returnValue;
        
        returnValue=null;
        Path path = Paths.get(filename);
        try 
        {
            byte[] encoded = Files.readAllBytes(path);
            returnValue=new String(encoded, "UTF8");
        } 
        catch (Exception e) 
        {
            System.err.println("Error :"+e.getMessage());
        }
        return returnValue;
    }
    
    /**
     * Converts the WGS84 polygons to RD lat/lon polygons
     */
    private void convertToRD()
    {
        MapDatumConvert mdc=new MapDatumConvert();
        PolygonLatLon   pRD;
        
        for (PolygonLatLon p : polygonsWGS84)
        {
            pRD=new PolygonLatLon();
            polygonsRD.add(p);
            for(LatLonCoordinate c : p.coordinates)
            {
                pRD.coordinates.add(mdc.wgs84ToRdLatLon(c));
            }
        }
    }
    
    /**
     * Reads the outline of the Netherlands from JSON file as a series of
     * polygons consisting of WGS84 coordinates.
     */
    private void readMap(String jsonString)
    {
        double lat;
        double lon;
        
        //JSON parser object to parse read file
        JSONParser jsonParser = new JSONParser();
         
        try
        {
            //Read JSON file
            Object obj = jsonParser.parse(jsonString);
 
            JSONObject map = (JSONObject) obj;
            
            JSONArray parts=(JSONArray)map.get("features");
            for (Object part : parts)
            {
                JSONObject geometry=(JSONObject)((JSONObject)part).get("geometry");
                JSONArray pgons=(JSONArray)geometry.get("coordinates");
                for (Object pgon : pgons)
                {
                    for (Object exterior : (JSONArray)pgon)
                    {
                        PolygonLatLon polygon=new PolygonLatLon();
                        for (Object coordinate : (JSONArray)exterior)
                        {
                            lon=Double.parseDouble(((JSONArray)coordinate).get(0).toString());
                            lat=Double.parseDouble(((JSONArray)coordinate).get(1).toString());
                            LatLonCoordinate llc=new LatLonCoordinate(lat, lon);
                            polygon.coordinates.add(llc);
                        }
                        polygonsWGS84.add(polygon);
                    }
                }
            }
            System.out.println("Done reading map");
        } 
        catch (ParseException e) 
        {
            System.err.println(e.getMessage());
        }
    }
    
    /**
     * Draws the outline map based on the WGS84 coordinates
     * @param g Graphics to use
     * @param projection Projection to use.
     */
    private void drawMap(Graphics g, List<PolygonLatLon> map, MapProjection projection)
    {
        LatLonCoordinate    c;
        LatLonCoordinate    prevC;
        DatumCoordinate     d;
        DatumCoordinate     prevD;
        double              x1, y1, x2, y2;

        for (PolygonLatLon p : polygonsWGS84)
        {
            c       =p.coordinates.get(0);
            prevD   =projection.latLonToMapDatum(c);
            for(int i=1; i<p.coordinates.size(); i++)
            {
                c   =p.coordinates.get(i);
                d   =projection.latLonToMapDatum(c);
                x1  =topLeftX+((d.easting-topLeft.easting)/meterPerPixelX+0.5);
                y1  =topLeftY+((topLeft.northing-d.northing)/meterPerPixelY+0.5);
                x2  =topLeftX+((prevD.easting-topLeft.easting)/meterPerPixelX+0.5);
                y2  =topLeftY+((topLeft.northing-prevD.northing)/meterPerPixelY+0.5);
                g.drawLine((int)x1, (int)y1, (int)x2, (int)y2);
                prevD=d;
            }
        }
    }
    
    /**
     * Draws a reference point
     * @param g Graphics to use
     * @param projection Projection to use
     * @param llc Coordinate of the reference point
     */
    private DatumCoordinate drawReferencePoint(Graphics g, MapProjection projection, LatLonCoordinate llc, String name)
    {
        DatumCoordinate dc;
        double          x;
        double          y;

        
        dc=projection.latLonToMapDatum(llc);
        x=topLeftX+(dc.easting -topLeft.easting )/meterPerPixelX+0.5; // Add 0.5 for proper rounding
        y=topLeftY+(topLeft.northing-dc.northing)/meterPerPixelY+0.5;
        g.fillOval((int)x-5, (int)y-5, 10, 10);    

        System.out.println(String.format("%s E/N: %8.1f/%8.1f", name, dc.easting, dc.northing));
        return dc;
    }
    
    /**
     * This method calibrates the map canvas parameters. It uses the top left
     * coordinate and bottom right coordinate and fit them to the canvas.
     * @param projection Projection on which the calibration must take place
     */
    private void calibrate(MapProjection projection, DatumType type)
    {
        Dimension d;
        
        d           =this.jPanelMap.getSize();
        topLeftX    =MARGIN;
        topLeftY    =MARGIN;
        bottomRightX=d.width-MARGIN;
        bottomRightY=d.height-MARGIN;        
        if (type==DatumType.DATUMTYPE_RD)
        {
            topLeft     =projection.latLonToMapDatum(TOPLEFT_RD);
            bottomRight =projection.latLonToMapDatum(BOTTOMRIGHT_RD);            
        }
        else
        {
            topLeft     =projection.latLonToMapDatum(TOPLEFT_WGS84);
            bottomRight =projection.latLonToMapDatum(BOTTOMRIGHT_WGS84);
        }
        meterPerPixelX  =(bottomRight.easting-topLeft.easting     )/(bottomRightX-topLeftX);
        meterPerPixelY  =(topLeft.northing   -bottomRight.northing)/(bottomRightY-topLeftY);
        meterPerPixel   =Math.max(meterPerPixelX, meterPerPixelY);
        if (jCheckBoxMenuItemXYScale.isSelected())
        {
            meterPerPixelX=meterPerPixel;
            meterPerPixelY=meterPerPixel;
        }
    }

    /**
     * Reports the difference between the two datum coordinate 
     * @param dc1
     * @param dc2 
     */
    private void report(DatumCoordinate dc1, DatumCoordinate dc2)
    {
        System.out.println(String.format("Diff     E/N: %8.1f/%8.1f Total: %6.1f meter", 
                            (dc2.easting-dc1.easting), (dc2.northing-dc1.northing),
                            Math.sqrt(Math.pow((dc2.easting-dc1.easting), 2.0)+Math.pow((dc2.northing-dc1.northing), 2.0))));        
    }
    
    /**
     * Execute a comparison between two projections. 
     * The map area is calibrated based on projection p1
     * @param g Graphics to use
     * @param p1 1st projection
     * @param type1 Datum type to use with projection 1
     * @param p2 2nd projection
     * @param type2 Datum type to use with projection 2
     */
    private void compare(Graphics g, MapProjection p1, DatumType type1, MapProjection p2, DatumType type2, boolean fitProjections)
    {
        List<PolygonLatLon> map1, map2;
        DatumCoordinate     dc1, dc2;               // Map datum of the center
        DatumCoordinate     dr1, dr2;               // Map datum of the reference point
        LatLonCoordinate    center1, center2;       // Center lat lon
        LatLonCoordinate    reference1, reference2; // Reference lat lon
        LatLonCoordinate    tl1, tl2, br1, br2;     // top-left and bottom-right lat lon
        
        if (type1==DatumType.DATUMTYPE_RD)
        {
            map1        =this.polygonsRD;
            center1     =OLV_RD;
            reference1  =MARTINI_RD;
            tl1         =TOPLEFT_RD;
            br1         =BOTTOMRIGHT_RD;
        }
        else
        {
            map1        =this.polygonsWGS84;
            center1     =OLV_WGS84;
            reference1  =MARTINI_WGS84;
            tl1         =TOPLEFT_WGS84;
            br1         =BOTTOMRIGHT_WGS84;
        }
        
        if (type2==DatumType.DATUMTYPE_RD)
        {
            map2        =this.polygonsRD;
            center2     =OLV_RD;
            reference2  =MARTINI_RD;
            tl2         =TOPLEFT_RD;
            br2         =BOTTOMRIGHT_RD;
        }
        else
        {
            map2        =this.polygonsWGS84;
            center2     =OLV_WGS84;
            reference2  =MARTINI_WGS84;
            tl2         =TOPLEFT_WGS84;
            br2         =BOTTOMRIGHT_WGS84;
        }
        
        calibrate(p1, type1);
        
        System.out.println(p1.getClass().toString());
        g.clearRect(topLeftX, topLeftY, bottomRightX-topLeftX, bottomRightX-topLeftX);
        g.setColor(Color.red);
        drawMap(g, map1, p1);
        dc1=drawReferencePoint(g, p1, center1, "OLV         ");
        dr1=drawReferencePoint(g, p1, reference1, "MARTINI     ");
        drawReferencePoint(g, p1, tl1, "Top left    ");
        drawReferencePoint(g, p1, br1, "Bottom Right");
        drawReferencePoint(g, p1, new LatLonCoordinate(tl1.phi, br1.lambda), "Top Right   ");
        drawReferencePoint(g, p1, new LatLonCoordinate(br1.phi, tl1.lambda), "Bottom Left ");

        if (fitProjections)
        {
            calibrate(p2, type2);
        }
        
        System.out.println(p2.getClass().toString());
        g.setColor(Color.blue);
        drawMap(g, map2, p2);
        dc2=drawReferencePoint(g, p2, center2, "OLV         ");
        dr2=drawReferencePoint(g, p2, reference2, "MARTINI     ");
        drawReferencePoint(g, p2, tl2, "Top left    ");
        drawReferencePoint(g, p2, br2, "Bottom Right");
        drawReferencePoint(g, p2, new LatLonCoordinate(tl2.phi, br2.lambda), "Top Right   ");
        drawReferencePoint(g, p2, new LatLonCoordinate(br2.phi, tl2.lambda), "Bottom Left ");
        
        System.out.println("Distance OLV-Martini Projection 1:");
        report(dc1, dr1);
        System.out.println("Distance OLV-Martini Projection 2:");
        report(dc2, dr2);
        System.out.println("Distance OLV between Projection 1 and Projection 2:");
        report(dc1, dc2);
        System.out.println("Distance MARTINI between Projection 1 and Projection 2:");
        report(dr1, dr2);
    }
            
    
    
    /**
     * Comparison between mercator and Web mercator
     * @param g Graphics to use
     */
    private void mercatorVsWebMercator(Graphics g, boolean fit)
    {
        MapProjection   projection1, projection2;
        
        System.out.println("Mercator vs Web Mercator");
        projection1           =new MercatorProjection(5.3872035);
        projection2           =new WebMercatorProjection(5.3872035);
        compare(g, projection1, DatumType.DATUMTYPE_WGS84, projection2, DatumType.DATUMTYPE_WGS84, fit);
    }

    /**
     * Comparison between mercator and Web mercator
     * @param g Graphics to use
     */
    private void mercatorVsTransverseMercator(Graphics g)
    {
        MapProjection   projection1, projection2;
        
        System.out.println("Mercator vs Transverse Mercator - fit");
        projection1           =new MercatorProjection(5.3872035);
        projection2           =TransverseMercatorProjection.OZI_WGS84_TM;
        compare(g, projection1, DatumType.DATUMTYPE_WGS84, projection2, DatumType.DATUMTYPE_WGS84, true);
    }

    /**
     * Comparison between Stereographic and Transverse Mercator.
     * The calculation radius has been chosen as fit parameter
     * @param g Graphics to use
     */
    private void stereographicWGS84VsTransverseMercatorWGS84(Graphics g)
    {
        MapProjection   projection1, projection2;

        System.out.println("Stereographic WGS84 vs Transverse Mercator WGS84");
        projection1           =new StereographicProjection(Ellipsoid.ELLIPSOID_WGS84, 
                                        0.9999079, 
                                        52.1551722, 5.3872035, 
                                        6383300, 
                                        0, 0, 
                                        -0.113);
        projection2           =new TransverseMercatorProjection(Ellipsoid.ELLIPSOID_WGS84, 
                                        0.9999079,
                                        52.1551722, 5.3872035, 
                                        0, 0);      
        compare(g, projection1, DatumType.DATUMTYPE_WGS84, projection2, DatumType.DATUMTYPE_WGS84, false);

    }

    /**
     * Compare Rijksdriehoeksmeting to Transverse Mercator
     * @param g 
     */
    private void stereographicRDVsTransverseMercatorRD(Graphics g)
    {
        MapProjection   projection1, projection2;

        System.out.println("Stereographic RD vs Transverse Mercator RD");
        projection1          =StereographicProjection.RIJKSDRIEHOEKSMETING;
        projection2          =new TransverseMercatorProjection(Ellipsoid.ELLIPSOID_BESSEL1841, 
                                                               0.9999079  ,
                                                               OLV_RD.phi, OLV_RD.lambda, 
                                                               463000.0, 155000.0);
        compare(g, projection1, DatumType.DATUMTYPE_RD, projection2, DatumType.DATUMTYPE_RD, false);
    }

    /**
     * Compare Rijksdriehoeksmeting to Transverse Mercator
     * @param g 
     */
    private void stereographicRDVsTransverseMercatorWGS84(Graphics g)
    {
        MapProjection   projection1, projection2;

        System.out.println("Stereographic RD vs Transverse Mercator WGS84");
        projection1          =StereographicProjection.RIJKSDRIEHOEKSMETING;
        projection2          =TransverseMercatorProjection.OZI_WGS84_TM;
        compare(g, projection1, DatumType.DATUMTYPE_RD, projection2, DatumType.DATUMTYPE_WGS84, false);
    }
    
    
    /**
     * The paint() method responsible for painting the canvas
     * @param g Graphics of the canvas
     */
    @Override
    public void paint(Graphics g)
    {
        super.paint(g);
        g=jPanelMap.getGraphics();
        switch (test)
        {
            case TEST1:
                mercatorVsWebMercator(g, false);
                break;
            case TEST2:
                stereographicWGS84VsTransverseMercatorWGS84(g);
                break;
            case TEST3:
                stereographicRDVsTransverseMercatorWGS84(g);
                break;
            case TEST4:
                stereographicRDVsTransverseMercatorRD(g);
                break;
            case TEST5:
                mercatorVsWebMercator(g, true);
                break;
            case TEST6:
                mercatorVsTransverseMercator(g);
                break;
        }
    }

    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents()
    {

        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jCheckBoxMenuItem1 = new javax.swing.JCheckBoxMenuItem();
        jPanelMap = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuShow = new javax.swing.JMenu();
        jMenuItemCompare1 = new javax.swing.JMenuItem();
        jMenuItemCompare5 = new javax.swing.JMenuItem();
        jMenuItemCompare2 = new javax.swing.JMenuItem();
        jMenuItemCompare3 = new javax.swing.JMenuItem();
        jMenuItemCompare4 = new javax.swing.JMenuItem();
        jMenuItemCompare6 = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        jCheckBoxMenuItemXYScale = new javax.swing.JCheckBoxMenuItem();

        jMenuItem1.setText("jMenuItem1");

        jMenuItem2.setText("jMenuItem2");

        jCheckBoxMenuItem1.setSelected(true);
        jCheckBoxMenuItem1.setText("jCheckBoxMenuItem1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanelMapLayout = new javax.swing.GroupLayout(jPanelMap);
        jPanelMap.setLayout(jPanelMapLayout);
        jPanelMapLayout.setHorizontalGroup(
            jPanelMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 665, Short.MAX_VALUE)
        );
        jPanelMapLayout.setVerticalGroup(
            jPanelMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 779, Short.MAX_VALUE)
        );

        jMenu1.setText("File");

        jMenuItemExit.setText("Exit");
        jMenuItemExit.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemExitActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItemExit);

        jMenuBar1.add(jMenu1);

        jMenuShow.setText("Show");

        jMenuItemCompare1.setText("Mercator vs Web Mercator");
        jMenuItemCompare1.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemCompare1ActionPerformed(evt);
            }
        });
        jMenuShow.add(jMenuItemCompare1);

        jMenuItemCompare5.setText("Mercator vs Web Mercator - Fit");
        jMenuItemCompare5.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemCompare5ActionPerformed(evt);
            }
        });
        jMenuShow.add(jMenuItemCompare5);

        jMenuItemCompare2.setText("Stereographic WGS84 vs Transverse Mercator WGS84");
        jMenuItemCompare2.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemCompare2ActionPerformed(evt);
            }
        });
        jMenuShow.add(jMenuItemCompare2);

        jMenuItemCompare3.setText("Stereographic RD vs Transverse Mercator WGS84");
        jMenuItemCompare3.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemCompare3ActionPerformed(evt);
            }
        });
        jMenuShow.add(jMenuItemCompare3);

        jMenuItemCompare4.setText("Stereographic RD vs Transverse Mercator RD");
        jMenuItemCompare4.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemCompare4ActionPerformed(evt);
            }
        });
        jMenuShow.add(jMenuItemCompare4);

        jMenuItemCompare6.setText("Mercator WGS84 vs Transverse Mercator WGS84 - Fit");
        jMenuItemCompare6.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jMenuItemCompare6ActionPerformed(evt);
            }
        });
        jMenuShow.add(jMenuItemCompare6);

        jMenuBar1.add(jMenuShow);

        jMenu2.setText("Options");

        jCheckBoxMenuItemXYScale.setSelected(true);
        jCheckBoxMenuItemXYScale.setText("Equal x and y scale");
        jCheckBoxMenuItemXYScale.addActionListener(new java.awt.event.ActionListener()
        {
            public void actionPerformed(java.awt.event.ActionEvent evt)
            {
                jCheckBoxMenuItemXYScaleActionPerformed(evt);
            }
        });
        jMenu2.add(jCheckBoxMenuItemXYScale);

        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanelMap, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jMenuItemCompare1ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCompare1ActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCompare1ActionPerformed
        test=TestType.TEST1;
        this.repaint();
    }//GEN-LAST:event_jMenuItemCompare1ActionPerformed

    private void jMenuItemExitActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemExitActionPerformed
    {//GEN-HEADEREND:event_jMenuItemExitActionPerformed
        System.exit(0);
    }//GEN-LAST:event_jMenuItemExitActionPerformed

    private void jMenuItemCompare2ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCompare2ActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCompare2ActionPerformed
        test=TestType.TEST2;
        this.repaint();
    }//GEN-LAST:event_jMenuItemCompare2ActionPerformed

    private void jMenuItemCompare3ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCompare3ActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCompare3ActionPerformed
        test=TestType.TEST3;
        this.repaint();
    }//GEN-LAST:event_jMenuItemCompare3ActionPerformed

    private void jMenuItemCompare4ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCompare4ActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCompare4ActionPerformed
        test=TestType.TEST4;
        this.repaint();
    }//GEN-LAST:event_jMenuItemCompare4ActionPerformed

    private void jMenuItemCompare5ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCompare5ActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCompare5ActionPerformed
        test=TestType.TEST5;
        this.repaint();
    }//GEN-LAST:event_jMenuItemCompare5ActionPerformed

    private void jMenuItemCompare6ActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jMenuItemCompare6ActionPerformed
    {//GEN-HEADEREND:event_jMenuItemCompare6ActionPerformed
        test=TestType.TEST6;
        this.repaint();
    }//GEN-LAST:event_jMenuItemCompare6ActionPerformed

    private void jCheckBoxMenuItemXYScaleActionPerformed(java.awt.event.ActionEvent evt)//GEN-FIRST:event_jCheckBoxMenuItemXYScaleActionPerformed
    {//GEN-HEADEREND:event_jCheckBoxMenuItemXYScaleActionPerformed
        this.repaint();
    }//GEN-LAST:event_jCheckBoxMenuItemXYScaleActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItem1;
    private javax.swing.JCheckBoxMenuItem jCheckBoxMenuItemXYScale;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItemCompare1;
    private javax.swing.JMenuItem jMenuItemCompare2;
    private javax.swing.JMenuItem jMenuItemCompare3;
    private javax.swing.JMenuItem jMenuItemCompare4;
    private javax.swing.JMenuItem jMenuItemCompare5;
    private javax.swing.JMenuItem jMenuItemCompare6;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenu jMenuShow;
    private javax.swing.JPanel jPanelMap;
    // End of variables declaration//GEN-END:variables
}
