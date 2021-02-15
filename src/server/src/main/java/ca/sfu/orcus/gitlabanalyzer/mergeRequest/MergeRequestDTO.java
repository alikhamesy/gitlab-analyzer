package ca.sfu.orcus.gitlabanalyzer.mergeRequest;

import org.gitlab4j.api.GitLabApi;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.models.Commit;
import org.gitlab4j.api.models.MergeRequest;
import org.gitlab4j.api.models.Note;
import org.gitlab4j.api.models.Participant;

import java.util.ArrayList;
import java.util.List;

public class MergeRequestDTO{


    private boolean hasConflicts;
    private boolean isOpen;
    private String assignedTo;
    private String author;
    private String description;
    private String sourceBranch;
    private String targetBranch;
    private int numAdditions;
    private int numDeletions;
    private ArrayList<String> notesName;
    private ArrayList<String> notes;
    private List<String> commiters;
    private List<Participant> participants;

    public MergeRequestDTO(GitLabApi gitLabApi, int projectID, MergeRequest presentMergeRequest) throws GitLabApiException {

        setOpen(presentMergeRequest.getState().compareTo("opened") == 0);
        setAuthor(presentMergeRequest.getAuthor().getName());
        setSourceBranch(presentMergeRequest.getSourceBranch());
        setTargetBranch(presentMergeRequest.getTargetBranch());
        setAssignedTo(presentMergeRequest.getAssignee().getName());
        setDescription(presentMergeRequest.getDescription());
        setHasConflicts(presentMergeRequest.getHasConflicts());
        setCommiters(gitLabApi.getMergeRequestApi().getCommits(projectID, presentMergeRequest.getIid()));
        setNumAdditions(gitLabApi.getMergeRequestApi().getCommits(projectID, presentMergeRequest.getIid()));
        setNumDeletions(gitLabApi.getMergeRequestApi().getCommits(projectID, presentMergeRequest.getIid()));
        setParticipants(gitLabApi.getMergeRequestApi().getParticipants(projectID, presentMergeRequest.getIid()));
        ArrayList<String> notesName = new ArrayList<>();
        ArrayList<String> notes = new ArrayList<>();
        List<Note> mrNotes = gitLabApi.getNotesApi().getMergeRequestNotes(projectID, presentMergeRequest.getIid());
        if (!mrNotes.isEmpty()) {
            for (Note note : mrNotes) {
                notesName.add(note.getAuthor().getName());
                notes.add(note.getBody());
            }
            setNotes(notes);
            setNotesName(notesName);
        }
    }

    public void setHasConflicts(boolean hasConflicts) {
        this.hasConflicts = hasConflicts;
    }

    public void setOpen(boolean open) {
        isOpen = open;
    }

    public void setAssignedTo(String assignedTo) {
        this.assignedTo = assignedTo;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setSourceBranch(String sourceBranch) {
        this.sourceBranch = sourceBranch;
    }

    public void setTargetBranch(String targetBranch) {
        this.targetBranch = targetBranch;
    }

    public void setNotesName(ArrayList<String> notesName) {
        this.notesName = notesName;
    }

    public void setNotes(ArrayList<String> notes) {
        this.notes = notes;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public void setNumAdditions(List<Commit> commits) {
        numAdditions = 0;
        for(Commit c : commits){
            numAdditions+=c.getStats().getAdditions();
        }
    }

    public void setNumDeletions(List<Commit> commits) {
        numDeletions = 0;
        for(Commit c : commits){
            numDeletions+=c.getStats().getDeletions();
        }
    }

    public void setCommiters(List<Commit> commits) {
        commiters.add(commits.get(0).getAuthorName());
        for(Commit c : commits){
            //Checks if commiter is already present in list, prevent duplicate authors
            String commitAuthor = c.getAuthorName();
            boolean isPresent = false;
            for(int i=0;i<commiters.size();i++){
                if(commiters.get(i).compareTo(commitAuthor)==0)
                    isPresent = true;
            }
            if(!isPresent)
                commiters.add(commitAuthor);
        }

    }

}
