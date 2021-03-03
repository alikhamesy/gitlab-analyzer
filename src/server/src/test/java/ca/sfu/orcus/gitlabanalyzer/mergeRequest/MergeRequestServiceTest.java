package ca.sfu.orcus.gitlabanalyzer.mergeRequest;

import ca.sfu.orcus.gitlabanalyzer.authentication.AuthenticationService;
import ca.sfu.orcus.gitlabanalyzer.commit.CommitDto;
import ca.sfu.orcus.gitlabanalyzer.utils.DateUtils;
import org.gitlab4j.api.*;
import org.gitlab4j.api.models.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class MergeRequestServiceTest {

    @InjectMocks
    private MergeRequestService mergeRequestService;

    @Mock
    private MergeRequestRepository mergeRequestRepository;
    @Mock
    private AuthenticationService authenticationService;
    @Mock
    private GitLabApi gitLabApi;
    @Mock
    private MergeRequestApi mergeRequestApi;
    @Mock
    private CommitsApi commitsApi;
    @Mock
    private Diff diffApi;
    @Mock
    private NotesApi notesApi;

    private static List<MergeRequest> mergeRequests;
    private static List<Commit> commits;
    private static CommitStats commitStats;

    private static final int projectId = 10;
    private static final String jwt = "";
    private static final int mergeRequestId = 9;
    private static final boolean hasConflicts = false;
    private static final boolean isOpen = true;
    private static final int userId = 6;
    private static final int userIdB = 7;
    private static final String assignedTo = "John";
    private static final String author = "John";
    private static final String description = "Random Description";
    private static final String sourceBranch = "Testing";
    private static final String targetBranch = "master";
    private static final int numAdditions = 6;
    private static final int numDeletions = 12;
    private static final ArrayList<String> notesName = new ArrayList<>();
    private static final ArrayList<String> notes = new ArrayList<>();
    private static final ArrayList<String> committers = new ArrayList<>();
    private static final List<Participant> participants = new ArrayList<>();
    private static final Date dateSince = new Date(System.currentTimeMillis() - 7L * 24 * 3600 * 1000);
    private static final Date dateNow = new Date();
    private static final Date dateUntil = new Date(System.currentTimeMillis() + 7L * 24 * 3600 * 1000);
    private static final long time = 10000000;
    private static final List<Note> notesList = new ArrayList<>();


    private static final String title = "title";
    private static final String authorEmail = "jimcarry@carryingyou.com";
    private static final String message = "";
    private static final int count = 10;
    private static final String sha = "123456";
    private static final String defaultBranch = "master";
    private static final List<Commit> commitList = new ArrayList<>();
    private static Project project;


    private static final boolean isNewFile = true;
    private static final boolean isDeletedFile = false;
    private static final boolean isRenamedFile = false;
    private static final String commitName = "Nerf";
    private static final String newPath = "root";
    private static final String oldPath = "";
    private static final String diff = "";


    @BeforeAll
    public static void setup() {
        mergeRequests = generateTestMergeRequestList();
        commitStats = getTestCommitStats();
        commits = generateTestCommitList();

    }

    @Test
    public void gitlabAPIPrimaryNullTest() {

        when(authenticationService.getGitLabApiFor(jwt)).thenReturn(null);
        gitLabApi = authenticationService.getGitLabApiFor(jwt);

        assertNull(mergeRequestService.getAllMergeRequests(jwt, projectId, dateSince, dateUntil));
        assertNull(mergeRequestService.getAllMergeRequests(gitLabApi, projectId, dateSince, dateUntil, userId));
        assertNull(mergeRequestService.getDiffFromMergeRequest(jwt, projectId, mergeRequestId));
        assertNull(mergeRequestService.getAllCommitsFromMergeRequest(jwt, projectId, mergeRequestId));
    }

    @Test
    public void getAllMergeRequestWithoutMemberID() throws GitLabApiException {

        when(gitLabApi.getMergeRequestApi()).thenReturn(mergeRequestApi);
        when(gitLabApi.getNotesApi()).thenReturn(notesApi);
        when(mergeRequestApi.getMergeRequests(projectId, Constants.MergeRequestState.MERGED)).thenReturn(mergeRequests);

        List<MergeRequestDto> mergeRequestDtoList = mergeRequestService.getAllMergeRequests(gitLabApi, projectId, dateSince, dateUntil);

        List<MergeRequestDto> expectedMergeRequestDtoList = new ArrayList<>();
        for (MergeRequest m : mergeRequests) {
            if (m.getCreatedAt().after(dateSince) && m.getCreatedAt().before(dateUntil))
                expectedMergeRequestDtoList.add(new MergeRequestDto(gitLabApi, projectId, m));
        }

        assertNotNull(mergeRequestDtoList);
        assertEquals(expectedMergeRequestDtoList.size(), mergeRequestDtoList.size());
        assertEquals(expectedMergeRequestDtoList, mergeRequestDtoList);

    }

    @Test
    public void getAllMergeRequestWithMemberID() throws GitLabApiException {

        when(gitLabApi.getMergeRequestApi()).thenReturn(mergeRequestApi);
        when(mergeRequestApi.getMergeRequests(projectId, Constants.MergeRequestState.MERGED)).thenReturn(mergeRequests);
        when(gitLabApi.getNotesApi()).thenReturn(notesApi);
        when(notesApi.getMergeRequestNotes(projectId, mergeRequestId)).thenReturn(notesList);

        List<MergeRequestDto> mergeRequestDtoList = mergeRequestService.getAllMergeRequests(gitLabApi, projectId, dateSince, dateUntil, userId);

        List<MergeRequestDto> expectedMergeRequestDtoList = new ArrayList<>();
        for (MergeRequest m : mergeRequests) {
            if (m.getAuthor().getId() == userId)
                expectedMergeRequestDtoList.add(new MergeRequestDto(gitLabApi, projectId, m));
        }

        assertNotNull(mergeRequestDtoList);
        assertEquals(expectedMergeRequestDtoList.size(), mergeRequestDtoList.size());
        assertEquals(mergeRequestDtoList, expectedMergeRequestDtoList);
    }

    @Test
    public void getAllCommitsFromMergeRequest() throws GitLabApiException {

        when(authenticationService.getGitLabApiFor(jwt)).thenReturn(gitLabApi);
        when(gitLabApi.getMergeRequestApi()).thenReturn(mergeRequestApi);
        when(mergeRequestApi.getCommits(projectId, mergeRequestId)).thenReturn(commits);
        when(gitLabApi.getCommitsApi()).thenReturn(commitsApi);
        //when(commitsApi.getCommits(projectId, defaultBranch, dateSince, dateUntil)).thenReturn(commitList);
        when(commitsApi.getCommit(projectId,sha)).thenReturn(commits.get(0));

        List<CommitDto> commitDtoList = mergeRequestService.getAllCommitsFromMergeRequest(jwt, projectId, mergeRequestId);
        System.out.println(commitDtoList);

        List<CommitDto> expectedCommitDtoList = new ArrayList<>();
        for (Commit c: commits) {
                expectedCommitDtoList.add(new CommitDto(gitLabApi, projectId, c));
        }

        assertNotNull(commitDtoList);
        assertEquals(expectedCommitDtoList.size(), commitDtoList.size());
        assertEquals(expectedCommitDtoList, commitDtoList);
    }

    public static List<MergeRequest> generateTestMergeRequestList() {
        List<MergeRequest> tempMergeRequestList = new ArrayList<>();
        MergeRequest tempMergeRequestA = new MergeRequest();
        MergeRequest tempMergeRequestB = new MergeRequest();

        Author tempAuthorA = new Author();
        tempAuthorA.setName(author);
        tempAuthorA.setId(userId);
        tempMergeRequestA.setAuthor(tempAuthorA);

        tempMergeRequestA.setIid(mergeRequestId);
        tempMergeRequestA.setHasConflicts(hasConflicts);
        tempMergeRequestA.setState("opened");
        Assignee tempAssigneeA = new Assignee();
        tempAssigneeA.setName(assignedTo);
        tempAssigneeA.setId(userId);
        tempMergeRequestA.setAssignee(tempAssigneeA);

        tempMergeRequestA.setDescription(description);
        tempMergeRequestA.setSourceBranch(sourceBranch);
        tempMergeRequestA.setTargetBranch(targetBranch);
        tempMergeRequestA.setCreatedAt(dateNow);
        tempMergeRequestA.setHasConflicts(false);
        tempMergeRequestA.setMergedAt(dateNow);


        Author tempAuthorB = new Author();
        tempAuthorB.setName(author);
        tempAuthorB.setId(userIdB);
        tempMergeRequestB.setAuthor(tempAuthorB);

        tempMergeRequestB.setIid(mergeRequestId);
        tempMergeRequestB.setHasConflicts(hasConflicts);
        tempMergeRequestB.setState("opened");
        Assignee tempAssigneeB = new Assignee();
        tempAssigneeB.setName(assignedTo);
        tempAssigneeB.setId(userId);
        tempMergeRequestB.setAssignee(tempAssigneeB);

        tempMergeRequestB.setDescription(description);
        tempMergeRequestB.setSourceBranch(sourceBranch);
        tempMergeRequestB.setTargetBranch(targetBranch);
        tempMergeRequestB.setCreatedAt(dateUntil);
        tempMergeRequestB.setHasConflicts(false);
        tempMergeRequestB.setMergedAt(dateUntil);

        tempMergeRequestList.add(tempMergeRequestA);
        tempMergeRequestList.add(tempMergeRequestB);

        return tempMergeRequestList;
    }

    public static List<Commit> generateTestCommitList() {


        Commit commitA = new Commit();
        Commit commitB = new Commit();
        List<Commit> generatedCommitList = new ArrayList<>();

        commitA.setId(String.valueOf(projectId));
        commitA.setTitle(title);
        commitA.setAuthorName(author);
        commitA.setAuthorEmail(authorEmail);
        commitA.setMessage(message);
        commitA.setId(sha);
        commitA.setCommittedDate(dateNow);
        commitA.setStats(commitStats);
        commitA.setShortId(sha);

        commitB.setId(String.valueOf(projectId));
        commitB.setTitle(title);
        commitB.setAuthorName(author);
        commitB.setAuthorEmail(authorEmail);
        commitB.setMessage(message);
        commitB.setId(sha);
        commitB.setCommittedDate(dateNow);
        commitB.setStats(commitStats);
        commitB.setShortId(sha);

        generatedCommitList.add(commitA);
        generatedCommitList.add(commitB);
        return generatedCommitList;
    }

    public static CommitStats getTestCommitStats() {
        CommitStats commitStats = new CommitStats();

        commitStats.setAdditions(count);
        commitStats.setDeletions(count);
        commitStats.setTotal(count*2);

        return commitStats;
    }



}

