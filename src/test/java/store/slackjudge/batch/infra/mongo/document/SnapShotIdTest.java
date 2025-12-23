package store.slackjudge.batch.infra.mongo.document;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class SnapShotIdTest {

    @Test
    @DisplayName("of()는 동일한 값(bojId, snapShotAt)을 가진 인스턴스를 생성")
    void of_createsInstance() {
        LocalDateTime t = LocalDateTime.of(2025, 12, 16, 10, 0);
        SnapShotId id = SnapShotId.of("test", t);

        assertThat(id.getBojId()).isEqualTo("test");
        assertThat(id.getSnapShotAt()).isEqualTo(t);
    }

    @Test
    @DisplayName("같은 필드 값을 가지면 equals는 true이고 hashCode도 같다")
    void equalsAndHashCode_sameValues() {
        LocalDateTime t = LocalDateTime.of(2025, 12, 16, 10, 0);

        SnapShotId a = SnapShotId.of("boj", t);
        SnapShotId b = SnapShotId.of("boj", t);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("bojId가 다르면 equals는 false")
    void equals_false_whenDifferentBojId() {
        LocalDateTime t = LocalDateTime.of(2025, 12, 16, 10, 0);

        SnapShotId a = SnapShotId.of("bojA", t);
        SnapShotId b = SnapShotId.of("bojB", t);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("snapShotAt이 다르면 equals는 false")
    void equals_false_whenDifferentSnapShotAt() {
        LocalDateTime t1 = LocalDateTime.of(2025, 12, 16, 10, 0);
        LocalDateTime t2 = LocalDateTime.of(2025, 12, 16, 11, 0);

        SnapShotId a = SnapShotId.of("boj", t1);
        SnapShotId b = SnapShotId.of("boj", t2);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    @DisplayName("null 또는 다른 타입과는 equals가 false")
    void equals_false_whenNullOrDifferentType() {
        SnapShotId a = SnapShotId.of("boj", LocalDateTime.of(2025, 12, 16, 10, 0));

        assertThat(a.equals(null)).isFalse();
        assertThat(a.equals("not-a-snapshot-id")).isFalse();
    }

    @Test
    @DisplayName("HashSet에서 동일 키는 중복 저장되지 않는다")
    void hashSet_deduplicatesByEqualsAndHashCode() {
        LocalDateTime t = LocalDateTime.of(2025, 12, 16, 10, 0);

        SnapShotId a = SnapShotId.of("boj", t);
        SnapShotId b = SnapShotId.of("boj", t);

        Set<SnapShotId> set = new HashSet<>();
        set.add(a);
        set.add(b);

        assertThat(set).hasSize(1);
        assertThat(set).contains(a);
    }

    @Test
    @DisplayName("HashMap 키로 사용 시 동일 키로 조회가 가능")
    void hashMap_keyLookupWorks() {
        LocalDateTime t = LocalDateTime.of(2025, 12, 16, 10, 0);

        SnapShotId key1 = SnapShotId.of("boj", t);
        SnapShotId key2 = SnapShotId.of("boj", t); // equals true

        Map<SnapShotId, String> map = new HashMap<>();
        map.put(key1, "value");

        assertThat(map.get(key2)).isEqualTo("value");
    }

}