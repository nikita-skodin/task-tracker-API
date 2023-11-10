package com.skodin.security;

import com.skodin.exceptions.BadRequestException;
import com.skodin.models.ProjectEntity;
import com.skodin.models.UserEntity;
import com.skodin.services.ProjectService;
import com.skodin.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("projectSecurityExpression")
@RequiredArgsConstructor
public class ProjectSecurityExpression {

    private final ProjectService projectService;

    public boolean checkUserProjectAccess(Long id) {
        ProjectEntity project = projectService.findById(id);
        System.err.println("checkUserProjectAccess");
        System.err.println(project);
        UserEntity user = UserService.getCurrentUser();
        return Objects.equals(project.getUser().getId(), user.getId());
    }

}


