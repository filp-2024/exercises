const core = require('@actions/core');
const github = require('@actions/github');

const main = async () => {

  core.info(`Start workflow`);

  const {payload} = github.context;

  const {INVITE_TOKEN} = process.env;

  const octokit = github.getOctokit(INVITE_TOKEN);

  octokit.rest.teams.removeRepoInOrg({
    org: 'filp-2024',
    team_slug: 'students',
    owner: payload.forkee.owner.login,
    repo: payload.forkee.name
  })


  // try {

  //
  //   const org = core.getInput('organization', {required: true});
  //
  //   const [username, group] = payload.issue.body.split("|")
  //
  //   const role = payload.issue.labels[0].name
  //
  //   try {
  //     await octokit.orgs.checkMembership({
  //       org,
  //       username: payload.issue.user.login,
  //     });
  //   } catch (error) {
  //
  //     await octokit.rest.teams.addOrUpdateMembershipForUserInOrg({
  //       org: org,
  //       team_slug: group,
  //       username: username,
  //       role: 'member',
  //     });
  //
  //     await octokit.rest.issues.createComment({
  //       owner: payload.repository.owner.login,
  //       repo: payload.repository.name,
  //       issue_number: payload.issue.number,
  //       body: "Access granted",
  //     });
  //
  //     await octokit.rest.issues.update({
  //       owner: payload.repository.owner.login,
  //       repo: payload.repository.name,
  //       issue_number: payload.issue.number,
  //       state: 'closed',
  //     });
  //   }
  // } catch (error) {
  //
  //   if (error.message.toString().includes('already a part')) {
  //
  //     await octokit.rest.issues.createComment({
  //       owner: payload.repository.owner.login,
  //       repo: payload.repository.name,
  //       issue_number: payload.issue.number,
  //       body: "Access already granted",
  //     });
  //
  //     await octokit.rest.issues.update({
  //       owner: payload.repository.owner.login,
  //       repo: payload.repository.name,
  //       issue_number: payload.issue.number,
  //       state: 'closed',
  //     });
  //
  //   }
  //
  //   return core.setFailed(error.message);
  // }
  return core.setOutput('Invitation sent');
};
main();
