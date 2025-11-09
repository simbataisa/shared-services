(
  function() {
    var token = karate.get('auth.token');
    var headers = {
      'Accept': 'application/json',
      'Content-Type': 'application/json'
    };
    if (token) headers['Authorization'] = 'Bearer ' + token;
    karate.log('Computed headers Authorization present:', !!headers['Authorization']);
    return headers;
  }
)