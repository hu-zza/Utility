package hu.zza.util.gitform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Predicate;
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
  private final Map<GitHubProject, CompletableFuture<Process>> results;
  private final ResultReport resultReport;

  public ProjectBuilder(Path gitRoot, Path gitFormRoot) {
    this.gitRoot = gitRoot;
    this.gitFormRoot = gitFormRoot;
    this.results = new HashMap<>();
    this.resultReport = new ResultReport();
  }

  /**
   * Pulls every found, non-existent project and builds the proper folder structure for them. First,
   * it tries to parse every YAML file in {@code gitFormRoot} to {@link GitHubProject}. Then it
   * filters out the parsing errors (nulls) and the rest are processed by {@link
   * ProjectBuilder#cloneIfAbsent(GitHubProject)}.
   */
  public void load() {
    results.clear();
    resultReport.setMainObjective("Load GitHub projects");

    try (Stream<Path> files = Files.list(gitFormRoot)) {
      files
          .filter(isYaml)
          .map(this::parseProjectFile)
          .filter(Objects::nonNull)
          .forEach(this::cloneIfAbsent);
      prepareResultReport();
    } catch (IOException e) {
      resultReport.appendAdditionalInfo("Cannot load projects:", e.toString());
    } finally {
      resultReport.print();
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
            project,
            runtime
                .exec(String.format("git clone %s %s", project.getOriginUrl(), rootPath))
                .onExit());
      } else {
        resultReport.appendAdditionalInfo(
            "Project already exists:", project.getProjectRoot().toString());
      }
    } catch (IOException e) {
      resultReport.appendAdditionalInfo(
          "Cannot clone project:",
          String.format("%s (%s)", project.getProjectRoot(), project.getOriginUrl()));
    }
  }

  /** Prepare the results of {@link ProjectBuilder#load()} according to {@code results} Map. */
  private void prepareResultReport() {
    List<GitHubProject> completed = new ArrayList<>();

    while (!results.isEmpty()) {
      for (var e : results.entrySet()) {
        if (e.getValue().isDone()) {
          completed.add(e.getKey());
          appendToResultReport(e);
        }
      }
      completed.forEach(results::remove);
      completed.clear();
    }
    resultReport.setSuccessful(true);
  }

  private void appendToResultReport(Entry<GitHubProject, CompletableFuture<Process>> entry) {
    String pathString = entry.getKey().getProjectRoot().toString();

    if (isDoneSuccessfully(entry.getValue())) {
      resultReport.appendResult(pathString);
    } else {
      resultReport.appendAdditionalInfo("Cannot load project:", pathString);
    }
  }

  /**
   * Retrieve the processing success of a {@link Future<Process>} safely. It returns false in case
   * of exception, but as far as it used on completed {@link Future<Process>} instances, it is
   * accurate.
   *
   * @param future a {@code Future<Process>} instance
   * @return {@code future.get().exitValue() == 0} or {@code false} if an exception occurs
   */
  private boolean isDoneSuccessfully(Future<Process> future) {
    try {
      return future.get().exitValue() == 0;
    } catch (ExecutionException | InterruptedException ignored) {
      return false;
    }
  }
}
