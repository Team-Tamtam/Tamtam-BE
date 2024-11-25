package tamtam.mooney.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import tamtam.mooney.global.common.BaseTimeEntity;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class User extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long userId;

    @Column(unique = true)
    @NotNull
    private String email;

    @NotNull
    private String loginType;
    @NotNull
    private String nickname;

    private LocalDate birth;

    @Enumerated(EnumType.STRING)
    @NotNull
    private Role role;

    @ColumnDefault("true")
    @NotNull
    private Boolean isPushAlarmEnabled;

    @Builder
    public User(Long userId, String email, String loginType, String nickname,
                LocalDate birth) {
        this.userId = userId;
        this.email = email;
        this.loginType = loginType;
        this.nickname = nickname;
        this.birth = birth;
        this.role = Role.ROLE_USER;
        this.isPushAlarmEnabled = true;
    }
}