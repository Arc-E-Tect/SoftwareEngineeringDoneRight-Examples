const assert = require('node:assert/strict');
const { Given, When, Then, Before, After } = require('@cucumber/cucumber');
const { request } = require('playwright-core');

const IFF_BASE_URL = process.env.IFF_SOCIAL_NETWORK_URL || 'https://localhost:8447';
const IFF_MODE = process.env.IFF_MODE || 'wiremock';

Before(async function () {
  this.socialNetworkApi = await request.newContext({
    baseURL: IFF_BASE_URL,
    ignoreHTTPSErrors: true,
    extraHTTPHeaders: {
      Accept: 'application/json'
    }
  });
});

After(async function () {
  await this.socialNetworkApi.dispose();
});

Given('the social network service is deployed', async function () {
  // verified by subsequent steps
});

Given('the social network service is up and running', async function () {
  if (IFF_MODE === 'wiremock') {
    const response = await this.socialNetworkApi.get('/__admin/mappings');
    assert.equal(response.status(), 200, 'WireMock admin endpoint is not reachable');
  } else {
    const response = await this.socialNetworkApi.get('/actuator/health');
    assert.ok(response.ok(), 'Health endpoint is not reachable');
  }
});

When('the social network health information is retrieved', async function () {
  this.healthResponse = await this.socialNetworkApi.get('/actuator/health');
  this.healthBody = await this.healthResponse.json();
});

Then('the social network service is operational', async function () {
  assert.equal(this.healthBody.status, 'UP');
});

When('the social network service version is requested', async function () {
  this.versionResponse = await this.socialNetworkApi.get('/');
  this.versionBody = await this.versionResponse.json();
});

Then('the social network version name is {string}', async function (expected) {
  assert.equal(this.versionBody.versionName, expected);
});

Then('the social network version code is {float}', async function (expected) {
  assert.ok(
    Math.abs(this.versionBody.versionCode - expected) < 0.001,
    `Expected versionCode ~${expected} but got ${this.versionBody.versionCode}`
  );
});
