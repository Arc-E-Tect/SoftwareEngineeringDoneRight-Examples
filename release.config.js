module.exports = {
  branches: ['main'],
  plugins: [
    '@semantic-release/commit-analyzer',
    '@semantic-release/release-notes-generator',
    '@semantic-release/changelog',
    [
      '@semantic-release/npm',
      {
        npmPublish: false,
      },
    ],
    '@semantic-release/git',
    '@semantic-release/github',
  ],
  releaseRules: [
    {
      type: 'feat',
      release: 'minor',
    },
    {
      type: 'fix',
      release: 'patch',
    },
    {
      type: 'perf',
      release: 'patch',
    },
    {
      type: 'refactor',
      release: false,
    },
    {
      type: 'test',
      release: false,
    },
    {
      type: 'ci',
      release: false,
    },
    {
      type: 'docs',
      release: false,
    },
    {
      type: 'chore',
      release: false,
    },
  ],
  changelog: {
    types: [
      {
        type: 'feat',
        section: '✨ Features',
        hidden: false,
      },
      {
        type: 'fix',
        section: '🐛 Bug Fixes',
        hidden: false,
      },
      {
        type: 'perf',
        section: '⚡ Performance Improvements',
        hidden: false,
      },
      {
        type: 'refactor',
        section: '♻️ Code Refactoring',
        hidden: false,
      },
      {
        type: 'test',
        section: '✅ Tests',
        hidden: true,
      },
      {
        type: 'ci',
        section: '👷 CI/CD',
        hidden: true,
      },
      {
        type: 'docs',
        section: '📚 Documentation',
        hidden: true,
      },
      {
        type: 'chore',
        section: '🔧 Chores',
        hidden: true,
      },
    ],
  },
};
