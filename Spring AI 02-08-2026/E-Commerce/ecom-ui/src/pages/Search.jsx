import { useSearchParams } from 'react-router-dom';
import { api } from '../api.js';
import ProductCard from '../components/ProductCard.jsx';
import { Alert, Empty, ProductSkeletons, useAsync } from '../ui.jsx';

/**
 * Keyword search results.
 *
 * The backend matches the words the shopper typed against product names, descriptions and category
 * names. A query phrased differently from the catalog text finds nothing, which is the limit of
 * keyword search.
 */
export default function Search() {
  const [params] = useSearchParams();
  const q = params.get('q') || '';

  const { data, loading, error } = useAsync(() => (q ? api.search(q) : Promise.resolve([])), [q]);
  const products = data || [];

  return (
    <div className="page">
      <div className="section-head">
        <div>
          <h1>Results for “{q}”</h1>
          {!loading && !error && (
            <p className="small muted" style={{ marginTop: 4 }}>
              {products.length} match{products.length === 1 ? '' : 'es'} in the catalog.
            </p>
          )}
        </div>
      </div>

      {error && <Alert>{error}</Alert>}
      {loading && <ProductSkeletons count={4} />}

      {!loading && !error && (
        products.length === 0
          ? (
            <Empty icon="🔍" title="No matches found">
              Nothing in the catalog contains “{q}”. Try a product name, a brand or a category.
            </Empty>
          )
          : <div className="grid">{products.map((p) => <ProductCard key={p.id} product={p} />)}</div>
      )}
    </div>
  );
}
