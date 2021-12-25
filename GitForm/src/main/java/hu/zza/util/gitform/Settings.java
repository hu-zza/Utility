package hu.zza.util.gitform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class Settings {
  private final Path homePath;
  private final Path settingsPath;
  private final Path settingsFilePath;
  private Path gitRoot;
  private Path gitFormRoot;

  public Settings() {
    homePath = Path.of(System.getProperty("user.home"));
    settingsPath = homePath.resolve(Path.of(".git-form"));
    settingsFilePath = settingsPath.resolve("settings.yaml");
    gitRoot = homePath.resolve("GIT");
    gitFormRoot = gitRoot.resolve("GitForm");
  }


  public Settings(Path homePath, Path settingsPath, Path settingsFilePath, Path gitRoot,
      Path gitFormRoot) {
    this.homePath = homePath;
    this.settingsPath = settingsPath;
    this.settingsFilePath = settingsFilePath;
    this.gitRoot = gitRoot;
    this.gitFormRoot = gitFormRoot;
  }

  public void init() {
    if (Files.exists(settingsPath)) {
      if (Files.isDirectory(settingsPath)) {
        initializeSettings();
      } else {
        System.err.println(settingsPath + " should be a directory.");
      }
    } else {
      initializeDefaultSettings();
    }
  }

  public Path getGitRoot() {
    return gitRoot;
  }

  public Path getGitFormRoot() {
    return gitFormRoot;
  }

  private void initializeSettings() {
    try {
      var map = YamlReader.parse(settingsFilePath);
      gitRoot = Path.of(map.get("git"));
      gitFormRoot = Path.of(map.get("git-form"));
    } catch (IOException e) {
      System.err.printf("Cannot load and initialize settings: %s", settingsFilePath);
    }
  }

  private void initializeDefaultSettings() {
    try {
      Files.createDirectories(settingsPath);
      Files.write(settingsFilePath, List.of("git: " + gitRoot, "git-form: " + gitFormRoot));
    } catch (IOException e) {
      System.err.printf("Cannot initialize settings folder and files: %s", settingsPath);
    }
  }
}
