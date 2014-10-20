package net.di2e.ecdr.search.transform.atom.response;

public abstract class AtomTest {

    protected static final String LOCATION_MARKER = "$LOCATION";

    // test files
    protected static final String ATOM_TEMPLATE_FILE = "/atom-template.xml";

    // WKT shapes
    protected static final String POINT_WKT = "POINT (-77.0366 38.8977)";
    protected static final String BOX_WKT = "POLYGON ((-71.032 42.943, -69.856 42.943, -69.856 43.039, -71.032 43.039, -71.032 42.943))";
    protected static final String LINE_WKT = "LINESTRING (-110.45 45.256, -109.48 46.46, -109.86 43.84)";
    protected static final String POLYGON_WKT = "POLYGON ((-110.45 45.256, -109.48 46.46, -109.86 43.84, -110.45 45.256))";

    // XML shapes
    protected static final String SIMPLE_BOX = "<georss:box xmlns:georss=\"http://www.georss.org/georss\">42.943 -71.032 43.039 -69.856</georss:box>";
    protected static final String SIMPLE_POINT = "<georss:point xmlns:georss=\"http://www.georss.org/georss\">38.8977 -77.0366</georss:point>";
    protected static final String SIMPLE_POLYGON = "<georss:polygon xmlns:georss=\"http://www.georss.org/georss\">45.256 -110.45 46.46 -109.48 43.84 -109.86 45.256 -110.45</georss:polygon>";
    protected static final String SIMPLE_LINE = "<georss:line xmlns:georss=\"http://www.georss.org/georss\">45.256 -110.45 46.46 -109.48 43.84 -109.86</georss:line>";

    protected static final String GML_BOX = "<georss:where xmlns:georss=\"http://www.georss.org/georss\" xmlns:gml=\"http://www.opengis.net/gml\"><gml:Envelope><gml:lowerCorner>42.943 -71.032</gml:lowerCorner><gml:upperCorner>43.039 -69.856</gml:upperCorner></gml:Envelope></georss:where>";
    protected static final String GML_POINT = "<georss:where xmlns:georss=\"http://www.georss.org/georss\" xmlns:gml=\"http://www.opengis.net/gml\"><gml:Point><gml:pos>38.8977 -77.0366</gml:pos></gml:Point></georss:where>";
    protected static final String GML_LINE = "<georss:where xmlns:georss=\"http://www.georss.org/georss\" xmlns:gml=\"http://www.opengis.net/gml\"><gml:LineString><gml:posList>45.256 -110.45 46.46 -109.48 43.84 -109.86</gml:posList></gml:LineString></georss:where>";
    protected static final String GML_POLYGON = "<georss:where xmlns:georss=\"http://www.georss.org/georss\" xmlns:gml=\"http://www.opengis.net/gml\"><gml:Polygon><gml:exterior><gml:LinearRing><gml:posList>45.256 -110.45 46.46 -109.48 43.84 -109.86 45.256 -110.45</gml:posList></gml:LinearRing></gml:exterior></gml:Polygon></georss:where>";


    protected static final String SITE_NAME = "ddf.distribution";
}
