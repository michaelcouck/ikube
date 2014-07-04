package ikube.model;

import javax.persistence.*;

/**
 * Custom search transfer object for the Twitter application.
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@EntityListeners(value = {SearchIncrementListener.class})
@SuppressWarnings("UnusedDeclaration")
public class SearchTwitter extends Search {

    @Transient
    int clusters;
    @Transient
    long startHour;
    @Transient
    long minutesOfHistory;
    @Transient
    String classification;
    @Transient
    Object[][] heatMapData;
    @Transient
    Object[][] timeLineSentiment;

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
