import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import { getItem, type Item } from "../api/client";

/**
 * A client-side route with a path parameter: hitting /items/1 directly is what proves the
 * backend's SPA fallback works (see SpaWebConfig and SpaRoutingTest).
 */
export default function ItemDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [item, setItem] = useState<Item | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (id === undefined) return;
    let active = true;
    getItem(id)
      .then((data) => active && setItem(data))
      .catch((cause: unknown) => active && setError(String(cause)));
    return () => {
      active = false;
    };
  }, [id]);

  if (error !== null) {
    return <p className="error">Could not load item: {error}</p>;
  }
  if (item === null) {
    return <p className="muted">Loading…</p>;
  }

  return (
    <>
      <h1>{item.name}</h1>
      <dl>
        <dt>id</dt>
        <dd>{item.id}</dd>
        <dt>status</dt>
        <dd>{item.status}</dd>
      </dl>
      <Link to="/">← All items</Link>
    </>
  );
}
