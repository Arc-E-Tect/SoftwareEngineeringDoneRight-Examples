const common = [
  'blue-needle-smoke/features/**/*.feature',
  '--require blue-needle-smoke/steps/system-admin/system-admin-steps.js',
  '--require blue-needle-smoke/steps/user-account/user-account-steps.js',
  '--require blue-needle-smoke/steps/auth-server/auth-server-steps.js',
  '--require blue-needle-smoke/steps/relationship/relationship-steps.js',
  '--require blue-needle-smoke/steps/social-network/social-network-steps.js',
].join(' ');

module.exports = { default: common };
