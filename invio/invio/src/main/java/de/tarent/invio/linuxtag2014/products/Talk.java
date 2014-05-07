package de.tarent.invio.linuxtag2014.products;

import java.util.Calendar;

/**
 * Talk represents a single talk on the Linuxtag conference.
 */
public class Talk {

    private final Calendar startTime;

    private final Calendar endTime;

    private final String name;

    private final String link;


    public Talk(final Calendar startTime, final Calendar endTime, final String name, final String link) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.name = name;
        this.link = link;
    }

    public Calendar getStartTime() {
        return startTime;
    }

    public Calendar getEndTime() {
        return endTime;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Talk talk = (Talk) o;

        if (!endTime.equals(talk.endTime)) {
            return false;
        }
        if (!link.equals(talk.link)) {
            return false;
        }
        if (!name.equals(talk.name)) {
            return false;
        }
        if (!startTime.equals(talk.startTime)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = startTime.hashCode();
        result = 31 * result + endTime.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + link.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Talk{" +
                "startTime=" + startTime.getTime() +
                ", endTime=" + endTime.getTime() +
                ", name='" + name + '\'' +
                ", link='" + link + '\'' +
                '}';
    }
}
