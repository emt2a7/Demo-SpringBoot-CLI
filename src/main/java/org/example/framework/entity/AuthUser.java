package org.example.framework.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "auth_user")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthUser {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "name", unique = true, length = 100)
    private String name;

    @Column(name = "age")
    private Integer age;

    @CreationTimestamp
    @Column(name = "create_time", updatable = false)
    private OffsetDateTime createTime;

    @Column(name = "create_user", length = 100, updatable = false)
    private String createUser;

    @UpdateTimestamp
    @Column(name = "update_time")
    private OffsetDateTime updateTime;

    @Column(name = "update_user", length = 100, insertable = false)
    private String updateUser;
}

