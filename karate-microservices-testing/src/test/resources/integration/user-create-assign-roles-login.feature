Feature: E2E — Create user, assign role, then login

  Background:
  # Base URL and auth setup using shared utilities
    * def base = karate.get('baseUrl') || java.lang.System.getenv('BASE_URL') || 'http://localhost:8080'
    * url base
    * def login = callonce read('classpath:common/auth/login.feature')
    * def auth = { token: #(login.token) }
    * configure headers = read('classpath:common/headers/common-headers.js')
    * def headersFn = read('classpath:common/headers/common-headers.js')
    * print 'Base URL:', base
    * print 'Admin token length:', auth.token.length
    * def headersPreview = headersFn()
    * print 'Computed headers preview:', headersPreview
    * def utils = karate.get('utils')
    * configure report = { showLog: true, showAllSteps: true }

  @e2e @smoke
  Scenario: Create a user, assign a role, and login with the new user
  - Steps:
  1) Create a unique user using helper with provided auth/headers
  2) Create a unique role and record role id
  3) Assign role to user via helper
  4) Login with new user's credentials
  5) Verify user can access own record
  - Assertions:
  - New user JWT token must exist and be of reasonable length
  - Verification helper completes successfully
  - Notes:
  - Helpers prioritize provided `auth`/`headers`; they avoid redundant login.
    # Create a unique user
    * def unique = java.util.UUID.randomUUID().toString()
    * print 'ProvidedHeaders:', headersPreview
    * print 'E2E unique id:', unique
    * def createUser = call read('classpath:common/helpers/create-user.feature@createUser') { auth: #(auth), headers: #(headersPreview), unique: #(unique) }
    * def userId = createUser.userId
    * def username = createUser.username
    * def password = createUser.password
    * print 'Created user credentials:', createUser
    * print 'Created user:', userId, username

    # Create a role to assign
    * def createRole = call read('classpath:common/helpers/create-role.feature@createRole') { auth: #(auth), headers: #(headersPreview), unique: #(unique) }
    * def roleId = createRole.roleId
    * print 'Created role:', createRole

    # Assign the role to the newly created user
    * def assignRes = call read('classpath:common/helpers/assign-role.feature@assignRoleToUser') { auth: #(auth), headers: #(headersPreview), userId: #(userId), roleId: #(roleId) }
    * print 'Assigned role to user:', roleId, '->', userId

    # Login with the new user's credentials
    * print 'Logging in new user:', username
    * def userLogin = call read('classpath:common/auth/login.feature') { username: #(username), password: #(password) }
    * print 'New user token:', userLogin.token
    * assert userLogin.token && userLogin.token.length > 20

    # Verify user can access own record using the new token
    * def verifyRes = call read('classpath:common/helpers/verify-user-access.feature@verifyUserAccess') { userId: #(userId), username: #(username), password: #(password) }
    * print 'Verification completed for user:', userId
