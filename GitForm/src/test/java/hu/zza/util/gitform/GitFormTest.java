package hu.zza.util.gitform;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GitFormTest {

  private static final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
  private static final PrintStream mockPrintStream = new PrintStream(byteStream);
  private static final Path resourcesPath = Path.of("src", "test", "resources");
  private static PrintStream originalPrintStream;

  @BeforeAll
  static void beforeAll() {
    originalPrintStream = System.out;
    System.setOut(mockPrintStream);
  }

  @AfterAll
  static void afterAll() {
    System.setOut(originalPrintStream);
  }

  @Test
  void runEmpty() throws IOException {
    assertDoesNotThrow(() -> GitForm.main(new String[] {}));
    String expectedOutput = Files.readString(resourcesPath.resolve("help.txt"));
    assertEquals(expectedOutput, byteStream.toString());
  }

  @Test
  void runLoad() {
    assertDoesNotThrow(() -> GitForm.main(new String[] {"load"}));
  }

  @Test
  void runSave() {
    assertDoesNotThrow(() -> GitForm.main(new String[] {"save"}));
  }
}
