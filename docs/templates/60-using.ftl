# Using in a build

## Freestyle build

To use ${solution_name} in a freestyle build, add it as a Build Step at the point where you want to build your project, and complete the following:

1. *Polaris CLI Installation*: Select the name you assigned to your ${polaris_cli_name} installation on the *Manage Jenkins > Global Tool Configuration* page.
1. *Polaris Arguments*: The arguments you want passed to the ${polaris_cli_name} (for example: *analyze*).
1. *Wait for Issues*: If you want the build to wait to see if ${polaris_product_name} finds issues in your project, check this box and
use the *If there are issues* field to select the action you want the plugin to take when issues are discovered. Click *Advanced...* if you want to adjust maximum
length of time the job will wait for issues (*Job timeout in minutes*).
If you want the build to proceed without waiting, leave the *Wait for Issues* box unchecked.
1. Click Save.

## Pipeline build

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

If you want the build determine whether ${polaris_product_name} finds issues in your project, after the *polaris* step, add the following step:
```
polarisIssueCheck returnIssueCount: '{true or false}', jobTimeoutInMinutes: '{maximum job wait time}'
```
* If you set *returnIssueCount* to true, polarisIssueCheck will provide the number of issues found as a return value.
* To adjust the maximum time the job will wait for ${polaris_product_name} to determine the issue count, set *jobTimeoutInMinutes*.
