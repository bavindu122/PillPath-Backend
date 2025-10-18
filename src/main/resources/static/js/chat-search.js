document.addEventListener('DOMContentLoaded', function () {
  const input = document.getElementById('pharmacySearchInput');
  const btn = document.getElementById('searchBtn');
  const results = document.getElementById('pharmacySearchResults');
  const message = document.getElementById('pharmacySearchMessage');

  function renderNoResult() {
    results.innerHTML = '';
    message.textContent = 'No search result';
    message.className = 'no-result';
  }

  function renderResults(list) {
    message.textContent = '';
    results.innerHTML = '';
    list.forEach(ph => {
      const el = document.createElement('div');
      el.className = 'result';
      const left = document.createElement('div');
      left.innerHTML = `<strong>${ph.name}</strong><div style="font-size:12px;color:#666">${ph.address || ''}</div>`;
      const right = document.createElement('div');
      const btn = document.createElement('button');
      btn.textContent = 'Start chat';
      btn.addEventListener('click', () => startChat(ph.id));
      right.appendChild(btn);
      el.appendChild(left);
      el.appendChild(right);
      results.appendChild(el);
    });
  }

  function search(q) {
    results.innerHTML = '';
    message.textContent = '';
    if (!q || q.trim().length === 0) {
      renderNoResult();
      return;
    }

    fetch(`/api/v1/pharmacies/search-for-chat?q=${encodeURIComponent(q)}`)
      .then(res => res.json())
      .then(obj => {
        const list = obj.results || [];
        if (!list || list.length === 0) {
          renderNoResult();
        } else {
          renderResults(list);
        }
      })
      .catch(err => {
        console.error('Search failed', err);
        renderNoResult();
      });
  }

  btn?.addEventListener('click', () => search(input.value));
  input?.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') search(input.value);
  });

  function startChat(pharmacyId) {
    const customerId = window.CURRENT_USER_ID || null;
    const body = { pharmacyId };
    if (customerId) body.customerId = customerId;

    // include Authorization header when available (local dev)
    const headers = { 'Content-Type': 'application/json' };
    if (window.AUTH_TOKEN) headers['Authorization'] = 'Bearer ' + window.AUTH_TOKEN;

    fetch('/api/chats/start', {
      method: 'POST',
      headers,
      body: JSON.stringify(body)
    })
      .then(res => res.json())
      .then(chat => {
        if (chat && chat.id) {
          // Redirect to chat UI if exists, otherwise log result
          window.location.href = `/chat/${chat.id}`;
        } else if (chat && chat.error) {
          alert('Unable to start chat: ' + chat.error);
        } else {
          console.log('Chat started', chat);
        }
      })
      .catch(err => {
        console.error('Start chat failed', err);
        alert('Unable to start chat');
      });
  }

});
