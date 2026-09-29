package ai.primeiq.springboard.items;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class ItemsApiTest {

  @Autowired private MockMvc mockMvc;

  @Test
  void listsItemsAsJson() throws Exception {
    mockMvc
        .perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(3)))
        .andExpect(jsonPath("$[0].id").value(1))
        .andExpect(jsonPath("$[0].name").value("First item"));
  }

  @Test
  void returnsSingleItem() throws Exception {
    mockMvc
        .perform(get("/api/items/2"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("active"));
  }

  @Test
  void unknownItemIsProblemDetailNotFound() throws Exception {
    mockMvc
        .perform(get("/api/items/999"))
        .andExpect(status().isNotFound())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
        .andExpect(jsonPath("$.status").value(404))
        .andExpect(jsonPath("$.detail").value("No item with id 999"));
  }
}
