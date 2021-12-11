package hu.zza.util.gitform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GitFormTest {

  private static final Path resourcesPath = Path.of("src", "test", "resources");
  private static PrintStream originalPrintStream;
  private final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
  private String expectedOutput;

  @BeforeAll
  static void beforeAll() {
    originalPrintStream = System.out;
  }

  @BeforeEach
  void setup() {
    System.setOut(new PrintStream(byteStream));
  }

  @AfterAll
  static void afterAll() {
    System.setOut(originalPrintStream);
  }

  @Test
  void runEmpty() {
    assertDoesNotThrow(() -> GitForm.main(new String[] {}));
    updateExpectedOutputFromFile("help.txt");
    assertWholeOutputMatches();
  }

  private void updateExpectedOutputFromFile(String filename) {
    try {
      expectedOutput = Files.readString(resourcesPath.resolve(filename));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void assertWholeOutputMatches() {
    assertEquals(expectedOutput, byteStream.toString());
  }

  @Test
  void runLoad() {
    assertDoesNotThrow(() -> GitForm.main(new String[] {"load"}));
    updateExpectedOutputFromFile("load.txt");
    assertSubstringOfOutputMatches(0, 1);
  }

  private void assertSubstringOfOutputMatches(int begin, int end) {
    if (expectedOutput.length() < end) {
      fail(
          String.format(
              "expectedOutput it too short: %d [substring: %d - %d]",
              expectedOutput.length(), begin, end));
    }

    if (byteStream.toString().length() < end) {
      fail(
          String.format(
              "byteStream it too short: %d [substring: %d - %d]",
              byteStream.toString().length(), begin, end));
    }

    assertEquals(expectedOutput.substring(begin, end), byteStream.toString().substring(begin, end));
  }

  @Test
  void runSave() {
    assertDoesNotThrow(() -> GitForm.main(new String[] {"save"}));
    updateExpectedOutputFromFile("save.txt");
    assertSubstringOfOutputMatches(0, 72);
  }
}
