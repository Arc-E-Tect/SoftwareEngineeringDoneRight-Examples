const assert = require('node:assert/strict');
const { Given, When, Then, Before, After } = require('@cucumber/cucumber');
const { request } = require('playwright-core');

const IFF_BASE_URL = process.env.IFF_USER_ACCOUNT_URL || 'https://localhost:8444';
const IFF_MODE = process.env.IFF_MODE || 'wiremock';

Before(async function () {
  this.baseUrl = IFF_BASE_URL;

  this.userAccountApi = await request.newContext({
    baseURL: this.baseUrl,
    ignoreHTTPSErrors: true,
    extraHTTPHeaders: {
      Accept: 'application/json'
    }
  });
});

After(async function () {
  await this.userAccountApi.dispose();
});

Given('the user account service is deployed', async function () {
  // verified by subsequent steps
});

Given('the user account service is up and running', async function () {
  if (IFF_MODE === 'wiremock') {
    const response = await this.userAccountApi.get('/__admin/mappings');
    assert.equal(response.status(), 200, 'WireMock admin endpoint is not reachable');
  } else {
    const response = await this.userAccountApi.get('/actuator/health');
    assert.ok(response.ok(), 'Health endpoint is not reachable');
  }
});

When('the user account health information is retrieved', async function () {
  this.healthResponse = await this.userAccountApi.get('/actuator/health');
  this.healthBody = await this.healthResponse.json();
});

Then('the user account service is operational', async function () {
  assert.equal(this.healthBody.status, 'UP');
});

When('the user account service version is requested', async function () {
  this.versionResponse = await this.userAccountApi.get('/');
  this.versionBody = await this.versionResponse.json();
});

Then('the user account version name is {string}', async function (expected) {
  assert.equal(this.versionBody.versionName, expected);
});

Then('the user account version code is {float}', async function (expected) {
  assert.ok(
    Math.abs(this.versionBody.versionCode - expected) < 0.001,
    `Expected versionCode ~${expected} but got ${this.versionBody.versionCode}`
  );
});

