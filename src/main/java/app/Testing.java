package app;

import java.util.HashSet;
import java.util.Set;


public class Testing {
    /**
     * Escapes single quotes in the given string by replacing each single quote with two single quotes.
     * This is useful for preparing strings for SQL queries.
     *
     * @param input The input string to escape.
     * @return The escaped string with single quotes properly escaped.
     */
    public static String escapeSingleQuotes(String input) {
        if (input == null) {
            return null;
        }
        return input.replace("'", "''");
    }

    /**
     * Creates a set of country names with single quotes escaped.
     *
     * @param selectedCountries The array of country names to be processed.
     * @return A set of country names with single quotes escaped.
     */
    public static Set<String> createEscapedCountrySet(String[] selectedCountries) {
        Set<String> escapedCountrySet = new HashSet<>();
        for (String country : selectedCountries) {
            escapedCountrySet.add(country);
        }
        return escapedCountrySet;
    }

    public static void main(String[] args) {
        String[] selectedCountries = {"Côte d'Ivoire", "O'Connor", "New Zealand"};
        Set<String> selectedCountrySet = createEscapedCountrySet(selectedCountries);

        // Print the set to see the escaped country names
        for (String country : selectedCountrySet) {
            System.out.println(country);
        }
    }
}
