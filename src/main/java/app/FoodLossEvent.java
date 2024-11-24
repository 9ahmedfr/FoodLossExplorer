package app;

public class FoodLossEvent {
    private String commodityName;
    private int year;
    private double lossPercentage;
    private String countryName;
    private String groupName;
    private String cause;
    private String activity;
    private String stage;

    public FoodLossEvent(String commodityName, String activity, String cause, String stage, double lossPercentage) {
        this.commodityName = commodityName;
        this.activity = activity;
        this.cause = cause;
        this.stage = stage;
        this.lossPercentage = lossPercentage;
    }

    public FoodLossEvent(String commodityName, double lossPercentage) {
        this.commodityName = commodityName;
        this.lossPercentage = lossPercentage;
    }


    public FoodLossEvent(String commodityName, int year, double lossPercentage) {
        this.commodityName = commodityName;
        this.year = year;
        this.lossPercentage = lossPercentage;
    }

    public FoodLossEvent(int year, double lossPercentage) {
        this.year = year;
        this.lossPercentage = lossPercentage;
    }

    // Getter and setter methods for each attribute
    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }

    public String getGroupName() {
      return groupName;
    }

    public void setGroupName(String groupName) {
      this.groupName = groupName;
    }

    public String getCommodityName() {
        return commodityName;
    }

    public void setCommodityName(String commodityName) {
        this.commodityName = commodityName;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public double getLossPercentage() {
        return lossPercentage;
    }

    public void setLossPercentage(double lossPercentage) {
        this.lossPercentage = lossPercentage;
    }

    public String getCause() {
        return cause;
    }

    public void setCause(String cause) {
        this.cause = cause;
    }

    public String getActivity() {
        return activity;
    }

    public void setActivity(String activity) {
        this.activity = activity;
    }

    public String getStage() {
        return stage;
    }

    public void setStage(String stage) {
        this.stage = stage;
    }

    public String toString() {
      String output;

      output = "\nCommodity Name: " + commodityName + "\n";
      output += "Activity: " + activity + "\n";
      output += "Stage: " + stage + "\n";
      output += "Cause: " + cause + "\n";
      output += "Loss Percentage: " + lossPercentage + "\n";

      return output;
    }
}
