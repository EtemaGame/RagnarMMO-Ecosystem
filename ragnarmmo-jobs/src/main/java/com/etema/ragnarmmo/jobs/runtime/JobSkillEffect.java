package com.etema.ragnarmmo.jobs.runtime;

@FunctionalInterface
public interface JobSkillEffect {
    boolean execute(JobSkillContext context);
}
