# Teams

## Objective

The goal of this app is to manage teams and their members in a GitHub organization. These teams will be used, along with other tools, to assign or display repositories by groups of students.

> **NOTE**. This application is **part of the toolkit** for managing classes with GitHub Classroom. It is recommended to **first read** the [main repository](https://github.com/raul-izquierdo/classroom-tools) to get an overview of the project and understand where this tool fits in.

## Use Cases

This tool has two main use cases:
1) During the course, as students enroll or change groups, use this tool to synchronize the teams in the GitHub organization.
2) When the course ends, use it to remove all the teams created for the semester.


### 1. Create or update teams and their members

This tool needs the GitHub accounts of the students to enroll them in their respective teams. This information is kept in the classroom roster and must be provided to `teams.jar`. For information on how to get the roster file, see [Obtaining the Roster File](https://github.com/raul-izquierdo/classroom-tools#obtaining-the-roster-file).

```bash
java -jar teams.jar <classroom_roster.csv>
```

After running `teams.jar`, the teams in the organization and their members will match the contents of the CSV file:
- New teams will be created for groups that do not already have a team.
- Existing teams will be updated to match the students listed in the CSV file. But note that students still **need to accept their invitations** to join the team.

> **Note**. Students without a GitHub username are skipped since it is required to invite students to teams. So it is important that this tool is used **after** students have accepted their invitations to the classroom and have their GitHub usernames set up.


### 2. End-of-semester Cleanup

When the course ends, delete the teams corresponding to the groups to prepare for a new semester:

```bash
java -jar teams.jar --clean -t <token> -o <organization>
```

This command will delete every team created from a group (but will not affect other teams in the organization).


## Usage

The JAR can be downloaded from the [releases page](https://github.com/raul-izquierdo/teams/releases).

```bash
java -jar teams.jar [<csvfile>] [-o <organization>] [-t <token>] [--clean]
```

| Option              | Description                                                                                                                            |
| ------------------- | ------------------------------------------------------------------------------------------- |
| `<csvfile>`         | The roster CSV file downloaded from GitHub Classroom (default: `classroom_roster.csv`). See [Obtaining the Roster file](https://github.com/raul-izquierdo/classroom-tools#obtaining-the-roster-file) for instructions on how to obtain this file                                                |
| `-o <organization>` | GitHub organization name.                                                                                                              |
| `-t <token>`        | GitHub API access. See [Obtaining the GitHub token](https://github.com/raul-izquierdo/classroom-tools#obtaining-the-github-token). token.                                                                                                               |
| `--clean`           | Remove all group teams from the organization (useful for a new academic semester).                                                     |

If `-o` or `-t` are not provided, the app tries to read the `GITHUB_ORG` and `GITHUB_TOKEN` variables from a `.env` file in the working directory:
```dotenv
GITHUB_ORG=<your-org>
GITHUB_TOKEN=<token>
```

## Generated Team Names for Groups

Teams names are generated from the group names by prefixing them with `group ` (note the space). Examples:

| Group      | Team name         |
|------------|-------------------|
| 01         | group 01          |
| i02        | group i02         |
| lab1       | group lab1        |
| 01_english | group 01_english  |


## License

See `LICENSE`.
Copyright (c) 2025 Raul Izquierdo Castanedo
