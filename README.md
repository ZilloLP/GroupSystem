# GroupSystem

## Description
This is a group management plugin that allows users to create and manage groups within the server.

## Commands
### Groupsystem commands:
- `/groupsystem help` - Shows you a list of all commands.
- `/groupsystem reload` - Reloads the plugin settings.

### Groups commands:
- `/groups info` - Gives you information about your group.
- `/groups info <playername>` - Gives you information about another player's group.
- `/groups create <groupname> <priority> <prefix>` - Creates a group.
- `/groups delete <groupname>` - Deletes a group.
- `/groups set <groupname> name <name>` - Sets the name of a group.
- `/groups set <groupname> prefix <prefix>` - Sets the prefix of a group.
- `/groups set <groupname> priority <priority>` - Sets the priority of a group.
- `/groups add permanently <playername> <groupname>` - Adds a player permanently to a group.
- `/groups add temporarily <playername> <groupname>` - Adds a player temporarily to a group.
- `/groups remove <playername>` - Removes a player from their group.

## How to set up an infosign
To create an infosign, write "[Info]" on the top line of the sign, or use the configuration settings specified in the `config.yml` file.
