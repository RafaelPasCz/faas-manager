package com.portfolio.faasmanager.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import jakarta.persistence.*;

@Entity
@Table(name = "tb_function")
@EntityListeners(AuditingEntityListener.class)
public class Function {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Boolean active = true; 

    @Column(nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "docker_image", nullable = false, length = 200)
    private String dockerImage;

    @Column(nullable = false, length = 50)
    private String runtime;

    @CreatedDate
    @Column(name = "saved_at", nullable = false, updatable = false)
    private LocalDateTime savedAt;

    public Function() {}

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getDockerImage() { return dockerImage; }
    public String getRuntime() { return runtime; }
    public LocalDateTime getSavedAt() { return savedAt; }
    public Boolean getActive() { return active; }

    public void setName(String name) { this.name = name; }
    public void setDockerImage(String dockerImage) { this.dockerImage = dockerImage; }
    public void setRuntime(String runtime) { this.runtime = runtime; }
    public void setActive(Boolean active) { this.active = active; }
}