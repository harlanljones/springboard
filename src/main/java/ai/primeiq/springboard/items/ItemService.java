package ai.primeiq.springboard.items;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

/**
 * In-memory example store — deliberately not a database.
 *
 * <p>Persistence is a ROADMAP item (docs/ROADMAP.md), not a half-wired dependency:
 * when it lands, replace this class and keep {@link ItemsController} untouched.
 */
@Service
public class ItemService {

  private final List<Item> items =
      List.of(
          new Item(1, "First item", "active"),
          new Item(2, "Second item", "active"),
          new Item(3, "Third item", "archived"));

  public List<Item> findAll() {
    return items;
  }

  public Optional<Item> findById(long id) {
    return items.stream().filter(i -> i.id() == id).findFirst();
  }
}
