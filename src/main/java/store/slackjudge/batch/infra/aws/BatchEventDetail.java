package store.slackjudge.batch.infra.aws;

import java.io.Serializable;

public record BatchEventDetail(
        String jobId,
        String status
) implements Serializable {
}
