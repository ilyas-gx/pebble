package io.pebbletemplates.boot.autoconfigure;

import io.pebbletemplates.pebble.PebbleEngine;
import io.pebbletemplates.pebble.attributes.methodaccess.MethodAccessValidator;
import io.pebbletemplates.pebble.extension.Extension;
import io.pebbletemplates.pebble.loader.ClasspathLoader;
import io.pebbletemplates.pebble.loader.Loader;
import io.pebbletemplates.spring.extension.SpringExtension;
import java.util.List;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.lang.Nullable;

@AutoConfiguration
@ConditionalOnClass(PebbleEngine.class)
@EnableConfigurationProperties(PebbleProperties.class)
@Import({PebbleServletWebConfiguration.class, PebbleReactiveWebConfiguration.class})
public class PebbleAutoConfiguration extends AbstractPebbleConfiguration {

  @Bean
  @ConditionalOnMissingBean(name = "pebbleLoader")
  public Loader<?> pebbleLoader(PebbleProperties properties) {
    ClasspathLoader loader = new ClasspathLoader();
    loader.setCharset(properties.getCharsetName());
    // classpath loader does not like leading slashes in resource paths
    loader.setPrefix(this.stripLeadingSlash(properties.getPrefix()));
    loader.setSuffix(properties.getSuffix());
    return loader;
  }

  @Bean
  @ConditionalOnMissingBean
  public SpringExtension springExtension(MessageSource messageSource) {
    return new SpringExtension(messageSource);
  }

  @Bean
  @ConditionalOnMissingBean(name = "pebbleEngine")
  public PebbleEngine pebbleEngine(PebbleProperties properties,
      Loader<?> pebbleLoader,
      SpringExtension springExtension,
      @Nullable List<Extension> extensions,
      @Nullable MethodAccessValidator methodAccessValidator) {
    PebbleEngine.Builder builder = new PebbleEngine.Builder();
    builder.loader(pebbleLoader);
    builder.extension(springExtension);
    if (extensions != null && !extensions.isEmpty()) {
      builder.extension(extensions.toArray(new Extension[extensions.size()]));
    }
    if (!properties.isCache()) {
      builder.cacheActive(false);
    }
    if (properties.getDefaultLocale() != null) {
      builder.defaultLocale(properties.getDefaultLocale());
    }
    builder.strictVariables(properties.isStrictVariables());
    builder.greedyMatchMethod(properties.isGreedyMatchMethod());
    if (methodAccessValidator != null) {
      builder.methodAccessValidator(methodAccessValidator);
    }
    return builder.build();
  }
}
