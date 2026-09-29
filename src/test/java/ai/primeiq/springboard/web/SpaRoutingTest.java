package ai.primeiq.springboard.web;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Guards the SPA routing contract that SpaWebConfig implements. Without these three
 * assertions a change to the resource resolver can silently break deep links or, worse,
 * start answering API typos with HTML.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SpaRoutingTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void rootIsForwardedToTheWelcomePage() throws Exception {
    // Boot's welcome-page support forwards "/" to index.html instead of writing the body,
    // so the assertion is the forward; the deep-link test below proves the content served.
    mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(forwardedUrl("index.html"));
  }

  @Test
  void clientSideDeepLinkFallsBackToIndexHtml() throws Exception {
    mockMvc
        .perform(get("/items/42"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_HTML))
        .andExpect(content().string(containsString("SPA-FIXTURE-INDEX")));
  }

  @Test
  void unknownApiPathStaysJsonNotFound() throws Exception {
    mockMvc
        .perform(get("/api/nope"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404));
  }

  @Test
  void missingAssetIsNotFoundNotHtml() throws Exception {
    mockMvc
        .perform(get("/assets/does-not-exist.js"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON));
  }
}
