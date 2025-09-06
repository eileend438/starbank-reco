package com.starbank.reco.management;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/management")
public class InfoController {

    private final BuildProperties build;
    private final String appName;

    public InfoController(ObjectProvider<BuildProperties> buildProvider,
                          @Value("${spring.application.name:starbank-reco}") String appName) {
        this.build = buildProvider.getIfAvailable();
        this.appName = appName;
    }

    @GetMapping("/info")
    public Map<String, String> info() {
        return Map.of(
                "name", appName,
                "version", build != null ? build.getVersion() : "dev"
        );
    }
}
