import { useEffect, useState } from 'react';
import { api } from '../../api.js';
import { useCategories } from '../../store.jsx';
import { Alert, Spinner } from '../../ui.jsx';

const EMPTY = { name: '', brand: '', category: '', price: '', stock: '', description: '' };

/**
 * Create-product form.
 *
 * The create endpoint is multipart and a main image is required, so the admin writes the copy and
 * uploads the photograph.
 */
export default function AddProductForm({ onCreated, onCancel }) {
  // Shared list, so a category created moments ago on the Categories tab is already selectable.
  const { categories } = useCategories();

  const [form, setForm] = useState(EMPTY);
  const [imageFile, setImageFile] = useState(null);
  const [preview, setPreview] = useState('');
  const [busy, setBusy] = useState('');
  const [error, setError] = useState('');

  const set = (k) => (e) => setForm((f) => ({ ...f, [k]: e.target.value }));

  // Revoke the object URL when it is replaced, otherwise each picked image leaks a blob.
  useEffect(() => () => { if (preview.startsWith('blob:')) URL.revokeObjectURL(preview); }, [preview]);

  function pickFile(e) {
    const file = e.target.files?.[0];
    if (!file) return;
    setImageFile(file);
    setPreview(URL.createObjectURL(file));
  }

  async function submit(e) {
    e.preventDefault();
    if (!imageFile) {
      setError('A main image is required. Upload one to continue.');
      return;
    }
    setBusy('save'); setError('');
    try {
      const created = await api.adminCreateProduct(
        {
          name: form.name,
          description: form.description,
          price: form.price,
          stock: form.stock,
          category: form.category,
        },
        imageFile,
      );
      setForm(EMPTY);
      setImageFile(null);
      setPreview('');
      onCreated?.(created);
    } catch (err) {
      setError(err.message);
    } finally {
      setBusy('');
    }
  }

  return (
    <div className="panel" style={{ marginBottom: 16 }}>
      <div className="section-head">
        <h2>Add a product</h2>
      </div>

      {error && <div style={{ marginBottom: 12 }}><Alert>{error}</Alert></div>}

      <form className="stack" onSubmit={submit}>
        <div className="form-grid">
          <div className="field">
            <label className="label">Product name</label>
            <input className="input" value={form.name} onChange={set('name')} required placeholder="Trailblaze Running Shoes" />
          </div>
          <div className="field">
            <label className="label">Brand</label>
            <input className="input" value={form.brand} onChange={set('brand')} placeholder="Nike" />
          </div>
          <div className="field">
            <label className="label">Category</label>
            <select className="select" value={form.category} onChange={set('category')} required>
              <option value="">Select a category…</option>
              {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
            </select>
          </div>
          <div className="field">
            <label className="label">Price (₹)</label>
            <input className="input" type="number" min="0" step="0.01" value={form.price} onChange={set('price')} required />
          </div>
          <div className="field">
            <label className="label">Stock quantity</label>
            <input className="input" type="number" min="0" value={form.stock} onChange={set('stock')} required />
          </div>
        </div>

        <div className="field">
          <label className="label">Description</label>
          <textarea className="textarea" value={form.description} onChange={set('description')}
            placeholder="Describe the product for the listing page." />
        </div>

        <div className="field">
          <label className="label">Main image (required)</label>

          <div className="row wrap" style={{ gap: 14, alignItems: 'flex-start' }}>
            <div
              className="product-thumb"
              style={{ width: 140, height: 140, flexShrink: 0, border: '1px solid var(--line)' }}
            >
              {preview
                ? <img src={preview} alt="Product preview" style={{ objectFit: 'contain' }} />
                : <span className="tiny faint center">No image yet</span>}
            </div>

            <div className="stack" style={{ gap: 6 }}>
              <input type="file" accept="image/*" onChange={pickFile} />
              <span className="tiny muted">
                A square photo on a plain background looks best in the catalog grid.
              </span>
            </div>
          </div>
        </div>

        <div className="row">
          <button className="btn btn-cta" disabled={!!busy}>
            {busy === 'save' ? <Spinner /> : 'Create product'}
          </button>
          <button type="button" className="btn" onClick={onCancel} disabled={!!busy}>Cancel</button>
        </div>
      </form>
    </div>
  );
}
