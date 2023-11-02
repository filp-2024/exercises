const core = require('@actions/core');
const github = require('@actions/github');

const main = async () => {

  core.info(`Start workflow`);

  const {payload} = github.context;

  core.info(`${payload}`);

  return core.setFailed('asdasdasd');
};
main();
