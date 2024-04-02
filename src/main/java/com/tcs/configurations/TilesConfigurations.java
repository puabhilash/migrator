/**
 * @author Pulluri.Abhilash
 * */
package com.tcs.configurations;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesConfigurer;
import org.springframework.web.servlet.view.tiles3.TilesViewResolver;
import org.thymeleaf.spring5.SpringTemplateEngine;

/**
 * The Class TilesConfigurations.
 */
@EnableWebMvc
@Configuration
public class TilesConfigurations implements WebMvcConfigurer{
	
	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(TilesConfigurations.class);
	
	/** The application context. */
	@Resource
    protected ApplicationContext applicationContext;

    /** The spring template engine. */
    @Resource
    protected SpringTemplateEngine springTemplateEngine;
	
    /**
     * Tiles configurer.
     *
     * @return the tiles configurer
     */
    @Bean
    public TilesConfigurer tilesConfigurer() {
		LOGGER.info("configuring tiles");
        final TilesConfigurer tilesConfigurer = new TilesConfigurer();
        tilesConfigurer.setDefinitions(new String[] { "/WEB-INF/views/tiles/tiles.xml" });
        tilesConfigurer.setCheckRefresh(true);
        return tilesConfigurer;
    }
	
	/**
	 * Configure ViewResolvers to deliver views.
	 *
	 * @param registry the registry
	 */
    @Override
    public void configureViewResolvers(final ViewResolverRegistry registry) {
        TilesViewResolver viewResolver = new TilesViewResolver();
        registry.jsp("/WEB-INF/views/html/", ".jsp");
        viewResolver.setOrder(1);
        registry.viewResolver(viewResolver);
    }

    /**
     * Configure ResourceHandlers to serve static resources.
     *
     * @param registry the registry
     */

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/res/**").addResourceLocations("classpath:/static/");
    }
    
    /**
     * View resolver.
     *
     * @return the view resolver
     */
    @Bean
    public ViewResolver viewResolver() {
        TilesViewResolver viewResolver = new TilesViewResolver();
        viewResolver.setCacheUnresolved(false);
        viewResolver.setOrder(0);
        return viewResolver;
    }
    
}
