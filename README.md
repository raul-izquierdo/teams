# Teams

## Objective

This application manages teams and their members in a GitHub organization. These teams are used, along with other tools, to grant access to repositories by student groups.

> **NOTE**: This application is **part of a toolkit** for managing classes with GitHub Classroom. It is recommended to **first read** the [main repository](https://github.com/raul-izquierdo/classroom-tools) to get an overview of the project and understand where this tool fits.

## Use Cases

This tool has two main use cases:
1. **During the course**: As students enroll or change groups, use this tool to synchronize the teams in the GitHub organization.
2. **At course end**: Remove all teams created for the semester.

### 1. Create or update teams and their members

This tool requires the GitHub accounts of students to enroll them in their respective teams. This information is stored in the classroom roster and must be provided to `teams.jar`. For information on how to obtain the roster file, see [Obtaining the Roster File](https://github.com/raul-izquierdo/classroom-tools#obtaining-the-roster-file).

> **Important**: Students without a GitHub username are skipped since a GitHub account is required to invite them to teams. Therefore, use this tool **after** students have accepted their first classroom assignment, when their GitHub usernames will be added to the roster.

```bash
java -jar teams.jar [<classroom_roster.csv>]
```

After running `teams.jar`, the teams in the organization and their members will match the contents of the CSV file:
- New teams will be created for groups that do not already have a team, and existing teams that do not correspond to any group in the CSV file will be deleted.
- Existing teams will be updated to match the students listed in the CSV file (by inviting or removing members).

Note that after running this tool, students still **need to accept their email invitations** to join the team.

Students with a **pending invitation** are considered existing team members, so they will not be invited again. This is useful when a student has not yet accepted their invitation but the team needs to be updated to add or remove other students.

### 2. End-of-semester cleanup

When the course ends, delete the teams corresponding to groups to prepare for a new semester:

```bash
java -jar teams.jar --clean -t <token> -o <organization>
```

This command will delete all teams created from groups (but will not affect other teams in the organization).


## Usage

The JAR can be downloaded from the [releases page](https://github.com/raul-izquierdo/teams/releases).

```bash
java -jar teams.jar [<csvfile>] [-o <organization>] [-t <token>] [--clean]
```

| Option              | Description                                                                                                                            |
| ------------------- | ------------------------------------------------------------------------------------------- |
| `<csvfile>`         | The roster CSV file downloaded from GitHub Classroom (default: `classroom_roster.csv`). See [Obtaining the Roster File](https://github.com/raul-izquierdo/classroom-tools#obtaining-the-roster-file) for instructions on how to obtain this file.                                                |
| `-o <organization>` | GitHub organization that contains the solutions                                                                                                              |
| `-t <token>`        | GitHub API access token. See [Obtaining the GitHub Token](https://github.com/raul-izquierdo/classroom-tools#obtaining-the-github-token).                                                                                                               |
| `--clean`           | Remove all group teams from the organization (useful for a new academic semester).                                                     |

If `-o` or `-t` are not provided, the app tries to read the `GITHUB_ORG` and `GITHUB_TOKEN` variables from a `.env` file in the working directory:
```dotenv
GITHUB_ORG=<your-org>
GITHUB_TOKEN=<token>
```

**Note:** The required organization is the one that contains the solution repositories. Depending on your preferences, this may differ from the organization linked to GitHub Classroom. Some instructors prefer to store solutions in a separate organization from the one used for assignments (which is my recommendation). In this case, be sure to specify the organization containing the solutions here.

## Generated Team Names for Groups

Team names are generated from group names by prefixing them with `group ` (note the space). Examples:

| Group      | Team name         |
|------------|-------------------|
| 01         | group 01          |
| i02        | group i02         |
| lab1       | group lab1        |
| 01_english | group 01_english  |


## License

See `LICENSE`.
Copyright (c) 2025 Raul Izquierdo Castanedo
