const assert = require('node:assert/strict');
const { Given, When, Then, Before, After } = require('@cucumber/cucumber');
const { request } = require('playwright-core');

const IFF_BASE_URL = process.env.IFF_AUTH_SERVER_URL || 'https://localhost:8445';
const IFF_MODE = process.env.IFF_MODE || 'wiremock';

Before(async function () {
  this.authServerApi = await request.newContext({
    baseURL: IFF_BASE_URL,
    ignoreHTTPSErrors: true,
    extraHTTPHeaders: {
      Accept: 'application/json'
    }
  });
});

After(async function () {
  await this.authServerApi.dispose();
});

Given('the auth server service is deployed', async function () {
  // verified by subsequent steps
});

Given('the auth server service is up and running', async function () {
  if (IFF_MODE === 'wiremock') {
    const response = await this.authServerApi.get('/__admin/mappings');
    assert.equal(response.status(), 200, 'WireMock admin endpoint is not reachable');
  } else {
    const response = await this.authServerApi.get('/actuator/health');
    assert.ok(response.ok(), 'Health endpoint is not reachable');
  }
});

When('the auth server health information is retrieved', async function () {
  this.healthResponse = await this.authServerApi.get('/actuator/health');
  this.healthBody = await this.healthResponse.json();
});

Then('the auth server service is operational', async function () {
  assert.equal(this.healthBody.status, 'UP');
});

When('the auth server service version is requested', async function () {
  this.versionResponse = await this.authServerApi.get('/');
  this.versionBody = await this.versionResponse.json();
});

Then('the auth server version name is {string}', async function (expected) {
  assert.equal(this.versionBody.versionName, expected);
});

Then('the auth server version code is {float}', async function (expected) {
  assert.ok(
    Math.abs(this.versionBody.versionCode - expected) < 0.001,
    `Expected versionCode ~${expected} but got ${this.versionBody.versionCode}`
  );
});
