package hu.zza.util.gitform;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ResultReportTest {

  Path resources = Path.of("src", "test", "resources", "reportSample");
  ResultReport report = new ResultReport();

  @Test
  void initialisation() {
    assertEquals("-", report.getMainObjective());
    assertFalse(report.isSuccessful());

    report = new ResultReport("A");
    assertEquals("A", report.getMainObjective());
    assertFalse(report.isSuccessful());
  }

  @Test
  void setters() {
    report.setMainObjective("B");
    report.setSuccessful(true);

    assertEquals("B", report.getMainObjective());
    assertTrue(report.isSuccessful());
  }

  @Test
  void appendResult() throws IOException {
    report.appendResult("A");
    report.appendResult("B");
    report.appendResult("C");

    assertEquals(Files.readString(resources.resolve("appendResult.txt")), report.toString());
  }

  @Test
  void appendAdditionalInfo() throws IOException {
    report.appendAdditionalInfo("Zero", "A");
    report.appendAdditionalInfo("One", "B");
    report.appendAdditionalInfo("Two", "C");

    assertEquals(Files.readString(resources.resolve("appendAdditional.txt")), report.toString());
  }

  @Test
  void clear() throws IOException {
    report.setMainObjective("A");
    report.setSuccessful(true);
    report.appendResult("B");
    report.appendAdditionalInfo("C", "D");
    assertEquals(Files.readString(resources.resolve("misc.txt")), report.toString());

    report.clear();
    assertEquals(Files.readString(resources.resolve("empty.txt")), report.toString());
  }
}
