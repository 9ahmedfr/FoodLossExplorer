package helper;

public class SimilarFoodResult {
  public String groupName;
  public double lossValue;
  public double similarity;

  public SimilarFoodResult() {
  }

  public SimilarFoodResult(String groupName, double lossValue, double similarity) {
    this.groupName = groupName;
    this.lossValue = lossValue;
    this.similarity = similarity;
  }

  public String getGroupName() {
    return groupName;
  }

  public String getLossValue() {
    return String.format("%.2f", lossValue);
  }

  public String getSimilarity() {
    return String.format("%.2f", similarity);
  }

  public String toString() {
    String output;

    output = "\nGroup Name: " + groupName + "\n";
      output += "Loss Value: " + lossValue + "\n";
      output += "Similarity Score: " + similarity + "\n";

      return output;
  }
}
