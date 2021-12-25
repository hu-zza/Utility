package hu.zza.util.gitform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface YamlReader {
  /**
   * Parses a YAML file as a simple dictionary of key-value pairs
   *
   * @param yamlFile the {@link Path path} of the YAML file to parse
   * @return a {@link Map<String,String> map}
   * @throws IOException if {@code Files.lines(yamlFile)} throws
   */
  static Map<String, String> parse(Path yamlFile) throws IOException {
    try (Stream<String> lines = Files.lines(yamlFile)) {
      return parse(lines);
    }
  }

  /**
   * Parses a stream of strings.
   *
   * @param stringEntries {@link Stream<String>} of {@link String strings} in a "key: value" format
   * @return a {@link Map<String,String> map}
   */
  static Map<String, String> parse(Stream<String> stringEntries) {
    return stringEntries
        .map(line -> line.split(": ", 2))
        .filter(arr -> arr.length == 2)
        .map(arr -> Map.entry(arr[0], arr[1]))
        .collect(Collectors.toMap(Entry::getKey, Entry::getValue));
  }
}
