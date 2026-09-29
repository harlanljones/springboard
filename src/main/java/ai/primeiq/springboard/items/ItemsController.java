package ai.primeiq.springboard.items;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/**
 * The example API slice the React app consumes. Copy this shape for new resources:
 * constructor injection, records in/out, errors as exceptions (rendered as RFC 9457
 * problem details because {@code spring.mvc.problemdetails.enabled=true}).
 */
@RestController
@RequestMapping("/api/items")
class ItemsController {

  private final ItemService itemService;

  ItemsController(ItemService itemService) {
    this.itemService = itemService;
  }

  @GetMapping
  List<Item> list() {
    return itemService.findAll();
  }

  @GetMapping("/{id}")
  Item get(@PathVariable long id) {
    return itemService
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No item with id " + id));
  }
}
