package ca.sfu.orcus.gitlabanalyzer.mergeRequest;

import ca.sfu.orcus.gitlabanalyzer.commit.CommitDTO;
import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.*;

import java.util.ArrayList;
import java.util.List;


public class MergeRequestRepository {

    public ArrayList<MergeRequestDTO> getAllMergeRequests(GitLabApi gitLabApi, int projectID) throws GitLabApiException {

        ArrayList<MergeRequestDTO> listMR = new ArrayList<>();
        List<MergeRequest> mergeRequests = gitLabApi.getMergeRequestApi().getMergeRequests(projectID);
        for (MergeRequest mr : mergeRequests) {
            MergeRequestDTO presentMergeRequest = new MergeRequestDTO(gitLabApi, projectID, mr);
            listMR.add(presentMergeRequest);
        }
        return listMR;
    }

    public ArrayList<CommitDTO> getAllCommitsFromMergeRequest(GitLabApi gitLabApi, int projectID) throws GitLabApiException {

        ArrayList<CommitDTO> listCommit = new ArrayList<>();
        List<MergeRequest> mergeRequests = gitLabApi.getMergeRequestApi().getMergeRequests(projectID);
        for (MergeRequest mr : mergeRequests) {
            List<Commit> presentCommit = gitLabApi.getMergeRequestApi().getCommits(projectID, mr.getId());
            for(Commit c:presentCommit){
                CommitDTO tempDTO = new CommitDTO(gitLabApi, projectID, c);
                listCommit.add(tempDTO);
            }
        }
        return listCommit;
    }

    public ArrayList<MergeRequestDiffDTO> getDiffFromMergeRequest(GitLabApi gitLabApi, int projectID, int mergeRequestID) throws GitLabApiException {
        ArrayList<MergeRequestDiffDTO> listDiff = new ArrayList<>();
        List<MergeRequest> mergeRequests = gitLabApi.getMergeRequestApi().getMergeRequests(projectID);
        for (MergeRequest mr : mergeRequests) {
            List<Commit> presentCommit = gitLabApi.getMergeRequestApi().getCommits(projectID, mr.getId());
            for(Commit c:presentCommit) {
                List<Diff> commitDiffs = gitLabApi.getCommitsApi().getDiff(projectID, c.getShortId());
                for(Diff d : commitDiffs) {
                    listDiff.add(new MergeRequestDiffDTO(c, d));
                }
            }
        }
        return listDiff;
    }


}


