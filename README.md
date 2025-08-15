
# Teams


## Objective

The goal of this app is to manage teams and their members in a GitHub organization. These teams can be used, along with other tools, to assign or display repositories by groups of students.

This tool has two main use cases:
1) During the course, as students enroll or change groups, use this tool to synchronize the teams in the GitHub organization.
2) When the course ends, use it to remove all the teams created for the semester.


### 1. Create or update teams and their members


```bash
java -jar teams.jar <classroom_roster.csv> -t <token> -o <organization>
```


The CSV file must be downloaded from the organization page. To get this file:
1. Go to your GitHub Classroom page.
2. Click on the "Roster" tab.
3. Click the "Download roster" button to get the CSV file.


After running `teams.jar`, the teams in the organization and their members will match the contents of the CSV file:
- New teams will be created for groups that do not already have a team.
- Existing teams will be updated to match the students listed in the CSV file.


**Note**
Students without a GitHub username are skipped. The GitHub username is required to add students to teams. So it is important that this tool is used **after** students have accepted their invitations to the classroom and have their GitHub usernames set up.


### 2. End-of-semester Cleanup

When the course ends, delete the teams corresponding to the groups to prepare for a new semester:

```bash
java -jar teams.jar --clean -t <token> -o <organization>
```

This command will delete every team created from a group (but will not affect other teams in the organization).


## Usage



The JAR can be downloaded from the [releases page](https://github.com/raul-izquierdo/create_teams/releases).

```bash
java -jar teams.jar [<csvfile>] [-t <token>] [-o <organization>] [--clean]
```

| Option                | Description                                                                                 |
|-----------------------|---------------------------------------------------------------------------------------------|
| `<csvfile>`           | The roster CSV file downloaded from GitHub Classroom (default: `classroom_roster.csv`).      |
| `-t <token>`          | GitHub API access token. If not provided, the tool will try to read it from the GITHUB_TOKEN environment variable or from a .env file. |
| `-o <organization>`   | GitHub organization name. If not provided, the tool will try to read it from the GITHUB_ORG environment variable or from a .env file. |
| `--clean`             | Remove all group teams from the organization (useful for a new academic semester).              |
| `-h`, `--help`        | Show this help message.                                                                     |


## GitHub Token


You need a GitHub personal access token with the `repo` and `admin:org` scopes to manage teams and members. For instructions on how to create a token, see the [GitHub documentation: Creating a personal access token](https://docs.github.com/en/authentication/keeping-your-account-and-data-secure/managing-your-personal-access-tokens#creating-a-personal-access-token-classic).


Once you have the token, you can provide it in three ways (in order of precedence):
1. As a command-line argument: `-t <token>`
2. In a `.env` file
3. As an environment variable: `GITHUB_TOKEN`


## License

MIT License

Copyright (c) 2025 Raul Izquierdo Castanedo
