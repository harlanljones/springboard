import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { listItems, type Item } from "../api/client";

export default function ItemsPage() {
  const [items, setItems] = useState<Item[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let active = true;
    listItems()
      .then((data) => active && setItems(data))
      .catch((cause: unknown) => active && setError(String(cause)));
    return () => {
      active = false;
    };
  }, []);

  if (error !== null) {
    return <p className="error">Could not load items: {error}</p>;
  }
  if (items === null) {
    return <p className="muted">Loading…</p>;
  }

  return (
    <>
      <h1>Items</h1>
      <p className="muted">Fetched from GET /api/items (Spring Boot on :8080).</p>
      <ul className="items">
        {items.map((item) => (
          <li key={item.id}>
            <Link to={`/items/${item.id}`}>{item.name}</Link>
            <span className={`tag tag-${item.status}`}>{item.status}</span>
          </li>
        ))}
      </ul>
    </>
  );
}
