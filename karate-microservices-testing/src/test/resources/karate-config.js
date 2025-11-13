function fn() {
  var env = karate.env || 'dev';
  var base = karate.properties['baseUrl'] || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080';
  var authBase = karate.properties['authBaseUrl'] || java.lang.System.getenv('AUTH_BASE_URL') || (base + '/api/v1/auth');
  var mockServerUrl = karate.properties['mockServerUrl'] || java.lang.System.getenv('MOCK_SERVER_URL') || 'http://localhost:8090';
  var useMockFlag = ('' + (karate.properties['mock.use'] || java.lang.System.getenv('USE_MOCK') || 'false')).toLowerCase() === 'true';

  var config = {
    env: env,
    baseUrl: base,
    authBaseUrl: authBase,
    mockServerUrl: mockServerUrl,
    timeout: 30000,
    retryConfig: { count: 5, interval: 2000 },
    mockMode: env === 'dev' || useMockFlag,
    auth: {},
    utils: karate.call('classpath:common/utils/data-generator.js')
  };

  // Start and route to the mock server in dev
  if (env === 'dev') {
    var server = karate.start('classpath:mocks/mock-server.feature');
    config.mockServerUrl = server.url;
    config.baseUrl = server.url;
    config.authBaseUrl = server.url + '/api/v1/auth';
  }

  // In non-dev envs, when USE_MOCK=true, route to provided/mockServerUrl without starting a new server
  if (env !== 'dev' && useMockFlag) {
    config.baseUrl = base;
    config.authBaseUrl = config.mockServerUrl + '/api/v1/auth';
  }

  if (env === 'qa') config.baseUrl = base;
  if (env === 'prod') config.baseUrl = base;

  karate.configure('connectTimeout', 10000);
  karate.configure('readTimeout', 30000);
  // Use a dynamic headers function so Authorization picks up auth.token after login
  karate.configure('headers', read('classpath:common/headers/common-headers.js'));
  return config;
}