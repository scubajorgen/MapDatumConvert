## Map Datum Convert

### Goal
Utility that converts Rijksdriehoeksmeting coorinates to WGS84 and vice versa.
It is meant to illustrate the method.

The method is described in [a document of the Johannes Postgroep](https://www.johannespostgroep.nl/wp-content/uploads/2008/10/rijksdriehoeksstelsel.pdf) and [a document from ncgeo (chapter 6)](https://ncgeo.nl/downloads/43Referentie.pdf).

![](images/method.png)

### Code
The code is presented in the class MapDatumConvert. Main methods are rdToWgs84() and wgs84ToRd().


Refer to the test class TestMapDatumConvert for examples on how to use the code.