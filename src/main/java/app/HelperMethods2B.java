package app;

import java.util.ArrayList;
import java.util.List;

public class HelperMethods2B {
    
    private static int idCounter = 0;

    public static synchronized int generateUniqueId() {
        idCounter++;
        return idCounter;
    }

    public static String escapeSingleQuotesSQL(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("'", "''");
    }

    public static String escapeSingleQuotesWithBackslash(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("'", "\\'");
    }

    public static String escapeSingleQuotesForHtml(String input) {

        if (input == null) {
            return null;
        }
        return input.replace("'", "&#39;");
        
    }

    public static String addStyleSheet(String html) {

        html += "<link rel='stylesheet' type='text/css' href='task2a.css' />";
        html += "<link rel='stylesheet' type='text/css' href='dropdown.css' />";
        html += "<link rel='stylesheet' type='text/css' href='dropdown2.css' />";

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

    public static String addColumnFilters(Boolean isCheckedActivity, Boolean isCheckedSupply, Boolean isCheckedCause) {

        String html = "";

        html +=  """
            <section class="box">
            <h2 class="box__title">Show Columns</h2> """;

        html += String.format("""

            <div class="filters">    
                <label class="form__label">  
                    <input type="checkbox" name="activity" value="activity" %s> <span>Activity</span>
                </label>
                <label class="form__label">
                    <input type="checkbox" name="supply" value="supply" %s> <span>Supply</span>
                </label>
                <label class="form__label">
                    <input type="checkbox" name="cause" value="cause" %s> <span>Cause</span>
                </label>
            </div> 

        """, isCheckedActivity ? "checked" : "", isCheckedSupply ? "checked" : "", isCheckedCause ? "checked" : "");

        html += "</section>";

        return html;
    }
    
    public static String addYearRangeSection(int startYear, int endYear, boolean yearsInBetween) {
        String html = """
            <section class="box">
                <h2 class="box__title">Year Range</h2>
                <div class="filters">
                <div class="input-field">
                    <label class="form__label">
                    Start Year: <input type="number" name="start-year" value="%d">
                    </label>
                </div>
                <div class="input-field">
                    <label class="form__label">
                    End Year: <input type="number" name="end-year" value="%d">
                    </label>
                </div>
                <label class="form__label1">
                    <input type="checkbox" name="years-inbetween" %s> Years in between
                </label>
                </div>
            </section>
            <form>
        </aside>
            """.formatted(startYear, endYear, yearsInBetween ? "checked" : "");
    
        return html;
    }
    public static String addSortSection(String sort) {
        
        String html = "";
        
        html += String.format("""

                    <section class="content">
                        <div class="main-content">
                            <div class="results-top">
                                <div class="results">
                                    <h3 class="results__title">Results</h3>
                                </div>
                                <div class="sort-by">
                                    <label class="sort-by__label"><button class="sort-button" type="submit">Sort</button>
                                        <select id="sortSelect" class="sort-by__select" name="sortOption">
                                            <option value="default" %s>Default</option>
                                            <option value="SL" %s>Start Year Loss: Low to High</option>
                                            <option value="SH" %s>Start Year Loss: High to Low</option>
                                            <option value="EL" %s>End Year Loss: Low to High</option>
                                            <option value="EH" %s>End Year Loss: High to Low</option>
                                        </select>
                                    </label>
                                </div>
                            </div>

                        """, 
                        sort.equals("default") ? "selected" : "", 
                        sort.equals("SL") ? "selected" : "", 
                        sort.equals("SH") ? "selected" : "", 
                        sort.equals("EL") ? "selected" : "", 
                        sort.equals("EH") ? "selected" : "");

        return html;
    }

    public static String addJavaScript() {

        String html = "";
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
                        let selectedOptions = $select.find('option:selected');
                        let nonSelectedOptions = $select.find('option:not(:selected)');

                        $select.html(''); // Clear the select options

                        // Sort selected options in ascending order
                        selectedOptions.sort(function(a, b) {
                            return a.value.localeCompare(b.value);
                        });

                        // Append sorted selected options at the top
                        selectedOptions.each(function() {
                            $select.append($(this));
                        });

                        // Append non-selected options after
                        nonSelectedOptions.each(function() {
                            $select.append($(this));
                        });

                        // Refresh the selectpicker to reflect changes
                        $select.selectpicker('refresh');
                    }

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

    public static String generateTableHeaders(String html, String columnsSelected) {
        
        html += "<tr><th>Food</th>";
        if (columnsSelected.contains("Activity")) {html += "<th>Activity</th>";}
        if (columnsSelected.contains("Supply")) {html += "<th>Supply</th>";}
        if (columnsSelected.contains("Cause")) {html += "<th>Cause</th>";}
        html += "<th>Loss</th></tr></thead><tbody>";
        return html;
    }

    public static String generateTableRows(String html, List<FoodLossEvent> foodLossEvents, String columnsSelected) {
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

    public static String generateTable(String columnsSelected, ArrayList<FoodLossEvent> foodLossEvents) {
        
        String html = "";

        if (columnsSelected.equals("")) {
            html += "<tr><th>Food</th><th>Loss</th></tr></thead><tbody>";
            for (FoodLossEvent event : foodLossEvents) {
                html += "<tr><td>" + event.getCommodityName() + "</td><td class='loss'>" + String.format("%.2f", event.getLossPercentage()) + "%</td></tr>";
            }
        } else {
            html = generateTableHeaders(html, columnsSelected);
            html = generateTableRows(html, foodLossEvents, columnsSelected);
        }
        return html;
    }
}
