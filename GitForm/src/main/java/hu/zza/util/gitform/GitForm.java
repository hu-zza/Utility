package hu.zza.util.gitform;

import java.nio.file.Path;

public class GitForm {
  private static final Path gitFolderRelativePath = Path.of("GIT");
  private static final Path gitFormFolderRelativePath = Path.of("GitForm");

  /**
   * GitForm helps to save your GitHub repositories and their structure as YAML files and build this
   * whole on different devices. More about this project:
   * https://git.zza.hu/Utility/tree/main/GitForm
   *
   * <p>The first parameter is mandatory. If it is {@code save}, the script saves every GitHub
   * project from {@code gitRoot} (now: /home/user/GIT) to {@code gitFormRoot} (now:
   * /home/user/GIT/GitForm). You can share this folder and YAML files easily.
   *
   * <p>If the first parameter is {@code load}, the script builds the same folder structure for
   * repositories and clones each and every GitHub project into the proper place (relative to the
   * current {@code gitRoot}).
   *
   * <p>A short example: There are two GitHub repositories 'projectA' at {@code
   * /home/userA/GIT/projectA} and 'projectB' at {@code /home/userA/GIT/B/projectB}, and this script
   * is on the {@code $PATH}.
   *
   * <p>If you call {@code git-form save}, this script creates a folder {@code
   * /home/userA/GIT/GitForm} and saves the core data of {@code /home/userA/GIT/projectA} as {@code
   * /home/userA/GIT/GitForm/projectA_<hash>.yaml} and {@code /home/userA/GIT/B/projectB} as {@code
   * /home/userA/GIT/GitForm/projectB_<hash>.yaml}.
   *
   * <p>At this point you can share this arbitrary ways. After you have forwarded to another device,
   * you have a {@code /home/userB/GIT/GitForm} folder with {@code projectA_<hash>.yaml} and {@code
   * projectB_<hash>.yaml}.
   *
   * <p>If you call {@code git-form load}, this script build the missing folder structure (folder
   * 'B') and clones the repositories. The result: 'projectA' at {@code /home/userB/GIT/projectA}
   * and 'projectB' at {@code /home/userB/GIT/B/projectB}
   *
   * @param args
   *     <p>[0] - mode:
   *     <p>save (the projects to GitForm folder as YAML files)
   *     <p>load (build everything from YAMLs of GitForm folder)
   */
  public static void main(String[] args) {
    Path homePath = Path.of(System.getProperty("user.home"));
    Path gitRoot = homePath.resolve(gitFolderRelativePath);
    Path gitFormRoot = gitRoot.resolve(gitFormFolderRelativePath);
    System.out.println();

    if (args.length == 0) {
      printHelp();

    } else if ("save".equals(args[0])) {
      new ProjectMapper(gitRoot, gitFormRoot).save();

    } else if ("load".equals(args[0])) {
      new ProjectBuilder(gitRoot, gitFormRoot).load();

    } else {
      printHelp();
    }
  }

  /** Prints help info about GitForm. */
  private static void printHelp() {
    System.out.printf("usage: git-form <command>%n%n");
    System.out.printf("commands:%n");

    String commandPattern = "\t%s\t\t%s%n";
    System.out.printf(commandPattern, "save", "saves the projects to GitForm folder as YAML files");
    System.out.printf(commandPattern, "load", "builds everything from YAMLs of GitForm folder");
    System.out.printf("%nMore info: https://git.zza.hu/Utility%n%n");
  }
}
