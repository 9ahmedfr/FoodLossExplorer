package app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.javalin.http.Context;
import io.javalin.http.Handler;


public class PageST2B implements Handler {

  // URL of this page relative to http://localhost:7001/
  public static final String URL = "/page2B.html";
  int defaultStartYear = JDBCConnection.getLowestYear() ;
  int defaultEndYear = JDBCConnection.getHighestYear();
  String[] selectedGroups;
  int startYear;
  int endYear;
  boolean isCheckedActivity = true;
  boolean isCheckedSupply = true;
  boolean isCheckedCause = true;
  boolean YearInBetween = true;
  boolean someExcluded = false;
  String sort = "DESC";

  @Override
  public void handle(Context context) throws Exception {

    // Form parameters

    // Get the sort option
    if (context.formParam("sortOption") == null) {sort = "DESC";}
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

    // Get the selected food groups
    selectedGroups = context.formParams("foodGroups[]").toArray(new String[0]);

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


    // Webpage HTML

    String html = "<html>";

    html += "<head>" + "<title>Food Groups</title>";
    html += "<meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>";
    html += "<link rel=\"icon\" href=\"FLW.png\" type=\"image/x-icon\">";

    // CSS
    html = addStyleSheet(html);

    html += "</head><body>";

    html += "<form action='page2B.html' method='post'>";

    // topnav
    html += CommonDesign.AddTopnav("page2B.html");

    // Header
    html += """
    <div class= "page-content">
      <section class="content-header">
        <h2 class="content-header_title">Food Group Data</h2>
      </section>
    """;

    // Sidebar
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

    // Food Group Selection
    html +=  """
    <section class="box">
      <div class="FoodGroups_box"><h2 class="box_title">Food Groups</h2>
        <div class="Refresh" title="Resubmit">
          <button class="FoodGroupsButton" type="submit">
            <i class="fa-solid fa-rotate-right"></i></div>
          </button>
        </div>
        <div class="container mt-5">
          <select class="selectpicker" multiple data-live-search="true" data-live-search-normalize="true"
          id="groupSelect" name="foodGroups[]" data-none-selected-text="Select Food Groups">
    """;

    //Keeping already selected groups selected

    html += "<option value='all_groups' style='font-weight:bold;'>Select All</option>";

    ArrayList<String> groupNames = JDBCConnection2.getAllGroups();
    Set<String> selectedFoodGroupSet = new HashSet<>(Arrays.asList(selectedGroups));
    for (String foodGroup : groupNames) {
      String optionHtml = "<option value='" + escapeSingleQuotesForHtml(foodGroup) + "'";
      if (selectedFoodGroupSet.contains(foodGroup)) {
        optionHtml += " selected";
      }
      optionHtml += ">" + foodGroup + "</option>";
      html += optionHtml;
    }

    html += """
        </select>
      </div>
    </section>
    """;


    // Filters

    //Columns Section
    html += addColumnFilters(isCheckedActivity, isCheckedSupply, isCheckedCause);

    //Year Range
    html += addYearRangeSection(startYear, endYear, YearInBetween);

    // Sumbit button
    html += """
        <section class="box" style="text-align: center; padding: 0px;">
          <button class="submitbutton" type="submit">Show Results</button>
        </section>
    </aside>
    """;

    // Main Content

    // Sorting
    html += addSortSection(sort);

    html +=  """
    <div class="table">
      <table class="table_content">
        <thead>
          <tr>
            <th>Food Group</th>
            <th>Start Year Loss</th>
            <th>End Year Loss</th>
            <th>Loss Change</th>
          </tr>
        </thead>
        <tbody>
    """;

    ArrayList<String> sortedGroups = JDBCConnection2.getSortedGroups(selectedGroups, startYear, endYear, sort, isCheckedActivity, isCheckedSupply, isCheckedCause);

    if (sortedGroups.size() == 0) {
      html += """
        <tr style="background-color:whitesmoke;">
          <td colspan=5 style="font-weight:bold; color:#939393; text-align:center; font-size:25px; padding:215px;"> No Food Groups Selected </td>
        </tr>
      """;
    }

    // Adding Food Groups
    for (String foodGroup: sortedGroups) {
      // Get the years for a foodGroup
      ArrayList<Integer> Years = JDBCConnection2.getYearRangeForAGroup(foodGroup, startYear, endYear, YearInBetween);

      int minYear;
      int maxYear;
      if (Years.size() == 0) { // If a group has no data for the years selected
        someExcluded = true;  // Display a message
        continue;
      } else {
        minYear = Years.get(0);
        maxYear = Years.get(Years.size() - 1);
      }

      // Start and end values for a foodGroup
      double startLossNum = JDBCConnection2.getGroupLossForAYear(foodGroup, minYear, isCheckedActivity, isCheckedSupply, isCheckedCause);
      String StartyearLoss = String.format("%.2f%%", startLossNum);
      double endLossNum = JDBCConnection2.getGroupLossForAYear(foodGroup, maxYear, isCheckedActivity, isCheckedSupply, isCheckedCause);
      String EndyearLoss = String.format("%.2f%%", endLossNum);

      // Loss percentage difference
      String lossChange = "";
      double lossChangeValue = endLossNum - startLossNum;
      if (lossChangeValue > 0) {
        lossChange = String.format("+%.2f%%", lossChangeValue);
      } else {
          lossChange = String.format("%.2f%%", lossChangeValue);
        }
      if (maxYear == minYear) {
        lossChange = "N/A";
      }

      // Generate a unique id for each foodGroup
      int GroupId = generateUniqueId();


      html += String.format(
        "<tr>" +
          "<td class='parent'>" +
            "<input type='checkbox' id='%d' onclick='showHideRow(\"hidden_row_%d\")'>" +
            "<label for='%d'>%s</label>" +
          "</td>" +
          "<td class='loss'>%s</td>" +
          "<td class='loss'>%s</td>" +
          "<td class='loss'>%s</td>" +
        "</tr>" +
        "<tr id='hidden_row_%d' class='hidden_row'><td colspan=5>",
        GroupId, GroupId, GroupId, foodGroup, StartyearLoss, EndyearLoss, lossChange, GroupId
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

      // Add Years
      for (int year : Years) {

        double lossPercentage = JDBCConnection2.getGroupLossForAYear(foodGroup, year, isCheckedActivity, isCheckedSupply, isCheckedCause);
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
            <thead>
        """;

        // Add Commodities
        ArrayList<FoodLossEvent> foodLossEventsforCommodities = JDBCConnection2.getCommodityLossForAYear(foodGroup, year, isCheckedActivity, isCheckedSupply, isCheckedCause);

        html += generateTable(ColumnsSelected, foodLossEventsforCommodities);

        html += "</tbody></table><div></td></tr>";

      }

      html += "</tbody></table></div></td></tr> ";

    }

    if (someExcluded) {
      html += """
        <tr style="background-color:whitesmoke;">
          <td colspan=5 style="color:#939393; text-align:center; font-size:25px; padding:215px;"> Some food groups have been excluded<br>as there is no data available within the selected years </td>
        </tr>
      """;
      someExcluded = false;
    }

    html +=  "</tbody></table></div></div></section></main>";

    // Add the JavaScript
    html += addJavaScript();

    // Finish the HTML webpage
    html += "</form>" + "</body>" + "</html>";

    // DO NOT MODIFY THIS
    // Makes Javalin render the webpage
    context.html(html);
  }

  // End of HTML


  // Methods used in this file - DO NOT MODIFY

  // Generate unique ID
  private int idCounter = 0;
  public synchronized int generateUniqueId() {
    idCounter++;
    return idCounter;
  }

  // Escape single quotes...
  // ...in SQL
  public static String escapeSingleQuotesSQL(String input) {
    if (input == null) {
      return null;
    }
    return input.replace("'", "''");
  }

  // ...in Java
  public String escapeSingleQuotesWithBackslash(String input) {
    if (input == null) {
      return null;
    }
    return input.replace("'", "\\'");
  }

  // ...in HTML
  public String escapeSingleQuotesForHtml(String input) {
    if (input == null) {
      return null;
    }
    return input.replace("'", "&#39;");
  }

  // Add CSS
  public String addStyleSheet(String html) {
    // Add the CSS for the page
    html += "<link rel='stylesheet' type='text/css' href='Task2B/task2b.css'/>";
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
        max-height: 200px; /* Set a maximum height for the dropdown menu */
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

  // Add Column Filters
  public String addColumnFilters(Boolean isCheckedActivity, Boolean isCheckedSupply, Boolean isCheckedCause) {
    String html = "";

    html +=  """
      <section class="box">
      <h2 class="box_title">Show Columns</h2>
    """;

    html += String.format("""
      <div class="filters">
        <label class="form_label_column">
          <input type="checkbox" name="activity" value="activity" %s> <span>Activity</span>
        </label>
        <label class="form_label_column">
          <input type="checkbox" name="supply" value="supply" %s> <span>Supply Stage</span>
        </label>
        <label class="form_label_column">
          <input type="checkbox" name="cause" value="cause" %s> <span>Cause of Loss</span>
        </label>
      </div>

      """, isCheckedActivity ? "checked" : "", isCheckedSupply ? "checked" : "", isCheckedCause ? "checked" : ""
    );

    html += "</section>";

    return html;
  }

  // Add Year Section
  public String addYearRangeSection(int startYear, int endYear, boolean yearsInBetween) {
    String html = """
    <section class="box">
      <h2 class="box_title">Year Selection</h2>
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
        <label class="form_label1" title="Use selected years as MIN and MAX, and include all years between">
          <input type="checkbox" name="years-inbetween" %s> Use as Range
        </label>
      </div>
    </section>
    """.formatted(startYear, defaultStartYear, defaultEndYear, endYear, defaultStartYear, defaultEndYear, yearsInBetween ? "checked" : "");

    return html;
  }

  // Add Sort Section
  public String addSortSection(String sort) {

    String html = "";

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
            </div>
            <div class="sort-by">
                <label class="sort-by_label" title="Sort"><span style = "font-weight:bold; color:#38414b; font-size:17px;">Sort By</span>
                  <select id="sortSelect" class="sort-by_select" name="sortOption" onchange="this.form.submit()">
                  <option value="DESC" %s>Loss Change: ↓</option>
                  <option value="ASC" %s>Loss Change: ↑</option>
                  </select>
                </label>
            </div>
          </div>

      """,
      sort.equals("DESC") ? "selected" : "",
      sort.equals("ASC") ? "selected" : ""
    );

    return html;
  }

  // Add JavaScript
  public String addJavaScript() {
    String html = "";

    // Add Bootstrap JS and Bootstrap Select JS
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
          let $select = $('#groupSelect');
          let allGroupsOption = $select.find('option[value="all_groups"]');
          let selectedOptions = $select.find('option:selected').not(allGroupsOption);
          let nonSelectedOptions = $select.find('option:not(:selected)').not(allGroupsOption);

          $select.html(''); // Clear the select options

          // Append "Select All Food Groups" option first
          $select.append(allGroupsOption);

          // Sort selected options in ascending order
          selectedOptions.sort(function(a, b) {
            return a.value.localeCompare(b.value);
          });

          // Append sorted selected options after "Select All Food Groups"
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
        $('#groupSelect').on('changed.bs.select', function (e, clickedIndex, isSelected, previousValue) {
          if ($(this).val() && $(this).val().includes('all_groups')) {
            // If select all is clicked, select all options
            $(this).find('option').prop('selected', true);
            $(this).selectpicker('refresh');
          } else if (previousValue && previousValue.includes('all_groups')) {
            // If select all was previously clicked and then another option is selected, deselect all options except "Select All Food Groups"
            let nonAllOptions = $(this).find('option').not('[value="all_groups"]');
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
        $('#groupSelect').on('changed.bs.select', function () {
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

    //JavaScript for Showing and Hiding Rows
    html += """
    <script>
      function showHideRow(rowId, element) {
        var row = document.getElementById(rowId);
        if (row.style.display === 'none' || row.style.display === '') {
          row.style.display = 'table-row';
          element.classList.remove('collapsed');
        } else {
          row.style.display = 'none';
          element.classList.add('collapsed');
          }
      }
    </script>
    """;

    return html;
  }

  // Generate the Commodities Table
  public String generateTable(String columnsSelected, ArrayList<FoodLossEvent> foodLossEvents) {
    String html = "";

    if (columnsSelected.equals("")) {
      html += "<tr><th>Commodity Name</th><th>Loss</th></tr></thead><tbody>";
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

  // Headers
  public String generateTableHeaders(String html, String columnsSelected) {
    html += "<tr><th>Commodity Name</th>";
    if (columnsSelected.contains("Activity")) {html += "<th>Activity</th>";}
    if (columnsSelected.contains("Supply")) {html += "<th>Supply Stage</th>";}
    if (columnsSelected.contains("Cause")) {html += "<th>Cause</th>";}
    html += "<th>Loss</th></tr></thead><tbody>";
    return html;
  }

  // Rows
  public String generateTableRows(String html, List<FoodLossEvent> foodLossEvents, String columnsSelected) {
    for (FoodLossEvent event : foodLossEvents) {
      String countryName = event.getCommodityName();
      String activity = event.getActivity();
      String supply = event.getStage();
      String cause = event.getCause();
      String loss = String.format("%.2f", event.getLossPercentage());

      html += "<tr><td>" + countryName + "</td>";
      if (columnsSelected.contains("Activity")) {html += "<td>" + activity + "</td>";}
      if (columnsSelected.contains("Supply")) {html += "<td>" + supply + "</td>";}
      if (columnsSelected.contains("Cause")) {html += "<td>" + cause + "</td>";}
      html += "<td class='loss'>" + loss + "%</td></tr>";
    }
    return html;
  }
}
