package app;

public class CommonDesign {

  public static String AddTopnav(String title) {
    // Concatenate strings using + for Java versions that don't support multi-line strings with triple quotes
    String html = "";

    String makeBold = " style='font-weight: bold; color:wheat;'";

    html += "<div class='topnav' style='position: sticky; top: 0; z-index: 300;'>"
          + "  <div class='logo'><a href='/'>Food Loss and Waste Explorer</a></div>"
          + "  <nav>"
          + "      <ul>"
          + "          <li><a href='/'" + (title.equals("/") ? makeBold: "") + ">Home</a></li>"
          + "          <li><a href='mission.html'" + (title.equals("mission.html") ? makeBold : "") + ">About</a></li>"
          + "          <li><a href='page2A.html'" + (title.equals("page2A.html") ? makeBold: "") + ">Country</a></li>"
          + "          <li><a href='page3A.html'" + (title.equals("page3A.html") ? makeBold: "") + ">Similar Country</a></li>"
          + "          <li><a href='page2B.html'" + (title.equals("page2B.html") ? makeBold: "") + ">Food</a></li>"
          + "          <li><a href='page3B.html'" + (title.equals("page3B.html") ? makeBold: "") + ">Similar Food</a></li>"
          + "      </ul>"
          + "  </nav>"
          + "</div>";

    return html;
}

  public static String AddFooter() {
    String html = "";
    html = html + """
      <div class='footer'>
        <p>COSC2803 Studio Project <em>Team 105</em></p>
      </div>
    """;

    return html;
  }

}
