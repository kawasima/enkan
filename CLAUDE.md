# Enkan Project Instructions

## Improvement Proposals (ADR)

Improvement proposals and architectural decisions are tracked as GitHub Issues.

- Use `gh issue create` to file new proposals
- Include the following sections in the issue body:
  - **Rationale** — why the change is needed
  - **Scope** — what is affected
  - **Proposed direction** — concrete approach
  - **Acceptance criteria** — how to verify completion
- Label proposals with `enhancement` and a priority label (`priority:high`, `priority:medium`, `priority:low`)
- Close the issue with a reference commit (`closes #N`) when implemented
- Close with `wontfix` label when rejected, with a brief reason in a comment

## Code Review Checklist

- Verify all branches for malformed/invalid input, not just the happy path.
- When multiple parsing strategies exist (e.g. quoted vs. unquoted), ensure malformed input in one strategy does not silently fall through to another and produce a wrong result.

## Pull Requests

- Always target `develop` as the base branch when creating PRs (not `main`)
- Use `gh pr create --base develop` explicitly
