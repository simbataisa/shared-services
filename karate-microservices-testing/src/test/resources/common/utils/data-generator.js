function() {
  return {
    randomEmail: function() { return 'user_' + java.util.UUID.randomUUID() + '@test.com'; },
    randomUser: function() { return { name: 'Test ' + java.util.UUID.randomUUID(), email: this.randomEmail() }; },
    waitFor: function(fn, timeoutMs, intervalMs) {
      var start = Date.now();
      var interval = intervalMs || 500;
      while (Date.now() - start < timeoutMs) {
        if (fn()) return true;
        java.lang.Thread.sleep(interval);
      }
      return false;
    }
  };
}