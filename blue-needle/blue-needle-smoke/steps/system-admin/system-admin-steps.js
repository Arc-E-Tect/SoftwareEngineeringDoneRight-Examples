const assert = require('node:assert/strict');
const { Given, When, Then, Before, After } = require('@cucumber/cucumber');
const { request } = require('playwright-core');

const IFF_BASE_URL = process.env.IFF_BASE_URL || 'https://localhost:8443';
const IFF_MODE = process.env.IFF_MODE || 'wiremock';

Before(async function () {
  this.baseUrl = IFF_BASE_URL;

  this.api = await request.newContext({
    baseURL: this.baseUrl,
    ignoreHTTPSErrors: true,
    extraHTTPHeaders: {
      Accept: 'application/json'
    }
  });
});

After(async function () {
  await this.api.dispose();
});

Given('the application is deployed', async function () {
  // verified by subsequent steps
});

Given('the service is deployed', async function () {
  // verified by subsequent steps
});

Given('the application is up and running', async function () {
  if (IFF_MODE === 'wiremock') {
    const response = await this.api.get('/__admin/mappings');
    assert.equal(response.status(), 200, 'WireMock admin endpoint is not reachable');
  } else {
    const response = await this.api.get('/actuator/health');
    assert.ok(response.ok(), 'Health endpoint is not reachable');
  }
});

When('health information is retrieved', async function () {
  this.healthResponse = await this.api.get('/actuator/health');
  this.healthBody = await this.healthResponse.json();
});

Then('the application is operational', async function () {
  assert.equal(this.healthBody.status, 'UP');
});

When('the service version is requested', async function () {
  this.versionResponse = await this.api.get('/');
  this.versionBody = await this.versionResponse.json();
});

Then('the version name is {string}', async function (expected) {
  assert.equal(this.versionBody.versionName, expected);
});

Then('the version code is {float}', async function (expected) {
  assert.ok(
    Math.abs(this.versionBody.versionCode - expected) < 0.001,
    `Expected versionCode ~${expected} but got ${this.versionBody.versionCode}`
  );
});

Then('the {string} service is integrated', async function (name) {
  assert.ok(
    Object.hasOwn(this.versionBody.dependencies ?? {}, name),
    `Expected dependency "${name}" to be present in version response`
  );
});
