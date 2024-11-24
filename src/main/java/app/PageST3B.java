package app;

import java.util.ArrayList;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import helper.SimilarFoodResult;


public class PageST3B implements Handler {

  // URL of this page relative to http://localhost:7001/
  public static final String URL = "/page3B.html";
  String[] selectedCommodities;
  int numberOfResults;
  String sort = "DESC";
  String similarity = "highest";

  @Override
  public void handle(Context context) throws Exception {

    // For parameters

    // Get the sort option
    if (context.formParam("sortOption") == null) {sort = "DESC";}
    else {sort = context.formParam("sortOption");}

    // Get the selected commodity
    selectedCommodities = context.formParams("commodities[]").toArray(new String[0]);

    // Number of Results
    if (context.formParam("numberOfResults") != null && !context.formParam("numberOfResults").trim().isEmpty()) {
        try {numberOfResults = Integer.parseInt(context.formParam("numberOfResults").trim());
        } catch (NumberFormatException e) {numberOfResults = 5;}
    } else {numberOfResults = 5;}

    // Get the similarity criteria
    if (context.formParam("option") == null) {similarity = "highest";}
    else {similarity = context.formParam("option");}

    // Webpage HTML

    String html = "<html>";

    html += "<head>" + "<title>Similar Food</title>";
    html += "<meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>";
    html += "<link rel=\"icon\" href=\"FLW.png\" type=\"image/x-icon\">";

    // CSS
    html = addStyleSheet(html);

    html += "</head><body>";

    html += "<form action='page3B.html' method='post' id='mainForm'>";

    // topnav
    html += CommonDesign.AddTopnav("page3B.html");

    // Header
    html += """
    <div class= "page-content">
      <section class="content-header">
        <h2 class="content-header_title">Similar Food Groups</h2>
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

    // Commodity selection
    html +=  """
    <section class="box">
      <div class="Commodities_box"><h2 class="box_title">Locations</h2>
        <div class="Refresh" title="Resubmit">
          <button class="CommodityButton" type="submit">
            <i class="fa-solid fa-rotate-right"></i></div>
          </button>
        </div>
        <div class="container mt-5">
          <select class="selectpicker" data-live-search="true" data-live-search-normalize="true"
          id="commoditySelect" name="commodities[]" data-none-selected-text="Select a Commodity">
          <option value="" disabled selected>Select a Commodity</option>
    """;

    //Keeping already selected commodities selected

    ArrayList<String> commodityNames = JDBCConnection2.getAllCommodities();

    String SelectedCommodity = "";

    if (!(selectedCommodities.length == 0)) {SelectedCommodity = selectedCommodities[0];}

    for (String commodity : commodityNames) {
        String optionHtml = "<option value='" + escapeSingleQuotesForHtml(commodity) + "' "
        + " data-tokens='" + escapeSingleQuotesForHtml(commodity) + "'";

        if (SelectedCommodity.equals(commodity)) {
            optionHtml += " selected";
        }
        optionHtml += ">" + commodity + "</option>";
        html += optionHtml;
    }

    html += """
        </select>
      </div>
    </section>
    """;

    //Similarity Criteria
    html += addSimilarityCriteriaSection(similarity);

    //Number of Results
    html += """
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

    // Sumbit button
    html += """
      <section class="box" style="text-align: center; padding: 0px;">
        <button class="submitbutton" type="submit">Show Results</button>
      </section>
    </aside>
    """;


    // Main Content

    String belongsTo = "";

    if (selectedCommodities.length != 0) {
      belongsTo = "<em>" + SelectedCommodity + "</em>&nbspbelongs to&nbsp<em>" + JDBCConnection2.getACommoditysGroup(SelectedCommodity) + "</em>";
    }

    html += String.format("""
    <section class="content">
      <div class="main-content">
        <div class="results-top">
          <div class="results">
            <h3 class="results_title">Results</h3>
            <p class="results_found">%s</p>
          </div>
    """, belongsTo);

    // Sorting
    html += addSortSection(sort);

    String type = "";
    if (similarity.equals("highest")) {
      type = "Highest Loss";
    } else {
      type = "Lowest Loss";
    }

    // Results Table
    html += String.format("""
    <div class="table">
      <table class="table_content">
        <thead>
          <tr>
            <th>Group Name</th>
            <th>%s</th>
            <th>Similarity Score</th>
          </tr>
        </thead>
        <tbody>
    """, type);

    ArrayList<SimilarFoodResult> sortedGroups = JDBCConnection2.getClosestGroups(SelectedCommodity, similarity, numberOfResults, sort);

    if (sortedGroups.size() == 0) {
      html += """
        <tr style="background-color:whitesmoke;">
          <td colspan=5 style="font-weight:bold; color:#939393; text-align:center; font-size:25px; padding:215px;"> No Food Group Selected </td>
        </tr>
      """;
    }

    // Adding table rows
    for (SimilarFoodResult group : sortedGroups) {
      html += "<tr>";
      html += "<td>" + group.getGroupName() + "</td>";
      html += "<td class='num'>" + group.getLossValue() + "</td>";
      html += "<td class='num'>" + group.getSimilarity() + "</td>";
      html += "</tr>";
    }


    html += "</tbody></table></div>";

    html += "</div></section></main>";

    // Add the JavaScript
    html += addJavaScript();

    html += addJavaScript2();

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
    html += "<link rel='stylesheet' type='text/css' href='Task3B/task3b.css' />";
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

  // Add Sort Section
  public String addSortSection(String sort) {
    String html = "";

    html += String.format("""
        <div class="sort-by">
          <label class="sort-by_label" title="Sort"><span style = "font-weight:bold; color:#38414b; font-size:17px;">Sort By</span>
            <select id="sortSelect" class="sort-by_select" name="sortOption" onchange="this.form.submit()">
            <option value="DESC" %s>Similarity Score: ↓</option>
              <option value="ASC" %s>Similarity Score: ↑</option>
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

  // Similarity Criteria section
  public String addSimilarityCriteriaSection(String similarity) {
    String html = String.format("""
      <section class="box">
        <h2 class="box_title">Similarity Criteria</h2>
        <div class="filters">
          <label class="similaritySelect">
            <select class="options" name="option">
              <option value="highest" %s>Highest Loss</option>
              <option value="lowest" %s>Lowest Loss</option>
            </select>
          </label>
        </div>
      </section>
      """, similarity.equals("highest") ? "selected" : "", similarity.equals("lowest") ? "selected" : ""
    );

    html += """
    <script>
      document.getElementById('mainForm').addEventListener('submit', function(event) {
        const commoditySelect = document.getElementById('commoditySelect');
        const selectedCommodities = Array.from(commoditySelect.selectedOptions).map(option => option.value);
        const similaritySelect = document.querySelector('.similaritySelect .options');
        const selectedSimilarity = similaritySelect.value;
      });
    </script>
    """;

    return html;
  }




  // NOT DONE CHANGEING (everything below)


  /*************************************************** Add the JavaScript ******************************************************/

  public String addJavaScript2() {

      String html = "";

      // Add the Bootstrap JS and Bootstrap Select JS for the Country Selection
      html += """

          <script>
              $(document).ready(function() {
                  // Initialize the selectpicker
                  $('#commoditySelect').selectpicker();

                  // Store the initial order of the options without the placeholder
                  let initialOrder = $('#commoditySelect option').not('[value=""]').clone();

                  // Flag to track if the placeholder has been removed
                  let placeholderRemoved = false;

                  // Function to move selected option to the top and remove placeholder if needed
                  function moveSelectedOptionToTop() {
                      let $select = $('#commoditySelect');
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
                      let $select = $('#commoditySelect');
                      let selectedOption = $select.find('option:selected');
                      $select.html(initialOrder.clone());

                      if (selectedOption.length > 0) {
                          $select.find(`option[value="${selectedOption.val()}"]`).remove();
                          $select.prepend(selectedOption);
                      }

                      $select.selectpicker('refresh');
                  }

                  // Move selected option to the top after selection
                  $('#commoditySelect').on('changed.bs.select', function(e, clickedIndex, isSelected, previousValue) {
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
