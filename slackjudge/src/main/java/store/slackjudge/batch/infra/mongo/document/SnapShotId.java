package store.slackjudge.batch.infra.mongo.document;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

@AllArgsConstructor(access = AccessLevel.PACKAGE)
@RequiredArgsConstructor
@Getter
public class SnapShotId implements Serializable {
    private String bojId;
    private LocalDateTime snapShotAt;

    public static SnapShotId of(String bojId,LocalDateTime snapShotAt){
        return new SnapShotId(bojId,snapShotAt);
    }
    @Override
    public boolean equals(Object o){
        if(this==o) return true;
        if (o==null || this.getClass()!=o.getClass()) return false;

        SnapShotId snapShotId=(SnapShotId) o;

        return Objects.equals(snapShotId.bojId,this.bojId)
               && Objects.equals(snapShotId.snapShotAt,this.snapShotAt);
    }

    @Override
    public int hashCode(){
        return Objects.hash(bojId,snapShotAt);
    }
}
