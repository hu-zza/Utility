package hu.zza.util.gitform;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class ResultReport {
  private final SortedSet<String> mainResults = new TreeSet<>();
  private final Map<String, SortedSet<String>> additionalInfo = new HashMap<>();
  private String mainObjective = "-";
  private boolean successful = false;

  public ResultReport() {
    this("-");
  }

  public ResultReport(String mainObjective) {
    this.mainObjective = mainObjective;
  }

  public String getMainObjective() {
    return mainObjective;
  }

  public void setMainObjective(String mainObjective) {
    this.mainObjective = mainObjective;
  }

  public boolean isSuccessful() {
    return successful;
  }

  public void setSuccessful(boolean successful) {
    this.successful = successful;
  }

  /**
   * Appends {@code result} to the main list.
   *
   * @param result one line of cardinal information
   */
  public void appendResult(String result) {
    mainResults.add(result);
  }

  /**
   * Appends the {@code info} to the {@code section}.
   *
   * @param section the title of the section
   * @param info one line of additional information
   */
  public void appendAdditionalInfo(String section, String info) {
    additionalInfo.computeIfAbsent(section, k -> new TreeSet<>());
    additionalInfo.get(section).add(String.format("\t\t- %s%n", info));
  }

  /**
   * Prints {@link ResultReport} to the console as a formatted summary given by {@link
   * ResultReport#toString()}.
   */
  public void print() {
    System.out.println(this);
  }

  /**
   * Returns the {@link String} representation of {@link ResultReport} as a formatted, multi-line
   * summary. The aim is to provide a nice, human-readable text ready to print out to the console or
   * concatenate with other formatted outputs.
   *
   * @return the formatted summary of {@link ResultReport}
   */
  @Override
  public String toString() {
    return String.format(
        "[%s] %s%n%n%S%n%s%n%n%S%n%s%n",
        successful ? "done" : "fail",
        mainObjective,
        "Result",
        getMainResultsAsString(),
        "Additional info",
        getAdditionalInfoAsString());
  }

  /** @return a multi-line {@link String} contains the whole {@code mainResults} list */
  private String getMainResultsAsString() {

    if (mainResults.isEmpty()) {
      return "\t- No result" + System.lineSeparator();
    }
    return mainResults.stream()
        .map(s -> "\t- " + s + System.lineSeparator())
        .collect(Collectors.joining());
  }

  /** @return a multi-line {@link String} contains the whole {@code additionalInfo} map */
  private String getAdditionalInfoAsString() {

    if (additionalInfo.isEmpty()) {
      return "\t- No additional info" + System.lineSeparator();
    }
    var stringBuilder = new StringBuilder();
    additionalInfo.entrySet().stream()
        .sorted(Entry.comparingByKey())
        .forEach(
            e ->
                stringBuilder.append(
                    String.format("%n\t%s%n%s", e.getKey(), String.join("", e.getValue()))));
    return stringBuilder.toString();
  }

  public void clear() {
    mainObjective = "-";
    successful = false;
    mainResults.clear();
    additionalInfo.clear();
  }
}
