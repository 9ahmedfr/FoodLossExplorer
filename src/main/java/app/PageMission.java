package app;

import java.util.ArrayList;

import helper.Student;

import helper.persona;
import helper.personaAttribute;
import io.javalin.http.Context;
import io.javalin.http.Handler;


public class PageMission implements Handler {

  // URL of this page relative to http://localhost:7001/
  public static final String URL = "/mission.html";

  @Override
  public void handle(Context context) throws Exception {
    String html = "<html>";

    html += "<head>" +
      "<title>Our Mission</title>";

    // CSS
    html += "<link rel='stylesheet' type='text/css' href='mission.css' />";
    html += "<link rel='stylesheet' href='https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.0.0-beta3/css/all.min.css'>";
    html += "<link rel=\"icon\" href=\"FLW.png\" type=\"image/x-icon\">";
    html += "</head>";

    html += "<body>";

    html += CommonDesign.AddTopnav("mission.html");

    // Header content block and mission statement
    html += String.format("""
      <div class='header'>
        <h1>Our Mission</h1>
        <p>We empower people and organizations globally to make informed decisions for food security and sustainability.<br>We do this by offering clear data, effective tools, and practical insights to tackle food loss and waste.</p>
      </div>
    """, JDBCConnection.getLowestYear(), JDBCConnection.getHighestYear());

    // Content

    html += """
      <div class='content'>
        """;

    // Persona section
    html += "<section class='container'>";

    html += """
      <div class = 'p-section'>
        <div class='content-heading-box'>
          <div class='content-heading'>
            <i class="fa-solid fa-user"></i>
          </div>
          <div class='content-heading'>
            <h3>Target personas</h3>
          </div>
        </div>
    """;

    // Add each persona from database
    html += "<div class='p-placement'>";
    for (persona p : JDBCConnection2.getPersonas()) {
      html += "<details class='persona'>";
      html += "<summary>";
      // html += "<slot name='summarySlot'>Details</slot>";
      html += "<img src='" + p.img + "' alt='Headshot of " + p.name + "'>";
      html += "<p>" + p.name;

      ArrayList<personaAttribute> attributes = JDBCConnection2.getAttributes(p.name);
      for (personaAttribute a : attributes) {
        if (a.type.equals("age")) {
          html += ", " + a.description + " ";
        }
      }

      html += "</p>";
      html += "</summary>";

      html += "<div class='persona-details'>";
      html += "<p>";
      for (personaAttribute a : attributes) {
        if (a.type.equals("occupation")) {
          html += a.description;
        }
      }
      html += "</p>";
      html += "<h4>Needs</h4>";
      html += "<ul>";
      for (personaAttribute a : attributes) {
        if (a.type.equals("need")) {
          html += "<li>" + a.description + "</li>";
        }
      }
      html += "</ul>";
      html += "<h4>Goals</h4>";
      html += "<ul>";
      for (personaAttribute a : attributes) {
        if (a.type.equals("goal")) {
          html += "<li>" + a.description + "</li>";
        }
      }
      html += "</ul>";

      html += "<h4>Skills & experience</h4>";
      html += "<ul>";
      for (personaAttribute a : attributes) {
        if (a.type.equals("skill")) {
          html += "<li>" + a.description + "</li>";
        }
      }
      html += "</ul>";

      html += "</div>"; // Close 'persona-details'
      html += "</details>"; // Close 'persona'
    }

    html += "</div>"; // Close 'p-placement'
    html += "</div>"; // Close 'p-section'

    // Team member section
    html += """
      <div class = 'tm-section'>
        <div class='content-heading-box'>
          <div class='content-heading'>
            <i class="fa-solid fa-user-tie"></i>
          </div>
          <div class='content-heading'>
            <h3>Our team members</h3>
          </div>
        </div>
    """;

    // Add each team member from database
    for (Student teamMember : JDBCConnection2.getTeamMembers()) {
      html += "<div class='team-member'>";
      html += "<p><a href='" + teamMember.link + "' class='team-link' target='_blank'>" + teamMember.name + "</a> (s" + teamMember.ID + ")</p>";
      html += "</div>";
    }

    // Close tm-section and container
    html += """
        </div>
      </section>
    """;

    // End 'content'
    html += """
      </div>
      """;

    html += "</body>" + "</html>";

    // DO NOT MODIFY THIS
    // Makes Javalin render the webpage
    context.html(html);
  }

}
