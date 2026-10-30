package com.vriddhi.auth.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String name; // LEARNER, TEACHER, ADMIN, CONTENT_REVIEWER, SUPER_ADMIN, MENTOR

    @Column(length = 255)
    private String description;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    /** Spring Security expects role authorities prefixed with ROLE_. */
    public String toAuthority() {
        return "ROLE_" + name;
    }
}
