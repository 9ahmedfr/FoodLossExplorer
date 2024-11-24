package app;

import io.javalin.http.Context;
import io.javalin.http.Handler;
//import kotlin.random.Random.Default;

//import javassist.compiler.ast.Variable;

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

public class PageIndex implements Handler {

    // URL of this page relative to http://localhost:7001/
    public static final String URL = "/";
    int defaultStartYear = JDBCConnection.getLowestYear();
    int defaultEndYear = JDBCConnection.getHighestYear();
    int startYear;
    int endYear;
    
    @Override
    public void handle(Context context) throws Exception {


        if (context.formParam("startYearLabel") != null && !context.formParam("startYearLabel").trim().isEmpty()) {
            try {startYear = Integer.parseInt(context.formParam("startYearLabel").trim());
            } catch (NumberFormatException e) {startYear = 0;}
        } else {startYear = 0;}
        
        if (context.formParam("endYearLabel") != null && !context.formParam("endYearLabel").trim().isEmpty()) {
            try {endYear = Integer.parseInt(context.formParam("endYearLabel").trim());
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

        // Create a simple HTML webpage in a String
        String html = "<html>";

        // Add some Header information
        html += "<head>" + 
               "<title>Homepage</title>";

        // Add some CSS (external file)
        html += "<meta charset='UTF-8'> <meta name='viewport' content='width=device-width, initial-scale=1.0'>";
        html += "<link rel='stylesheet' type='text/css' href='index.css' />";
        html += "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css'>";
        html += "<link rel=\"icon\" href=\"FLW.png\" type=\"image/x-icon\">";
        html += "</head>";

        // Add the body
        html += "<body>";

        // Add the topnav
        html += CommonDesign.AddTopnav("/");

        // Add header content block

        FoodLossEvent foodLossEvent = JDBCConnection.getHighestLossForAYearRangeForCommodity(startYear, endYear);

        double highestLossPercentage = foodLossEvent.getLossPercentage();
        String commodityName = foodLossEvent.getCommodityName();
        int highestLossYear = foodLossEvent.getYear();

        html += String.format("""
            <div class='header'>
                <h1>Explore the Impact of Food Loss and Waste</h1>
                <p>Discover comprehensive insights and statistics from %d to %d</p>
                <a href='page2A.html'><button>Start Exploring</button></a>
            </div>
            """, defaultStartYear, defaultEndYear);

        // Add HTML for the page content
        
        html += String.format("""
            <div class='content'>
                <section class="data-snapshot">

                    <div class="snapshot-box">
                        <form method='post' action='/'>
                        <i class="fas fa-calendar-alt calendar-icon"></i>
                        <div class="snapshot-heading">Year Range</div>
                        <div class="range-slider-container">
                            <i class="fa-solid fa-magnifying-glass" style="color:transparent;"></i>
                            <div class="selected-years">
                                <input class="SliderLabel" id="startYearLabel" name="startYearLabel" value='%d' readonly>
                            </div>
                            <div class="range-slider" id="yearRange">
                                <div class="slider-handle" id="startHandle"></div>
                                <div class="slider-handle" id="endHandle"></div>
                            </div>
                            <div class="selected-years">
                                <input class="SliderLabel" id="endYearLabel" name="endYearLabel" value='%d' readonly>
                            </div>
                            <button class="YearButton" type="submit"><i class="fa-solid fa-magnifying-glass"></i></button>
                        </div>
                        </form>
                    </div>

                    <div class="snapshot-box">
                        <i class="fas fa-exclamation-triangle warning-icon"></i>
                        <div class="snapshot-heading">Highest Loss</div>
                        <p>%.1f%% in %d</p>
                    </div>

                    <div class="snapshot-box">
                        <i class="fas fa-leaf commodity-icon"></i>
                        <div class="snapshot-heading">Affected Commodity</div>
                        <p>%s</p>
                    </div>

                </section>
            """,startYear, endYear, highestLossPercentage, highestLossYear, commodityName);

        
        html += """
            <section class="introduction">

            <h2>Topics Covered</h2>
            
            <p>Food Loss, Food Waste, Global Impact, and More</p>
            
            <div class="quick-links">
                <a href='page2A.html'><button>Country Statistics</button></a>
                <a href='page3A.html'><button>Similar Countries</button></a>
                <a href='page2B.html'><button>Food Statistics</button></a>
                <a href='page3B.html'><button>Similar Foods</button></a>
            </div>

            </section>
                """;

        // Close Content div
        html += "</div>";

        // Footer
        //html += CommonDesign.AddFooter();
        

        html += String.format("""
            <script>
                const yearRange = document.getElementById('yearRange');
                const startHandle = document.getElementById('startHandle');
                const endHandle = document.getElementById('endHandle');
                const startYearLabel = document.getElementById('startYearLabel');
                const endYearLabel = document.getElementById('endYearLabel');

                const minYear = %d;
                const maxYear = %d;
                const totalYears = maxYear - minYear;
                const rangeWidth = yearRange.offsetWidth - startHandle.offsetWidth;

                let isDraggingStart = false;
                let isDraggingEnd = false;

                function yearToPosition(year) {
                    return ((year - minYear) / totalYears) * rangeWidth;
                }

                function positionToYear(position) {
                    return Math.round(minYear + (position / rangeWidth) * totalYears);
                }

                function setHandlePositions() {
                    const startYear = parseInt(startYearLabel.value, 10);
                    const endYear = parseInt(endYearLabel.value, 10);

                    const startPosition = yearToPosition(startYear);
                    const endPosition = yearToPosition(endYear);

                    startHandle.style.left = `${startPosition}px`;
                    endHandle.style.left = `${endPosition}px`;
                }

                startHandle.addEventListener('mousedown', () => {
                    isDraggingStart = true;
                });

                endHandle.addEventListener('mousedown', () => {
                    isDraggingEnd = true;
                });

                document.addEventListener('mousemove', (e) => {
                    if (isDraggingStart) {
                        const posX = e.clientX - yearRange.getBoundingClientRect().left;
                        const newPos = Math.min(Math.max(0, posX), rangeWidth);
                        startHandle.style.left = `${newPos}px`;
                        updateYears();
                    } else if (isDraggingEnd) {
                        const posX = e.clientX - yearRange.getBoundingClientRect().left;
                        const newPos = Math.min(Math.max(0, posX), rangeWidth);
                        endHandle.style.left = `${newPos}px`;
                        updateYears();
                    }
                });

                document.addEventListener('mouseup', () => {
                    isDraggingStart = false;
                    isDraggingEnd = false;
                });

                function updateYears() {
                    const startYear = positionToYear(startHandle.offsetLeft);
                    const endYear = positionToYear(endHandle.offsetLeft);

                    startYearLabel.value = startYear;
                    endYearLabel.value = endYear;
                }

                setHandlePositions(); // Set handle positions based on user input on page load
            </script>
        """, defaultStartYear, defaultEndYear);


        // Finish the HTML webpage
        html += "</body>" + "</html>";


        // DO NOT MODIFY THIS
        // Makes Javalin render the webpage
        context.html(html);
    }
}