package com.googlecode.sardine.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.http.HttpResponse;
import org.apache.http.entity.StringEntity;
import org.w3c.dom.Element;

import com.googlecode.sardine.model.Multistatus;
import com.googlecode.sardine.model.ObjectFactory;

/**
 * Basic utility code. I borrowed some code from the webdavlib for parsing dates.
 * 
 * @author jonstevens
 */
public class SardineUtil {

    /** */
    final static JAXBContext CONTEXT;

    /** cached version of getResources() webdav xml GET request */
    private final static StringEntity GET_RESOURCES;

    static {
        try {
            GET_RESOURCES = new StringEntity(//
                    "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n" + //
                            "<propfind xmlns=\"DAV:\">\n" + //
                            "   <allprop/>\n" + //
                            "</propfind>", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not find encoding, JVM broken?", e);
        }
        try {
            CONTEXT = JAXBContext.newInstance(ObjectFactory.class);
        } catch (JAXBException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * Date formats used for Date parsing.
     */
    static final SimpleDateFormat formats[] = { new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US),
            new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss'Z'", Locale.US),
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US),
            new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US),
            new SimpleDateFormat("EEEEEE, dd-MMM-yy HH:mm:ss zzz", Locale.US),
            new SimpleDateFormat("EEE MMMM d HH:mm:ss yyyy", Locale.US) };

    /**
     * GMT timezone.
     */
    final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");

    static {
        for (SimpleDateFormat format : formats) {
            format.setTimeZone(gmtZone);
        }
    }

    /**
     * Hides the irritating declared exception.
     */
    public static String encode(String value) {
        try {
            return URLEncoder.encode(value, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * Hides the irritating declared exception.
     * 
     * @return null if there is an IllegalArgumentException
     * @throws RuntimeException
     *             if there is an UnsupportedEncodingException
     */
    public static String decode(String value) {
        try {
            return URLDecoder.decode(value, "utf-8");
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }

    /**
     * Loops over all the possible date formats and tries to find the right one.
     * 
     * @param dateValue
     */
    public static Date parseDate(String dateValue) {
        if (dateValue == null)
            return null;

        Date date = null;
        for (final SimpleDateFormat format : formats) {
            try {
                date = ((SimpleDateFormat) format.clone()).parse(dateValue);
                break;
            } catch (ParseException e) {
                // We loop through this until we found a valid one.
                continue;
            }
        }

        return date;
    }

    /**
     * Is the status code 2xx
     */
    public static boolean isGoodResponse(int statusCode) {
        return ((statusCode >= 200) && (statusCode <= 299));
    }

    /**
     * Stupid wrapper cause it needs to be in a try/catch
     */
    public static StringEntity getResourcesEntity() {
        return GET_RESOURCES;
    }

    /**
     * Build PROPPATCH entity.
     */
    public static StringEntity getResourcePatchEntity(Map<String, String> setProps, List<String> removeProps) {
        try {
            final StringBuilder sb = new StringBuilder("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
            sb.append("<D:propertyupdate xmlns:D=\"DAV:\" xmlns:S=\"SAR:\">\n");

            if (setProps != null) {
                sb.append("<D:set>\n");
                sb.append("<D:prop>\n");
                for (Map.Entry<String, String> prop : setProps.entrySet()) {
                    sb.append("<S:");
                    sb.append(prop.getKey()).append(">");
                    sb.append(prop.getValue()).append("</S:");
                    sb.append(prop.getKey()).append(">\n");
                }
                sb.append("</D:prop>\n");
                sb.append("</D:set>\n");
            }

            if (removeProps != null) {
                sb.append("<D:remove>\n");
                sb.append("<D:prop>\n");
                for (String removeProp : removeProps) {
                    sb.append("<S:");
                    sb.append(removeProp).append("/>");
                }
                sb.append("</D:prop>\n");
                sb.append("</D:remove>\n");
            }

            sb.append("</D:propertyupdate>\n");

            return new StringEntity(sb.toString(), "UTF-8");

        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Could not find encoding, JVM broken?", e);
        }
    }

    /**
     * Helper method for getting the Multistatus response processor.
     */
    public static Multistatus getMultistatus(Unmarshaller unmarshaller, HttpResponse response, String url)
            throws SardineException {
        try {
            return (Multistatus) unmarshaller.unmarshal(response.getEntity().getContent());
        } catch (JAXBException ex) {
            throw new SardineException("Problem unmarshalling the data", url, ex);
        } catch (IOException ex) {
            throw new SardineException(ex);
        }
    }

    /**
     * Creates a simple Map from the given custom properties of a response. This implementation does not take into
     * account name spaces.
     * 
     * @param elements
     *            custom properties.
     * @return a map from the custom properties.
     */
    public static Map<String, String> extractCustomProps(List<Element> elements) {
        final Map<String, String> customPropsMap = new HashMap<String, String>(elements.size());

        for (final Element element : elements) {
            final String[] keys = element.getTagName().split(":", 2);
            final String key = (keys.length > 1) ? keys[1] : keys[0];

            customPropsMap.put(key, element.getTextContent());
        }

        return customPropsMap;
    }

    /**
     * Creates an {@link Unmarshaller} from the {@link SardineUtil#CONTEXT}.
     * 
     * @return a new Unmarshaller.
     * @throws JAXBException
     */
    public static Unmarshaller createUnmarshaller() {
        try {
            return CONTEXT.createUnmarshaller();
        } catch (JAXBException e) {
            throw new RuntimeException("Could not create unmarshaller", e);
        }
    }

}
