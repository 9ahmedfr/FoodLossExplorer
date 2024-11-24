package app;

/**
 * Class represeting a Region from the Studio Project database
 *
 * @author Halil Ali, 2024. email: halil.ali@rmit.edu.au
 */

public class Region {

   private int m49Code;
   private String name;
   private double AverageLoss;
   private double foodScore;
   private double lossScore;
   private double overallScore;
   private int commonFoodProducts;
   private int uniqueFoodProducts;
   private int foodProductCount;
   private String countryName;

   /**
    * Create a Region and set the fields
    */
   public Region(String name) {
      this.name = name;
   }

   public Region(int m49Code, String name) {
      this.m49Code = m49Code;
      this.name = name;
   }

   public Region(String name, double foodScore) {
      this.name = name;
      this.foodScore = foodScore;
   }

   public void setLossScore(double lossScore) {
      this.lossScore = lossScore;
   }

   public double getLossScore() {
      return lossScore;
   }

   public void setOverallScore(double overallScore) {
      this.overallScore = overallScore;
   }

   public double getOverallScore() {
      return overallScore;
   }

   public double getFoodScore() {
      return foodScore;
   }

   public int getM49Code() {
      return m49Code;
   }

   public String getName() {
      return name;
   }

   public void setFoodScore(double foodScore) {
      this.foodScore = foodScore;
   }

   public void setName(String name) {
      this.name = name;
   }

   public void setM49Code(int m49Code) {
      this.m49Code = m49Code;
   }

   public void setAverageLoss(double AverageLoss) {
      this.AverageLoss = AverageLoss;
   }

   public double getAverageLoss() {
      return AverageLoss;
   }

   public void setNumberOfCommonProducts(int commonFoodProducts) {
      this.commonFoodProducts = commonFoodProducts;
   }

   public int getNumberOfCommonProducts() {
      return commonFoodProducts;
   }

   public void setNumberOfUniqueProducts(int uniqueFoodProducts) {
      this.uniqueFoodProducts = uniqueFoodProducts;
   }

   public int getNumberOfUniqueProducts() {
      return uniqueFoodProducts;
   }

   public void setFoodProductCount(int foodProductCount) {
      this.foodProductCount = foodProductCount;
   }

   public int getFoodProductCount() {
      return foodProductCount;
   }

   public void setCountryName(String countryName) {
      this.countryName = countryName;
   }

   public String getCountryName() {
      return countryName;
   }

}
