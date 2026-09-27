// Offline cache for Italian Tutor.
//
// The whole app (all lessons included) lives in index.html, so caching that
// one file plus the PWA assets is enough to run the app with no internet.
//
// Strategy: network-first for same-origin GET requests, falling back to the
// cache when offline. Every successful network response is copied into the
// cache, so the offline copy stays up to date as long as the user opens the
// app online now and then. Bump CACHE_NAME to force old caches to be dropped.
const CACHE_NAME = 'italian-tutor-v4';
const OWN = 'italian-tutor-';   // only ever delete THIS app's old caches – every DSR app shares the github.io origin's cache storage
const CORE_ASSETS = [
  './',
  './index.html',
  './manifest.json',
  './icon.svg',
];

self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then((cache) => cache.addAll(CORE_ASSETS))
      .then(() => self.skipWaiting())
  );
});

self.addEventListener('activate', (event) => {
  event.waitUntil(
    caches.keys()
      .then((keys) => Promise.all(
        keys.filter((key) => key !== CACHE_NAME && key.indexOf(OWN) === 0).map((key) => caches.delete(key))
      ))
      .then(() => self.clients.claim())
  );
});

self.addEventListener('fetch', (event) => {
  const req = event.request;
  if (req.method !== 'GET') return;

  const url = new URL(req.url);
  if (url.origin !== self.location.origin) return;

  event.respondWith(
    fetch(req)
      .then((res) => {
        if (res && res.ok) {
          const copy = res.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(req, copy));
        }
        return res;
      })
      .catch(() => caches.match(req).then((cached) => {
        if (cached) return cached;
        // Fall back to the app shell for navigations made while offline.
        if (req.mode === 'navigate') return caches.match('./index.html');
        return Response.error();
      }))
  );
});
