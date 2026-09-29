import { Link, Route, Routes } from "react-router-dom";
import ItemsPage from "./pages/ItemsPage";
import ItemDetailPage from "./pages/ItemDetailPage";

export default function App() {
  return (
    <div className="shell">
      <header className="bar">
        <Link to="/" className="brand">
          springboard
        </Link>
        <nav>
          <Link to="/">Items</Link>
        </nav>
      </header>
      <main>
        <Routes>
          <Route path="/" element={<ItemsPage />} />
          <Route path="/items/:id" element={<ItemDetailPage />} />
        </Routes>
      </main>
    </div>
  );
}
