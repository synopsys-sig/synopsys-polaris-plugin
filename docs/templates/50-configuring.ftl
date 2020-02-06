# Configuring

Once the ${solution_name} plugin is installed, before you can use it in a job, you need to configure:

1. ${polaris_cli_name}
1. ${solution_name}

## Configuring ${polaris_cli_name}

Use the following process to configure ${polaris_cli_name}:

1. In Jenkins, navigate to *Manage Jenkins > Global Tool Configuration*.
1. Scroll down to the ${cli_config_header} section, click ${add_polaris_cli_button}, and complete the following:
    * *Name*: A name for the ${polaris_cli_name} installation.
    * *Required Installation directory*: To let ${solution_name} install the ${polaris_cli_name} from your ${polaris_product_name} server automatically, leave the this box checked,
and proceed to the next step. To point ${solution_name} to an existing ${polaris_cli_name} installation, uncheck this box
and enter the path to the ${polaris_cli_name} directory in the *Required Installation directory* field.
1. Click Save.

## Configuring ${solution_name}

Use the following process to configure ${solution_name}:

1. After installing ${solution_name} in Jenkins, navigate to *Manage Jenkins > Configure System*.
1. Scroll down to the ${plugin_config_header} section and complete the following:
* *Polaris URL*: The URL to your ${polaris_product_name} instance.
* *Polaris credentials*: The Jenkins secret text credentials that you have created to store your ${polaris_product_name} access token.
* *Advanced*: If you want to change the timeout that ${solution_name} will use when waiting
for a response from the ${polaris_product_name} server, click *Advanced*
and enter the timeout in seconds in the *Polaris connection timeout* field.
* *Test connection to Polaris*: To test that ${solution_name} can connect to ${polaris_product_name}
with the configuration data you have entered, click *Test connection to Polaris*.
1. Click Save.
