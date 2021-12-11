package hu.zza.util.gitform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Simple representation of a GitHub project ({@code originUrl}) with an arbitrary project name
 * ({@code projectName}) and its local {@link Path path} ({@code projectRoot}) relative to the
 * user's local GitHub root folder.
 */
public class GitHubProject {
  private static final UnaryOperator<String> nameExtractor =
      gitHubUrl -> gitHubUrl.substring(gitHubUrl.lastIndexOf('/') + 1, gitHubUrl.length() - 4);

  private final String projectName;
  private final Path projectRoot;
  private final String originUrl;

  public GitHubProject(Path projectRoot, String originUrl) {
    this(nameExtractor.apply(originUrl), projectRoot, originUrl);
  }

  public GitHubProject(String projectName, Path projectRoot, String originUrl) {
    this.projectName = projectName.strip();
    this.projectRoot = projectRoot;
    this.originUrl = originUrl.strip();
  }

  /**
   * Parses a YAML file with a dictionary of at least these keys: name, local, origin
   *
   * @param projectInfoFile the {@link Path path} of the YAML file to parse
   * @return {@link GitHubProject}
   * @throws IOException if {@code Files.lines(projectInfoFile)} throws
   */
  public static GitHubProject parse(Path projectInfoFile) throws IOException {
    try (Stream<String> lines = Files.lines(projectInfoFile)) {
      return parse(lines);
    }
  }

  /**
   * Parse a {@link GitHubProject} from a stream of strings. The necessary keys are: name, local,
   * origin
   *
   * @param stringEntries {@link Stream<String>} of {@link String strings} in a "key: value" format
   * @return {@link GitHubProject}
   */
  public static GitHubProject parse(Stream<String> stringEntries) {
    var map =
        stringEntries
            .map(line -> line.split(": ", 2))
            .filter(arr -> arr.length == 2)
            .map(arr -> Map.entry(arr[0], arr[1]))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    return new GitHubProject(map.get("name"), Path.of(map.get("local")), map.get("origin"));
  }

  /**
   * Exports the current instance of {@link GitHubProject} ready to write out with {@link
   * Files#write(Path, Iterable, OpenOption...)}
   *
   * @return {@link List <String>} with every field in a simple YAML dictionary format: "key: value"
   */
  public List<String> getExportList() {
    return List.of("name: " + projectName, "local: " + projectRoot, "origin: " + originUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(projectRoot);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GitHubProject that = (GitHubProject) o;
    return Objects.equals(projectRoot, that.projectRoot);
  }

  public String getProjectName() {
    return projectName;
  }

  public Path getProjectRoot() {
    return projectRoot;
  }

  public String getOriginUrl() {
    return originUrl;
  }
}
