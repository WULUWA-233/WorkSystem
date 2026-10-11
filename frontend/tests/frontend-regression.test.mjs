import test from 'node:test'
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import { createRequire } from 'node:module'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
import vm from 'node:vm'

const require = createRequire(import.meta.url)
const ts = require('typescript')
const pinia = require('pinia')
const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const clean = value => JSON.parse(JSON.stringify(value))

function fixture(options = {}) {
  const cache = new Map()
  const values = new Map(Object.entries(options.storage || {}))
  const localStorage = {
    getItem: key => values.get(key) ?? null,
    setItem: (key, value) => values.set(key, String(value)),
    removeItem: key => values.delete(key)
  }
  const piniaInstance = pinia.createPinia()
  pinia.setActivePinia(piniaInstance)
  const currentUser = { id: 2, username: 'sales', realName: '销售人员', roles: ['SALES'] }
  const authApi = {
    login: async () => ({ accessToken: 'fresh-token', user: currentUser }),
    getCurrentUser: options.getCurrentUser || (async () => currentUser),
    logout: async () => {}
  }
  const messages = [], navigations = [], patchCalls = []
  let requestHandler, responseHandler, errorHandler, guard
  const router = {
    currentRoute: { value: { path: '/customer/list', fullPath: '/customer/list?page=2' } },
    replace: async target => { navigations.push(target) },
    beforeEach: callback => { guard = callback }
  }
  const instance = {
    interceptors: {
      request: { use: callback => { requestHandler = callback } },
      response: { use: (success, failure) => { responseHandler = success; errorHandler = failure } }
    },
    patch: async (url, data) => { patchCalls.push({ url, data }) },
    get: async () => ({}), post: async () => ({}), put: async () => ({}), delete: async () => ({})
  }
  const stubs = {
    '@/api/auth': authApi,
    'pinia': { ...pinia, getActivePinia: () => piniaInstance },
    'axios': { __esModule: true, default: { create: () => instance } },
    'element-plus': { ElMessage: { error: message => messages.push(message) } },
    'vue-router': { createRouter: () => router, createWebHistory: () => ({}) },
  }
  const globals = { localStorage, document: { title: '' }, __viteEnv: { VITE_API_BASE_URL: '/api/v1' } }
  function load(relative) {
    if (cache.has(relative)) return cache.get(relative).exports
    const module = { exports: {} }
    cache.set(relative, module)
    const source = readFileSync(path.join(root, relative), 'utf8').replaceAll('import.meta.env', '__viteEnv')
    const compiled = ts.transpileModule(source, { compilerOptions: {
      target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.CommonJS, esModuleInterop: true
    } }).outputText
    function resolve(specifier) {
      if (stubs[specifier]) return stubs[specifier]
      if (specifier === '@/router') {
        options.onRouterImport?.(load('src/stores/auth.ts').useAuthStore(piniaInstance))
        return { __esModule: true, default: router }
      }
      if (specifier.startsWith('@/')) return load('src/' + specifier.slice(2) + '.ts')
      if (specifier.startsWith('.')) return load(path.posix.normalize(path.posix.join(path.posix.dirname(relative), specifier)) + '.ts')
      return require(specifier)
    }
    vm.runInNewContext(compiled, { ...globals, exports: module.exports, module, require: resolve }, { filename: relative })
    return module.exports
  }
  return {
    load, localStorage, values, messages, navigations, patchCalls, router, authApi,
    store: () => load('src/stores/auth.ts').useAuthStore(piniaInstance),
    request: config => requestHandler(config),
    response: response => responseHandler(response),
    failure: error => errorHandler(error),
    guard: to => guard(to)
  }
}

test('remembered account migration removes the old plaintext password', () => {
  const f = fixture({ storage: { crm_remember_login: JSON.stringify({ username: 'sales', password: 'secret', rememberMe: true }) } })
  const storage = f.load('src/utils/storage.ts')
  assert.equal(storage.getRememberedUsername(), 'sales')
  assert.deepEqual(JSON.parse(f.localStorage.getItem('crm_remember_login')), { username: 'sales', rememberMe: true })
  storage.setRememberedUsername(null)
  assert.equal(f.localStorage.getItem('crm_remember_login'), null)
})

test('select options include records beyond the server page limit', async () => {
  const f = fixture()
  const { fetchAllPages } = f.load('src/utils/pagination.ts')
  const records = Array.from({ length: 250 }, (_, index) => ({ id: index + 1 }))
  const queries = []
  const result = await fetchAllPages(async query => {
    queries.push(clean(query))
    return { total: records.length, records: records.slice((query.pageNo - 1) * query.pageSize, query.pageNo * query.pageSize) }
  }, { stage: 5 })
  assert.equal(result.length, 250)
  assert.deepEqual(queries, [
    { stage: 5, pageNo: 1, pageSize: 100 },
    { stage: 5, pageNo: 2, pageSize: 100 },
    { stage: 5, pageNo: 3, pageSize: 100 }
  ])
})

test('ignored pagination cannot cause an endless option request loop', async () => {
  const { fetchAllPages } = fixture().load('src/utils/pagination.ts')
  let calls = 0
  await assert.rejects(fetchAllPages(async () => {
    calls++
    return { total: 150, records: [{ id: 1 }] }
  }), /分页/)
  assert.equal(calls, 2)
})

test('order completion and cancellation send a JSON body', async () => {
  const f = fixture()
  const api = f.load('src/api/order.ts')
  await api.completeOrder(4)
  await api.cancelOrder(5)
  assert.deepEqual(clean(f.patchCalls), [
    { url: '/sales/orders/4/complete', data: {} },
    { url: '/sales/orders/5/cancel', data: {} }
  ])
})

test('pagination normalization preserves caller state and translates response fields', async () => {
  const f = fixture()
  f.load('src/api/request.ts')
  const params = { pageNo: 2, pageSize: 50, customerId: 3 }
  const config = await f.request({ params, headers: {} })
  assert.deepEqual(clean(params), { pageNo: 2, pageSize: 50, customerId: 3 })
  assert.deepEqual(clean(config.params), { page: 2, size: 50, customerId: 3 })
  const result = await f.response({ data: { code: 200, data: { page: 2, size: 50, total: 100, records: [] } } })
  assert.equal(result.pageNo, 2)
  assert.equal(result.pageSize, 50)
})

test('cached roles grant no write permission before server validation', async () => {
  const user = { id: 2, username: 'sales', realName: '销售', roles: ['SALES'] }
  const f = fixture({ storage: { crm_access_token: 'cached-token', crm_user: JSON.stringify(user) } })
  const store = f.store()
  assert.equal(store.isLoggedIn, false)
  assert.equal(store.canWriteBusiness, false)
  await store.fetchCurrentUser()
  assert.equal(store.isLoggedIn, true)
  assert.equal(store.canWriteBusiness, true)
})

test('unknown roles and auditors never gain business write permission', async () => {
  const f = fixture(), store = f.store()
  await store.loginAction('sales', 'password')
  store.setUser({ id: 2, username: 'other', realName: '其他', roles: ['USER'] })
  assert.equal(store.canWriteBusiness, false)
  store.setUser({ id: 2, username: 'auditor', realName: '审计', roles: ['ADMIN', 'AUDITOR'] })
  assert.equal(store.canWriteBusiness, false)
})

test('HTTP 401 clears both persistent and in-memory auth state', async () => {
  const f = fixture(), store = f.store()
  await store.loginAction('sales', 'password')
  f.load('src/api/request.ts')
  await assert.rejects(f.failure({
    response: { status: 401 },
    config: { url: '/customer/customers', headers: { Authorization: 'Bearer fresh-token' } }
  }))
  assert.equal(store.token, null)
  assert.equal(store.user, null)
  assert.equal(store.canWriteBusiness, false)
  assert.equal(f.localStorage.getItem('crm_access_token'), null)
  assert.deepEqual(clean(f.navigations), [{ name: 'Login', query: { redirect: '/customer/list?page=2' } }])
})

test('business 401 expires a session too', async () => {
  const f = fixture(), store = f.store()
  await store.loginAction('sales', 'password')
  f.load('src/api/request.ts')
  await assert.rejects(f.response({
    data: { code: 401, message: 'Unauthorized' },
    config: { url: '/auth/me', headers: { Authorization: 'Bearer fresh-token' } }
  }), /Unauthorized/)
  assert.equal(store.isLoggedIn, false)
})

test('an old token response cannot clear a newly signed-in session', async () => {
  const f = fixture(), store = f.store()
  await store.loginAction('sales', 'password')
  f.load('src/api/request.ts')
  await assert.rejects(f.failure({
    response: { status: 401 },
    config: { url: '/customer/customers', headers: { Authorization: 'Bearer old-token' } }
  }))
  assert.equal(store.token, 'fresh-token')
  assert.equal(store.isLoggedIn, true)
  assert.equal(f.navigations.length, 0)
})

test('bad login credentials do not trigger an expired-session redirect', async () => {
  const f = fixture()
  f.load('src/api/request.ts')
  await assert.rejects(f.failure({
    response: { status: 401, data: { message: '用户名或密码错误' } },
    config: { url: '/auth/login', headers: {} }
  }))
  assert.equal(f.navigations.length, 0)
  assert.deepEqual(f.messages, ['用户名或密码错误'])
})

test('a late current-user response cannot restore logged-out state', async () => {
  let finish
  const f = fixture({ getCurrentUser: () => new Promise(resolve => { finish = resolve }) })
  const store = f.store()
  store.setToken('cached-token')
  const pending = store.fetchCurrentUser()
  store.clearAuth()
  finish({ id: 2, username: 'sales', realName: '销售', roles: ['SALES'] })
  await pending
  assert.equal(store.user, null)
  assert.equal(store.token, null)
})

test('route guard redirects an authenticated user away from login', async () => {
  const f = fixture(), store = f.store()
  await store.loginAction('sales', 'password')
  f.load('src/router/index.ts')
  assert.deepEqual(clean(await f.guard({ name: 'Login', meta: { public: true }, fullPath: '/login' })), { name: 'Dashboard' })
})

test('route restoration refreshes stale cached roles before authorizing access', async () => {
  let fetches = 0
  const f = fixture({
    storage: { crm_access_token: 'cached-token', crm_user: JSON.stringify({ id: 2, username: 'sales', realName: '销售', roles: ['ADMIN'] }) },
    getCurrentUser: async () => { fetches++; return { id: 2, username: 'sales', realName: '销售', roles: ['SALES'] } }
  })
  f.load('src/router/index.ts')
  const result = await f.guard({ name: 'UserList', meta: { roles: ['ADMIN'] }, fullPath: '/system/users' })
  assert.deepEqual(clean(result), { name: 'Forbidden' })
  assert.equal(fetches, 1)
})

test('a failed old route restore cannot clear a later successful login', async () => {
  let rejectRestore
  const f = fixture({
    storage: { crm_access_token: 'old-token' },
    getCurrentUser: () => new Promise((_resolve, reject) => { rejectRestore = reject })
  })
  f.load('src/router/index.ts')
  const pending = f.guard({ name: 'CustomerList', meta: { roles: ['SALES'] }, fullPath: '/customer/list' })
  await f.store().loginAction('sales', 'password')
  rejectRestore(new Error('old session expired'))
  assert.equal(await pending, true)
  assert.equal(f.store().token, 'fresh-token')
  assert.equal(f.store().isLoggedIn, true)
})

test('an expiry redirect cannot override a login completed during router loading', async () => {
  const f = fixture({ onRouterImport: store => {
    store.setToken('new-session')
    store.setUser({ id: 3, username: 'new', realName: '新用户', roles: ['MANAGER'] })
    store.sessionValidated = true
  } })
  await f.store().loginAction('sales', 'password')
  f.load('src/api/request.ts')
  await assert.rejects(f.failure({ response: { status: 401 }, config: {
    url: '/auth/me', headers: { Authorization: 'Bearer fresh-token' }
  } }))
  assert.equal(f.store().token, 'new-session')
  assert.equal(f.navigations.length, 0)
  assert.equal(f.messages.length, 0)
})

test('simultaneous route restores share a single current-user request', async () => {
  let finish, calls = 0
  const f = fixture({ storage: { crm_access_token: 'cached-token' }, getCurrentUser: () => {
    calls++
    return new Promise(resolve => { finish = resolve })
  } })
  const store = f.store()
  const first = store.fetchCurrentUser(), second = store.fetchCurrentUser()
  assert.equal(first, second)
  finish({ id: 2, username: 'sales', realName: '销售', roles: ['SALES'] })
  await Promise.all([first, second])
  assert.equal(calls, 1)
  assert.equal(store.isLoggedIn, true)
})

test('an old failed restore cannot clear a new login with an identical token', async () => {
  let rejectRestore
  const f = fixture({ storage: { crm_access_token: 'fresh-token' },
    getCurrentUser: () => new Promise((_resolve, reject) => { rejectRestore = reject })
  })
  f.load('src/router/index.ts')
  const pending = f.guard({ name: 'CustomerList', meta: { roles: ['SALES'] }, fullPath: '/customer/list' })
  await f.store().loginAction('sales', 'password')
  rejectRestore(new Error('old session failed'))
  assert.equal(await pending, true)
  assert.equal(f.store().token, 'fresh-token')
})

test('an old HTTP 401 cannot clear a new login with an identical token', async () => {
  const f = fixture(), store = f.store()
  await store.loginAction('sales', 'password')
  f.load('src/api/request.ts')
  const oldRequest = await f.request({ url: '/auth/me', headers: {} })
  await store.loginAction('sales', 'password')
  await assert.rejects(f.failure({ response: { status: 401 }, config: oldRequest }))
  assert.equal(store.token, 'fresh-token')
  assert.equal(store.isLoggedIn, true)
  assert.equal(f.navigations.length, 0)
})


function viewFunction(relative, name, globals) {
  const source = readFileSync(path.join(root, relative), 'utf8')
  const script = source.split('<script setup lang="ts">')[1].split('</script>')[0]
  const ast = ts.createSourceFile(relative, script, ts.ScriptTarget.Latest, true)
  const declaration = ast.statements.find(node => ts.isFunctionDeclaration(node) && node.name?.text === name)
  assert.ok(declaration, `Missing ${name} in ${relative}`)
  const code = ts.transpileModule(declaration.getText(ast) + '\nmodule.exports = ' + name, {
    compilerOptions: { target: ts.ScriptTarget.ES2022, module: ts.ModuleKind.CommonJS }
  }).outputText
  const module = { exports: {} }
  vm.runInNewContext(code, { ...globals, module, console })
  return module.exports
}

const pagedViews = [
  ['order/OrderListView.vue', 'orderApi', 'getOrderPage'],
  ['customer/CustomerListView.vue', 'customerApi', 'getCustomerPage'],
  ['opportunity/OpportunityListView.vue', 'oppApi', 'getOpportunityPage'],
  ['followRecord/FollowRecordView.vue', 'followApi', 'getFollowRecordPage'],
  ['system/users/UserListView.vue', 'userApi', 'getUserPage'],
  ['system/roles/RoleListView.vue', 'roleApi', 'getRolePage'],
  ['system/operationLogs/OperationLogView.vue', 'logApi', 'getOperationLogPage']
]

for (const [relative, apiName, method] of pagedViews) {
  test(`${relative}: a slow old page cannot overwrite the current page or clear its loading state`, async () => {
    const pending = [], loading = { value: false }, tableData = { value: [] }, total = { value: 0 }
    const query = { pageNo: 1, pageSize: 10 }
    const fetchData = viewFunction('src/views/' + relative, 'fetchData', {
      query, loading, tableData, total, dateRange: { value: null }, expDateRange: { value: null },
      latestFetchId: 0,
      [apiName]: { [method]: () => new Promise(resolve => pending.push(resolve)) }
    })
    const first = fetchData()
    query.pageNo = 2
    const second = fetchData()
    pending[0]({ records: [{ id: 101 }], total: 21, pageNo: 1, pageSize: 10 })
    await first
    assert.equal(loading.value, true)
    assert.equal(tableData.value.length, 0)
    pending[1]({ records: [{ id: 201 }], total: 30, pageNo: 2, pageSize: 10 })
    await second
    assert.equal(tableData.value[0].id, 201)
    assert.equal(query.pageNo, 2)
    assert.equal(total.value, 30)
    assert.equal(loading.value, false)

    const third = fetchData()
    query.pageNo = 3
    const fourth = fetchData()
    pending[3]({ records: [{ id: 301 }], total: 40, pageNo: 3, pageSize: 10 })
    await fourth
    pending[2]({ records: [{ id: 201 }], total: 30, pageNo: 2, pageSize: 10 })
    await third
    assert.equal(tableData.value[0].id, 301)
    assert.equal(query.pageNo, 3)
    assert.equal(total.value, 40)
  })
}

for (const [relative, apiName, method] of [
  ['customer/CustomerDetailView.vue', 'customerApi', 'getCustomerOrders'],
  ['opportunity/OpportunityDetailView.vue', 'oppApi', 'getOpportunityOrders']
]) {
  test(`${relative}: related order paging ignores a late response`, async () => {
    const pending = [], orders = { value: [] }, orderTotal = { value: 0 }, orderQuery = { pageNo: 1, pageSize: 10 }
    const loadOrders = viewFunction('src/views/' + relative, 'loadOrders', {
      id: 2, orders, orderTotal, orderQuery, latestOrderFetchId: 0,
      [apiName]: { [method]: () => new Promise(resolve => pending.push(resolve)) }
    })
    const first = loadOrders()
    orderQuery.pageNo = 2
    const second = loadOrders()
    pending[1]({ records: [{ id: 201 }], total: 25 })
    await second
    pending[0]({ records: [{ id: 101 }], total: 25 })
    await first
    assert.equal(orders.value[0].id, 201)
  })
}

test('analytics filters ignore a delayed previous query and preserve latest chart data', async () => {
  const pending = [], loading = { value: false }, chartLoading = { value: false }
  const trendData = { value: [] }, salesData = { value: [] }
  const fetchAll = viewFunction('src/views/analytics/AnalyticsView.vue', 'fetchAll', {
    latestFetchId: 0, loading, chartLoading, trendData, salesData, stageData: { value: [] },
    orderStatusData: { value: [] }, industryData: { value: [] }, regionData: { value: [] },
    query: { groupBy: 'day' }, getTimeParams: () => ({}), getDateParams: () => ({}),
    analyticsApi: {
      getCustomerTrend: () => new Promise(resolve => pending.push(resolve)),
      getOpportunityStage: async () => [], getOrderStatus: async () => [],
      getSalesPerformance: async () => [], getCustomerIndustry: async () => [], getCustomerRegion: async () => []
    }
  })
  const first = fetchAll(), second = fetchAll()
  pending[0]([{ date: '2026-01-01', count: 1 }])
  await first
  assert.equal(loading.value, true)
  assert.equal(chartLoading.value, true)
  pending[1]([{ date: '2026-02-01', count: 2 }])
  await second
  assert.equal(trendData.value[0].count, 2)
  assert.equal(loading.value, false)
  assert.equal(chartLoading.value, false)
})

