package com.jotechhub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/profile-images/**")
                .addResourceLocations("file:uploads/profile-images/");

        registry.addResourceHandler("/uploads/organizers/**")
                .addResourceLocations("file:uploads/organizers/");
    }
}