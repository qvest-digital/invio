package de.tarent.invio.linuxtag2014.products;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import de.tarent.invio.linuxtag2014.App;
import de.tarent.invio.linuxtag2014.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * A Product(-type, i.e. no quantity (that would be Article) and no location (that would be ProductItem).
 * Note: the name has two parts: "xxx xxx (yyy, yyy...)" The first part (the x) is the "shortName". It must be unique
 * (because it will be used as a map-key).
 * The y-part is called the categories. It will not usually be displayed but is used for searching.
 *
 * Note: while the equality and hashcode consider all fields it is common to only look at the barcode.
 *       I.e. the barcode alone should already be unique.
 */
public class Product {

    private static final String TAG = Product.class.getCanonicalName();

    // Blue icon for the booths.
    public static final Bitmap boothPOI = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.poi_b);
    // Grey icon for the rooms.
    public static final Bitmap roomPOI = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.poi_g);
    // Default icon for products/search
    public static final Bitmap defaultPOI = BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.poi);

    public static final int TYPE_ROOM = 1;

    public static final int TYPE_BOOTH = 2;

    private int price;

    private long barcode;

    private String name;

    private String mapShortName;

    private Bitmap icon;

    private List<Talk> talks = new ArrayList<Talk>();

    private int type = 0;

    /**
     * Constructor.
     *
     * @param barcode the product barcode
     * @param name the product name
     * @param price the product price in euro-cent
     */
    public Product(final long barcode, final String name, final int price) {
        this.barcode = barcode;
        this.price = price;

        if (name.startsWith("room")) {
            type = TYPE_ROOM;
            icon = roomPOI;
            this.name = name.substring(5);
            parseTalks();
        } else if (name.startsWith("booth")) {
            type = TYPE_BOOTH;
            icon = boothPOI;
            this.name = name.substring(6);
        } else {
            icon = defaultPOI;
            this.name = name;
        }
    }


    /**
     * Parse talks of the Linuxtag conference from the product categories string. Categories string within the braces
     * has now talks in it and looks like this:
     * (08.05.2014 12:00-13:00_State of the Union_http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=116,
     * 08.05.2014 18:30-19:00_Systemsicherheit_http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=1142)
     * The separator char for each talk is ",".
     *
     */
    private void parseTalks() {

        //Format of the date string which should be parsed
        final SimpleDateFormat formatDate = new SimpleDateFormat("dd.MM.yyyy HH:mm");

        //Split categories string into single talks strings
        final String[] talksArray = getCategories().split(",");

        //Iterate over each talk string
        for(int i = 0; i < talksArray.length; i++) {

            //Current talk string
            final String talkString = talksArray[i].trim();
            Calendar startTime = null;
            Calendar endTime = null;
            String name = null;
            String link = null;


            //Split the single talk string into time, name and link:
            //08.05.2014 12:00-13:00_State of the Union_http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=116
            //This will be split into
            //Date: 08.05.2014 12:00-13:00
            //Name: State of the Union
            //Link: http://www.linuxtag.org/2014/de/programm/vortragsdetails/?eventid=116
            final String[] talkData = talkString.split("_");
            if(talkData.length == 3) {
                name = talkData[1];
                link = talkData[2];
                String time = talkData[0];

                //Single time spring will be for example: "08.05.2014 10:00-10:30", so we split it into the date
                //string and time strings
                String[] dateAndTimes = time.split(" ");
                if(dateAndTimes.length == 2) {
                    try {
                        //Date string will be for example "08.05.2014"
                        final String dateString = dateAndTimes[0].trim();
                        //Split the single time string "10:00-10:30" into startTime and endTime and add dateString to it
                        //to produce full time strings such as "08.05.2014 10:00"
                        final String[] times = dateAndTimes[1].split("-");
                        if(times.length == 2) {
                            startTime = parseStringToCalendar(dateString + " " + times[0], formatDate);
                            endTime = parseStringToCalendar(dateString + " " + times[1], formatDate);
                        } else {
                            Log.e(TAG, "Bad time string for the talk: " + name);
                        }
                    } catch (ParseException e) {
                        Log.e(TAG, "Could not parse date of the talk: " + name);
                    }


                } else {
                    Log.e(TAG, "Bad time string for the talk: " + name);
                }

                //Create a new talk object depending on parsed data
                final Talk talk = new Talk(startTime, endTime, name, link);
                talks.add(talk);
            } else {
                Log.e(TAG, "Could not parse talk data from this string: " + talkString);
            }
        }
    }




    private Calendar parseStringToCalendar(final String input, final SimpleDateFormat format) throws ParseException {

        Calendar calendar = new GregorianCalendar();
        Date date = format.parse(input);
        calendar.setTime(date);

        return calendar;
    }

    public void setMapShortName(String name) {
        mapShortName = name;
    }

    public List<Talk> getTalks() {
        return talks;
    }

    public int getType() {
        return type;
    }

    @Override //NOSONAR - Not duplicate code. Plenty of equals look similar.
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Product product = (Product) o;

        if (barcode != product.barcode) {
            return false;
        }
        if (price != product.price) {
            return false;
        }
        if (name != null ? !name.equals(product.name) : product.name != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = price;
        result = 31 * result + (int) (barcode ^ (barcode >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    /**
     * The shortName is the name without the categories (the part in braces, at the end). The line-separator '_' is
     * replaced with a space ' '.
     * @return the short name
     */
    public String getShortName() {
        if (name.indexOf('(') > 0 ) {
            return name.substring(0, name.indexOf('(')).replace('_', ' ').trim();
        } else {
            return name.replace('_', ' ').trim();
        }
    }

    /**
     * The first part of the name, before the '_' and also without the categories (the part in braces, at the end)
     * @return the first line of name
     */
    public String getNameLineOne() {
        if ((name.indexOf('(') > 0) && (name.indexOf('_') < 0)) {
            return name.substring(0, name.indexOf('(')).trim();
        } else if (name.indexOf('_') > 0) {
            return name.substring(0, name.indexOf('_')).trim();
        } else {
            return name;
        }
    }

    /**
     * The second part of the name, between the '_' and the '(' (the categories).
     * Note: does not work if there is no category!
     * @return the second line of the name, or "" if no second line exists.
     */
    public String getNameLineTwo() {
        if (name.indexOf('_') > 0) {
            if (name.indexOf('(') > 0) {
                return name.substring(name.indexOf('_') + 1, name.indexOf('(')).trim();
            } else {
                return name.substring(name.indexOf('_') + 1, name.length()).trim();
            }
        } else {
            return "";
        }
    }

    /**
     * The categories are the list in braces at the end of the full product name.
     * @return the list of categories as a simple string, or "" if no categories are specified
     */
    public String getCategories() {
        if (name.indexOf('(') > 0) {
            return name.substring(name.indexOf('(') + 1, name.length() - 1);
        } else {
            return "";
        }
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(final int price) {
        this.price = price;
    }

    public long getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    /**
     * Returns the specific product icon if one exists. If no icon exists, return a standard icon.
     *
     * @return the {@link #icon}
     */
    public Bitmap getIcon() {
        if (icon != null) {
            return this.icon;
        } else {
            return BitmapFactory.decodeResource(App.getContext().getResources(), R.drawable.poi);
        }
    }

}
