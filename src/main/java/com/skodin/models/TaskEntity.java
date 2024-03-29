package com.skodin.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Entity
@Getter
@Setter
@ToString(exclude = {"taskStateEntity"})
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "task")
public class TaskEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @NotBlank(message = "name should not be empty")
    @Size(min = 3, max = 20,
            message = "name`s length should be between 3 and 20 chars")
    String name;

    @Builder.Default
    Instant createdAt = Instant.now();

    @Size(max = 100,
            message = "description`s length should be less than 100")
    String description;

    @ManyToOne
    @JoinColumn(name = "task_state_id", referencedColumnName = "id")
    TaskStateEntity taskStateEntity;

}
