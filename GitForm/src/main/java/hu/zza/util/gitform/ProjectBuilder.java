package hu.zza.util.gitform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * An instance of this class is used to synchronize* GitHub projects according to parsable GitForm
 * YAML files representing {@link GitHubProject projects}. These files can be made by {@link
 * ProjectMapper#save()}.
 *
 * <p>* Synchronization means in this case the building of the folder structure (relative to {@code
 * gitRoot}) and the cloning of the GitHub repository. The real sync is on the backlog.
 */
public class ProjectBuilder {
  private static final Runtime runtime = Runtime.getRuntime();
  private static final Predicate<Path> isYaml = p -> p.toString().endsWith(".yaml");
  private final Path gitRoot;
  private final Path gitFormRoot;
  private final Map<String, Future<Boolean>> results = new HashMap<>();
  private final Supplier<Boolean> isLoadCompleted =
      () -> results.values().stream().allMatch(Future::isDone);

  public ProjectBuilder(Path gitRoot, Path gitFormRoot) {
    this.gitRoot = gitRoot;
    this.gitFormRoot = gitFormRoot;
  }

  /**
   * Pulls every found, non-existent project and builds the proper folder structure for them. First,
   * it tries to parse every YAML file in {@code gitFormRoot} to {@link GitHubProject}. Then it
   * filters out the parsing errors (nulls) and the rest are processed by {@link
   * ProjectBuilder#cloneIfAbsent(GitHubProject)}.
   */
  public void load() {
    try (Stream<Path> files = Files.list(gitFormRoot)) {
      results.clear();
      files
          .filter(isYaml)
          .map(this::parseProjectFile)
          .filter(Objects::nonNull)
          .forEach(this::cloneIfAbsent);
      printResults();
    } catch (IOException e) {
      System.err.println("Cannot load from origin:");
      e.printStackTrace();
      System.err.println();
    }
  }

  /**
   * @param projectFile {@link Path} of a YAML file with a dictionary of at least these keys: name,
   *     local, origin
   * @return {@link GitHubProject}, or {@code null} if {@link IOException} occurs during file
   *     handling
   */
  private GitHubProject parseProjectFile(Path projectFile) {
    try {
      return GitHubProject.parse(projectFile);
    } catch (IOException ignored) {
      return null;
    }
  }

  /**
   * If the given GitHub project ({@link GitHubProject#getOriginUrl()}) doesn't exist at the
   * specific location ({@link GitHubProject#getProjectRoot()}) it tries to clone. The project
   * location is always relative to the {@code gitRoot}. If the parent directories don't exist, it
   * creates them first.
   *
   * @param project the instance of {@link GitHubProject} representing a GitHub project to clone
   */
  private void cloneIfAbsent(GitHubProject project) {
    try {
      Path rootPath = gitRoot.resolve(project.getProjectRoot());
      if (Files.notExists(rootPath)) {

        Files.createDirectories(rootPath.getParent());

        results.put(
            String.format("Project %%s:%n%s%n%n", project.getProjectRoot()),
            runtime
                .exec(String.format("git clone %s %s", project.getOriginUrl(), rootPath))
                .onExit()
                .thenApply(f -> f.exitValue() == 0));
      }
    } catch (IOException e) {
      System.err.printf(
          "Cannot clone project %s%nOrigin: %s%n%n",
          project.getProjectRoot(), project.getOriginUrl());
      e.printStackTrace();
      System.err.println();
    }
  }

  /** Prints the results of {@link ProjectBuilder#load()} according to {@code results} Map. */
  private void printResults() {
    if (isLoadCompleted.get()) {
      results.forEach(
          (k, v) ->
              System.out.printf(k, getBooleanFuture(v) ? "loaded successfully" : "loading failed"));
    }
  }

  /**
   * Retrieve the value of a {@link Future<Boolean>} safely. It returns false in case of exception,
   * but as far as it used on completed {@link Future<Boolean>} instances, it is accurate.
   *
   * @param future a {@code Future<Boolean>} instance
   * @return {@code future.get()} or {@code false} if an exception occurs
   */
  private Boolean getBooleanFuture(Future<Boolean> future) {
    try {
      return future.get();
    } catch (InterruptedException | ExecutionException e) {
      return false;
    }
  }
}
