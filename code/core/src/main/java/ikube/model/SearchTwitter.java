package ikube.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Transient;

/**
 * Custom search transfer object for the Twitter application.
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@SuppressWarnings("UnusedDeclaration")
public class SearchTwitter extends Search {

    @Transient
    private int clusters;
    @Transient
    private long startHour;
    @Transient
    private long minutesOfHistory;
    @Transient
    private String classification;
    @Transient
    private Object[][] heatMapData;
    @Transient
    private Object[][] timeLineSentiment;

    public Object[][] getTimeLineSentiment() {
        return timeLineSentiment;
    }

    public void setTimeLineSentiment(final Object[][] timeLineSentiment) {
        this.timeLineSentiment = timeLineSentiment;
    }

    public Object[][] getHeatMapData() {
        return heatMapData;
    }

    public void setHeatMapData(final Object[][] heatMapData) {
        this.heatMapData = heatMapData;
    }

    public String getClassification() {
        return classification;
    }

    public void setClassification(final String classification) {
        this.classification = classification;
    }

    public long getMinutesOfHistory() {
        return minutesOfHistory;
    }

    public void setMinutesOfHistory(final long minutesOfHistory) {
        this.minutesOfHistory = minutesOfHistory;
    }

    public long getStartHour() {
        return startHour;
    }

    public void setStartHour(final long startHour) {
        this.startHour = startHour;
    }

    public int getClusters() {
        return clusters;
    }

    public void setClusters(final int clusters) {
        this.clusters = clusters;
    }
}
