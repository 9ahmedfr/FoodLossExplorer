package app;

//import java.lang.reflect.Array;
//import java.lang.reflect.Array;
import java.util.ArrayList;

import io.javalin.http.Context;
import io.javalin.http.Handler;

/*import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;*/

/**
 * Example Index HTML class using Javalin
 * <p>
 * Generate a static HTML page using Javalin
 * by writing the raw HTML into a Java String object
 *
 * @author Timothy Wiley, 2023. email: timothy.wiley@rmit.edu.au
 * @author Santha Sumanasekara, 2021. email: santha.sumanasekara@rmit.edu.au
 * @author Halil Ali, 2024. email: halil.ali@rmit.edu.au
 */

public class PageST3A implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page3A.html";
    int defaultStartYear = JDBCConnection.getLowestYear() ; 
    int defaultEndYear = JDBCConnection.getHighestYear();
    String[] selectedLocations;
    int year = defaultEndYear;
    int numberOfResults;
    String sort = "HL";
    String similarity = "food";
    String similarityType = "absolute";
    Boolean isCountry = null;
    Boolean isRegion = null;
    Boolean NotSelected = false;
    int mostCommonYear = JDBCConnection.getMostCommonYear();

        @Override
    public void handle(Context context) throws Exception {

        /************************************************************* GET THE FORM PARAMETERS *****************************************************************/

        // Get the sort option
        if (context.formParam("sortOption") == null) {sort = "HL";}
        else {sort = context.formParam("sortOption");}

        // Get the selected countries
        selectedLocations = context.formParams("countries[]").toArray(new String[0]);

        // Get the start and end years
        if (context.formParam("year") != null && !context.formParam("year").trim().isEmpty()) {
            try {year = Integer.parseInt(context.formParam("year").trim());
            } catch (NumberFormatException e) {year = mostCommonYear ;}
        } else {year = mostCommonYear;}

        // Number of Results
        if (context.formParam("numberOfResults") != null && !context.formParam("numberOfResults").trim().isEmpty()) {
            try {numberOfResults = Integer.parseInt(context.formParam("numberOfResults").trim());
            } catch (NumberFormatException e) {numberOfResults = 5;}
        } else {numberOfResults = 5;}

        // Get the similarity criteria 
        if (context.formParam("option") == null) {similarity = "food";}
        else {similarity = context.formParam("option");}

        // Get the similarity type
        if (context.formParam("option1") == null) {similarityType = "absolute";}
        else {similarityType = context.formParam("option1");}
        
        /************************************************************* HTML FOR PAGE STARTS HERE *****************************************************************/


        String html = "<html>";

        html += "<head>" + "<title>Similar Country</title>";
        html += "<meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>";
        html += "<link rel=\"icon\" href=\"FLW.png\" type=\"image/x-icon\">";

        // Add some CSS (JAVA-METHOD)
        html = addStyleSheet(html);

        html += "</head><body>";

        html += "<form action='page3A.html' method='post' id='mainForm'>";

        // Add the topnav
        html += CommonDesign.AddTopnav("page3A.html");

        // HTML for content header
        html += """
            <div class= "page-content">
                <section class="content-header"> 
                    <h2 class="content-header_title">Similar Countries</h2>
                </section> 
        """;
            
        /************************************************************* SIDEBAR *****************************************************************/

                html += """
                    <main class="main">
                        <aside class="sidebar"> 
                """;

                html += """
                    <section class="box" style="padding: 0px;">
                        <div class="Filter-heading">
                            <h2>Filters</h2>
                        </div>
                    </section>
                """;
                
                /************************************************ Country Selection ************************************************************/
                
                html +=  """
                    <section class="box">
                            <div class="Countries_box"><h2 class="box_title">Locations</h2>
                                <div class="Refresh" title="Resubmit">
                                    <button class="CountryButton" type="submit">
                                        <i class="fa-solid fa-rotate-right"></i></div>
                                    </button>
                                </div>
                                <div class="container mt-5">
                                    <select class="selectpicker" data-live-search="true" data-live-search-normalize="true" 
                                    id="countrySelect" name="countries[]" data-none-selected-text="Select Countries">
                                    <option value="" disabled selected>Select a Location</option>
                """;

                //Keeping already selected countries selected

                ArrayList<String> CountryNames = JDBCConnection.getAllCountryNames();
                ArrayList<Region> Regions = null;

                String SelectedLocation = "";

                if (!(selectedLocations.length == 0)) {SelectedLocation = selectedLocations[0];}

                for (String country : CountryNames) {
                    String optionHtml = "<option style='font-weight:bold' value='" + escapeSingleQuotesForHtml(country) + "' "
                    + " data-tokens='" + escapeSingleQuotesForHtml(country) + "'";

                    if (SelectedLocation.equals(country)) {
                        optionHtml += " selected";
                    }
                    optionHtml += ">" + country + "</option>";
                    html += optionHtml;

                    Regions = JDBCConnection.getAllRegionNamesForACountry(country);
                    
                    for (Region region : Regions) {
                        optionHtml = "<option value='" + escapeSingleQuotesForHtml(country) + "_" +escapeSingleQuotesForHtml(region.getName()) + "_" + region.getM49Code() + "' "
                        + " data-tokens='" + escapeSingleQuotesForHtml(country) + "_" + escapeSingleQuotesForHtml(region.getName()) + "'";

                        if (SelectedLocation.equals(country + "_" + region.getName() + "_" + region.getM49Code())) {
                            optionHtml += " selected";
                        }
                        optionHtml += ">" + region.getName() + "</option>";
                        html += optionHtml;
                    }

                }
                
                html += """
                            </select>
                        </div>
                    </section> 
                """;

                /************************************************ Filters ************************************************************/

                //Year Range
                html += addYearRangeSection(year);

                //Number of Results
                html += addNumberOfResultsSection(numberOfResults);

                //Similarity Criteria
                html += addSimilarityCriteriaSection(similarity);

                //Similarity Type
                html += addSimilarityTypeSection(similarityType);
            
                /************************************************ Submit Button ************************************************************/

                html += """
                            <section class="box" style="text-align: center; padding: 0px;">
                                <button class="submitbutton" type="submit">Show Results</button>
                            </section>
                    </aside>
                """;

            String locationName = "";
            int RegionM49Code = 0;
            String RegionCountryName = "";
            ArrayList<String> CountriesInYear = new ArrayList<>();
            ArrayList<Region> RegionsInYear = new ArrayList<>();
                
            if (CountryNames.contains(SelectedLocation) && !SelectedLocation.equals("")) { isCountry = true; isRegion = false; NotSelected = false;
                locationName = SelectedLocation;} 

            else if (SelectedLocation.contains("_") && !SelectedLocation.equals("")) { isRegion = true; isCountry = false; NotSelected = false;
                String[] splitSelectedLocation = SelectedLocation.split("_");
                RegionCountryName = splitSelectedLocation[0];
                locationName = splitSelectedLocation[1];
                RegionM49Code = Integer.parseInt(splitSelectedLocation[2]);
            }   

            else { NotSelected = true; isCountry = false; isRegion = false; }

            if (isCountry) { CountriesInYear = JDBCConnection.getCountriesInYear(year, locationName); }
            if (isRegion) { RegionsInYear = JDBCConnection.getRegionsInYear(year, locationName, RegionM49Code); }

            /********************************************************* MAIN CONTENT STARTS HERE **********************************************************/
            
            String display = "";
            String pluralorNot = "";

            if (isRegion && RegionsInYear.size() == 1) {pluralorNot = " Region";}
            else if (isRegion && (RegionsInYear.size() > 1 || RegionsInYear.size() == 0)) {pluralorNot = " Regions";}
            else if (isCountry && CountriesInYear.size() == 1) {pluralorNot = " Country";}
            else if (isCountry && (CountriesInYear.size() > 1 || CountriesInYear.size() == 0)) {pluralorNot = " Countries";}

            if (isCountry) {display = "(" + CountriesInYear.size() + pluralorNot + " found)";}
            if (isRegion) {display = "(" + RegionsInYear.size() + pluralorNot + " found)";}
            
            html += String.format("""
                    <section class="content">
                        <div class="main-content">
                            <div class="results-top">
                                <div class="results">
                                    <h3 class="results_title">Results</h3>
                                    <p class="results_found">%s</p>
                                </div>
            """, display);

            /************************************************ Sorting ************************************************************/

            html += addSortSection(sort);

            /************************************************ Table ************************************************************/

            html +=  String.format("""
                            <div class="table">
                                <table class="table_content">
                                    <thead>
                                        <tr>
                                            <th>Rank</th>
                                            <th>%s%s%s</th>
                                            <th>Similarity Score</th>
                                            <th %s>Common Foods</th>
                                            <th %s>Combined Foods</th>
                                            <th %s>Average Loss</th>
                                        </tr>
                                    </thead>
                                    <tbody> """,
                                    NotSelected ? "Location" : "",
                                    isRegion ? "Region" : "",
                                    isCountry ? "Country" : "",
                                    similarity.equals("loss") ? "style = 'display:none;'" : "",
                                    (similarityType.equals("absolute") || similarity.equals("loss")) ? "style = 'display:none;'" : "",
                                    similarity.equals("food") ? "style = 'display:none;'" : "");
                                
            if (NotSelected) {
                html += """
                    <tr style="background-color:whitesmoke;"> 
                        <td colspan=7 style="font-weight:bold; color:#939393; text-align:center; font-size:25px; padding:200px;"> No Locations Selected </td>
                    </tr>""";
            }

            int tryYear = 0;

            if (isCountry && CountriesInYear.size() == 0) {

                tryYear = JDBCConnection.getClosestYearForACountry(year, locationName);

                if (tryYear == 0) {
                    html += """

                    <tr style="background-color:whitesmoke;"> 
                        <td colspan=7 style="font-weight:bold; color:#939393; text-align:center; font-size:25px; padding:190px;"> No Data found for Other Countries in the Years Data exists for the Selected Country</td>
                    </tr>""";

                }
                
                else {

                    html += """
                        <tr style="background-color:whitesmoke;"> 
                            <td colspan=7 style="font-weight:bold; color:#939393; text-align:center; font-size:25px; padding:170px;"> 
                            <div class="No-results"> No Data Available for """
                            + " Selected Country in " + year + ".</div><div class='No-results-tryYear'><button type='submit' id='tryYear'> Try "
                             + tryYear + " </button>" + """
                            <div class="help-icon">
                                <i class="fas fa-question-circle"></i>
                                <div class="tooltip">The <b>Closest Year</b> to the <b>Selected Year</b> 
                                where <b>Data</b> exists for the <b>Selected Location</b> and at least one <b>Other Location</b></div>
                            </div></div></td></tr>""";
                }
            }

            if (isRegion && RegionsInYear.size() == 0) {

                tryYear = JDBCConnection.getClosestYearForARegion(year, locationName, RegionM49Code);

                if (tryYear == 0) {
                    html += """

                    <tr style="background-color:whitesmoke;"> 
                        <td colspan=7 style="font-weight:bold; color:#939393; text-align:center; font-size:25px; padding:190px;"> No Data found for Other Regions in the Years Data exists for the Selected Region</td>
                    </tr>""";

                }
                
                else {
            
                    html += """
                        <tr style="background-color:whitesmoke;"> 
                            <td colspan=7 style="font-weight:bold; color:#939393; text-align:center; font-size:25px; padding:170px;"> 
                            <div class="No-results"> No Data Available for """
                            + " Selected Region in " + year + ".</div><div class='No-results-tryYear'><button type='submit' id='tryYear'> Try "
                             + tryYear + " </button>" + """
                            <div class="help-icon">
                                <i class="fas fa-question-circle"></i>
                                <div class="tooltip">The <b>Closest Year</b> to the <b>Selected Year</b> 
                                where <b>Data</b> exists for the <b>Selected Location</b> and at least one <b>Other Location</b></div>
                            </div></div></td></tr>""";

                }
            }
            
            html += String.format("""
                <script>
                    document.addEventListener('DOMContentLoaded', function() {
                        var tryYear = %d;
                        var yearInput = document.getElementById('yearInput');
                        var tryYearSpan = document.getElementById('tryYear');

                        if (yearInput && tryYearSpan) {
                            tryYearSpan.addEventListener('click', function() {
                                yearInput.value = tryYear;
                            });
                        }
                    });
                </script>
                """, tryYear);
            

            if (isCountry && CountriesInYear.size() > 0) {

                Country SelectedCountry = new Country(locationName);
                SelectedCountry = JDBCConnection.getOverallSimilarityScoreForCountry(SelectedCountry, year, locationName, similarity, similarityType);
                double SelectedOverallSimilarityScore = SelectedCountry.getOverallScore();
                
                html += "<tr>";
                html += "<td class='selected'>Selected</td>";
                html += "<td>%s</td>".formatted(locationName);
                html += "<td class='num'>%.2f</td>".formatted(SelectedOverallSimilarityScore);
                if (!similarity.equals("loss")) { html += "<td class='num'>%d</td>".formatted(SelectedCountry.getNumberOfCommonProducts());}
                if (!similarityType.equals("absolute") && !similarity.equals("loss")) { html += "<td class='num'>%d</td>".formatted(SelectedCountry.getNumberOfUniqueProducts());}
                if (!similarity.equals("food")) { html += "<td class='num'>%.2f</td>".formatted(SelectedCountry.getAverageLoss());}
                html += "</tr>";


                ArrayList<Country> sortedCountries = new ArrayList<>();
                
                for (String Country: CountriesInYear) {
                    Country countryObject = new Country(Country);
                    countryObject = JDBCConnection.getOverallSimilarityScoreForCountry(countryObject, year, locationName, similarity, similarityType);
                    sortedCountries.add(countryObject);
                }

                if (sort.equals("HL")) {
                    sortedCountries.sort((c1, c2) -> {
                        int compareOverallScore = Double.compare(c2.getOverallScore(), c1.getOverallScore());
                        if (compareOverallScore != 0) {
                            return compareOverallScore;
                        } else {
                            return Double.compare(c2.getNumberOfCommonProducts(), c1.getNumberOfCommonProducts());
                        }
                    });
                }
                
                else if (sort.equals("LH")) {
                    sortedCountries.sort((c1, c2) -> {
                        int compareOverallScore = Double.compare(c1.getOverallScore(), c2.getOverallScore());
                        if (compareOverallScore != 0) {
                            return compareOverallScore;
                        } else {
                            return Double.compare(c1.getNumberOfCommonProducts(), c2.getNumberOfCommonProducts());
                        }
                    });
                }

                for (int i = 0; i < numberOfResults && i < sortedCountries.size(); i++) {
                    
                    Country countryObject = sortedCountries.get(i);
                    String Country = countryObject.getName();
                    double overallSimilarityScore = countryObject.getOverallScore();
                    int commonFoodProducts = countryObject.getNumberOfCommonProducts();
                    int uniqueFoodProducts = countryObject.getNumberOfUniqueProducts();
                    double countryLossPercentage = countryObject.getAverageLoss();
                    int rank = i + 1;

                    html += "<tr>";
                    html += "<td class='rank'>%d</td>".formatted(rank);
                    html += "<td>%s</td>".formatted(Country);
                    html += "<td class='num'>%.2f</td>".formatted(overallSimilarityScore);
                    if (!similarity.equals("loss")) { html += "<td class='num'>%d</td>".formatted(commonFoodProducts);}
                    if (!similarityType.equals("absolute") && !similarity.equals("loss")) { html += "<td class='num'>%d</td>".formatted(uniqueFoodProducts);}
                    if (!similarity.equals("food")) { html += "<td class='num'>%.2f</td>".formatted(countryLossPercentage);}
                    html += "</tr>";

                }

            }

            else if (isRegion && RegionsInYear.size() > 0) {

                Region SelectedRegion = new Region(RegionM49Code, locationName);
                SelectedRegion = JDBCConnection.getOverallSimilarityScoreForRegion(SelectedRegion, year, locationName, RegionM49Code, similarity, similarityType);
                double SelectedOverallSimilarityScore = SelectedRegion.getOverallScore();

                html += "<tr>";
                html += "<td class='selected'>Selected</td>";
                html += "<td>%s</td>".formatted(locationName + " <span style='color:#939393;'>(" + RegionCountryName + ")</span>");
                html += "<td class='num'>%.2f</td>".formatted(SelectedOverallSimilarityScore);
                if (!similarity.equals("loss")) { html += "<td class='num'>%d</td>".formatted(SelectedRegion.getNumberOfCommonProducts());}
                if (!similarityType.equals("absolute") && !similarity.equals("loss")) { html += "<td class='num'>%d</td>".formatted(SelectedRegion.getNumberOfUniqueProducts());}
                if (!similarity.equals("food")) { html += "<td class='num'>%.2f</td>".formatted(SelectedRegion.getAverageLoss());}
                html += "</tr>";

                ArrayList<Region> sortedRegions = new ArrayList<>();

                for (Region regionObject: RegionsInYear) {
                    regionObject = JDBCConnection.getOverallSimilarityScoreForRegion(regionObject, year, locationName, RegionM49Code, similarity, similarityType);
                    sortedRegions.add(regionObject);
                }

                if (sort.equals("HL")) {
                    sortedRegions.sort((r1, r2) -> {
                        int compareOverallScore = Double.compare(r2.getOverallScore(), r1.getOverallScore());
                        if (compareOverallScore != 0) {
                            return compareOverallScore;
                        } else {
                            return Double.compare(r2.getNumberOfCommonProducts(), r1.getNumberOfCommonProducts());
                        }
                    });
                }
                
                else if (sort.equals("LH")) {
                    sortedRegions.sort((r1, r2) -> {
                        int compareOverallScore = Double.compare(r1.getOverallScore(), r2.getOverallScore());
                        if (compareOverallScore != 0) {
                            return compareOverallScore;
                        } else {
                            return Double.compare(r1.getNumberOfCommonProducts(), r2.getNumberOfCommonProducts());
                        }
                    });
                }

                for (int i = 0; i < numberOfResults && i < sortedRegions.size(); i++) {
                    
                    Region regionObject = sortedRegions.get(i);
                    String Region = regionObject.getName();
                    double overallSimilarityScore = regionObject.getOverallScore();
                    int commonFoodProducts = regionObject.getNumberOfCommonProducts();
                    int uniqueFoodProducts = regionObject.getNumberOfUniqueProducts();
                    double regionLossPercentage = regionObject.getAverageLoss();
                    String countryName = regionObject.getCountryName();
                    int rank = 0;
                    if (sort.equals("HL")){rank = i + 1;}
                    else if (sort.equals("LH")){rank = sortedRegions.size() - i;}

                    html += "<tr>";
                    html += "<td class='rank'>%d</td>".formatted(rank);
                    html += "<td>%s</td>".formatted(Region + " <span style='color:#939393;'>(" + countryName + ")</span>");
                    html += "<td class='num'>%.2f</td>".formatted(overallSimilarityScore);
                    if (!similarity.equals("loss")) { html += "<td class='num'>%d</td>".formatted(commonFoodProducts);}
                    if (!similarityType.equals("absolute") && !similarity.equals("loss")) { html += "<td class='num'>%d</td>".formatted(uniqueFoodProducts);}
                    if (!similarity.equals("food")) { html += "<td class='num'>%.2f</td>".formatted(regionLossPercentage);}
                    html += "</tr>";

                }

            }

            
            html +=  "</tbody></table></div>";

            html += """

            <div class='similarity-help'>
                <div class="text-similarity">How scores are calculated</div>
                <i class=\"fas fa-question-circle\"></i>
                <div class='tooltip2'><img src='Calculations.png' alt='Similarity-Calculations' style='width:95vh;'></div>
            </div>
            
            """;
            
            
            html += "</div></section></main>";

            // Footer
            //html += CommonDesign.AddFooter();

            // Add the JavaScript
            html += addJavaScript();

            html += addJavaScript2();

            // Finish the HTML webpage
            html += "</form>" + "</body>" + "</html>";  

            // DO NOT MODIFY THIS
            // Makes Javalin render the webpage
            context.html(html);
    }
    
    /**************************************************************** END OF HTML *****************************************************************/

    // DO NOT MODIFY THIS
    // METHODS USED WITHIN THE FILE

    /********************Generate a UNIQUE ID for Each Country *************************/

    private int idCounter = 0;

    public synchronized int generateUniqueId() {
        idCounter++;
        return idCounter;
    }

    /*************************************** Escaping single quotes ******************************************/

        // Escape single quotes in SQL
        public static String escapeSingleQuotesSQL(String input) {
            if (input == null) {
                return null;
            }
            return input.replace("'", "''");
        }

        // Escape single quotes in Java
        public String escapeSingleQuotesWithBackslash(String input) {
            if (input == null) {
                return null;
            }
            return input.replace("'", "\\'");
        }

        // Escape single quotes for HTML
        public String escapeSingleQuotesForHtml(String input) {
            if (input == null) {
                return null;
            }
            return input.replace("'", "&#39;");
        }
    
    /******************************************* Add CSS to the HTML **********************************************/
    
    public String addStyleSheet(String html) {


        // Add the CSS for the page
        html += "<link rel='stylesheet' type='text/css' href='Task3A/task3a.css' />";
        html += "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css'/>";
        html += "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css'>";

        // Add the Bootstrap CSS

        html += "<link rel='stylesheet' type='text/css' href='Task2A/dropdown.css' />";
        html += "<link rel='stylesheet' type='text/css' href='Task2A/dropdown2.css' />";
        html += """

            <style>

            .bootstrap-select .dropdown-toggle .filter-option {
                font-size: 16px; /* Adjust the font size */
            }

            .bootstrap-select .dropdown-menu {
                font-size: 15px; /* Adjust the font size of dropdown items */
                max-height: 175px; /* Set a maximum height for the dropdown menu */
                overflow-y: auto; /* Enable vertical scrolling if needed */
                max-width: 300px; /* Set a maximum width for the dropdown menu */
                white-space: normal; /* Allow text to wrap */
                word-wrap: break-word; /* Ensure long words are broken to fit */
            }

            .dropdown-menu.show {
                max-width: 300px; /* Set a maximum width for the dropdown menu */
                white-space: normal; /* Allow text to wrap */
                word-wrap: break-word; /* Ensure long words are broken to fit */
            }

            .bootstrap-select .dropdown-menu li a {
                padding-left: 10px; /* Adjust left padding for text */
                padding-right: 4px; /* Adjust right padding for text */
                padding-top: 7px; /* Adjust top padding for text */
                padding-bottom: 7px; /* Adjust bottom padding for text */
                white-space: normal; /* Allow text to wrap */
            }

            .bootstrap-select .dropdown-menu li.selected a span.check-mark {
                transform: scale(0.9); /* Adjust the scale value for the checkmark */
                padding-top: 3px;

            }
            
            .bootstrap-select .dropdown-toggle {
                border: 1px solid #ccc;
                box-shadow: none; /* Remove any default shadow */
            }

            .bootstrap-select {
                text-align: left;
            }
            
            .dropdown-item:hover {
                background-color: #f0f0f0; /* Light grey */
                color: #212529;
            }

            </style>

            """;

        return html;
    }


    /******************************************* Add the Year Selection Section **********************************************/
    
    public String addYearRangeSection(int year) {
        
        String html = """
            <section class="box">
                <h2 class="box_title">Select Year</h2>
                <div class="filters">
                <div class="input-field">
                    <label class="form_label">
                    <div class="Year-select"><input type="number" id="yearInput" name="year" value="%d" min="%d" max="%d"></div>
                    </label>
                </div>
                </div>
            </section>
            """.formatted(year, defaultStartYear, defaultEndYear);
    
        return html;
    }

    /******************************************* Add the Number of Results Section **********************************************/

    public String addNumberOfResultsSection(int numberOfResults) {
        
        String html = """
            <section class="box">
                <h2 class="box_title">Number of Results</h2>
                <div class="filters">
                <div class="input-field">
                    <label class="form_label">
                    <div class="Results-Select"><input type="number" name="numberOfResults" value="%d" min="0" max="500"></div>
                    </label>
                </div>
                </div>
            </section>
            """.formatted(numberOfResults);
    
        return html;
    }

    /************************************************ Similarity Criteria ****************************************************/

    public String addSimilarityCriteriaSection(String similarity) {

        String html = String.format("""
            <section class="box">
                <h2 class="box_title">Similarity Criteria</h2>
                <div class="filters"> 
                    <label class="similaritySelect">
                        <select class="options" name="option">
                            <option value="food" %s>Common Foods</option>
                            <option value="loss" %s>Loss Percentage</option>
                            <option value="both" %s>Both (Country Only)</option>
                        </select>
                    </label>
                </div> 
            </section>
            """, similarity.equals("food") ? "selected" : "", similarity.equals("loss") ? "selected" : "", similarity.equals("both") ? "selected" : "");

        html += """
        <script>
            document.getElementById('mainForm').addEventListener('submit', function(event) {
                const countrySelect = document.getElementById('countrySelect');
                const selectedCountries = Array.from(countrySelect.selectedOptions).map(option => option.value);
                const similaritySelect = document.querySelector('.similaritySelect .options');
                const selectedSimilarity = similaritySelect.value;

                if (selectedSimilarity === 'both' && selectedCountries.some(country => country.includes('_'))) {
                    alert('Please note: "Both" cannot be selected for Regions.');
                    event.preventDefault();
                }
            });
        </script>
                """;
    
        return html;
    }

    /************************************************ Similarity Type ****************************************************/

    public String addSimilarityTypeSection(String similarityType) {

        String html = String.format("""
            <section class="box">
                <h2 class="box_title">Similarity Type</h2>
                <div class="filters">    
                    <label class="similaritySelect">
                        <select class="options" name="option1">
                            <option value="absolute" %s>Absolute Values</option>
                            <option value="overlap" %s>Level of Overlap</option>
                        </select>
                    </label>
                </div>
            </section>
            """, similarityType.equals("absolute") ? "selected" : "", similarityType.equals("overlap") ? "selected" : "");

        return html;
    }

    /******************************************* Add the Sort Section **********************************************/

    public String addSortSection(String sort) {

        String html = "";
        
        html += String.format("""

                                <div class="sort-by">
                                    <label class="sort-by_label" title="Sort"><span style = "font-weight:bold; color:#38414b; font-size:17px;">Sort By</span>
                                        <select id="sortSelect" class="sort-by_select" name="sortOption" onchange="this.form.submit()">
                                            <option value="LH" %s>Similarity Score: ↑</option>
                                            <option value="HL" %s>Similarity Score: ↓</option>
                                        </select>
                                    </label>
                                </div>
                            </div>

                        """, 
                        sort.equals("LH") ? "selected" : "",
                        sort.equals("HL") ? "selected" : "");

        return html;
    }

    /*************************************************** Add the JavaScript ******************************************************/

    public String addJavaScript2() {

        String html = "";

        // Add the Bootstrap JS and Bootstrap Select JS for the Country Selection
        html += """

            <script>
                $(document).ready(function() {
                    // Initialize the selectpicker
                    $('#countrySelect').selectpicker();

                    // Store the initial order of the options without the placeholder
                    let initialOrder = $('#countrySelect option').not('[value=""]').clone();

                    // Flag to track if the placeholder has been removed
                    let placeholderRemoved = false;

                    // Function to move selected option to the top and remove placeholder if needed
                    function moveSelectedOptionToTop() {
                        let $select = $('#countrySelect');
                        let selectedOption = $select.find('option:selected');

                        // Remove the placeholder option when an option is selected for the first time
                        if (!placeholderRemoved && selectedOption.length > 0 && selectedOption.val() !== "") {
                            $select.find('option[value=""]').remove();
                            placeholderRemoved = true;
                        }

                        $select.prepend(selectedOption);
                        $select.selectpicker('refresh');
                    }

                    // Function to restore initial order excluding the selected option
                    function restoreInitialOrder() {
                        let $select = $('#countrySelect');
                        let selectedOption = $select.find('option:selected');
                        $select.html(initialOrder.clone());

                        if (selectedOption.length > 0) {
                            $select.find(`option[value="${selectedOption.val()}"]`).remove();
                            $select.prepend(selectedOption);
                        }

                        $select.selectpicker('refresh');
                    }

                    // Move selected option to the top after selection
                    $('#countrySelect').on('changed.bs.select', function(e, clickedIndex, isSelected, previousValue) {
                        moveSelectedOptionToTop();
                        restoreInitialOrder();
                    });
                });
            </script>

            """;

        return html;

    }

    public String addJavaScript() {

        String html = "";

        // Add the Bootstrap JS and Bootstrap Select JS for the Country Selection
        html += """
            
                <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
                <!-- Bootstrap JS -->
                <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.bundle.min.js"></script>
                <!-- Bootstrap Select JS -->
                <script src="https://cdn.jsdelivr.net/npm/bootstrap-select@1.13.14/dist/js/bootstrap-select.min.js"></script>

            """;

        return html;

    }

    /************************************************* END ***********************************************/

}
