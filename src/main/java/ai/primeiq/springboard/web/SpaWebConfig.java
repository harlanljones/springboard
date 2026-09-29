package ai.primeiq.springboard.web;

import java.io.IOException;
import java.util.Set;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.resource.PathResourceResolver;

/**
 * Serves the built React app out of {@code classpath:/static} and forwards unknown
 * client-side routes to {@code index.html} so React Router deep links work.
 *
 * <p>Two guard rails, both load-bearing:
 *
 * <ul>
 *   <li>{@code /api/**} and {@code /actuator/**} are never answered with HTML — an unknown
 *       API path must stay a JSON 404, otherwise a typo'd endpoint silently returns the SPA
 *       and the frontend parses an HTML page as JSON.
 *   <li>Paths that look like files (contain a dot) are never rewritten to {@code index.html},
 *       so a missing asset is a 404 instead of "HTML served as JavaScript".
 * </ul>
 */
@Configuration
class SpaWebConfig implements WebMvcConfigurer {

  private static final Set<String> RESERVED_PREFIXES = Set.of("api/", "actuator/");

  @Override
  public void addResourceHandlers(ResourceHandlerRegistry registry) {
    registry
        .addResourceHandler("/**")
        .addResourceLocations("classpath:/static/")
        .resourceChain(true)
        .addResolver(new SpaFallbackResolver());
  }

  private static final class SpaFallbackResolver extends PathResourceResolver {

    @Override
    protected Resource getResource(String resourcePath, Resource location) throws IOException {
      Resource requested = location.createRelative(resourcePath);
      if (requested.exists() && requested.isReadable()) {
        return requested;
      }
      if (isReserved(resourcePath) || looksLikeFile(resourcePath)) {
        return null;
      }
      Resource index = new ClassPathResource("/static/index.html");
      // Absent in a backend-only checkout (no `cd web && bun run build` yet): stay a 404
      // rather than blowing up the resource chain.
      return index.exists() ? index : null;
    }

    private static boolean isReserved(String resourcePath) {
      return RESERVED_PREFIXES.stream().anyMatch(resourcePath::startsWith);
    }

    private static boolean looksLikeFile(String resourcePath) {
      String lastSegment = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
      return lastSegment.contains(".");
    }
  }
}
