package hu.zza.util.gitform;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.util.Lists.list;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class GitHubProjectTest {
  List<String> rawLines = List.of("name: Project name", "local: path", "origin: Origin URL");
  GitHubProject project = new GitHubProject("Project name", Path.of("path"), "Origin URL");

  @Test
  void init() {
    assertThat(project)
        .extracting(
            GitHubProject::getProjectName,
            GitHubProject::getProjectRoot,
            GitHubProject::getOriginUrl)
        .isEqualTo(list("Project name", Path.of("path"), "Origin URL"));
  }

  @Test
  void parseLines() {
    var parsed = GitHubProject.parse(rawLines.stream());
    assertEquals(project, parsed);
  }

  @Test
  void parseFile() throws IOException {
    var parsed = GitHubProject.parse(Path.of(System.getenv("GIT_FORM"), "clim_3056492.yaml"));

    assertThat(parsed)
        .extracting(
            GitHubProject::getProjectName,
            GitHubProject::getProjectRoot,
            GitHubProject::getOriginUrl)
        .isEqualTo(list("clim", Path.of("clim"), "git@github.com:hu-zza/clim.git"));
  }

  @Test
  void getExportList() {
    assertEquals(rawLines, project.getExportList());
  }
}
