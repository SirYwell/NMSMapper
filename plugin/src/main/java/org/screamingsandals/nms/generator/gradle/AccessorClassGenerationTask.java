package org.screamingsandals.nms.generator.gradle;

import lombok.SneakyThrows;
import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.screamingsandals.nms.generator.build.AccessorClassGenerator;
import org.screamingsandals.nms.generator.configuration.NMSMapperConfiguration;

public abstract class AccessorClassGenerationTask extends DefaultTask {
    @SneakyThrows
    @TaskAction
    public void run() {
        var extension = getProject().getExtensions().getByType(NMSMapperConfiguration.class);
        var projectFolder = getProject().getProjectDir();

        var generator = new AccessorClassGenerator(extension, projectFolder);
        generator.run();
    }
}