package hu.zza.util.gitform;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An instance of this class is used to save GitHub repositories as GitForm YAML files ready to send
 * to another device. With these files you can build the exact same structure (relative to {@code
 * gitRoot}) of your GitHub projects with {@link ProjectBuilder#load()}.
 */
public class ProjectMapper {
  private final Settings settings;
  private final ResultReport resultReport;
  private final Path gitRoot;
  private final Path gitFormRoot;

  public ProjectMapper(Settings settings) {
    this(settings, new ResultReport());
  }

  public ProjectMapper(Settings settings, ResultReport resultReport) {
    this.settings = settings;
    this.resultReport = resultReport;
    gitRoot = settings.getGitRoot();
    gitFormRoot = settings.getGitFormRoot();
  }

  /**
   * Saves every GitHub project from {@code gitRoot} to {@code gitFormRoot} as a YAML file. First,
   * it prepares the output directory ({@code gitFormRoot}). Then filtering out the parsing errors
   * (nulls) and the rest are processed by {@link ProjectBuilder#cloneIfAbsent(GitHubProject)}.
   */
  public void save() {
    resultReport.clear();
    resultReport.setMainObjective("Save GitHub projects");

    try {
      prepareGitFormDirectory();
      saveProjects(collectProjects());
      resultReport.setSuccessful(true);
    } catch (IOException e) {
      resultReport.appendAdditionalInfo("Cannot save projects:", e.toString());
    } finally {
      resultReport.print();
    }
  }

  /**
   * Create {@code gitFormRoot} directory if it doesn't exist.
   *
   * @throws IOException if {@link Files#createDirectories(Path, FileAttribute[])} throws, or if
   *     {@code gitFormRoot} exists but not a directory
   */
  private void prepareGitFormDirectory() throws IOException {
    if (Files.exists(gitFormRoot)) {
      if (!Files.isDirectory(gitFormRoot)) {
        throw new IOException(gitFormRoot + " should be a directory.");
      }
    } else {
      Files.createDirectories(gitFormRoot);
    }
  }

  /**
   * Saves GitHub projects ({@link GitHubProject} instances) one by one.
   *
   * @param projects {@link Stream<GitHubProject>} of {@link GitHubProject}
   */
  private void saveProjects(List<GitHubProject> projects) {
    try {
      projects.forEach(this::safelySaveGitHubProject);
    } catch (IllegalStateException ignored) {
      resultReport.appendResult("There is no project to save.");
    }
  }

  /**
   * Searches for GitHub projects and returns with them as a {@link Stream}. First, walks through
   * the {@code gitRoot}, then filters for directories with Git projects. Finally, parse the
   * appropriate {@link Path paths} to {@link GitHubProject}. (And filter out non-GitHub projects:
   * Without origin URL the parsing result is null.)
   *
   * @return {@link Stream<GitHubProject>} of {@link GitHubProject projects} found in {@code
   *     gitRoot} and parsed successfully
   * @throws IOException if {@link Files#walk(Path, FileVisitOption...)} throws
   */
  private List<GitHubProject> collectProjects() throws IOException {
    try (Stream<Path> files = Files.walk(gitRoot)) {

      return files
          .filter(Files::isDirectory)
          .filter(this::isProject)
          .map(this::createProjectFromPath)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
    }
  }

  /**
   * Tries to save a {@link GitHubProject GitHub project} as a YAML file to {@code gitFormRoot}. If
   * it fails, there are error messages, but nothing propagated.
   *
   * @param project a {@link GitHubProject} to save to {@code gitFormRoot}
   */
  private void safelySaveGitHubProject(GitHubProject project) {
    try {
      saveGitHubProject(project);
    } catch (FileAlreadyExistsException ignored) {
      resultReport.appendAdditionalInfo(
          "Project already exists:", project.getProjectRoot().toString());
    } catch (IOException e) {
      resultReport.appendAdditionalInfo(
          "Cannot save project:", project.getProjectRoot().toString());
    }
  }

  /**
   * Checks whether this is the root of a Git project or not. If it founds a {@code .git} folder, it
   * assumes yes.
   *
   * @param path {@code Path} to the project root folder
   * @return true if it's a possible Git project
   */
  private boolean isProject(Path path) {
    try (Stream<Path> files = Files.list(path)) {

      return files
          .filter(Files::isDirectory)
          .map(Path::getFileName)
          .anyMatch(s -> s.toString().equals(".git"));

    } catch (IOException e) {
      resultReport.appendAdditionalInfo(
          "Cannot be determined whether it is a project:", path.toString());
      return false;
    }
  }

  /**
   * Creates a {@link GitHubProject} from found project folder ({@link Path}) and extracted origin
   * URL.
   *
   * @param projectRoot {@link Path} to the project root folder
   * @return {@link GitHubProject} on success or null (if it cannot extract the origin URL)
   */
  private GitHubProject createProjectFromPath(Path projectRoot) {
    String originUrl = getOriginUrl(projectRoot);

    return originUrl == null
        ? null
        : new GitHubProject(
            gitRoot.relativize(projectRoot), originUrl.substring(originUrl.indexOf("url = ") + 6));
  }

  /**
   * Writes out a {@link GitHubProject} as a YAML file to {@code gitFormRoot}.
   *
   * @param project a {@link GitHubProject} to save
   * @throws IOException if {@link Files#write(Path, Iterable, OpenOption...)} throws
   */
  private void saveGitHubProject(GitHubProject project) throws IOException {
    Files.write(
        gitFormRoot.resolve(
            String.format("%s_%d.yaml", project.getProjectName(), project.hashCode())),
        project.getExportList(),
        StandardOpenOption.CREATE_NEW);

    resultReport.appendResult(project.getProjectRoot().toString());
  }

  /**
   * Extracts the origin URL from a Git config file.
   *
   * @param projectRoot {@link Path} to the project root folder
   * @return the origin URL as a {@link String}
   */
  private String getOriginUrl(Path projectRoot) {
    try (Stream<String> lines = Files.lines(projectRoot.resolve(".git/config"))) {

      return lines.filter(line -> line.contains("url = ")).findFirst().orElse(null);

    } catch (IOException e) {
      resultReport.appendAdditionalInfo("Cannot retrieve origin URL:", projectRoot.toString());
      return null;
    }
  }
}
