import { api } from '../api.js';
import HeroCarousel from '../components/HeroCarousel.jsx';
import ProductCard from '../components/ProductCard.jsx';
import { Alert, Empty, ProductSkeletons, useAsync } from '../ui.jsx';

export default function Home() {
  const { data, loading, error } = useAsync(() => api.products(1, 24), []);
  const products = data?.content || [];

  return (
    <div className="page">
      <HeroCarousel />

      <section id="catalog">
        <div className="section-head">
          <h2>All products</h2>
          {data?.totalElements != null && (
            <span className="small muted">{data.totalElements} item{data.totalElements === 1 ? '' : 's'}</span>
          )}
        </div>

        {error && <Alert>{error}</Alert>}
        {loading && <ProductSkeletons count={8} />}

        {!loading && !error && (
          products.length === 0
            ? <Empty icon="🗂️" title="No products yet" >The catalog is empty. An admin can add products from the Admin Console.</Empty>
            : <div className="grid">{products.map((p) => <ProductCard key={p.id} product={p} />)}</div>
        )}
      </section>
    </div>
  );
}
