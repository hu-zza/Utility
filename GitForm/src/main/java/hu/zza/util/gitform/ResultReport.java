package hu.zza.util.gitform;

import java.util.HashMap;
import java.util.Map;

public class ResultReport {
  private String mainObjective = "-";
  private boolean successful = false;
  private final Map<String, StringBuilder> additionalInfo = new HashMap<>();

  public ResultReport() {
    this("");
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
   * Appends the {@code value} as a line of information to the section {@code key}.
   *
   * @param key the title of the section of {@code additionalInfo}
   * @param value one line of additional information
   */
  public void appendAdditionalInfo(String key, String value) {
    additionalInfo.computeIfAbsent(key, k -> new StringBuilder());
    additionalInfo.computeIfPresent(key, (k, v) -> v.append(String.format("\t%s%n", value)));
  }

  // TODO: JavaDoc
  /** Prints additional information about the running of {@link ProjectBuilder#load()}. */
  public void print() {
    System.out.println(this);
  }

  // TODO: JavaDoc
  @Override
  public String toString() {
    return String.format(
        "[%s] %s%n%n%s%n%n",
        successful ? "done" : "fail", mainObjective, getAdditionalInfoAsString());
  }

  private String getAdditionalInfoAsString() {
    StringBuilder stringBuilder = new StringBuilder("ADDITIONAL INFO: ");

    if (additionalInfo.isEmpty()) {
      stringBuilder.append("-");
    } else {
      additionalInfo.forEach((k, v) -> stringBuilder.append(String.format("%n%s%n%n%s%n%n", k, v)));
    }
    return stringBuilder.toString();
  }

  public void clear() {
    mainObjective = "-";
    successful = false;
    additionalInfo.clear();
  }
}
