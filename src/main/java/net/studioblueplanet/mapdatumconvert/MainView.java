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
import java.io.FileReader;
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
    
    public enum TestType {TEST1, TEST2, TEST3, TEST4};
    
    
    private static final    String              USER_AGENT      = "Mozilla/5.0";

    private static final    String              POST_PARAMS     = "userName=JohnDoe";   
    private static final    String              GEBIEDSDEEL_URL ="https://service.pdok.nl/cbs/gebiedsindelingen/2022/wfs/v1_0?"+
                                                                 "request=GetFeature&service=WFS&typenames=landsdeel_gegeneraliseerd&version=2.0.0&"+
                                                                 "outputformat=json&srsname=urn:ogc:def:crs:EPSG::4326";
    private static final    boolean             READ_FROM_PDOK  =true;
    
    // Martinitoren
    private static final    LatLonCoordinate    MARTINI_WGS84   =new LatLonCoordinate(53.2193683, 6.56827);
    private static final    LatLonCoordinate    MARTINI_RD      =new LatLonCoordinate(53.2204823, 6.5688795);

    // OLV Amersfoort
    private static final    LatLonCoordinate    OLV_WGS84       =new LatLonCoordinate(52.1551722, 5.3872035);    
    private static final    LatLonCoordinate    OLV_RD          =new LatLonCoordinate(52.156160556, 5.387638889);
    
    private final           LatLonCoordinate    topLeftLL       =new LatLonCoordinate(53.80, 3.10);
    private final           LatLonCoordinate    bottomRightLL   =new LatLonCoordinate(50.70, 7.30);
    private                 DatumCoordinate     topLeft;
    private                 DatumCoordinate     bottomRight;
    private                 int                 topLeftX;
    private                 int                 topLeftY;
    private                 int                 bottomRightX;
    private                 int                 bottomRightY;
    private                 double              meterPerPixel;
    
    private final           List<PolygonLatLon> polygonsWGS84   =new ArrayList<>();    
    private final           List<PolygonLatLon> polygonsRD      =new ArrayList<>();    
    
    private                 TestType            test            =TestType.TEST1;
   
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
                x1  =topLeftX+(d.easting-topLeft.easting)/meterPerPixel;
                y1  =topLeftY+(topLeft.northing-d.northing)/meterPerPixel;
                x2  =topLeftX+(prevD.easting-topLeft.easting)/meterPerPixel;
                y2  =topLeftY+(topLeft.northing-prevD.northing)/meterPerPixel;
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
        x=topLeftX+(dc.easting -topLeft.easting )/meterPerPixel;
        y=topLeftY+(topLeft.northing-dc.northing)/meterPerPixel;
        g.fillOval((int)x, (int)y, 10, 10);    

        System.out.println(String.format("%s E/N: %8.1f/%8.1f", name, dc.easting, dc.northing));
        return dc;
    }
    
    /**
     * This method calibrates the map canvas parameters. It uses the top left
     * coordinate and bottom right coordinate and fit them to the canvas.
     * @param projection Projection on which the calibration must take place
     */
    private void calibrate(MapProjection projection)
    {
        Dimension d;
        double          dxPixel;
        double          dyPixel;
        
        d           =this.jPanelMap.getSize();
        topLeftX    =0;
        topLeftY    =0;
        bottomRightX=d.width;
        bottomRightY=d.height;        
        topLeft     =projection.latLonToMapDatum(topLeftLL);
        bottomRight =projection.latLonToMapDatum(bottomRightLL);
        dxPixel=(bottomRight.easting-topLeft.easting     )/(bottomRightX-topLeftX);
        dyPixel=(topLeft.northing   -bottomRight.northing)/(bottomRightY-topLeftY);
        meterPerPixel=Math.max(dxPixel, dyPixel);
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
     * Execute a comparison
     * @param g
     * @param p1
     * @param map1
     * @param center1
     * @param referencePoint1
     * @param p2
     * @param map2
     * @param center2
     * @param referencePoint2 
     */
    private void compare(Graphics g, 
                         MapProjection p1, List<PolygonLatLon> map1, LatLonCoordinate center1, LatLonCoordinate referencePoint1, 
                         MapProjection p2, List<PolygonLatLon> map2, LatLonCoordinate center2, LatLonCoordinate referencePoint2)
    {
        DatumCoordinate dc1, dc2;
        
        calibrate(p1);
        
        System.out.println(p1.getClass().toString());
        g.clearRect(topLeftX, topLeftY, bottomRightX-topLeftX, bottomRightX-topLeftX);
        g.setColor(Color.red);
        drawMap(g, map1, p1);
        drawReferencePoint(g, p1, center1, "OLV     ");
        dc1=drawReferencePoint(g, p1, referencePoint1, "MARTINI ");

        System.out.println(p2.getClass().toString());
        g.setColor(Color.blue);
        drawMap(g, map2, p2);
        drawReferencePoint(g, p2, center2, "OLV     ");
        dc2=drawReferencePoint(g, p2, referencePoint2, "MARTINI ");
        
        report(dc1, dc2);        
    }
            
    
    
    /**
     * Comparison between mercator and Web mercator
     * @param g Graphics to use
     */
    private void mercatorVsWebMercator(Graphics g)
    {
        MapProjection   projection1, projection2;
        
        System.out.println("Mercator vs Web Mercator");
        projection1           =new MercatorProjection(5.3872035);
        projection2           =new WebMercatorProjection(5.3872035);
        compare(g, projection1, polygonsWGS84, OLV_WGS84, MARTINI_WGS84,
                   projection2, polygonsWGS84, OLV_WGS84, MARTINI_WGS84);
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
        compare(g, projection1, polygonsWGS84, OLV_WGS84, MARTINI_WGS84, 
                   projection2, polygonsWGS84, OLV_WGS84, MARTINI_WGS84);

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
        compare(g, projection1, polygonsRD, OLV_RD, MARTINI_RD, 
                   projection2, polygonsRD, OLV_RD, MARTINI_RD);
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
        compare(g, projection1, polygonsRD, OLV_RD, MARTINI_RD, 
                   projection2, polygonsWGS84, OLV_WGS84, MARTINI_WGS84);
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
                mercatorVsWebMercator(g);
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

        jPanelMap = new javax.swing.JPanel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItemExit = new javax.swing.JMenuItem();
        jMenuShow = new javax.swing.JMenu();
        jMenuItemCompare1 = new javax.swing.JMenuItem();
        jMenuItemCompare2 = new javax.swing.JMenuItem();
        jMenuItemCompare3 = new javax.swing.JMenuItem();
        jMenuItemCompare4 = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout jPanelMapLayout = new javax.swing.GroupLayout(jPanelMap);
        jPanelMap.setLayout(jPanelMapLayout);
        jPanelMapLayout.setHorizontalGroup(
            jPanelMapLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 800, Short.MAX_VALUE)
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

        jMenuBar1.add(jMenuShow);

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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItemCompare1;
    private javax.swing.JMenuItem jMenuItemCompare2;
    private javax.swing.JMenuItem jMenuItemCompare3;
    private javax.swing.JMenuItem jMenuItemCompare4;
    private javax.swing.JMenuItem jMenuItemExit;
    private javax.swing.JMenu jMenuShow;
    private javax.swing.JPanel jPanelMap;
    // End of variables declaration//GEN-END:variables
}
