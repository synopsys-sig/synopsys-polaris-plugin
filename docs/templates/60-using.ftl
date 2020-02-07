# Using in a build

To use ${solution_name} in a freestyle build, add it as a Build Step at the point where you want to build your project, and complete the following:

1. *Polaris CLI Installation*: Select the name you assigned to your ${polaris_cli_name} installation on the *Manage Jenkins > Global Tool Configuration* page.
1. *Polaris Arguments*: The arguments you want passed to the ${polaris_cli_name} (for example: *analyze*).
1. Click Save.

To use ${solution_name} in a pipeline build, add a the following step at the point where you want to build your project:
```
polaris arguments: '{${polaris_cli_name} argument(s)}', polarisCli: '{${polaris_cli_name} installation name}'
```
* The value of *arguments* is a string containing all of the arguments you want to pass to ${polaris_cli_name}.
* The value of *polarisCli* is the name you assigned to your ${polaris_cli_name} installation on the *Manage Jenkins > Global Tool Configuration* page.

For example:
```
polaris arguments: 'analyze', polarisCli: 'MyPolarisCLI'
```