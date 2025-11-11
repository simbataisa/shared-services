(
  function() {
    return {
      // Generates a conservative RFC-compliant email to satisfy jakarta.validation @Email
      randomEmail: function() {
        var uuid = java.util.UUID.randomUUID().toString().replace(/-/g, '');
        return 'user' + uuid + '@example.com';
      },
      randomUser: function() {
        return { name: 'Test ' + java.util.UUID.randomUUID(), email: this.randomEmail() };
      },
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
)