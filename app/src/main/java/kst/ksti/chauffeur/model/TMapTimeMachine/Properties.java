package kst.ksti.chauffeur.model.TMapTimeMachine;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Properties implements Serializable {

    @SerializedName("totalDistance")
    @Expose
    private Integer totalDistance;

    @SerializedName("totalTime")
    @Expose
    private Integer totalTime;

    @SerializedName("taxiFare")
    @Expose
    private Integer taxiFare;

    @SerializedName("departureTime")
    @Expose
    private String departureTime;

    @SerializedName("arrivalTime")
    @Expose
    private String arrivalTime;

    public Integer getTotalDistance() {
        return totalDistance;
    }

    public void setTotalDistance(Integer totalDistance) {
        this.totalDistance = totalDistance;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
    }

    public Integer getTaxiFare() {
        return taxiFare;
    }

    public void setTaxiFare(Integer taxiFare) {
        this.taxiFare = taxiFare;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }
}
