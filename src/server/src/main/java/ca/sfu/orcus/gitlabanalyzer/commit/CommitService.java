package ca.sfu.orcus.gitlabanalyzer.commit;

import ca.sfu.orcus.gitlabanalyzer.authentication.GitLabApiWrapper;
import ca.sfu.orcus.gitlabanalyzer.config.ConfigDto;
import ca.sfu.orcus.gitlabanalyzer.config.ConfigService;
import ca.sfu.orcus.gitlabanalyzer.file.FileDto;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.Diff;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.ws.rs.NotFoundException;
import java.util.*;

@Service
public class CommitService {
    private final CommitRepository commitRepository;
    private final GitLabApiWrapper gitLabApiWrapper;
    private final ConfigService configService;

    @Autowired
    public CommitService(CommitRepository commitRepository, GitLabApiWrapper gitLabApiWrapper, ConfigService configService) {
        this.commitRepository = commitRepository;
        this.gitLabApiWrapper = gitLabApiWrapper;
        this.configService = configService;
    }

    public List<CommitDto> getAllCommits(String jwt, int projectId, Date since, Date until) {
        GitLabApi gitLabApi = gitLabApiWrapper.getGitLabApiFor(jwt);
        if (gitLabApi == null) {
            return null;
        }
        return getAllCommitDtos(jwt, gitLabApi, projectId, since, until);
    }

    private List<CommitDto> getAllCommitDtos(String jwt, GitLabApi gitLabApi, int projectId, Date since, Date until) {
        try {
            String defaultBranch = gitLabApi.getProjectApi().getProject(projectId).getDefaultBranch();
            List<Commit> allGitCommits = gitLabApi.getCommitsApi().getCommits(projectId, defaultBranch, since, until);
            List<CommitDto> allCommits = new ArrayList<>();
            for (Commit commit : allGitCommits) {
                ConfigDto currentConfig = configService.getCurrentConfig(jwt)
                        .orElseThrow(() -> new NotFoundException("Current config not found"));
                CommitScoreCalculator scoreCalculator = new CommitScoreCalculator(currentConfig);
                List<Diff> diffs = gitLabApi.getCommitsApi().getDiff(projectId, commit.getId());
                List<FileDto> fileScores = scoreCalculator.getCommitScore(diffs);
                CommitDto presentCommit = new CommitDto(gitLabApi, projectId, commit, fileScores);
                allCommits.add(presentCommit);
            }
            return allCommits;
        } catch (GitLabApiException e) {
            return null;
        }
    }

    public List<CommitDto> returnAllCommitsOfAMember(String jwt, int projectId, Date since, Date until, String memberName) {
        GitLabApi gitLabApi = gitLabApiWrapper.getGitLabApiFor(jwt);
        if (gitLabApi == null) {
            return null;
        }
        return returnAllCommits(jwt, gitLabApi, projectId, since, until, memberName);
    }

    private List<CommitDto> returnAllCommits(String jwt, GitLabApi gitLabApi, int projectId, Date since, Date until, String name) {
        if (gitLabApi == null) {
            return null;
        }
        try {
            String defaultBranch = gitLabApi.getProjectApi().getProject(projectId).getDefaultBranch();
            List<Commit> allGitCommits = gitLabApi.getCommitsApi().getCommits(projectId, defaultBranch, since, until);
            List<CommitDto> allCommits = new ArrayList<>();
            for (Commit commit : allGitCommits) {
                if (commit.getAuthorName().equalsIgnoreCase(name)) {
                    ConfigDto currentConfig = configService.getCurrentConfig(jwt)
                            .orElseThrow(() -> new NotFoundException("Current config not found"));
                    CommitScoreCalculator scoreCalculator = new CommitScoreCalculator(currentConfig);
                    List<Diff> diffList = gitLabApi.getCommitsApi().getDiff(projectId, commit.getId());
                    List<FileDto> fileScores = scoreCalculator.getCommitScore(diffList);
                    CommitDto presentCommit = new CommitDto(gitLabApi, projectId, commit, fileScores);
                    allCommits.add(presentCommit);
                }
            }
            return allCommits;
        } catch (GitLabApiException e) {
            return null;
        }
    }

    public CommitDto getSingleCommit(String jwt, int projectId, String sha) {
        GitLabApi gitLabApi = gitLabApiWrapper.getGitLabApiFor(jwt);
        if (gitLabApi == null) {
            return null;
        }
        try {
            Commit gitCommit = gitLabApi.getCommitsApi().getCommit(projectId, sha);
            ConfigDto currentConfig = configService.getCurrentConfig(jwt)
                    .orElseThrow(() -> new NotFoundException("Current config not found"));
            CommitScoreCalculator scoreCalculator = new CommitScoreCalculator(currentConfig);
            List<Diff> diffList = gitLabApi.getCommitsApi().getDiff(projectId, gitCommit.getId());
            List<FileDto> fileScores = scoreCalculator.getCommitScore(diffList);
            return new CommitDto(gitLabApi, projectId, gitCommit, fileScores);
        } catch (GitLabApiException e) {
            return null;
        }
    }

    public String getDiffOfCommit(String jwt, int projectId, String sha) {
        GitLabApi gitLabApi = gitLabApiWrapper.getGitLabApiFor(jwt);
        if (gitLabApi == null) {
            return null;
        }
        CommitDto commitDto = getSingleCommit(jwt, projectId, sha);
        return commitDto.getDiffs();
    }
}