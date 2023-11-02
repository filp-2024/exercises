const core = require('@actions/core');
const github = require('@actions/github');

const main = async () => {
  core.info(`Start workflow`);

  const {payload} = github.context;

  console.log(payload)

  return -1;
};
main();
