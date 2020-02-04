# Introduction

${polaris_product_name} helps security and development teams analyze security risks
in their software products. The ${solution_name} plugin enables you to invoke ${polaris_product_name}
analysis from your Jenkins builds.
${solution_name} can be used in both freestyle and pipeline jobs.

When the ${solution_name} plugin runs, it checks the configured
${polaris_product_name} server and the Jenkins node to see if the correct version
of the ${polaris_product_name} Command Line Interpreter (CLI). If not, the plugin
installs the ${polaris_product_name} CLI. It then executes the CLI, which
analyzes your project, and uploads results to your ${polaris_product_name} server.
