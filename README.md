# Teams

This Java application automates the creation of GitHub teams and the assignment of students to those teams. The students are read from the CSV roster file exported from GitHub Classroom.


## Features

The main functionalities of this application include:
- Reads a CSV file with student identifiers and GitHub usernames.
- Creates GitHub teams based on the group id which is part of the student identifier.
- Adds students to their corresponding teams.

## Usage

The jar can be downloaded from the [releases page](https://github.com/raul-izquierdo/create_teams/releases).

```bash
java -jar teams.jar [<csvfile>] [-t <token>] [-o <organization>] [-p <teams_prefix>]
```

### CSV File Format

The CSV file is just the file exported from GitHub Classroom. For example:

```csv
"identifier","github_username","github_id","name"
"01-Pérez Pérez, Manolo", "account121", "", ""
"02-Izquierdo Castanedo, Raúl","my_GH_account","",""
```

Output:
- Creates teams named "group-01", "group-02" (or with your chosen prefix).
- Adds the first student to "group-01" and the second to "group-02".

Skipped elements:
- Only the first two columns in the CSV are used. The rest of the columns are ignored.
- If a team already exists, its creation will be skipped.
- Students without GitHub username are skipped. The GitHub ID is required for adding students to teams.

#### Options

| Option                | Description                                                                                 |
|-----------------------|---------------------------------------------------------------------------------------------|
| `<csvfile>`           | The roster CSV file downloaded from the classroom (default = `classroom_roster.csv`).        |
| `-t <token>`          | GitHub API access token. If not provided, will try to read from the GITHUB_TOKEN environment variable or from an .env file. |
| `-o <organization>`   | GitHub organization name. If not provided, will try to read from the GITHUB_ORG environment variable or from an .env file. |
| `-p <teams_prefix>`   | Prefix to add to the team names. If not provided, uses `group-` as the default prefix.        |
| `-h`, `--help`        | Show this help message.                                                                     |

#### Example

```bash
java -jar teams.jar classroom_roster.csv -o my-org -t my-token -p group-
```

## The Token

You can provide the GitHub API token in three ways (in order of precedence):
1. As a command-line argument: `-t <token>`
2. In a `.env` file
3. As an environment variable: `GITHUB_TOKEN`

## License

MIT License

Copyright (c) 2025 Raul Izquierdo Castanedo
