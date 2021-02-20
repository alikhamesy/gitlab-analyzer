package ca.sfu.orcus.gitlabanalyzer.mergeRequest;

import ca.sfu.orcus.gitlabanalyzer.commit.CommitDTO;
import com.google.gson.Gson;
import org.gitlab4j.api.GitLabApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
public class MergeRequestController {

    private final MergeRequestService mergeRequestService;
    int EPOCH_TO_DATE_FACTOR = 1000; //to multiply the long from parseLong() by 1000 to convert to milliseconds, for Java's date constructor

    @Autowired
    public MergeRequestController(MergeRequestService mergeRequestService) {
        this.mergeRequestService = mergeRequestService;

    }

    @GetMapping("/api/project/{projectId}/mergeRequests")
    public String getMergeRequests(@CookieValue(value = "sessionId") String jwt,
                                   HttpServletResponse response,
                                   @PathVariable int projectId,
                                   @RequestParam(required = false, defaultValue = "0") long since,
                                   @RequestParam(required = false, defaultValue = "-1") long until) {

        Date dateSince = new Date(since * EPOCH_TO_DATE_FACTOR);
        Date dateUntil = calculateUntil(until);
        Gson gson = new Gson();
        List<MergeRequestDTO> mergeRequestDTOS = mergeRequestService.getAllMergeRequests(jwt, projectId, dateSince, dateUntil);
        response.setStatus(mergeRequestDTOS == null ? 401 : 200);
        return gson.toJson(mergeRequestDTOS);
    }

    private Date calculateUntil(long until) {
        if (until == -1) {
            return new Date(); // until now
        }
        else {
            return new Date(until * EPOCH_TO_DATE_FACTOR); // until given value
        }
    }

    @GetMapping("/api/project/{projectId}/mergerequest/{mergerequestId}/commits")
    public String getCommitsFromMergeRequests(@CookieValue(value = "sessionId") String jwt,
                                              HttpServletResponse response,
                                              @PathVariable int mergerequestId,
                                              @PathVariable int projectId) {
        List<CommitDTO> commitDTOS = mergeRequestService.getAllCommitsFromMergeRequest(jwt, projectId, mergerequestId);
        response.setStatus(commitDTOS == null ? 401 : 200);
        Gson gson = new Gson();
        return gson.toJson(commitDTOS);
    }

    @GetMapping("/api/project/{projectId}/mergerequest/{mergerequestId}/diff")
    public String getDiffsFromMergeRequests(@CookieValue(value = "sessionId") String jwt,
                                            HttpServletResponse response,
                                            @PathVariable int mergerequestId,
                                            @PathVariable int projectId) {
        List<MergeRequestDiffDTO> mergeRequestDiffDTOS = mergeRequestService.getDiffFromMergeRequest(jwt, projectId, mergerequestId);
        response.setStatus(mergeRequestDiffDTOS == null ? 401 : 200);
        Gson gson = new Gson();
        return gson.toJson(mergeRequestDiffDTOS);
    }
}