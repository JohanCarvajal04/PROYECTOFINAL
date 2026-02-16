package com.app.uteq.Entity;

import java.time.LocalDateTime;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(exclude = {"roles", "credentials"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "users")
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "iduser")
    @EqualsAndHashCode.Include
    private Integer idUser;

    @Column(name = "names", nullable = false, length = 255)
    private String names;

    @Column(name = "surnames", nullable = false, length = 255)
    private String surnames;

    @Column(name = "cardid", nullable = false, length = 10, unique = true)
    private String cardId;

    @Column(name = "institutionalemail", nullable = false, length = 255, unique = true)
    private String institutionalEmail;

    @Column(name = "personalmail", length = 255, unique = true)
    private String personalMail;

    @Column(name = "phonenumber", length = 15)
    private String phoneNumber;

    @Column(name = "statement", nullable = false)
    @Builder.Default
    private Boolean statement = true;

    @ManyToOne
    @JoinColumn(name = "configurationsidconfiguration", nullable = false)
    private Configurations configuration;

    @OneToOne
    @JoinColumn(name = "credentialsidcredentials")
    private Credentials credentials;

    @Column(name = "createdat", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updatedat")
    private LocalDateTime updatedAt;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "iduser"), inverseJoinColumns = @JoinColumn(name = "idrole"))
    private Set<Roles> roles;
}
