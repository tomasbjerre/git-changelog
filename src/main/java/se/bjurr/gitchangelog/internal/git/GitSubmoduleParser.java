package se.bjurr.gitchangelog.internal.git;

import static org.slf4j.LoggerFactory.getLogger;

import com.google.common.base.Optional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import se.bjurr.gitchangelog.api.GitChangelogApi;
import se.bjurr.gitchangelog.api.exceptions.GitChangelogRepositoryException;
import se.bjurr.gitchangelog.api.model.Changelog;
import se.bjurr.gitchangelog.internal.git.model.GitCommit;
import se.bjurr.gitchangelog.internal.settings.Settings;

public class GitSubmoduleParser {
  private static final Logger LOG = getLogger(GitSubmoduleParser.class);

  public GitSubmoduleParser() {}

  public HashMap<GitCommit, List<Changelog>> parseForSubmodules(
      final GitChangelogApi gitChangelogApi,
      final boolean useIntegrationIfConfigured,
      final GitRepo gitRepo,
      final List<GitCommit> commits) {

    HashMap<GitCommit, List<Changelog>> submoduleSections = new HashMap<>();
    Pattern submoduleNamePattern =
        Pattern.compile(
            "(?m)^\\+{3} b/([\\w/\\s-]+)$\\n-Subproject commit (\\w+)$\\n\\+Subproject commit (\\w+)$");

    Settings settings = gitChangelogApi.getSettings();

    Optional<String> cachedFromCommit = settings.getFromCommit();
    Optional<String> cachedToCommit = settings.getToCommit();
    Optional<String> cachedFromRef = settings.getFromRef();
    Optional<String> cachedToRef = settings.getToRef();
    String cachedFromRepo = settings.getFromRepo();

    for (GitCommit commit : commits) {
      String diff = gitRepo.getDiff(commit.getHash());
      Matcher submoduleMatch = submoduleNamePattern.matcher(diff);
      while (submoduleMatch.find()) {
        String submoduleName = submoduleMatch.group(1);
        String previousSubmoduleHash = submoduleMatch.group(2);
        String currentSubmoduleHash = submoduleMatch.group(3);
        GitRepo submodule = gitRepo.getSubmodule(submoduleName);
        if (submodule == null) {
          continue;
        }

        settings.setFromCommit(previousSubmoduleHash);
        settings.setToCommit(currentSubmoduleHash);
        settings.setFromRef(null);
        settings.setToRef(null);
        settings.setFromRepo(submodule.getDirectory());

        if (!submoduleSections.containsKey(commit)) {
          submoduleSections.put(commit, new ArrayList<>());
        }
        List<Changelog> submoduleSectionList = submoduleSections.get(commit);
        try {
          submoduleSectionList.add(
              GitChangelogApi.gitChangelogApiBuilder()
                  .withSettings(settings)
                  .getChangelog(useIntegrationIfConfigured));
        } catch (GitChangelogRepositoryException e) {
          e.printStackTrace();
        }
      }
    }

    if (cachedFromCommit.isPresent()) {
      settings.setFromCommit(cachedFromCommit.get());
    }
    if (cachedToCommit.isPresent()) {
      settings.setToCommit(cachedToCommit.get());
    }
    if (cachedFromRef.isPresent()) {
      settings.setFromRef(cachedFromRef.get());
    }
    if (cachedToRef.isPresent()) {
      settings.setToRef(cachedToRef.get());
    }
    settings.setFromRepo(cachedFromRepo);

    return submoduleSections;
  }
}