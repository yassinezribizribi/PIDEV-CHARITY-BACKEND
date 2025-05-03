package tn.esprit.examen.nomPrenomClasseExamen.Configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Iterate through existing converters
        for (HttpMessageConverter<?> converter : converters) {
            if (converter instanceof MappingJackson2HttpMessageConverter) {
                MappingJackson2HttpMessageConverter jsonConverter =
                        (MappingJackson2HttpMessageConverter) converter;
                List<MediaType> supported = new ArrayList<>(jsonConverter.getSupportedMediaTypes());
                // Add the charset variant if it's not already there
                MediaType jsonUtf8 = MediaType.valueOf("application/json;charset=UTF-8");
                if (!supported.contains(jsonUtf8)) {
                    supported.add(jsonUtf8);
                }
                jsonConverter.setSupportedMediaTypes(supported);
            }
        }
    }
}
