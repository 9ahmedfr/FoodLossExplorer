package app;

//import java.lang.reflect.Array;
//import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

public class PageST2A implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/page2A.html";
    int defaultStartYear = JDBCConnection.getLowestYear() ; 
    int defaultEndYear = JDBCConnection.getHighestYear();
    String[] selectedCountries;
    int startYear;
    int endYear;
    boolean isCheckedActivity = true;
    boolean isCheckedSupply = true;
    boolean isCheckedCause = true;
    boolean YearInBetween = true;
    String sort = "AZ";

    @Override
    public void handle(Context context) throws Exception {

        /************************************************************* GET THE FORM PARAMETERS *****************************************************************/

        // Get the sort option
        if (context.formParam("sortOption") == null) {sort = "AZ";}
        else {sort = context.formParam("sortOption");}

        // Get the selected columns
        if (context.formParam("activity") != null) {isCheckedActivity = true;
        } else {isCheckedActivity = false;}

        if (context.formParam("supply") != null) {isCheckedSupply = true;
        } else {isCheckedSupply = false;}

        if (context.formParam("cause") != null) {isCheckedCause = true;
        } else {isCheckedCause = false;}

        // Get the selected columns
        String ColumnsSelected = "";
        if(isCheckedActivity) {ColumnsSelected += "Activity";}
        if(isCheckedSupply) {ColumnsSelected += "Supply";}
        if(isCheckedCause) {ColumnsSelected += "Cause";}

        // Get the selected countries
        selectedCountries = context.formParams("countries[]").toArray(new String[0]);
        
        // Get the years in between option
        if (context.formParam("years-inbetween") != null) {YearInBetween = true;
        } else {YearInBetween = false;}

        // Get the start and end years
        if (context.formParam("start-year") != null && !context.formParam("start-year").trim().isEmpty()) {
            try {startYear = Integer.parseInt(context.formParam("start-year").trim());
            } catch (NumberFormatException e) {startYear = 0;}
        } else {startYear = 0;}
        
        if (context.formParam("end-year") != null && !context.formParam("end-year").trim().isEmpty()) {
            try {endYear = Integer.parseInt(context.formParam("end-year").trim());
            } catch (NumberFormatException e) {endYear = 0;}
        } else {endYear = 0;}
        
        // Validate the start and end years
        if (startYear == 0) {startYear = defaultStartYear;} 
        if (endYear == 0) {endYear = defaultEndYear;}

        if (startYear > endYear) {
            int temp = startYear;
            startYear = endYear;
            endYear = temp;
        }
        
        /************************************************************* HTML FOR PAGE STARTS HERE *****************************************************************/


        String html = "<html>";

        html += "<head>" + "<title>Country</title>";
        html += "<meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>";
        html += "<link rel=\"icon\" href=\"FLW.png\" type=\"image/x-icon\">";

        // Add some CSS (JAVA-METHOD)
        html = addStyleSheet(html);

        html += "</head><body>";

        html += "<form action='page2A.html' method='post'>";

        // Add the topnav
        html += CommonDesign.AddTopnav("page2A.html");

        // HTML for content header
        html += """
            <div class= "page-content">
                <section class="content-header"> 
                    <h2 class="content-header_title">Country Data</h2>
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
                            <div class="Countries_box"><h2 class="box_title">Countries</h2>
                                <div class="Refresh" title="Resubmit">
                                    <button class="CountryButton" type="submit">
                                        <i class="fa-solid fa-rotate-right"></i></div>
                                    </button>
                                </div>
                                <div class="container mt-5">
                                    <select class="selectpicker" multiple data-live-search="true" data-live-search-normalize="true" 
                                    id="countrySelect" name="countries[]" data-none-selected-text="Select Countries">
                """;

                //Keeping already selected countries selected

                html += "<option value='all_countries' style='font-weight:bold;'>Select All</option>";

                ArrayList<String> countryNames = JDBCConnection.getAllCountryNames();
                Set<String> selectedCountrySet = new HashSet<>(Arrays.asList(selectedCountries));
                for (String country : countryNames) {
                    String optionHtml = "<option value='" + escapeSingleQuotesForHtml(country) + "'";
                    if (selectedCountrySet.contains(country)) {
                        optionHtml += " selected";
                    }
                    optionHtml += ">" + country + "</option>";
                    html += optionHtml;
                }
                
                html += """
                            </select>
                        </div>
                    </section> 
                """;

                /************************************************ Filters ************************************************************/

                //Columns Section
                html += addColumnFilters(isCheckedActivity, isCheckedSupply, isCheckedCause);
            
                //Year Range
                html += addYearRangeSection(startYear, endYear, YearInBetween);
            
                /************************************************ Submit Button ************************************************************/

                html += """
                            <section class="box" style="text-align: center; padding: 0px;">
                                <button class="submitbutton" type="submit">Show Results</button>
                            </section>
                    </aside>
                """;


        /********************************************************* MAIN CONTENT STARTS HERE **********************************************************/
                
            /************************************************ Sorting ************************************************************/

            html += addSortSection(sort, ColumnsSelected);

            /************************************************ Table ************************************************************/

            html +=  """
                            <div class="table">
                                <table class="table_content">
                                    <thead>
                                        <tr>
                                            <th>Country</th>
                                            <th>Start Year Loss</th>
                                            <th>End Year Loss</th>
                                            <th>Loss Change</th>
                                            <th>Average Loss</th>
                                        </tr>
                                    </thead>
                                    <tbody> """;
            
        
            ArrayList<String> listOfSelectedCountries = JDBCConnection.getSortedCountryNames(selectedCountries, startYear, endYear, 
                                                        sort, isCheckedActivity, isCheckedSupply, isCheckedCause);


            if (listOfSelectedCountries.size() == 0) {
                html += """
                    <tr style="background-color:whitesmoke;"> 
                        <td colspan=5 style="font-weight:bold; color:#939393; text-align:center; font-size:25px; padding:205px;"> No Countries Selected </td>
                    </tr>""";
            }

            /********************************************************* ADDING THE COUNTRIES **********************************************************/


            for (String country: listOfSelectedCountries) {

                // Get the years for a country
                ArrayList<Integer> Years = JDBCConnection.getYearRangeForACountry(country, startYear, endYear, YearInBetween);

                // The earliest and latest year for a given country within a specified range
                int minYear = JDBCConnection.getMinOrMaxYearForACountryYearRange(country, startYear, endYear, "min");
                int maxYear = JDBCConnection.getMinOrMaxYearForACountryYearRange(country, startYear, endYear, "max");

                // Loss for Start and End Year For A Country Using Min and Max Year
                String StartyearLoss = String.format("%.2f%%", JDBCConnection.getLossForAYear(country, minYear, isCheckedActivity, isCheckedSupply, isCheckedCause));
                String EndyearLoss = String.format("%.2f%%", JDBCConnection.getLossForAYear(country, maxYear, isCheckedActivity, isCheckedSupply, isCheckedCause));
                
                // Get the loss change for a country for start and end year
                String lossChange = "";
                double lossChangeValue = JDBCConnection.getLossChangeForACountry(country, minYear, maxYear, isCheckedActivity, isCheckedSupply, isCheckedCause); 
                if (lossChangeValue > 0) {lossChange = String.format("+%.2f%%", lossChangeValue);
                } else {lossChange = String.format("%.2f%%", lossChangeValue);}
                if (maxYear == minYear) {lossChange = "N/A";}

                // Get the average loss for a country
                double averageLossValue = JDBCConnection.getAverageLossForACountry(country, minYear, maxYear, isCheckedActivity, isCheckedSupply, isCheckedCause, YearInBetween);
                String averageLoss = String.format("%.2f%%", averageLossValue);
                

                // Generate a unique id for each country
                int CountryId = generateUniqueId();


                // If there are no years for a country
                if (Years.size() == 0) {  
                    StartyearLoss = "N/A";
                    EndyearLoss = "N/A";
                    lossChange = "N/A";
                    averageLoss = "N/A";
                }
                
                html += String.format(
                    "<tr>" +
                        "<td class='parent'>" +
                            "<input type='checkbox' id='%d' onclick='showHideRow(\"hidden_row_%d\")'>" +
                            "<label for='%d'>%s</label>" +
                        "</td>" +
                        "<td class='loss'>%s</td>" +
                        "<td class='loss'>%s</td>" +
                        "<td class='loss'>%s</td>" +
                        "<td class='loss'>%s</td>" +
                    "</tr>" +
                    "<tr id='hidden_row_%d' class='hidden_row'><td colspan=5>",
                    CountryId, CountryId, CountryId, country, StartyearLoss, EndyearLoss, lossChange, averageLoss, CountryId
                );
                

                if (Years.size() != 0) {
                    
                    html += """
                        <div class="table_content_year_container">   
                            <table class="table_content_year">
                                <thead>
                                    <tr>
                                        <th>Year</th>
                                        <th>Average Yearly Loss</th>
                                    </tr> 
                                </thead>
                                <tbody>
                        """;
                } 
                
                else {
                    
                    html += """
                        <div class="table_content_year_container">   
                            <table class="table_content_year">
                                <thead>
                                    <tr>
                                        <th>Year</th>
                                        <th>Average Yearly Loss</th>
                                    </tr> 
                                </thead>
                                <tbody>
                                <tr style="background-color:#e7f5ff;;"> 
                                    <td colspan=2 style="font-weight:bold; color:#777777; text-align:center; font-size:1.2rem; padding:30px;"> No Data For Selected Years </td>
                                </tr>
                                </tbody>
                        """;
                } 

                /********************************************************* ADDING THE YEARS **********************************************************/
                
                for (int year : Years) {

                    double lossPercentage = JDBCConnection.getLossForAYear(country, year, isCheckedActivity, isCheckedSupply, isCheckedCause);
                    int YearId = generateUniqueId();
                
                    html += String.format(
                        "<tr>" +
                            "<td class='parent'>" +
                                "<input type='checkbox' id='%d' onclick='showHideRow(\"hidden_row_%d\")'>" +
                                "<label for='%d'>%d</label>" +
                            "</td>" +
                            "<td class='loss'>%.2f%%</td>" +
                        "</tr>" +
                        "<tr id='hidden_row_%d' class='hidden_row'><td colspan=2>",
                        YearId, YearId, YearId, year, lossPercentage, YearId
                    );
                
                    html += """
                        <div class="table_content_commodity_container">
                            <table class="table_content_commodity">
                                <thead>""";
                    

                    /********************************************************* ADDING THE COMMODITIES **********************************************************/

                    ArrayList<FoodLossEvent> foodLossEventsforCommodities = JDBCConnection.getCommodityLossForAYear(country, year, isCheckedActivity, isCheckedSupply, isCheckedCause);

                    html += generateTable(ColumnsSelected, foodLossEventsforCommodities);

                    html += "</tbody></table><div></td></tr>";
                                
                }

                html += "</tbody></table></div></td></tr> ";

            }

            html +=  "</tbody></table></div>";
            
            html += """

            <div class='information-help'>
                <div class="text-information">Data Details</div>
                <i class=\"fas fa-question-circle\"></i>
                <div class='tooltip2'>
                    <h3>Please Note:</h3>
                    <ul>
                    <li>If the <b>Selected Years</b> are not <b>Shown</b>, it means the <b>Chosen Country</b> does <b>Not</b> have <b>Data Available</b> for that <b>Year</b>.</li>
                    <li>When the <b>"Years Between"</b> option is <b>Unchecked</b>, we display the <b>Closest Available Years</b> to the <b>Selected Start</b> and <b>End Years</b> to provide the most <b>Relevant Information</b>.</li>
                    <li>All <b>Columns</b> are <b>Averaged</b> based on the selected options: <b>"Activity"</b>, <b>"Supply"</b>, or <b>"Cause"</b>. Consequently, the displayed <b>Loss Values</b> may vary due to <b>Data Aggregation</b></li>
                    </ul>
                </div>
            </div>
            
            """;

            html += "</div></section></main>";

            // Footer
            //html += CommonDesign.AddFooter();

            // Add the JavaScript
            html += addJavaScript();

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
        html += "<link rel='stylesheet' type='text/css' href='Task2A/task2a.css' />";
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
                max-height: 150px; /* Set a maximum height for the dropdown menu */
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

    /******************************************* Add the Column Filters Section **********************************************/

    public String addColumnFilters(Boolean isCheckedActivity, Boolean isCheckedSupply, Boolean isCheckedCause) {

        String html = "";

        html +=  """
            <section class="box">
            <h2 class="box_title">Show Columns</h2> """;

        html += String.format("""

            <div class="filters">    
                <label class="form_label_column">  
                    <input type="checkbox" name="activity" value="activity" %s> <span>Activity</span>
                </label>
                <label class="form_label_column">
                    <input type="checkbox" name="supply" value="supply" %s> <span>Supply</span>
                </label>
                <label class="form_label_column">
                    <input type="checkbox" name="cause" value="cause" %s> <span>Cause</span>
                </label>
            </div> 

        """, isCheckedActivity ? "checked" : "", isCheckedSupply ? "checked" : "", isCheckedCause ? "checked" : "");

        html += "</section>";

        return html;
    }

    /******************************************* Add the Year Selection Section **********************************************/
    
    public String addYearRangeSection(int startYear, int endYear, boolean yearsInBetween) {
        
        String html = """
            <section class="box">
                <h2 class="box_title">Year Range</h2>
                <div class="filters">
                <div class="input-field">
                    <label class="form_label">
                    <div class="Year-select-name">Start Year: </div><div class="Year-select"><input type="number" name="start-year" value="%d" min="%d" max="%d"></div>
                    </label>
                </div>
                <div class="input-field">
                    <label class="form_label">
                    <div class="Year-select-name">End Year: </div><div class="Year-select"><input type="number" name="end-year" value="%d" min="%d" max="%d"></div>
                    </label>
                </div>
                <label class="form_label1" title="Display Data for the Years in between">
                    <input type="checkbox" name="years-inbetween" %s> Years Between
                </label>
                </div>
            </section>
            """.formatted(startYear, defaultStartYear, defaultEndYear, endYear, defaultStartYear, defaultEndYear, yearsInBetween ? "checked" : "");
    
        return html;
    }
    
    /******************************************* Add the Sort Section **********************************************/

    public String addSortSection(String sort, String columnsSelected) {

        String html = "";

        String columns = "";

        if (columnsSelected.equals("")) {columns += "" ;}
        else {columns = "(";}

        if (columnsSelected.contains("Activity")) {columns += "Activity";}
        if (columnsSelected.contains("Supply") && columnsSelected.contains("Activity")) {columns += ", Supply";}
        else if (columnsSelected.contains("Supply")) {columns += "Supply";}
        if (columnsSelected.contains("Cause") && ((columnsSelected.contains("Activity")) || (columnsSelected.contains("Supply")))) {columns += ", Cause";}
        else if (columnsSelected.contains("Cause")) {columns += "Cause";}

        if (columnsSelected.equals("")) {columns += "";}
        else {columns += ")";}

        
        html += String.format("""

                    <section class="content">
                        <div class="main-content">
                            <div class="results-top">
                                <div class="results">
                                    <h3 class="results_title">Results</h3>
                                    <div class="help-icon">
                                        <i class="fas fa-question-circle"></i>
                                        <div class="tooltip">Use arrows to expands rows (▼▲)</div>
                                    </div>
                                    <p class="columns">%s</p>
                                </div>
                                <div class="sort-by">
                                    <label class="sort-by_label" title="Sort"> <span style = "font-weight:bold; color:#38414b; font-size:17px;">Sort By</span>
                                        <select id="sortSelect" class="sort-by_select" name="sortOption" onchange="this.form.submit()">
                                            <option value="AZ" %s>Country: A to Z</option>
                                            <option value="ZA" %s>Country: Z to A</option>
                                            <option value="SL" %s>Start Year Loss: ↑</option>
                                            <option value="SH" %s>Start Year Loss: ↓</option>
                                            <option value="EL" %s>End Year Loss: ↑</option>
                                            <option value="EH" %s>End Year Loss: ↓</option>
                                        </select>
                                    </label>
                                </div>
                            </div>

                        """, 
                        columns,
                        sort.equals("AZ") ? "selected" : "",
                        sort.equals("ZA") ? "selected" : "", 
                        sort.equals("SL") ? "selected" : "", 
                        sort.equals("SH") ? "selected" : "", 
                        sort.equals("EL") ? "selected" : "", 
                        sort.equals("EH") ? "selected" : "");

        return html;
    }

    /*************************************************** Add the JavaScript ******************************************************/

    public String addJavaScript() {

        String html = "";

        // Add the Bootstrap JS and Bootstrap Select JS for the Country Selection
        html += """
            <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.5.1/jquery.min.js"></script>
            <!-- Bootstrap JS -->
            <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.5.2/js/bootstrap.bundle.min.js"></script>
            <!-- Bootstrap Select JS -->
            <script src="https://cdn.jsdelivr.net/npm/bootstrap-select@1.13.14/dist/js/bootstrap-select.min.js"></script>
            
            <script>
                $(document).ready(function() {
                    // Initialize the selectpicker
                    $('.selectpicker').selectpicker();

                    // Function to reorder selected options to the top and sort in ascending order
                    function reorderAndSortOptions() {
                        let $select = $('#countrySelect');
                        let allCountriesOption = $select.find('option[value="all_countries"]');
                        let selectedOptions = $select.find('option:selected').not(allCountriesOption);
                        let nonSelectedOptions = $select.find('option:not(:selected)').not(allCountriesOption);

                        $select.html(''); // Clear the select options

                        // Append "Select All Countries" option first
                        $select.append(allCountriesOption);

                        // Sort selected options in ascending order
                        selectedOptions.sort(function(a, b) {
                            return a.value.localeCompare(b.value);
                        });

                        // Append sorted selected options after "Select All Countries"
                        selectedOptions.each(function() {
                            $select.append($(this));
                        });

                        // Append non-selected options after selected options
                        nonSelectedOptions.each(function() {
                            $select.append($(this));
                        });

                        // Refresh the selectpicker to reflect changes
                        $select.selectpicker('refresh');
                    }

                    // Function to handle select all option
                    $('#countrySelect').on('changed.bs.select', function (e, clickedIndex, isSelected, previousValue) {
                        if ($(this).val() && $(this).val().includes('all_countries')) {
                            // If select all is clicked, select all options
                            $(this).find('option').prop('selected', true);
                            $(this).selectpicker('refresh');
                        } else if (previousValue && previousValue.includes('all_countries')) {
                            // If select all was previously clicked and then another option is selected, deselect all options except "Select All Countries"
                            let nonAllOptions = $(this).find('option').not('[value="all_countries"]');
                            nonAllOptions.prop('selected', false);
                            $(this).selectpicker('refresh');
                        } else {
                            // Otherwise, reorder and sort options
                            reorderAndSortOptions();
                        }
                    });

                    // Function to clear search input
                    function clearSearchInput() {
                        $('.bs-searchbox input').val('');
                    }

                    // Reorder and sort options on change and clear search input
                    $('#countrySelect').on('changed.bs.select', function () {
                        reorderAndSortOptions();
                        clearSearchInput();
                    });

                    // Clear search input on pressing enter
                    $(document).on('keypress', '.bs-searchbox input', function(e) {
                        if (e.which == 13) {
                            e.preventDefault(); // Prevent form submission
                            clearSearchInput();
                        }
                    });

                    // Initial call to reorder and sort options
                    reorderAndSortOptions();
                });
            </script>


            """;
        
        // Add the JavaScript for Showing and Hiding Rows
        html += """
            <script>
            function showHideRow(rowId, element) {
                var row = document.getElementById(rowId);
                if (row.style.display === 'none' || row.style.display === '') {
                    row.style.display = 'table-row';
                    if (element) {
                        element.classList.remove('collapsed');
                    }
                } else {
                    row.style.display = 'none';
                    if (element) {
                        element.classList.add('collapsed');
                    }
                }
            }
            </script>

            """;

        return html;

    }

    /************************************************* Generate the Commodities Table ***********************************************/

    
    public String generateTable(String columnsSelected, ArrayList<FoodLossEvent> foodLossEvents) {
    
        String html = "";

        if (columnsSelected.equals("")) {
            html += "<tr><th>Food</th><th>Loss</th></tr></thead><tbody>";
            for (FoodLossEvent event : foodLossEvents) {
                html += "<tr><td>" + event.getCommodityName() + "</td><td class='loss'>" + 
                String.format("%.2f", event.getLossPercentage()) + "%</td></tr>";
            }
        } else {
            html = generateTableHeaders(html, columnsSelected);
            html = generateTableRows(html, foodLossEvents, columnsSelected);
        }
        return html;
    }

        // Generate the Commodities Table Headers
        public String generateTableHeaders(String html, String columnsSelected) {
            
            html += "<tr><th>Food</th>";
            if (columnsSelected.contains("Activity")) {html += "<th>Activity</th>";}
            if (columnsSelected.contains("Supply")) {html += "<th>Supply</th>";}
            if (columnsSelected.contains("Cause")) {html += "<th>Cause</th>";}
            html += "<th>Loss</th></tr></thead><tbody>";
            return html;
        }

        // Generate the Commodities Table Rows
        public String generateTableRows(String html, List<FoodLossEvent> foodLossEvents, String columnsSelected) {
            for (FoodLossEvent event : foodLossEvents) {

                String commodityName = event.getCommodityName();
                String activity = event.getActivity();
                String supply = event.getStage();
                String cause = event.getCause();
                String loss = String.format("%.2f", event.getLossPercentage());

                html += "<tr><td>" + commodityName + "</td>";
                if (columnsSelected.contains("Activity")) {html += "<td>" + activity + "</td>";}
                if (columnsSelected.contains("Supply")) {html += "<td>" + supply + "</td>";}
                if (columnsSelected.contains("Cause")) {html += "<td>" + cause + "</td>";}
                html += "<td class='loss'>" + loss + "%</td></tr>";
            }
            return html;
        }

    /************************************************* END ***********************************************/

}