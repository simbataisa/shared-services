function() {
  return {
    isEmail: function(s) { return java.util.regex.Pattern.compile("^[^@\n]+@[^@\n]+\\.[^@\n]+$").matcher(s).matches(); },
    isUuid: function(s) { return java.util.regex.Pattern.compile("^[0-9a-fA-F-]{36}$").matcher(s).matches(); }
  };
}