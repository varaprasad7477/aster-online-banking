const $ = (id) => document.getElementById(id);
const money = (value) => new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(value);
let token = sessionStorage.getItem('asterToken');
let account = null;
let transactions = [];
let action = 'transfer';
let registering = false;
let toastTimer;

function toast(message, error = false) {
  const box = $('toast');
  box.textContent = message;
  box.classList.toggle('error', error);
  box.hidden = false;
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => { box.hidden = true; }, 4200);
}

async function request(path, options = {}) {
  const response = await fetch(path, {
    ...options,
    headers: { 'Content-Type': 'application/json', ...(token ? { Authorization: `Bearer ${token}` } : {}), ...options.headers }
  });
  const contentType = response.headers.get('content-type') || '';
  const data = contentType.includes('json') ? await response.json() : await response.text();
  if (!response.ok) {
    if (response.status === 401 && !path.includes('/auth/')) signOut(false);
    throw new Error(data?.error || (typeof data === 'string' && data) || `Request failed (${response.status})`);
  }
  return data;
}

function signOut(notify = true) {
  token = null;
  sessionStorage.removeItem('asterToken');
  $('app').hidden = true;
  $('auth').hidden = false;
  $('modal').hidden = true;
  if (notify) toast('You have signed out.');
}

function setMode(register) {
  registering = register;
  $('register-fields').hidden = !register;
  $('auth-title').textContent = register ? 'Start something good.' : 'Good to see you again.';
  $('auth-subtitle').textContent = register ? 'Create your Aster account in a few simple steps.' : 'Sign in to your account to see the full picture.';
  $('auth-submit').firstChild.textContent = register ? 'Create account ' : 'Sign in ';
  $('auth-switch-text').textContent = register ? 'Already have an account?' : 'New to Aster?';
  $('switch-mode').textContent = register ? 'Sign in' : 'Create an account';
  $('demo-login').hidden = register;
  for (const input of $('register-fields').querySelectorAll('input')) input.required = register;
}

async function login(email, password) {
  const result = await request('/api/auth/login', { method: 'POST', body: JSON.stringify({ email, password }) });
  token = result.token;
  sessionStorage.setItem('asterToken', token);
  await loadDashboard();
}

async function loadDashboard() {
  try {
    const [profile, details, history] = await Promise.all([
      request('/api/account/profile'), request('/api/account/details'), request('/api/account/transactions')
    ]);
    account = details;
    transactions = history;
    const firstName = profile.fullName.split(' ')[0];
    const hour = new Date().getHours();
    $('greeting').firstChild.textContent = `Good ${hour < 12 ? 'morning' : hour < 17 ? 'afternoon' : 'evening'}, ${firstName} `;
    $('avatar').textContent = profile.fullName.split(' ').slice(0, 2).map(word => word[0]).join('').toUpperCase();
    $('balance').textContent = money(details.balance);
    $('account-number').textContent = String(details.accountNumber).replace(/(\d{4})(?=\d)/g, '$1 ');
    $('account-type').textContent = `${details.accountType[0]}${details.accountType.slice(1).toLowerCase()} account`;
    $('today').textContent = new Intl.DateTimeFormat('en-IN', { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }).format(new Date());
    renderList('recent-list', transactions.slice(0, 4));
    renderActivity();
    $('auth').hidden = true;
    $('app').hidden = false;
  } catch (error) {
    toast(error.message, true);
  }
}

function transactionInfo(tx) {
  switch (tx.type) {
    case 'CREDIT': return { title: 'Money added', inbound: true, icon: '＋' };
    case 'DEBIT': return { title: 'Withdrawal', inbound: false, icon: '↙' };
    case 'CREDIT_TRANSFER': return { title: 'Transfer received', inbound: true, icon: '↘' };
    default: return { title: 'Transfer sent', inbound: false, icon: '↗' };
  }
}

function renderList(id, items) {
  const list = $(id);
  list.replaceChildren();
  if (!items.length) {
    const empty = document.createElement('div');
    empty.className = 'empty-state';
    empty.innerHTML = '<span class="empty-symbol">✳</span>No transactions yet. Your activity will show up here.';
    list.append(empty);
    return;
  }
  for (const tx of items) {
    const info = transactionInfo(tx);
    const row = document.createElement('div');
    row.className = 'transaction-row';
    const icon = document.createElement('span');
    icon.className = `transaction-icon ${info.inbound ? '' : 'out'}`;
    icon.textContent = info.icon;
    const main = document.createElement('div');
    main.className = 'transaction-main';
    const title = document.createElement('strong');
    title.textContent = info.title;
    const detail = document.createElement('small');
    const date = tx.time ? new Intl.DateTimeFormat('en-IN', { day: 'numeric', month: 'short', year: 'numeric', hour: 'numeric', minute: '2-digit' }).format(new Date(tx.time)) : 'Just now';
    detail.textContent = `${date}${tx.receiverAccount ? ` · Account ${tx.receiverAccount}` : ''}`;
    main.append(title, detail);
    const amount = document.createElement('span');
    amount.className = `transaction-amount ${info.inbound ? 'in' : ''}`;
    amount.textContent = `${info.inbound ? '+' : '−'}${money(tx.amount)}`;
    row.append(icon, main, amount);
    list.append(row);
  }
}

function renderActivity() {
  const filter = $('activity-filter').value;
  renderList('activity-list', transactions.filter(tx => filter === 'all' || (transactionInfo(tx).inbound ? 'in' : 'out') === filter));
}

function showView(view) {
  $('overview-view').hidden = view !== 'overview';
  $('activity-view').hidden = view !== 'activity';
  $('page-label').textContent = view === 'overview' ? 'Overview' : 'Activity';
  for (const button of document.querySelectorAll('.nav-item')) button.classList.toggle('active', button.dataset.view === view);
}

function openAction(nextAction) {
  action = nextAction;
  const config = {
    transfer: ['↗', 'Send money', 'Move funds to another Aster account.', 'Send money'],
    deposit: ['＋', 'Add money', 'Add funds to this demo account.', 'Add money'],
    withdraw: ['↙', 'Withdraw money', 'Take funds from your available balance.', 'Withdraw']
  }[action];
  $('modal-symbol').textContent = config[0];
  $('modal-title').textContent = config[1];
  $('modal-description').textContent = config[2];
  $('action-submit').firstChild.textContent = `${config[3]} `;
  $('recipient-wrap').hidden = action !== 'transfer';
  $('action-form').reset();
  $('modal').hidden = false;
  $('action-form').querySelector(action === 'transfer' ? '[name="receiverAccount"]' : '[name="amount"]').focus();
}

$('auth-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const form = event.currentTarget;
  const data = Object.fromEntries(new FormData(form));
  const button = $('auth-submit');
  button.disabled = true;
  try {
    if (registering) {
      await request('/api/auth/register', { method: 'POST', body: JSON.stringify({ ...data, phoneNumber: Number(data.phoneNumber) }) });
      toast('Account created. Welcome to Aster!');
    }
    await login(data.email, data.password);
  } catch (error) { toast(error.message, true); }
  finally { button.disabled = false; }
});
$('demo-login').addEventListener('click', async () => {
  $('demo-login').disabled = true;
  try { await login('alex@aster.demo', 'AsterDemo2026!'); }
  catch (error) { toast(error.message, true); }
  finally { $('demo-login').disabled = false; }
});
$('switch-mode').addEventListener('click', () => setMode(!registering));
$('logout').addEventListener('click', () => signOut());
for (const button of document.querySelectorAll('.nav-item')) button.addEventListener('click', () => showView(button.dataset.view));
for (const button of document.querySelectorAll('[data-action]')) button.addEventListener('click', () => openAction(button.dataset.action));
$('view-all').addEventListener('click', () => showView('activity'));
$('activity-filter').addEventListener('change', renderActivity);
$('copy-account').addEventListener('click', async () => {
  try { await navigator.clipboard.writeText(String(account.accountNumber)); toast('Account number copied.'); }
  catch { toast(`Account number: ${account.accountNumber}`); }
});
$('modal-close').addEventListener('click', () => { $('modal').hidden = true; });
$('modal').addEventListener('click', (event) => { if (event.target === $('modal')) $('modal').hidden = true; });
document.addEventListener('keydown', (event) => { if (event.key === 'Escape') $('modal').hidden = true; });
$('action-form').addEventListener('submit', async (event) => {
  event.preventDefault();
  const fields = new FormData(event.currentTarget);
  const amount = Number(fields.get('amount'));
  if (!Number.isFinite(amount) || amount <= 0 || Math.round(amount * 100) !== amount * 100) return toast('Enter a valid amount with up to two decimal places.', true);
  const button = $('action-submit');
  button.disabled = true;
  try {
    if (action === 'transfer') {
      const receiverAccount = Number(fields.get('receiverAccount'));
      if (!Number.isSafeInteger(receiverAccount) || receiverAccount <= 0) throw new Error('Enter a valid recipient account number.');
      await request('/api/transaction/transfer', { method: 'POST', body: JSON.stringify({ amount, receiverAccount }) });
    } else {
      const type = action === 'deposit' ? 'CREDIT' : 'DEBIT';
      await request(`/api/transaction/${type}?amount=${encodeURIComponent(amount)}`, { method: 'POST' });
    }
    $('modal').hidden = true;
    await loadDashboard();
    toast(`${action === 'transfer' ? 'Transfer' : action === 'deposit' ? 'Deposit' : 'Withdrawal'} completed successfully.`);
  } catch (error) { toast(error.message, true); }
  finally { button.disabled = false; }
});

if (token) loadDashboard();
